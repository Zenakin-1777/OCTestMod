package com.zenakin.octestmod.config.pages;

import cc.polyfrost.oneconfig.config.annotations.Button;
import cc.polyfrost.oneconfig.gui.pages.Page;
import cc.polyfrost.oneconfig.utils.InputHandler;
import com.zenakin.octestmod.config.TestConfig;
import com.zenakin.octestmod.OCTestMod;
import cc.polyfrost.oneconfig.config.annotations.Switch;

import java.util.ArrayList;
import java.util.List;

public class MapBlacklistPage {
    public List<String> blacklistedMaps = new ArrayList<>();

    @Switch(
            name = "Map 1"
    )
    public boolean map1 = false;

    @Switch(
            name = "Map 2"
    )
    public boolean map2 = false;

    @Switch(
            name = "Map 3"
    )
    public boolean map3 = false;

    @Switch(
            name = "Map 4"
    )
    public boolean map4 = false;

    @Button(
            name = "Save Blacklist",
            text = "Click here to save"
    )
    Runnable runnable = this::saveSwitchStates;

    public void onToggle(String mapName, boolean isToggled) {
        if (isToggled) {
            blacklistedMaps.add(mapName);
        } else {
            blacklistedMaps.remove(mapName);
        }
    }

    // Method to save the state of each switch
    public void saveSwitchStates() {
        onToggle("Map 1", map1);
        onToggle("Map 2", map2);
        onToggle("Map 3", map3);
        onToggle("Map 4", map4);
    }
}
