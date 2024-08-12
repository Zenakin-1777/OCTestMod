package com.zenakin.octestmod;

import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.data.OptionSize;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
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
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StringUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import cc.polyfrost.oneconfig.utils.commands.CommandManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The entrypoint of the Example Mod that initializes it.
 *
 * @see Mod
 * @see InitializationEvent
 */
@Mod(modid = com.zenakin.octestmod.OCTestMod.MODID, name = com.zenakin.octestmod.OCTestMod.NAME, version = com.zenakin.octestmod.OCTestMod.VERSION)
public class OCTestMod {

    // Sets the variables from `gradle.properties`. See the `blossom` config in `build.gradle.kts`.
    public static final String MODID = "octestmod";
    public static final String NAME = "OCTestMod";
    public static final String VERSION = "1.0.0";
    @Mod.Instance(MODID)
    public static OCTestMod instance;
    public TestConfig config;
    private int tickCounter = 0;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // Register the config and commands.
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        config = new TestConfig();
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new PlayerLoginHandler());
    }

    /* OLD:
    public class PlayerLoginHandler {
        @SubscribeEvent
        public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Someone has joined the lobby.."));
            if (!OCTestMod.instance.config.isModEnabled || !isInBedwarsGame()) return;
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Passed initial checks (Mod State + In Game Check), moving onto map check!"));

            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (isMapBlacklisted()) {
                        String mapName = getCurrentMapFromScoreboard();
                        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("NON-IDEAL LOBBY, DODGE RECOMMENDED! Cause: Blacklisted map - " + mapName));
                    } else {
                        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Passed secondary check (Map Blacklist), moving onto player check!"));
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
            }, 0, 10000); // 10 seconds
        }
    }
     */

    public class PlayerLoginHandler {
        @SubscribeEvent
        public void onPlayerLogin(FMLNetworkEvent.ClientConnectedToServerEvent  event) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    // Ensure Minecraft client and player are not null
                    if (Minecraft.getMinecraft().thePlayer != null) {
                        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Logged into server! Starting checks..."));
                        startPeriodicChecks();
                    }
                }
            }, 3000);
        }
    }

    public void startPeriodicChecks() {
        scheduler.scheduleAtFixedRate(this::performChecks, 0, (long) TestConfig.scanInterval, TimeUnit.SECONDS);
    }

    private void performChecks() {
        if (!TestConfig.isModEnabled || !isInBedwarsGame()) return;

        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Passed initial checks (Mod State + In Game Check), moving onto map check!"));

        if (isMapBlacklisted()) {
            String mapName = getCurrentMapFromScoreboard();
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("NON-IDEAL LOBBY, DODGE RECOMMENDED! Cause: Blacklisted map - " + mapName));
        } else {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Passed secondary check (Map Blacklist), moving onto player check!"));
            for (String playerName : getPlayersInTabList()) {
                if (getPlayersInTabList().contains(playerName)) continue;
                getPlayersInTabList().add(playerName);

                try {
                    JsonObject playerData = getPlayerData(playerName);
                    int bedwarsLevel = getBedwarsLevel(playerData);
                    if (bedwarsLevel >= TestConfig.starThreshold) {
                        notifyUser(playerName, bedwarsLevel);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
        if (scoreboard != null) {
            ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
            if (objective != null) {
                return objective.getDisplayName();
            }
            return null;
        }
        return  null;
    }

    public static String getCurrentMapFromScoreboard() {
        List<String> scoreboardDetails = getSidebarLines();
        String mapName = null;
        for (String s : scoreboardDetails) {
            String sCleaned = cleanSB(s);

            if (sCleaned.contains("Map: ")) {
                mapName = sCleaned.substring(5).trim();
            }
        }
        return mapName;
    }

    // cleanSB() and getSidebarLines() are taken from Dungeon Rooms Mod by Quantizr(_risk) who used some code from Danker's Skyblock Mod under the GNU General Public License.
    public static String cleanSB(String scoreboard) {
        char[] nvString = StringUtils.stripControlCodes(scoreboard).toCharArray();
        StringBuilder cleaned = new StringBuilder();

        for (char c : nvString) {
            if ((int) c > 20 && (int) c < 127) {
                cleaned.append(c);
            }
        }
        return cleaned.toString();
    }

    public static List<String> getSidebarLines() {
        List<String> lines = new ArrayList<>();
        Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
        if (scoreboard == null) return null;
        ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        if (objective == null) return null;
        Collection<Score> scores = scoreboard.getSortedScores(objective);
        List<Score> list = scores.stream()
                .filter(input -> input != null && input.getPlayerName() != null && !input.getPlayerName().startsWith("#"))
                .collect(Collectors.toList());

        if (list.size() > 15) {
            scores = Lists.newArrayList(Iterables.skip(list, scores.size() - 15));
        } else {
            scores = list;
        }

        for (Score score : scores) {
            ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
            lines.add(ScorePlayerTeam.formatPlayerName(team, score.getPlayerName()));
        }

        return lines;
    }

    public static boolean isInBedwarsGame() {
        String currentArea = getCurrentAreaFromScoreboard();
        String currentMap = getCurrentMapFromScoreboard();
        return currentArea != null && currentMap != null && currentArea.toLowerCase().contains("bed wars");
    }

    public boolean isMapBlacklisted() {
        String map = getCurrentMapFromScoreboard();
        if (map == null) {
            return false;
        }

        map = map.trim();

        switch (map) {
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
