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

    public GameStateDisplay() {
        super("Currently in", true);
    }

    @Override
    public String getText(boolean example) {
        if (mod.isInBedwarsGame()) return "Bedwars!";
        return "not Bedwars..";
    }
}
