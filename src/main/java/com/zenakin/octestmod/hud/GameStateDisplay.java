package com.zenakin.octestmod.hud;

import cc.polyfrost.oneconfig.hud.SingleTextHud;
import com.zenakin.octestmod.OCTestMod;
import com.zenakin.octestmod.config.TestConfig;

/**
 * An example OneConfig HUD that is started in the config and displays text.
 *
 * @see TestConfig#hud
 */
public class GameStateDisplay extends SingleTextHud {
    public int gameState;

    public GameStateDisplay() {
        super(" ", true);
    }

    @Override
    public String getText(boolean example) {
        if (OCTestMod.isInBedwarsGame() && TestConfig.instance.isModEnabled) {
            gameState = 1;
            //TODO: Add logic/call method that will scan players in the lobby
            return "Currently in Bedwars!";
        } else if (!OCTestMod.isInBedwarsGame() && TestConfig.instance.isModEnabled) {
            gameState = 2;
            return "Currently not in Bedwars..";
        } else {
            gameState = 0;
            return "Mod Dissabled";
        }
    }
}
