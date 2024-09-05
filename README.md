Bedwars overlay with some extra features.

Credit:
- ChatGPT
- Ryan MC
- Zenakin


TODO:
- [IMPORTANT] mod takes long time to scan lobbies, maybe make it more efficient?
- [CORE FEATURE] add team name filter by /pl ing before starting?
- [QOL] add more stats (bed breaks; finals; etc)to display and fix current toggles to not only stop displaying them, but also stop considering players over the toggled-off threshold as "high level"
- [IMPORTANT] stop the startup message from spamming
- [NEXT] indicate high level teams (maybe with letter prefix)
- [NEXT] add compatibility with bridge (starting with duplicating the isInBedwars() method)
- [QOL] add sorting to overlay
- [NEXT] add a rate limit tracker (using RateLimit-Remaining)
- [IMPORTANT] fix dependancies not working (Overlay toggle, Cache Clear toggle)
- [CORE FEATURE] fix notification sounds
- [NEXT] add reset to defaults button to some of the configs
- [NEXT] add "updateUrl" to mcmod.info

Patch:
3. GameStateDisplay.java
   Potential Issues:
   OCTestMod.isInBedwarsGame(): Ensure that this method correctly determines the game state and handles any potential exceptions or errors.
   OCTestMod.statusHudColour and OCTestMod.instance.displayMessage: Verify that these are correctly initialized and not null before they are used in the getText method.
   Recommendations:
   Initialization Checks: Add checks to ensure OCTestMod.statusHudColour and OCTestMod.instance.displayMessage are initialized. Example:
   java
   Copy code
   if (OCTestMod.statusHudColour == null) {
   // Handle error or set default color
   }
   Error Handling: Ensure that the methods used to determine game state do not throw unexpected exceptions.
4. StatusHudSettingsPage.java
   Potential Issues:
   Configuration Loading:
   Ensure that the configuration settings for HUD are properly loaded and that any user interface components are correctly initialized.
   User Input Handling:
   Check that user inputs and settings modifications are validated and handled correctly.
   Recommendations:
   Configuration Validity: Add validation for configuration settings and ensure that any changes are applied correctly.
   UI Initialization: Verify that the user interface components are properly initialized and integrated with the mod’s logic.
   General Recommendations:
   Logging: Add logging to trace values and states throughout the mod to help identify where crashes occur.
   Testing: Perform thorough testing in various scenarios to catch edge cases or integration issues.
   Exception Handling: Implement robust exception handling throughout the code to prevent crashes due to unexpected conditions.
