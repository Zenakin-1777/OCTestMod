package com.zenakin.octestmod.hud;

import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.hud.TextHud;
import com.zenakin.octestmod.config.TestConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An example OneConfig HUD that is started in the config and displays text.
 *
 * @see TestConfig#hud2
 */
public class BedwarsOverlayDisplay extends TextHud {
    public static Map<String, String> playerStats = new HashMap<>();

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
        line.add("Username: Stars | WLR");
        if (TestConfig.isModEnabled) {
            for (Map.Entry<String, String> entry : playerStats.entrySet()) {
                line.add(entry.getKey() + ": " + entry.getValue());
            }
        } else {
            line.clear();
            line.add("Not in game");
        }
    }

    public static void writeHUD(String playerName, int bedwarsLevel, float bedwarsWLR) {
        StringBuilder hudText = new StringBuilder();
        if (levelHUD) {
            hudText.append(bedwarsLevel).append("⭐");
        }
        if (wlrHUD) {
            if (levelHUD) {
                hudText.append(" | ");
            }
            hudText.append(bedwarsWLR);
        }
        if (!playerStats.containsKey(playerName)) {
            playerStats.put(playerName, hudText.toString());
        }
    }
}
