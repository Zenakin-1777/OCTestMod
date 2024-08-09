package com.zenakin.octestmod.hud;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.hud.SingleTextHud;
import com.zenakin.octestmod.OCTestMod;
import com.zenakin.octestmod.config.TestConfig;

/**
 * An example OneConfig HUD that is started in the config and displays text.
 *
 * @see TestConfig#hud
 */
public class TestHud extends SingleTextHud {
    public TestHud() {
        super("Mod is", true);
    }

    @Override
    public String getText(boolean example) {
        if(OCTestMod.instance.isModEnabled) return "Enabled!";
        return "Dissabled..";
    }
}
