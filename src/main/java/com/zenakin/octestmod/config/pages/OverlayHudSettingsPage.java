package com.zenakin.octestmod.config.pages;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import com.zenakin.octestmod.hud.BedwarsOverlayDisplay;

public class OverlayHudSettingsPage {
    @HUD(
            name = "Display Bedwars overlay"
    )
    public BedwarsOverlayDisplay hud2 = new BedwarsOverlayDisplay();
}
