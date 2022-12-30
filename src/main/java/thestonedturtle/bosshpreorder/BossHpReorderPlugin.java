/*
 * Copyright (c) 2022, TheStonedTurtle <https://github.com/TheStonedTurtle>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package thestonedturtle.bosshpreorder;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Varbits;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(
	name = "Boss HP Reorder"
)
public class BossHpReorderPlugin extends Plugin
{
	private static final String RUNELITE_GROUP_KEY = "runelite";
	private static final String POSITION_KEY = "_preferredPosition";
	private static final String LOCATION_KEY = "_preferredLocation";
	private static final String HP_BAR_NAME = "HEALTH_OVERLAY_BAR";

	private static final int XP_DROPS_SHOWN_VARBIT = 4702;
	private static final int HP_BAR_TEXT_UPDATE_SCRIPT_ID = 2102;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ConfigManager configManager;

	@Inject
	private BossHpReorderConfig config;

	@Provides
	BossHpReorderConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BossHpReorderConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		final String position = configManager.getConfiguration(RUNELITE_GROUP_KEY, HP_BAR_NAME + POSITION_KEY);
		final String location = configManager.getConfiguration(RUNELITE_GROUP_KEY, HP_BAR_NAME + LOCATION_KEY);

		if (location != null)
		{
			return;
		}

		clientThread.invoke(() -> adjustHealthBarLocation(position));
	}

	@Override
	protected void shutDown() throws Exception
	{
		resetWidgetPositions();
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged e)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		// If the HP bar is set to a dynamic location do not make any changes
		if (configManager.getConfiguration(RUNELITE_GROUP_KEY, HP_BAR_NAME + LOCATION_KEY) != null)
		{
			return;
		}

		if (e.getVarbitId() == XP_DROPS_SHOWN_VARBIT
			|| e.getVarbitId() == Varbits.EXPERIENCE_TRACKER_POSITION
			|| e.getVarbitId() == Varbits.BOSS_HEALTH_OVERLAY)
		{
			clientThread.invoke(() -> adjustHealthBarLocation(configManager.getConfiguration(RUNELITE_GROUP_KEY, HP_BAR_NAME + POSITION_KEY)));
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged e)
	{
		if (e.getGroup().equals(BossHpReorderConfig.GROUP_KEY))
		{
			// If the HP bar is set to a dynamic location do not make any changes
			if (configManager.getConfiguration(RUNELITE_GROUP_KEY, HP_BAR_NAME + LOCATION_KEY) != null)
			{
				return;
			}

			clientThread.invoke(() -> adjustHealthBarLocation(configManager.getConfiguration(RUNELITE_GROUP_KEY, HP_BAR_NAME + POSITION_KEY)));
			return;
		}

		if (!e.getGroup().equals(RUNELITE_GROUP_KEY))
		{
			return;
		}

		if (e.getKey().equals(HP_BAR_NAME + POSITION_KEY))
		{
			clientThread.invoke(() -> adjustHealthBarLocation(e.getNewValue()));
		}
		else if (e.getKey().equals(HP_BAR_NAME + LOCATION_KEY))
		{
			// If the widget was changed to a dynamic location we want to reset everything's forced position
			if (e.getNewValue() != null)
			{
				resetWidgetPositions();
			}
		}
	}

	@Subscribe
	private void onScriptPostFired(final ScriptPostFired e)
	{
		// When the HP bar is loaded we may need to adjust the position
		// The easiest way to check when the HP bar has been loaded is whenever the HP Bar Text has changed
		if (e.getScriptId() != HP_BAR_TEXT_UPDATE_SCRIPT_ID)
		{
			return;
		}

		// If the HP bar is set to a dynamic location do not make any changes
		if (configManager.getConfiguration(RUNELITE_GROUP_KEY, HP_BAR_NAME + LOCATION_KEY) != null)
		{
			return;
		}

		clientThread.invoke(() -> adjustHealthBarLocation(configManager.getConfiguration(RUNELITE_GROUP_KEY, HP_BAR_NAME + POSITION_KEY)));
	}

	private void adjustHealthBarLocation(final String newVal)
	{
		// If xp drops or the boss HP bar aren't being shown we don't need to do anything
		if (client.getVarbitValue(XP_DROPS_SHOWN_VARBIT) == 0 || client.getVarbitValue(Varbits.BOSS_HEALTH_OVERLAY) == 1)
		{
			resetWidgetPositions();
			return;
		}

		// If the XP drops aren't set to the middle then the HP bar will not be auto adjusted
		final int xpLoc = client.getVarbitValue(Varbits.EXPERIENCE_TRACKER_POSITION);
		if (xpLoc != 1)
		{
			resetWidgetPositions();
			return;
		}

		// If the Boss Health Bar isn't available then we don't need to do anything
		final Widget hpContainer = client.getWidget(WidgetID.HEALTH_OVERLAY_BAR_GROUP_ID, 4);
		final Widget hpBar = client.getWidget(WidgetInfo.HEALTH_OVERLAY_BAR);
		if (hpContainer == null || hpBar == null)
		{
			resetWidgetPositions();
			return;
		}

		final Widget xpTrackerContainer = client.getWidget(WidgetID.EXPERIENCE_DROP_GROUP_ID, 3);
		final Widget xpTracker = client.getWidget(WidgetID.EXPERIENCE_DROP_GROUP_ID, 4);
		if (xpTrackerContainer == null || xpTracker == null)
		{
			resetWidgetPositions();
			return;
		}

		// As long as the location of the xp drops is set to middle (1) it will adjust this overlay regardless of it's snapped position
		// The default position for the HP Bar is `TOP_CENTER` which will be saved as `null` in the config
		// We only care about the top snapped positions as otherwise the adjustments don't really matter
		if (newVal == null || newVal.equals("TOP_RIGHT") || newVal.equals("TOP_LEFT"))
		{
			// Force the containers to the top of the screen
			// y = 23 is the default position of the hpContainer
			xpTrackerContainer.setForcedPosition(xpTrackerContainer.getRelativeX(), config.barOffset());
			hpContainer.setForcedPosition(hpContainer.getRelativeX(), config.barOffset());

			// If the boss HP bar is in the center of the screen move the XP drops down
			// This will prevent the XP drops from displaying over the HP bar
			if (newVal == null)
			{
				xpTracker.setForcedPosition(xpTracker.getRelativeX(), hpBar.getHeight());
			}
			else
			{
				xpTracker.setForcedPosition(-1, -1);
			}
		}
		else
		{
			// For any other scenario the default behavior is fine
			resetWidgetPositions();
		}
	}

	private void resetWidgetPositions()
	{
		final Widget xpTrackerContainer = client.getWidget(WidgetID.EXPERIENCE_DROP_GROUP_ID, 3);
		final Widget xpTracker = client.getWidget(WidgetID.EXPERIENCE_DROP_GROUP_ID, 4);
		final Widget hpContainer = client.getWidget(WidgetID.HEALTH_OVERLAY_BAR_GROUP_ID, 4);

		resetWidgetForcedPosition(xpTrackerContainer);
		resetWidgetForcedPosition(xpTracker);
		resetWidgetForcedPosition(hpContainer);
	}

	private void resetWidgetForcedPosition(final Widget w)
	{
		if (w == null)
		{
			return;
		}
		w.setForcedPosition(-1, -1);
	}
}
