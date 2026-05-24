package me.gemini.dashboard;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffectTypeCategory;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class DashboardPlugin extends JavaPlugin implements Listener {

    private final Map<UUID, PlayerSettings> playerSettings = new HashMap<>();
    private final Map<UUID, Map<PotionEffectType, Long>> effectHistory = new HashMap<>();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    // Update state
    private File pendingUpdateFile = null;

    // Define warm color hex codes
    private static final String C_GOLD = "<#FFD700>";
    private static final String C_ORANGE = "<#FFA500>";
    private static final String C_YELLOW = "<#FFFF00>";
    private static final String C_RED = "<#FF4500>";
    private static final String C_GREEN = "<#55FF55>";
    private static final String C_GRAY = "<#AAAAAA>";
    private static final String C_PURPLE = "<#DA70D6>";

    private static class PlayerSettings {
        boolean showXyz = true;
        boolean showBiome = true;
        boolean showNetherXyz = true;
        boolean globalEnabled = true;

        void toggleGlobal() { globalEnabled = !globalEnabled; }
        void toggleXyz() { showXyz = !showXyz; }
        void toggleBiome() { showBiome = !showBiome; }
        void toggleNetherXyz() { showNetherXyz = !showNetherXyz; }
    }

    @Override
    public void onEnable() {
        getLogger().info("Dashboard v1.0 enabled! Auto-updater and features active.");

        getServer().getPluginManager().registerEvents(this, this);

        // Register Commands
        LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();

            var dashboardBuilder = Commands.literal("dashboard")
                .then(Commands.literal("toggle").executes(ctx -> toggleSetting(ctx.getSource(), "global")))
                .then(Commands.literal("xyz").executes(ctx -> toggleSetting(ctx.getSource(), "xyz")))
                .then(Commands.literal("biome").executes(ctx -> toggleSetting(ctx.getSource(), "biome")))
                .then(Commands.literal("nether").executes(ctx -> toggleSetting(ctx.getSource(), "nether")))
                .then(Commands.literal("update").executes(ctx -> {
                    checkForUpdates(ctx.getSource());
                    return 1;
                }));

            commands.register(dashboardBuilder.build(), "Control the dashboard", List.of("db"));

            var posBuilder = Commands.literal("pos")
                .executes(ctx -> {
                    if (!(ctx.getSource().getSender() instanceof Player player)) {
                        ctx.getSource().getSender().sendPlainMessage("ᴘʟᴀʏᴇʀѕ ᴏɴʟʏ.");
                        return 1;
                    }
                    sharePosition(player);
                    return 1;
                });

            commands.register(posBuilder.build(), "Share your position globally", List.of("where"));
        });

        // Dashboard update task
        getServer().getGlobalRegionScheduler().runAtFixedRate(this, scheduledTask -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateActionBar(player);
            }
        }, 1L, 5L);

        // Check for updates on startup
        checkForUpdates(null);
    }

    /**
     * Checks for updates from the web.
     */
    private void checkForUpdates(CommandSourceStack source) {
        String updateUrl = "https://www.qclid.space/api/plugin-version";
        String startMsg = "[Dashboard] Checking for updates...";
        String failMsg = "Updating failed, will try again next time server restarts.";

        if (source != null) source.getSender().sendMessage(miniMessage.deserialize(C_GOLD + toSmallCaps(startMsg)));
        else getLogger().info(toSmallCaps(startMsg));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(updateUrl)).GET().build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(response -> {
            if (response.statusCode() != 200) {
                if (source != null) source.getSender().sendMessage(miniMessage.deserialize(C_RED + toSmallCaps(failMsg)));
                else getLogger().warning(toSmallCaps(failMsg));
                return;
            }

            try {
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                String latestVersion = json.get("version").getAsString();
                String downloadUrl = json.get("downloadUrl").getAsString();

                if (isNewer(latestVersion, getDescription().getVersion())) {
                    String msg = C_GOLD + toSmallCaps("New update found! Downloading v") + latestVersion;
                    if (source != null) source.getSender().sendMessage(miniMessage.deserialize(msg));
                    else getLogger().info(msg);

                    downloadAndPrepareUpdate(downloadUrl);
                } else if (source != null) {
                    source.getSender().sendMessage(miniMessage.deserialize(C_GREEN + toSmallCaps("Plugin is up to date!")));
                }
            } catch (Exception e) {
                if (source != null) source.getSender().sendMessage(miniMessage.deserialize(C_RED + toSmallCaps(failMsg)));
                else getLogger().warning(toSmallCaps(failMsg) + " (" + e.getMessage() + ")");
            }
        });
    }

    private void downloadAndPrepareUpdate(String url) {
        try {
            File tempFile = File.createTempFile("dashboard-update", ".jar");
            tempFile.deleteOnExit();

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofFile(tempFile.toPath())).thenAccept(res -> {
                if (res.statusCode() == 200) {
                    this.pendingUpdateFile = tempFile;
                    String msg = C_GOLD + toSmallCaps("Update downloaded! Replacing file on server shutdown.");
                    Bukkit.broadcast(miniMessage.deserialize(msg), "dashboard.admin");
                    getLogger().info(msg);
                }
            });
        } catch (Exception e) {
            getLogger().severe("Failed to download update: " + e.getMessage());
        }
    }

    private boolean isNewer(String latest, String current) {
        try {
            return Double.parseDouble(latest) > Double.parseDouble(current);
        } catch (Exception e) {
            return !latest.equalsIgnoreCase(current);
        }
    }

    @Override
    public void onDisable() {
        // Direct replacement on shutdown
        if (pendingUpdateFile != null && pendingUpdateFile.exists()) {
            try {
                File currentJar = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
                getLogger().info(toSmallCaps("Attempting direct replacement of ") + currentJar.getName());

                // On Windows, the file is usually locked until the process fully exits.
                // However, we attempt the overwrite here. If it fails, we fall back to a rename-swap.
                try {
                    Files.copy(pendingUpdateFile.toPath(), currentJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    // Fallback: If direct overwrite is locked (Windows), use a temp swap or the update folder
                    // which is specifically designed to handle this between process exit/start.
                    File updateFolder = getServer().getUpdateFolderFile();
                    if (!updateFolder.exists()) updateFolder.mkdirs();
                    Files.copy(pendingUpdateFile.toPath(), new File(updateFolder, currentJar.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                    getLogger().warning(toSmallCaps("Direct overwrite locked. Scheduled replacement via update folder."));
                }
            } catch (Exception e) {
                getLogger().severe("Failed to replace JAR: " + e.getMessage());
            }
        }
        getLogger().info("Dashboard disabled.");
    }

    // --- Feature Logic ---

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerListPing(ServerListPingEvent event) {
        World world = Bukkit.getWorlds().get(0);
        if (world != null) {
            long day = world.getFullTime() / 24000L;
            String motdContent = C_GOLD + toSmallCaps("Day : ") + C_ORANGE + day;
            event.motd(miniMessage.deserialize(motdContent));
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Location loc = player.getLocation();
        String worldName = player.getWorld().getName();
        String deathMsg = String.format("%s☠ %s 📍 <white>%d, %d, %d</white> %s %s",
            C_PURPLE, toSmallCaps("Death location"), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), toSmallCaps("in"), toSmallCaps(worldName));
        player.sendMessage(miniMessage.deserialize(deathMsg));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPotionEffect(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getAction() != EntityPotionEffectEvent.Action.ADDED && event.getAction() != EntityPotionEffectEvent.Action.CHANGED) return;
        PotionEffect newEffect = event.getNewEffect();
        if (newEffect == null || newEffect.getType().getCategory() != PotionEffectTypeCategory.HARMFUL) return;

        Map<PotionEffectType, Long> history = effectHistory.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        long currentTime = System.currentTimeMillis();
        Long lastTime = history.get(newEffect.getType());

        if (lastTime != null && (currentTime - lastTime) < 60000L) {
            int newDuration = newEffect.getDuration() / 2;
            if (newDuration > 0) {
                event.setCancelled(true);
                player.addPotionEffect(new PotionEffect(newEffect.getType(), newDuration, newEffect.getAmplifier(),
                    newEffect.isAmbient(), newEffect.hasParticles(), newEffect.hasIcon()));
                player.sendMessage(miniMessage.deserialize(C_RED + "⚠ " + toSmallCaps("Resistance active") + ": " +
                    C_ORANGE + toSmallCaps(newEffect.getType().key().value().replace("minecraft:", "").replace("_", " ")) + " " + toSmallCaps("duration halved!")));
            }
        }
        history.put(newEffect.getType(), currentTime);
    }

    private void sharePosition(Player player) {
        Location loc = player.getLocation();
        String posMsg = String.format("%s🗺 %s %s %s 📍 <white>%d, %d, %d</white> %s %s",
            C_GOLD, player.getName(), C_ORANGE, toSmallCaps("is at"), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), toSmallCaps("in"), toSmallCaps(player.getWorld().getName()));
        Bukkit.broadcast(miniMessage.deserialize(posMsg));
    }

    private int toggleSetting(CommandSourceStack source, String type) {
        if (!(source.getSender() instanceof Player player)) {
            source.getSender().sendPlainMessage("ᴘʟᴀʏᴇʀѕ ᴏɴʟʏ.");
            return 1;
        }
        PlayerSettings settings = playerSettings.computeIfAbsent(player.getUniqueId(), k -> new PlayerSettings());
        String msg;
        switch (type) {
            case "global" -> { settings.toggleGlobal(); msg = settings.globalEnabled ? C_GOLD + toSmallCaps("Dashboard enabled!") : "<red>" + toSmallCaps("Dashboard disabled!"); }
            case "xyz" -> { settings.toggleXyz(); msg = settings.showXyz ? C_ORANGE + toSmallCaps("XYZ display enabled!") : "<red>" + toSmallCaps("XYZ display disabled!"); }
            case "biome" -> { settings.toggleBiome(); msg = settings.showBiome ? C_YELLOW + toSmallCaps("Biome display enabled!") : "<red>" + toSmallCaps("Biome display disabled!"); }
            case "nether" -> { settings.toggleNetherXyz(); msg = settings.showNetherXyz ? C_RED + toSmallCaps("Nether XYZ enabled!") : "<dark_red>" + toSmallCaps("Nether XYZ disabled!"); }
            default -> msg = toSmallCaps("Unknown toggle.");
        }
        player.sendMessage(miniMessage.deserialize(msg));
        if (!settings.globalEnabled) player.sendActionBar(Component.empty());
        return 1;
    }

    private void updateActionBar(Player player) {
        PlayerSettings settings = playerSettings.computeIfAbsent(player.getUniqueId(), k -> new PlayerSettings());
        if (!settings.globalEnabled) return;
        Location loc = player.getLocation();
        StringBuilder sb = new StringBuilder();
        if (settings.showXyz) sb.append(C_GOLD).append("<b>🗺</b> ").append(C_ORANGE).append(loc.getBlockX()).append(" ").append(loc.getBlockY()).append(" ").append(loc.getBlockZ()).append(" ");
        if (settings.showNetherXyz) {
            World.Environment env = player.getWorld().getEnvironment();
            if (env == World.Environment.NORMAL || env == World.Environment.NETHER) {
                double ratio = (env == World.Environment.NORMAL) ? 0.125 : 8.0;
                if (!sb.isEmpty()) sb.append(C_GRAY).append("| ");
                sb.append((env == World.Environment.NORMAL) ? C_RED + "<b>🔥</b> " : C_GREEN + "<b>🌳</b> ").append((int)(loc.getX()*ratio)).append(" ").append((int)(loc.getZ()*ratio)).append(" ");
            }
        }
        if (settings.showBiome) {
            if (!sb.isEmpty()) sb.append(C_GRAY).append("| ");
            sb.append(C_YELLOW).append("❊ ").append(toSmallCaps(player.getWorld().getBiome(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()).key().value().replace("minecraft:", "").replace("_", " ")));
        }
        if (!sb.isEmpty()) player.sendActionBar(miniMessage.deserialize(sb.toString().trim()));
    }

    private String toSmallCaps(String input) {
        if (input == null) return "";
        String normal = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String small  = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀѕᴛᴜᴠᴡхʏᴢᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀѕᴛᴜᴠᴡхʏᴢ";
        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            int index = normal.indexOf(c);
            result.append(index != -1 ? small.charAt(index) : c);
        }
        return result.toString();
    }
}
