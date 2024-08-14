package com.zenakin.octestmod.config;

import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.annotations.Number;
import cc.polyfrost.oneconfig.config.data.PageLocation;
import com.zenakin.octestmod.OCTestMod;
import com.zenakin.octestmod.hud.GameStateDisplay;
import com.zenakin.octestmod.config.pages.MapBlacklistPage;
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
            description = "Enable/Dissable most features of the mod",
            size = OptionSize.DUAL
    )
    public static boolean isModEnabled = true;

    @Number(
            name = "Bedwars stars level threshold",
            description = "Maximum bedwars stars an oponent can have before a lobby dodge is recommended",
            size = OptionSize.DUAL,
            min = 10, max = 5000,
            step = 25
    )
    public static int starThreshold = 150;

    /*
    @Checkbox(
            name = "Cache clear toggle",
            description = "Whether or not the mod will automatically clear the player data cache (recommended to increase efficiency)",
            size = OptionSize.SINGLE
    )
    public static boolean toggleCacheExpiry = true;
     */

    @Number(
            name = "Cache clear time",
            description = "How long in minutes before the player data cache expires",
            size = OptionSize.DUAL,
            min = 0, max = 10080,
            step = 1
    )
    public static int cacheExpiry = 120;

    @Slider(
            name = "Time between scans",
            description = "The time in seconds to wait before performing the next lobby scan",
            min = 1, max = 30,
            step = 30
    )
    public static int scanInterval = 5;

    @Slider(
            name = "Time between requests",
            description = "The time in milliseconds to wait before submitting another request to Hypixel's API",
            min = 500, max = 30000,
            step = 500
    )
    public static int requestInterval = 1000;

    @Text(
            name = "Hypixel API Key",
            placeholder = "Paste your API key here",
            secure = true, multiline = false
    )
    public static String apiKey = "";

    @HUD(
            name = "Display current gamemode"
    )
    public GameStateDisplay hud = new GameStateDisplay();

    @Page(
            name = "Map Blacklist",
            location = PageLocation.TOP,
            description = "More maps will be added soon!"
    )
    public MapBlacklistPage mapBlacklistPage = new MapBlacklistPage();

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
        /* NOT WORKING!!!!!
        this.addDependency("hud", () -> !isModEnabled);
        this.hideIf("cacheExpiry", () -> !toggleCacheExpiry);
         */

        initialize();

        OCTestMod.instance.config = this;
    }
}

