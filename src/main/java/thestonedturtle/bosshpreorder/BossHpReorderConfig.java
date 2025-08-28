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

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup(BossHpReorderConfig.GROUP_KEY)
public interface BossHpReorderConfig extends Config
{
	String GROUP_KEY = "bosshpreorder";
    String TOB_KEY = "applyToTob";
    String TOB_BAR_OFFSET_KEY = "tobBarOffset";

    @Range(
        min = 5,
        max = 23
    )
    @ConfigItem(
        keyName = "barOffset",
        name = "HP Bar Offset",
        description = "How many pixels from the top of the screen the HP bar should be offset by",
        position = 1
    )
    default int barOffset()
    {
        return 23;
    }


    @ConfigSection(
        name = "Theatre of Blood",
        description = "The options that relate to the Theatre of Blood",
        position = 1
    )
    String tobSection = "theatreOfBlood";

    @ConfigItem(
        keyName = TOB_KEY,
        name = "Apply to TOB",
        description = "Should the plugin also apply the offset to the Theatre of Blood progress bar?",
        position = 1,
        section = tobSection
    )
    default boolean applyToTob()
    {
        return true;
    }

    @Range(
        min = 1,
        max = 25
    )
    @ConfigItem(
        keyName = "tobBarOffset",
        name = "HP Bar Offset",
        description = "How many pixels from the top of the screen the HP bar should be offset by",
        position = 2,
        section = tobSection
    )
    default int tobBarOffset()
    {
        return 25;
    }
}
