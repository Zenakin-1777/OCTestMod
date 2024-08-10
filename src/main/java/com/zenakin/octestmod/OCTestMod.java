package com.zenakin.octestmod;

import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.data.OptionSize;
import com.zenakin.octestmod.config.TestConfig;
import cc.polyfrost.oneconfig.events.event.InitializationEvent;
import com.zenakin.octestmod.hud.GameStateDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import cc.polyfrost.oneconfig.utils.commands.CommandManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

/**
 * The entrypoint of the Example Mod that initializes it.
 *
 * @see Mod
 * @see InitializationEvent
 */
@Mod(modid = com.zenakin.octestmod.OCTestMod.MODID, name = com.zenakin.octestmod.OCTestMod.NAME, version = com.zenakin.octestmod.OCTestMod.VERSION)
public class OCTestMod {

    // Sets the variables from `gradle.properties`. See the `blossom` config in `build.gradle.kts`.
    public static final String MODID = "@ID@";
    public static final String NAME = "@NAME@";
    public static final String VERSION = "@VER@";
    @Mod.Instance(MODID)
    public static com.zenakin.octestmod.OCTestMod INSTANCE;
    public static TestConfig config;

    // Register the config and commands.
    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
        config = new TestConfig();
        MinecraftForge.EVENT_BUS.register(this);
        System.out.println("Mod Initializing...");
    }

    private static String getCurrentAreaFromScoreboard() {
        Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
        ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        if (objective != null) {
            return objective.getDisplayName();
        }
        return null;
    }

    public static boolean isInBedwarsGame() {
        String currentArea = getCurrentAreaFromScoreboard();
        //TODO: Fix and add -> && currentArea.toLowerCase().contains("map: ")
        return currentArea != null && currentArea.toLowerCase().contains("bed wars");
    }
}
