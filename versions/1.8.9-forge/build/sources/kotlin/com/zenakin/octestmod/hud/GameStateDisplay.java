package com.zenakin.octestmod.hud;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.hud.SingleTextHud;
import com.zenakin.octestmod.OCTestMod;
import com.zenakin.octestmod.config.TestConfig;

/**
 * An example OneConfig HUD that is started in the config and displays text.
 *
 * @see TestConfig#hud2
 */
public class GameStateDisplay extends SingleTextHud {
    OCTestMod mod = new OCTestMod();
    public int gameState;

    public GameStateDisplay() {
        super(" ", true);
    }

    @Override
    public String getText(boolean example) {
        if (mod.isInBedwarsGame() && TestConfig.instance.isModEnabled) {
            gameState = 1;
            return "Currently in Bedwars!";
        } else if (!mod.isInBedwarsGame() && TestConfig.instance.isModEnabled) {
            gameState = 2;
            return "Currently not in Bedwars..";
        } else {
            gameState = 0;
            return "Mod Dissabled";
        }

    }
}
