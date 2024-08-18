package com.zenakin.octestmod.hud;

import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.hud.SingleTextHud;
import cc.polyfrost.oneconfig.hud.TextHud;
import com.zenakin.octestmod.OCTestMod;
import com.zenakin.octestmod.config.TestConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

import java.util.ArrayList;
import java.util.List;

/**
 * An example OneConfig HUD that is started in the config and displays text.
 *
 * @see TestConfig#hud2
 */
public class BedwarsOverlayDisplay extends TextHud {
    public static List<String> playerStats = new ArrayList<>();

    @Switch(
            name = "Bedwars Level",
            description = "Whether or not to display the Bedwars level"
    )
    public static boolean levelHUD = true;

    @Switch(
            name = "Bedwars WLR",
            description = "Whether or not to display the Bedwars WLR"
    )
    public static boolean wlrHUD = true;

    public BedwarsOverlayDisplay() {
        super(true);
    }

    @Override
    protected void getLines(List<String> line, boolean example) {
        if (TestConfig.isModEnabled) {
            line.addAll(playerStats);
        } else {
            line.clear();
            line.add("Not in game");
        }
    }

    public static void writeHUD(String playerName, int bedwarsLevel, float bedwarsWLR) {
        if (BedwarsOverlayDisplay.levelHUD && BedwarsOverlayDisplay.wlrHUD) {
            playerStats.add(playerName + ": " + bedwarsLevel + " | " + bedwarsWLR);
        } else if (!levelHUD && wlrHUD) {
            playerStats.add(playerName + ": " + bedwarsWLR);
        } else if (levelHUD && !wlrHUD) {
            playerStats.add(playerName + ": " + bedwarsLevel);
        } else {
            playerStats.add(playerName);
        }
    }
}
