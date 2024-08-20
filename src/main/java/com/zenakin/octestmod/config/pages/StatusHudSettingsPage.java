package com.zenakin.octestmod.config.pages;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import com.zenakin.octestmod.hud.GameStateDisplay;

public class StatusHudSettingsPage {
    @HUD(
            name = "Display status"
    )
    public GameStateDisplay hud = new GameStateDisplay();
}
