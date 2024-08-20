package com.zenakin.octestmod.hud;

import cc.polyfrost.oneconfig.hud.TextHud;
import com.zenakin.octestmod.OCTestMod;
import com.zenakin.octestmod.config.TestConfig;
import com.zenakin.octestmod.config.pages.OverlayHudSettingsPage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An example OneConfig HUD that is started in the config and displays text.
 *
 * @see OverlayHudSettingsPage#hud2
 */
public class BedwarsOverlayDisplay extends TextHud {
    public static Map<String, String> playerStats = new HashMap<>();

    public BedwarsOverlayDisplay() {
        super(true);
    }

    @Override
    protected void getLines(List<String> line, boolean example) {
        if (OCTestMod.doneInit) {
            if (TestConfig.overlayColour != null) {
                color = TestConfig.overlayColour;
            }


            if (TestConfig.isModEnabled) {
                line.add(determineOverlayFormatting());

                for (Map.Entry<String, String> entry : playerStats.entrySet()) {
                    line.add(entry.getKey() + ": " + entry.getValue());
                }
            } else {
                line.clear();
                line.add("pending...");
            }
        }
    }

    public static String determineOverlayFormatting() {
        StringBuilder formatting = new StringBuilder("[Username]");

        if (TestConfig.levelHUD) {
            formatting.append(": [Stars]");
        }

        if (TestConfig.wlrHUD) {
            if (TestConfig.levelHUD) {
                formatting.append(" | ");
            }
            formatting.append("[WLR]");
        }

        return formatting.toString();
    }

    public static void writeHUD(boolean nicked, String playerName, int bedwarsLevel, float bedwarsWLR) {
        StringBuilder hudText = new StringBuilder();
        if (TestConfig.levelHUD) {
            hudText.append(bedwarsLevel).append("⭐");
        }
        if (TestConfig.wlrHUD) {
            if (TestConfig.levelHUD) {
                hudText.append(" | ");
            }
            hudText.append(bedwarsWLR);
        }
        if (nicked) {
            hudText.append(" - NICKED");
        }
        if (!playerStats.containsKey(playerName)) {
            playerStats.put(playerName, hudText.toString());
        }
    }
}
