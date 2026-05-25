package space.qclid.dashboard;

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
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
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
    private final Map<UUID, UUID> tpRequests = new HashMap<>();
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

    private static final String G_GOLD = "<gradient:#FFD700:#FFA500>";

    private static class PlayerSettings {
        boolean showXyz = true;
        boolean showBiome = true;
        boolean showNetherXyz = true;
        boolean globalEnabled = true;
        int tpCharges = 0;
        final Map<String, Location> waypoints = new LinkedHashMap<>();
        Location destination = null;

        void toggleGlobal() { globalEnabled = !globalEnabled; }
        void toggleXyz() { showXyz = !showXyz; }
        void toggleBiome() { showBiome = !showBiome; }
        void toggleNetherXyz() { showNetherXyz = !showNetherXyz; }
    }

    @Override
    public void onEnable() {
        getLogger().info("Dashboard v" + getDescription().getVersion() + " enabled! Auto-updater and features active.");

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

            var tpBuilder = Commands.literal("tp")
                .then(Commands.literal("accept").executes(ctx -> {
                    handleTpResponse(ctx.getSource(), true);
                    return 1;
                }))
                .then(Commands.literal("deny").executes(ctx -> {
                    handleTpResponse(ctx.getSource(), false);
                    return 1;
                }))
                .executes(ctx -> {
                    if (!(ctx.getSource().getSender() instanceof Player player)) {
                        ctx.getSource().getSender().sendPlainMessage("ᴘʟᴀʏᴇʀѕ ᴏɴʟʏ.");
                        return 1;
                    }
                    openTpMainMenu(player);
                    return 1;
                });

            commands.register(tpBuilder.build(), "Teleport GUI system", List.of());

            var destBuilder = Commands.literal("destination")
                .then(Commands.literal("clear").executes(ctx -> {
                    if (!(ctx.getSource().getSender() instanceof Player player)) return 1;
                    PlayerSettings settings = playerSettings.get(player.getUniqueId());
                    if (settings != null) settings.destination = null;
                    player.sendMessage(miniMessage.deserialize(C_GOLD + toSmallCaps("Destination cleared!")));
                    return 1;
                }))
                .then(Commands.argument("x", com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg())
                    .then(Commands.argument("y", com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg())
                        .then(Commands.argument("z", com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg())
                            .executes(ctx -> {
                                if (!(ctx.getSource().getSender() instanceof Player player)) return 1;
                                double x = ctx.getArgument("x", Double.class);
                                double y = ctx.getArgument("y", Double.class);
                                double z = ctx.getArgument("z", Double.class);
                                PlayerSettings settings = playerSettings.computeIfAbsent(player.getUniqueId(), k -> new PlayerSettings());
                                settings.destination = new Location(player.getWorld(), x, y, z);
                                player.sendMessage(miniMessage.deserialize(C_GOLD + toSmallCaps("Destination set to ") + (int)x + ", " + (int)y + ", " + (int)z));
                                return 1;
                            }))));

            commands.register(destBuilder.build(), "Set a navigation destination", List.of("dest"));
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
        String startMsg = "Checking for updates...";
        String failMsg = "Updating failed, will try again next time server restarts.";

        if (source != null) source.getSender().sendMessage(miniMessage.deserialize(C_GOLD + toSmallCaps("[Dashboard] " + startMsg)));
        else getLogger().info(startMsg);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(updateUrl)).GET().build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(response -> {
            if (response.statusCode() != 200) {
                if (source != null) source.getSender().sendMessage(miniMessage.deserialize(C_RED + toSmallCaps("[Dashboard] " + failMsg)));
                else getLogger().warning(failMsg);
                return;
            }

            try {
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                String latestVersion = json.get("version").getAsString();
                String downloadUrl = json.get("downloadUrl").getAsString();

                if (isNewer(latestVersion, getDescription().getVersion())) {
                    String msg = "New update found! Downloading v" + latestVersion;
                    if (source != null) source.getSender().sendMessage(miniMessage.deserialize(C_GOLD + toSmallCaps("[Dashboard] " + msg)));
                    else getLogger().info(msg);

                    downloadAndPrepareUpdate(downloadUrl);
                } else {
                    String upToDateMsg = "Plugin is up to date!";
                    if (source != null) source.getSender().sendMessage(miniMessage.deserialize(C_GREEN + toSmallCaps("[Dashboard] " + upToDateMsg)));
                    else getLogger().info(upToDateMsg);
                }
            } catch (Exception e) {
                if (source != null) source.getSender().sendMessage(miniMessage.deserialize(C_RED + toSmallCaps("[Dashboard] " + failMsg)));
                else getLogger().warning(failMsg + " (" + e.getMessage() + ")");
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

    // --- TP GUI System ---

    private static final String GUI_TP_MAIN = "ᴛᴇʟᴇᴘᴏʀᴛ ᴍᴇɴᴜ";
    private static final String GUI_TP_SELECT = "ѕᴇʟᴇᴄᴛ ᴀ ᴘʟᴀʏᴇʀ";

    private void openTpMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, miniMessage.deserialize(G_GOLD + toSmallCaps(GUI_TP_MAIN)));
        PlayerSettings settings = playerSettings.computeIfAbsent(player.getUniqueId(), k -> new PlayerSettings());

        ItemStack grayGlass = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) inv.setItem(i, grayGlass);

        inv.setItem(13, createItem(Material.ENDER_EYE, G_GOLD + toSmallCaps("teleport stats"),
            C_YELLOW + toSmallCaps("charges") + ": " + C_ORANGE + settings.tpCharges));

        inv.setItem(29, createItem(Material.DIAMOND, G_GOLD + toSmallCaps("charge tp"),
            C_GRAY + toSmallCaps("click with 20 diamonds to add 1 charge")));

        inv.setItem(31, createItem(Material.ENDER_PEARL, G_GOLD + toSmallCaps("use charge"),
            C_GRAY + toSmallCaps("click to select a player to teleport to")));

        inv.setItem(33, createItem(Material.NAME_TAG, G_GOLD + toSmallCaps("create waypoint"),
            C_GRAY + toSmallCaps("click with 30 diamonds to save current location")));

        // Waypoints in bottom 2 rows (36-53)
        int slot = 36;
        for (Map.Entry<String, Location> entry : settings.waypoints.entrySet()) {
            if (slot > 53) break;
            Location l = entry.getValue();
            inv.setItem(slot++, createItem(Material.COMPASS, G_GOLD + toSmallCaps(entry.getKey()),
                C_YELLOW + toSmallCaps("location") + ": " + C_ORANGE + l.getBlockX() + " " + l.getBlockY() + " " + l.getBlockZ(),
                C_RED + toSmallCaps("cost") + ": " + C_ORANGE + "5 " + toSmallCaps("charges")));
        }

        player.openInventory(inv);
    }

    private void openPlayerSelector(Player player) {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        players.remove(player);
        int size = ((players.size() / 9) + 1) * 9;
        Inventory inv = Bukkit.createInventory(null, Math.min(54, Math.max(9, size)), miniMessage.deserialize(G_GOLD + toSmallCaps(GUI_TP_SELECT)));

        for (Player p : players) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(p);
            meta.displayName(miniMessage.deserialize(G_GOLD + p.getName()));
            head.setItemMeta(meta);
            inv.addItem(head);
        }
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = miniMessage.serialize(event.getView().title());
        // Clean title for comparison (MiniMessage tags might vary slightly)
        boolean isMain = title.contains(toSmallCaps(GUI_TP_MAIN));
        boolean isSelect = title.contains(toSmallCaps(GUI_TP_SELECT));

        if (!isMain && !isSelect) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        PlayerSettings settings = playerSettings.computeIfAbsent(player.getUniqueId(), k -> new PlayerSettings());

        if (isMain) {
            int slot = event.getRawSlot();
            if (slot == 29) { // Charge TP
                ItemStack cursor = event.getCursor();
                if (cursor.getType() == Material.DIAMOND && cursor.getAmount() >= 20) {
                    cursor.setAmount(cursor.getAmount() - 20);
                    settings.tpCharges++;
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                    openTpMainMenu(player);
                } else {
                    player.sendMessage(miniMessage.deserialize(C_RED + toSmallCaps("You need 20 diamonds on your cursor!")));
                }
            } else if (slot == 31) { // Use Charge
                if (settings.tpCharges > 0) openPlayerSelector(player);
                else player.sendMessage(miniMessage.deserialize(C_RED + toSmallCaps("You have no charges left!")));
            } else if (slot == 33) { // Create Waypoint
                ItemStack cursor = event.getCursor();
                if (cursor.getType() == Material.DIAMOND && cursor.getAmount() >= 30) {
                    cursor.setAmount(cursor.getAmount() - 30);
                    String wpName = "waypoint " + (settings.waypoints.size() + 1);
                    settings.waypoints.put(wpName, player.getLocation().clone());
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1.2f);
                    player.sendMessage(miniMessage.deserialize(C_GREEN + toSmallCaps("Created permanent ") + wpName));
                    openTpMainMenu(player);
                } else {
                    player.sendMessage(miniMessage.deserialize(C_RED + toSmallCaps("You need 30 diamonds on your cursor!")));
                }
            } else if (slot >= 36 && slot <= 53) { // Click Waypoint
                if (clicked.getType() == Material.COMPASS) {
                    if (settings.tpCharges >= 5) {
                        String name = miniMessage.serialize(clicked.getItemMeta().displayName());
                        // Extract name from gradient if needed, or just look up by slot order
                        int index = slot - 36;
                        if (index < settings.waypoints.size()) {
                            String key = new ArrayList<>(settings.waypoints.keySet()).get(index);
                            Location loc = settings.waypoints.get(key);
                            settings.tpCharges -= 5;
                            player.teleport(loc);
                            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                            player.sendMessage(miniMessage.deserialize(C_GREEN + toSmallCaps("Teleported to ") + key));
                            player.closeInventory();
                        }
                    } else {
                        player.sendMessage(miniMessage.deserialize(C_RED + toSmallCaps("You need 5 charges to use a waypoint!")));
                    }
                }
            }
        } else if (isSelect) {
            if (clicked.getType() == Material.PLAYER_HEAD) {
                SkullMeta meta = (SkullMeta) clicked.getItemMeta();
                if (meta.getOwningPlayer() != null && meta.getOwningPlayer().getName() != null) {
                    Player target = Bukkit.getPlayer(meta.getOwningPlayer().getUniqueId());
                    if (target != null) {
                        tpRequests.put(target.getUniqueId(), player.getUniqueId());
                        target.sendMessage(miniMessage.deserialize(G_GOLD + toSmallCaps(player.getName() + " wants to teleport to you!")));
                        target.sendMessage(miniMessage.deserialize(C_YELLOW + toSmallCaps("Use /tp accept or /tp deny")));
                        player.sendMessage(miniMessage.deserialize(C_GREEN + toSmallCaps("Teleport request sent to " + target.getName())));
                        player.closeInventory();
                    }
                }
            }
        }
    }

    private void handleTpResponse(CommandSourceStack source, boolean accept) {
        if (!(source.getSender() instanceof Player target)) {
            source.getSender().sendPlainMessage("ᴘʟᴀʏᴇʀѕ ᴏɴʟʏ.");
            return;
        }

        UUID requesterId = tpRequests.remove(target.getUniqueId());
        if (requesterId == null) {
            target.sendMessage(miniMessage.deserialize(C_RED + toSmallCaps("No pending teleport requests.")));
            return;
        }

        Player requester = Bukkit.getPlayer(requesterId);
        if (requester == null) {
            target.sendMessage(miniMessage.deserialize(C_RED + toSmallCaps("Requester is no longer online.")));
            return;
        }

        if (accept) {
            PlayerSettings reqSettings = playerSettings.get(requesterId);
            if (reqSettings != null && reqSettings.tpCharges > 0) {
                reqSettings.tpCharges--;
                requester.teleport(target.getLocation());
                requester.playSound(requester.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                target.playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                requester.sendMessage(miniMessage.deserialize(C_GREEN + toSmallCaps("Teleport request accepted!")));
                target.sendMessage(miniMessage.deserialize(C_GREEN + toSmallCaps("Teleporting " + requester.getName() + " to you.")));
            } else {
                target.sendMessage(miniMessage.deserialize(C_RED + toSmallCaps("Requester no longer has enough charges.")));
                requester.sendMessage(miniMessage.deserialize(C_RED + toSmallCaps("Teleport failed: No charges left.")));
            }
        } else {
            target.sendMessage(miniMessage.deserialize(C_RED + toSmallCaps("Teleport request denied.")));
            requester.sendMessage(miniMessage.deserialize(C_RED + toSmallCaps(target.getName() + " denied your teleport request.")));
        }
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(miniMessage.deserialize(name));
        List<Component> loreList = new ArrayList<>();
        for (String s : lore) loreList.add(miniMessage.deserialize(s));
        meta.lore(loreList);
        item.setItemMeta(meta);
        return item;
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

        if (settings.destination != null && settings.destination.getWorld().equals(player.getWorld())) {
            double dist = loc.distance(settings.destination);
            String dir = getDirection(player, settings.destination);
            if (!sb.isEmpty()) sb.append(C_GRAY).append("| ");
            sb.append(C_PURPLE).append("🎯 ").append(C_ORANGE).append((int)dist).append("m ").append(C_YELLOW).append(dir).append(" ");
        }

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

    private String getDirection(Player player, Location target) {
        double angle = Math.toDegrees(Math.atan2(target.getZ() - player.getLocation().getZ(), target.getX() - player.getLocation().getX()));
        angle = (angle + 360) % 360;

        double playerYaw = (player.getLocation().getYaw() + 90 + 360) % 360;
        double relativeAngle = (angle - playerYaw + 360) % 360;

        if (relativeAngle > 337.5 || relativeAngle <= 22.5) return "⬆";
        if (relativeAngle > 22.5 && relativeAngle <= 67.5) return "↗";
        if (relativeAngle > 67.5 && relativeAngle <= 112.5) return "➡";
        if (relativeAngle > 112.5 && relativeAngle <= 157.5) return "↘";
        if (relativeAngle > 157.5 && relativeAngle <= 202.5) return "⬇";
        if (relativeAngle > 202.5 && relativeAngle <= 247.5) return "↙";
        if (relativeAngle > 247.5 && relativeAngle <= 292.5) return "⬅";
        if (relativeAngle > 292.5 && relativeAngle <= 337.5) return "↖";
        return "⬆";
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
