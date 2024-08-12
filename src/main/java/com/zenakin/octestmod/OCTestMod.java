package com.zenakin.octestmod;

import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.data.OptionSize;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zenakin.octestmod.config.TestConfig;
import com.zenakin.octestmod.config.pages.MapBlacklistPage;
import cc.polyfrost.oneconfig.events.event.InitializationEvent;
import com.zenakin.octestmod.hud.GameStateDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import cc.polyfrost.oneconfig.utils.commands.CommandManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    public static OCTestMod instance;
    public TestConfig config;

    // Register the config and commands.
    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
        config = new TestConfig();
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new PlayerLoginHandler());
        System.out.println("Mod Initializing...");
    }

    public class PlayerLoginHandler {
        @SubscribeEvent
        public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
            if (!OCTestMod.instance.config.isModEnabled || !isInBedwarsGame()) return;

            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (isMapBlacklisted()) {
                        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("NON-IDEAL LOBBY, DODGE RECOMMENDED!"));
                    } else {
                        for (String playerName : getPlayersInTabList()) {
                            if (getPlayersInTabList().contains(playerName)) continue;
                            getPlayersInTabList().add(playerName);

                            try {
                                JsonObject playerData = getPlayerData(playerName);
                                int bedwarsLevel = getBedwarsLevel(playerData);
                                if (bedwarsLevel >= OCTestMod.instance.config.starThreshold) {
                                    notifyUser(playerName, bedwarsLevel);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            } //TODO: Wipe players list on world change.
                        }
                    }
                }
            }, 0, 15000); // 15 seconds
        }
    }

    private JsonObject getPlayerData(String playerName) throws Exception {
        String urlString = "https://api.hypixel.net/player?key=" + TestConfig.apiKey + "&name=" + playerName;
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(response.toString());
            return jsonElement.getAsJsonObject();
        }
    }

    private int getBedwarsLevel(JsonObject playerData) {
        JsonObject stats = playerData.getAsJsonObject("player").getAsJsonObject("stats").getAsJsonObject("Bedwars");
        return stats.get("bedwars_level").getAsInt();
    }

    public Set<String> getPlayersInTabList() {
        Set<String> scannedPlayers = ConcurrentHashMap.newKeySet();
        for (EntityPlayer player : Minecraft.getMinecraft().theWorld.playerEntities) {
            scannedPlayers.add(player.getName());
        }
        return scannedPlayers;
    }

    private void notifyUser(String playerName, int bedwarsLevel) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("NON-IDEAL LOBBY, DODGE RECOMMENDED! Cause: " + playerName + " LVL: " + bedwarsLevel));
    }

    private static String getCurrentAreaFromScoreboard() {
        Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
        ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        if (objective != null) {
            return objective.getDisplayName();
        }
        return null;
    }

    private static String getCurrentMapFromScoreboard() {
        Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
        ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);

        if (objective != null) {
            Collection<Score> scores = scoreboard.getSortedScores(objective);

            for (Score score : scores) {
                String scoreText = score.getPlayerName();
                if (scoreText.startsWith("map: ")) {
                    // Extracts the map name after "map: "
                    return scoreText.substring(5).trim();
                }
            }
        }

        return null;
    }

    public static boolean isInBedwarsGame() {
        String currentArea = getCurrentAreaFromScoreboard();
        String currentMap = getCurrentMapFromScoreboard();
        return currentArea != null && currentMap != null && currentArea.toLowerCase().contains("bed wars");
    }

    public boolean isMapBlacklisted() {
        switch (Objects.requireNonNull(getCurrentMapFromScoreboard())) {
            case "Aqil":
                return MapBlacklistPage.map1;
            case "Dockyard":
                return MapBlacklistPage.map2;
            case "Rooted":
                return MapBlacklistPage.map3;
            case "Aetius":
                return MapBlacklistPage.map4;
            case "Arid":
                return MapBlacklistPage.map5;
            case "Casita":
                return MapBlacklistPage.map6;
            case "Fruitbrawl":
                return MapBlacklistPage.map7;
            case "Gelato":
                return MapBlacklistPage.map8;
            case "Keep":
                return MapBlacklistPage.map9;
            case "Montipora":
                return MapBlacklistPage.map10;
            case "Nebuc":
                return MapBlacklistPage.map11;
            case "Retreat":
                return MapBlacklistPage.map12;
            case "Vigilante":
                return MapBlacklistPage.map13;
            case "Amazon":
                return MapBlacklistPage.map14;
            case "Ashfire":
                return MapBlacklistPage.map15;
            case "Blossom":
                return MapBlacklistPage.map16;
            case "Gateway":
                return MapBlacklistPage.map17;
            case "Harvest":
                return MapBlacklistPage.map18;
            case "Ironclad":
                return MapBlacklistPage.map19;
            case "Lotus":
                return MapBlacklistPage.map20;
            case "Mirage":
                return MapBlacklistPage.map21;
            case "Pernicious":
                return MapBlacklistPage.map22;
            case "Hollow":
                return MapBlacklistPage.map23;
            case "Scorched Sands":
                return MapBlacklistPage.map24;
            case "Waterfall":
                return MapBlacklistPage.map25;
            case "Arcade":
                return MapBlacklistPage.map26;
            default:
                return false; // Return false if the map name doesn't match any known maps
        }
    }
}
