package com.zenakin.octestmod.config;

import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.annotations.Number;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.data.PageLocation;
import com.zenakin.octestmod.OCTestMod;
import com.zenakin.octestmod.config.pages.MapBlacklistPage;
import com.zenakin.octestmod.config.pages.StatusHudSettingsPage;
import com.zenakin.octestmod.config.pages.OverlayHudSettingsPage;
import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import cc.polyfrost.oneconfig.config.data.OptionSize;

/**
 * The main Config entrypoint that extends the Config type and inits the config options.
 * See <a href="https://docs.polyfrost.cc/oneconfig/config/adding-options">this link</a> for more config Options
 */
public class TestConfig extends Config {
    public static TestConfig instance;

    @Switch(
            name = "Main Toggle",
            description = "Enable/Dissable most features of the mod"
    )
    public static boolean isModEnabled = true;

    @Switch(
            name = "Overlay Toggle",
            description = "Enable/Dissable the player list overlay"
    )
    public static boolean isOverlayEnabled = true;

    @Switch(
            name = "Display Bedwars Level",
            description = "Whether or not to display the Bedwars level on the overlay"
    )
    public static boolean levelHUD = true;

    @Switch(
            name = "Display Bedwars WLR",
            description = "Whether or not to display the Bedwars WLR on the overlay"
    )
    public static boolean wlrHUD = true;

    @Number(
            name = "Bedwars stars threshold",
            description = "Maximum level before identified as a threat",
            size = OptionSize.DUAL,
            min = 10, max = 5000,
            step = 25
    )
    public static int starThreshold = 150;

    @Number(
            name = "Bedwars WLR threshold",
            description = "Maximum WLR before identified as a therat",
            size = OptionSize.DUAL,
            min = 0.0f, max = 10.0f
    )
    public static float wlrThreshold = 0.2f;

    @Number(
            name = "Cache refresh interval",
            description = "Time in minutes before the player data cache is refreshed",
            size = OptionSize.DUAL,
            min = 0, max = 1440,
            step = 20
    )
    public static int cacheExpiry = 120;

    @Checkbox(
            name = "Cache clear toggle",
            description = "Whether or not the mod will automatically clear the player data cache (recommended)"
    )
    public static boolean toggleCacheDeletion = true;

    @Number(
            name = "Cache deletion time",
            description = "Time in minutes before the player data stored in the cache is cleared",
            min = 0, max = 44640,
            step = 1440
    )
    public static int cacheDeletionTime = 10080;

    @Slider(
            name = "Bedwars win/loss ratio precision",
            description = "precision after the decimal point",
            min = 0, max = 10.1F,
            step = 1
    )
    public static int precision = 2;

    @Slider(
            name = "Time between scans",
            description = "The time in seconds to wait before performing the next lobby scan",
            min = 1, max = 30.1f,
            step = 1
    )
    public static int scanInterval = 5;

    @Color(
            name = "Good lobby colour",
            description = "The colour of the text on the status display for a good lobby"
    )
    public static OneColor goodColour = new OneColor(0, 255, 0);

    @Color(
            name = "Bad lobby colour",
            description = "The colour of the text on the status display for a bad lobby"
    )
    public static OneColor badColour = new OneColor(255, 0, 0);

    @Color(
            name = "Overlay Text Colour",
            description = "The colour of the text on the Bedwars overlay"
    )
    public static OneColor overlayColour = new OneColor(255, 0, 255);

    @Text(
            name = "Hypixel API Key",
            placeholder = "Paste your API key here",
            secure = true, multiline = false
    )
    public static String apiKey = "";

    @Page(
            name = "Map Blacklist",
            location = PageLocation.BOTTOM,
            description = "More maps will be added soon!"
    )
    public MapBlacklistPage mapBlacklistPage = new MapBlacklistPage();

    @Page(
            name = "Status HUD Settings",
            location = PageLocation.BOTTOM,
            description = "Extra settings to adjust how the status display looks"
    )
    public StatusHudSettingsPage statusHudSettingsPage = new StatusHudSettingsPage();

    @Page(
            name = "Overlay Settings",
            location = PageLocation.BOTTOM,
            description = "Extra settings to adjust how the Bedwars overlay looks"
    )
    public OverlayHudSettingsPage overlayHudSettingsPage = new OverlayHudSettingsPage();

/*
    @Dropdown(
            name = "Example Dropdown", // Name of the Dropdown
            options = {"Option 1", "Option 2", "Option 3", "Option 4"} // Options available.
    )
    public static int exampleDropdown = 1; // Default option (in this case "Option 2")
*/
    public TestConfig() {
        super(new Mod(OCTestMod.NAME, ModType.UTIL_QOL), OCTestMod.MODID + ".json");
        //TODO: MAKE THIS WORK vvv
        //this.addDependency("hud2", () -> isOverlayEnabled);
        //this.hideIf("cacheDeletionTime", () -> !toggleCacheDeletion);

        initialize();

        OCTestMod.instance.config = this;
    }
}

