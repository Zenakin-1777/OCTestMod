package com.zenakin.octestmod.hud;

import cc.polyfrost.oneconfig.config.annotations.Color;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.hud.SingleTextHud;
import com.zenakin.octestmod.OCTestMod;
import com.zenakin.octestmod.config.TestConfig;

/**
 * An example OneConfig HUD that is started in the config and displays text.
 *
 * @see TestConfig#hud
 */
public class GameStateDisplay extends SingleTextHud {

    @Color(
            name = "Default HUD colour"
    )
    public static OneColor hudColour = new OneColor(297, 68, 94, 210);

    public GameStateDisplay() {
        super("STATUS", true);
    }

    @Override
    public String getText(boolean example) {
        if (OCTestMod.isInBedwarsGame() && TestConfig.isModEnabled) {
            hudColour = OCTestMod.statusHudColour;
            return OCTestMod.instance.displayMessage;
        } else if (!OCTestMod.isInBedwarsGame() && TestConfig.isModEnabled) {
            return "Currently not in Bedwars..";
        } else {
            return "Mod Dissabled";
        }
    }
}
