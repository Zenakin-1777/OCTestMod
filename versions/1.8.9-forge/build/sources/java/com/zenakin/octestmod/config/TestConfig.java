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
            size = OptionSize.SINGLE
    )
    public boolean isModEnabled = true; // The default value for the boolean Switch.

    @Number(
            name = "Bedwars stars level threshold",
            min = 10, max = 5000,
            step = 50
    )
    public int starThreshold = 150; // default value

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
    @Slider(
            name = "Example Slider",
            min = 0f, max = 100f, // Minimum and maximum values for the slider.
            step = 10 // The amount of steps that the slider should have.
    )
    public static float exampleSlider = 50f; // The default value for the float Slider.

    @Dropdown(
            name = "Example Dropdown", // Name of the Dropdown
            options = {"Option 1", "Option 2", "Option 3", "Option 4"} // Options available.
    )
    public static int exampleDropdown = 1; // Default option (in this case "Option 2")
*/
    public TestConfig() {
        super(new Mod(OCTestMod.NAME, ModType.UTIL_QOL), OCTestMod.MODID + ".json");
        /* NOT WORKING!!!!!
        this.addDependency("hud", () -> isModEnabled);
        this.hideIf("hud2", () -> !isModEnabled);
         */
        initialize();

        instance = this;
    }
}

