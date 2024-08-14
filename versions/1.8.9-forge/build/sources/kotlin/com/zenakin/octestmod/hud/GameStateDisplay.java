package com.zenakin.octestmod.hud;

import cc.polyfrost.oneconfig.hud.SingleTextHud;
import com.zenakin.octestmod.OCTestMod;
import com.zenakin.octestmod.config.TestConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

/**
 * An example OneConfig HUD that is started in the config and displays text.
 *
 * @see TestConfig#hud
 */
public class GameStateDisplay extends SingleTextHud {

    public GameStateDisplay() {
        super("STATUS", true);
    }

    @Override
    public String getText(boolean example) {
        if (OCTestMod.isInBedwarsGame() && TestConfig.isModEnabled) {
            return OCTestMod.instance.displayMessage;
        } else if (!OCTestMod.isInBedwarsGame() && TestConfig.isModEnabled) {
            return "Currently not in Bedwars..";
        } else {
            return "Mod Dissabled";
        }
    }
}
