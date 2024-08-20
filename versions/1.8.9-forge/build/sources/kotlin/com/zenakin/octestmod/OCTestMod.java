package com.zenakin.octestmod;

import cc.polyfrost.oneconfig.config.core.OneColor;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zenakin.octestmod.config.TestConfig;
import com.zenakin.octestmod.config.pages.MapBlacklistPage;
import cc.polyfrost.oneconfig.events.event.InitializationEvent;
import com.zenakin.octestmod.hud.BedwarsOverlayDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.*;
import net.minecraft.util.ChatComponentText;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.Timer;
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
    public String displayMessage;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<String, JsonObject> playerDataCache = new ConcurrentHashMap<>();
    private final Map<String, Long> cacheTimestamps = new ConcurrentHashMap<>();
    public static Map<String, Boolean> playersInParty = new HashMap<>();
    private final Map<String, Boolean> playersToNickCheck = new ConcurrentHashMap<>();
    private static final long CACHE_EXPIRY = TimeUnit.MINUTES.toMillis(TestConfig.cacheExpiry);
    private static final long CACHE_DELETION_TIME = TimeUnit.MINUTES.toMillis(TestConfig.cacheDeletionTime);
    private long lastRequestTime = 0;
    public static OneColor statusHudColour = new OneColor(255, 255, 255);
    public static boolean doneInit = false;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        doneInit = false;
        config = new TestConfig();
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new PlayerLoginHandler());
    }

    public class PlayerLoginHandler {
        /* DEPRECATED:
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
            }, 5000);
        }
         */

        @SubscribeEvent
        public void onWorldLoad(WorldEvent.Load event) {
            doneInit = false;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    ChatComponentText message0 = new ChatComponentText("World updated! Starting checks...");
                    ChatStyle style = new ChatStyle()
                            .setColor(EnumChatFormatting.LIGHT_PURPLE)
                            .setBold(true)
                            .setItalic(true)
                            .setUnderlined(true);
                    message0.setChatStyle(style);
                    Minecraft.getMinecraft().thePlayer.addChatMessage(message0);
                    BedwarsOverlayDisplay.playerStats.clear();
                    playersToNickCheck.clear();
                    statusHudColour = new OneColor(255, 255, 255);
                    doneInit = true;
                    //TODO: add "if (!scheduler.awaitTermination(60, TimeUnit.SECONDS))" if scheduler.shutdown(); isn't working right
                    scheduler.shutdown();
                    if (TestConfig.isModEnabled && isInBedwarsGame()) {
                        startPeriodicChecks();
                    }

                }
            }, 4000);

        }
    }

    public void startPeriodicChecks() {
        scheduler.scheduleAtFixedRate(this::performChecks, 1, TestConfig.scanInterval, TimeUnit.SECONDS);
        //DEBUGGING: Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("(-/3) Started preiodic scheduler"));
    }

    private void performChecks() {
        if (TestConfig.toggleCacheDeletion) {
            long currentTime = System.currentTimeMillis();
            playerDataCache.keySet().removeIf(playerName ->
                    currentTime - cacheTimestamps.getOrDefault(playerName, 0L) >= CACHE_DELETION_TIME
            );
            cacheTimestamps.keySet().removeIf(playerName ->
                    currentTime - cacheTimestamps.getOrDefault(playerName, 0L) >= CACHE_DELETION_TIME
            );
        }

        //DEBUGGING: Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("(0/3) Beginning initial checks.."));
        if (!TestConfig.isModEnabled || !isInBedwarsGame()) {
            displayMessage = "Check scoreboard and mod state";
            return;
        }

        /* DEBUGGING:
        ChatComponentText message = new ChatComponentText("(1/3) Passed initial checks (Mod State + In Game Check), moving onto map check!");
        message.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN));
        Minecraft.getMinecraft().thePlayer.addChatMessage(message);
         */
        statusHudColour = TestConfig.goodColour;
        displayMessage = "Good lobby so far..";

        if (isMapBlacklisted()) {
            ResourceLocation soundLocation = new ResourceLocation("octestmod", "notification_ping");
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(soundLocation));
            statusHudColour = TestConfig.badColour;
            displayMessage = "BLACKLISTED MAP";
        } else {
            /* DEBUGGING:
            ChatComponentText message2 = new ChatComponentText("(2/3) Passed secondary check (Map Blacklist), moving onto player check!");
            message2.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN));
            Minecraft.getMinecraft().thePlayer.addChatMessage(message2);
             */

            for (String playerName : getPlayersInTabList()) {
                //OLD: checkNicks(playerName);
                //Necessary??: if (getPlayersInTabList().contains(playerName)) continue;
                //getPlayersInTabList().add(playerName);
                //TODO: DEBUGGING (1) -
                //Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("(0.5?) Adding player to PlayersInTabList list bc was absent: " + playerName));
                try {
                    //TODO: DEBUGGING (1 & 3) -
                    //Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("(-) Checking Request Delay"));
                performRequestWithDelay(() -> {
                    //Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("(-) Finished Request Delay"));
                    try {
                            //TODO: DEBUGGING (1) -
                            //Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("(1.5) Passed player scanning, trying to get playerData"));
                            JsonObject playerData = getPlayerData(playerName);
                            boolean nicked = checkNick(playerData);
                            //TODO: DEBUGGING (1) -
                            //Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("(6) Player data retrieved from API or cache"));
                            int bedwarsLevel = getBedwarsLevel(playerData);
                            //TODO: DEBUGGING (1) -
                            //Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("(7) Bedwars level retrieved: " + bedwarsLevel));
                            float bedwarsWLR = getBedwarsWLR(playerData);
                            //TODO: DEBUGGING (1) -
                            //Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("(7.5) Bedwars WLR retrieved: " + bedwarsWLR));
                        if (bedwarsLevel >= TestConfig.starThreshold || bedwarsWLR >= TestConfig.wlrThreshold || nicked) {
                            ResourceLocation soundLocation = new ResourceLocation("octestmod", "notification_ping");
                            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(soundLocation));
                            BedwarsOverlayDisplay.writeHUD(nicked, playerName, bedwarsLevel, bedwarsWLR);
                            statusHudColour = TestConfig.badColour;
                            displayMessage = "HIGH LEVEL PLAYER";
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            }
        }
    }

    public boolean checkNick(JsonObject playerData) {
        String jsonString = "{\"success\":true,\"player\":null}";
        JsonElement jsonElement = new JsonParser().parse(jsonString);

        return playerData.get("player").isJsonNull();
    }

    public void sendCommand(String command, String value) {
        // Ensure the command starts with a '/'
        if (!command.startsWith("/")) {
            command = "/" + command + " " + value;
        } else {
            command = command + value;
        }

            Minecraft.getMinecraft().thePlayer.sendChatMessage(command);
    }

    /* OLD
    public void checkNicks(String playerName) {
        if (!playersToNickCheck.containsKey(playerName)) {
            playersToNickCheck.put(playerName, true);
            try {
                tempDelay(() -> {
                    sendCommand("t", playerName);

                    ChatComponentText message0 = new ChatComponentText(playerName);
                    ChatStyle style = new ChatStyle()
                            .setColor(EnumChatFormatting.GREEN)
                            .setBold(true);
                    message0.setChatStyle(style);
                    Minecraft.getMinecraft().thePlayer.addChatMessage(message0);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
     */

    private synchronized void tempDelay(Runnable requestTask) throws InterruptedException {
        long time = System.currentTimeMillis();
        long timeSinceLast = time - lastRequestTime;
        int intervalInMs = 500;

        if (timeSinceLast < intervalInMs) {
            Thread.sleep(intervalInMs - timeSinceLast);
        }

        requestTask.run();
        lastRequestTime = System.currentTimeMillis();
    }

    private synchronized void performRequestWithDelay(Runnable requestTask) throws InterruptedException {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastRequest = currentTime - lastRequestTime;

        if (timeSinceLastRequest < TestConfig.scanInterval) {
            Thread.sleep(TestConfig.scanInterval - timeSinceLastRequest);
        }

        requestTask.run();
        lastRequestTime = System.currentTimeMillis();
    }

    private JsonObject getPlayerData(String playerName) throws Exception {
        long currentTime = System.currentTimeMillis();

        if (playerDataCache.containsKey(playerName) && (currentTime - cacheTimestamps.get(playerName) < CACHE_EXPIRY)) {
            return playerDataCache.get(playerName);
        }

        String urlString = "https://api.hypixel.net/player?key=" + TestConfig.apiKey + "&name=" + playerName;
        //TODO: DEBUGGING (1) -
        //Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("(2) FETCHING FROM API: " + urlString));
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                //TODO: DEBUGGING (1) -
                //Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("(3) Input Line Wasn't Null: " + inputLine));
                response.append(inputLine);
            }

            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(response.toString());
            JsonObject playerData = jsonElement.getAsJsonObject();

            playerDataCache.put(playerName, playerData);
            cacheTimestamps.put(playerName, currentTime);

            //TODO: DEBUGGING (1) -
            //Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("(-) Player Data Cached."));
            //Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("(4) Json Object supposedly returned."));
            return playerData;
        }
    }

    private int getBedwarsLevel(JsonObject playerData) {
        //TODO: DEBUGGING (1) -
        //Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("(-) Identifying and separating BW level from stats"));
        JsonObject stats = playerData.getAsJsonObject("player").getAsJsonObject("achievements");
        //TODO: DEBUGGING (1) -
        //Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("(5) Retrieving bedwars level: " + stats.get("bedwars_level").toString()));
        return stats.get("bedwars_level").getAsInt();
    }

    private float getBedwarsWLR(JsonObject playerData) {
        //TODO: DEBUGGING (1) -
        //Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("(-) Identifying and separating BW WLR from stats"));
        JsonObject stats = playerData.getAsJsonObject("player").getAsJsonObject("stats").getAsJsonObject("Bedwars");
        int wins = stats.get("wins_bedwars").getAsInt();
        int losses = stats.get("losses_bedwars").getAsInt();
        //TODO: DEBUGGING (1) -
        //Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("(5) Retrieving bedwars WLR: " + (float) (wins / losses)));
        return (round((float)wins / losses, TestConfig.precision));
    }

    public Set<String> getPlayersInTabList() {
        Set<String> scannedPlayers = ConcurrentHashMap.newKeySet();

        for (EntityPlayer player : Minecraft.getMinecraft().theWorld.playerEntities) {
            //TODO: DEBUGGING (1) -
            //Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("(0) ADDING PLAYERS TO LIST FROM TAB: " + player.getName()));
            String playerName = player.getName();

            //TODO: Filter out party members and the player
            if (playersInParty.containsKey(playerName)) continue;

            scannedPlayers.add(playerName);
        }
        //Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("(1) PLAYERS ADDED TO LIST FROM TAB: " + scannedPlayers));
        return scannedPlayers;
    }

    /* DEPRECATED:
    public void notifyUser(String causeName, String cause, int bedwarsLevel) {
        ChatComponentText message3 = new ChatComponentText("NON-IDEAL LOBBY, DODGE RECOMMENDED! Cause - " + causeName + cause + " " + bedwarsLevel);
        message3.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED));
        Minecraft.getMinecraft().thePlayer.addChatMessage(message3);
    }
     */

    private static String getCurrentAreaFromScoreboard() {
        Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
        if (scoreboard != null) {
            ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
            if (objective != null) {
                String obj = objective.getDisplayName();
                return cleanSB(obj);
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
            case "Acropolis":
                return MapBlacklistPage.map1;
            case "Aetius":
                return MapBlacklistPage.map2;
            case "Airshow":
                return MapBlacklistPage.map3;
            case "Amazon":
                return MapBlacklistPage.map4;
            case "Ambush":
                return MapBlacklistPage.map5;
            case "Apollo":
                return MapBlacklistPage.map6;
            case "Arcade":
                return MapBlacklistPage.map7;
            case "Arid":
                return MapBlacklistPage.map8;
            case "Ashfire":
                return MapBlacklistPage.map9;
            case "Aqil":
                return MapBlacklistPage.map10;
            case "Bio-Hazard":
                return MapBlacklistPage.map11;
            case "Blossom":
                return MapBlacklistPage.map12;
            case "Cascade":
                return MapBlacklistPage.map13;
            case "Casita":
                return MapBlacklistPage.map14;
            case "Cliffside":
                return MapBlacklistPage.map15;
            case "Crogorm":
                return MapBlacklistPage.map16;
            case "Crypt":
                return MapBlacklistPage.map17;
            case "Deadwood":
                return MapBlacklistPage.map18;
            case "Dockyard":
                return MapBlacklistPage.map19;
            case "Dragon Light":
                return MapBlacklistPage.map20;
            case "Dragonstar":
                return MapBlacklistPage.map21;
            case "Gateway":
                return MapBlacklistPage.map22;
            case "Glacier":
                return MapBlacklistPage.map23;
            case "Hanging Gardens":
                return MapBlacklistPage.map24;
            case "Harvest":
                return MapBlacklistPage.map25;
            case "Hollow":
                return MapBlacklistPage.map26;
            case "Impere":
                return MapBlacklistPage.map27;
            case "Ironclad":
                return MapBlacklistPage.map28;
            case "Keep":
                return MapBlacklistPage.map29;
            case "Lightstone":
                return MapBlacklistPage.map30;
            case "Lighthouse":
                return MapBlacklistPage.map31;
            case "Lotus":
                return MapBlacklistPage.map32;
            case "Lucky Rush":
                return MapBlacklistPage.map33;
            case "Meso":
                return MapBlacklistPage.map34;
            case "Mirage":
                return MapBlacklistPage.map35;
            case "Nebuc":
                return MapBlacklistPage.map36;
            case "Orbit":
                return MapBlacklistPage.map37;
            case "Orchestra":
                return MapBlacklistPage.map38;
            case "Pavilion":
                return MapBlacklistPage.map39;
            case "Pernicious":
                return MapBlacklistPage.map40;
            case "Playground":
                return MapBlacklistPage.map41;
            case "Polygon":
                return MapBlacklistPage.map42;
            case "Rooted":
                return MapBlacklistPage.map43;
            case "Rooftop":
                return MapBlacklistPage.map44;
            case "Sanctum":
                return MapBlacklistPage.map45;
            case "Scorched Sands":
                return MapBlacklistPage.map46;
            case "Serenity":
                return MapBlacklistPage.map47;
            case "Siege":
                return MapBlacklistPage.map48;
            case "Sky Rise":
                return MapBlacklistPage.map49;
            case "Slumber":
                return MapBlacklistPage.map50;
            case "Solace":
                return MapBlacklistPage.map51;
            case "Speedway":
                return MapBlacklistPage.map52;
            case "Steampunk":
                return MapBlacklistPage.map53;
            case "Toro":
                return MapBlacklistPage.map54;
            case "Tuzi":
                return MapBlacklistPage.map55;
            case "Urban Plaza":
                return MapBlacklistPage.map56;
            case "Vigilante":
                return MapBlacklistPage.map57;
            case "Waterfall":
                return MapBlacklistPage.map58;
            case "Yue":
                return MapBlacklistPage.map59;
            case "Zarzul":
                return MapBlacklistPage.map60;
            default:
                return false; // Return false if the map name doesn't match any known maps
        }
    }

    private static float round(float val, int precision){
        int tmp1 = (int) (val*Math.pow(10, precision));
        return (float) tmp1 / (float) Math.pow(10, precision);

    }
}
