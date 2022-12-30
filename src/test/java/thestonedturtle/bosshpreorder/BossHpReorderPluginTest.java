package thestonedturtle.bosshpreorder;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class BossHpReorderPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(BossHpReorderPlugin.class);
		RuneLite.main(args);
	}
}