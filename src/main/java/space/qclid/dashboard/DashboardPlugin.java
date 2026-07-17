package space.qclid.dashboard;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import space.qclid.dashboard.codex.CodexFeature;
import space.qclid.dashboard.data.DataManager;
import space.qclid.dashboard.feature.*;

/**
 * Dashboard Plugin — main entry point.
 *
 * This class is intentionally slim: it wires together the feature modules,
 * registers the command lifecycle handler, and starts the recurring schedulers.
 * All business logic lives in the feature/* and data/* packages.
 */
public class DashboardPlugin extends JavaPlugin {

    private static final long ACTION_BAR_INTERVAL = 5L;
    private static final long UPDATE_CHECK_INTERVAL = 6000L;

    @Override
    public void onEnable() {
        getLogger().info("Dashboard v" + getDescription().getVersion() + " enabled!");

        // ── Data layer ────────────────────────────────────────────────────────
        DataManager dataManager = new DataManager(this);

        // ── Features (Listener subclasses self-register in their constructors) ─
        UpdateFeature        updateFeature        = new UpdateFeature(this);
        ActionBarFeature     actionBarFeature      = new ActionBarFeature(this, dataManager, updateFeature);
        NavigationFeature    navigationFeature     = new NavigationFeature(dataManager);
        WaypointFeature      waypointFeature       = new WaypointFeature(dataManager);
        DestinationFeature   destinationFeature    = new DestinationFeature(dataManager);
        TrackFeature         trackFeature          = new TrackFeature(dataManager);
        PosFeature           posFeature            = new PosFeature();
        LinkedChestFeature   linkedChestFeature    = new LinkedChestFeature(dataManager);
        DeathInventoryFeature deathInventoryFeature = new DeathInventoryFeature(this, dataManager);
        NewGadgetsFeature    newGadgetsFeature      = new NewGadgetsFeature(this);
        this.codexFeature = new CodexFeature(this, dataManager, newGadgetsFeature);

        // passive listeners (no commands)
        new ServerPingFeature(this);
        new PotionResistanceFeature(this);

        // ── Commands ──────────────────────────────────────────────────────────
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            var commands = event.registrar();
            actionBarFeature    .registerCommands(commands);
            waypointFeature     .registerCommands(commands);
            destinationFeature  .registerCommands(commands);
            trackFeature        .registerCommands(commands);
            posFeature          .registerCommands(commands);
            linkedChestFeature  .registerCommands(commands);
            deathInventoryFeature.registerCommands(commands);
            codexFeature        .registerCommands(commands);
        });

        // ── Schedulers ────────────────────────────────────────────────────────
        getServer().getGlobalRegionScheduler().runAtFixedRate(this, task -> {
            for (var player : Bukkit.getOnlinePlayers()) {
                actionBarFeature.update(player);
                navigationFeature.spawnParticles(player);
            }
        }, 1L, ACTION_BAR_INTERVAL);

        // Auto-update check every 5 minutes (6000 ticks)
        getServer().getGlobalRegionScheduler().runAtFixedRate(this,
            task -> updateFeature.checkForUpdates(null, true), 1L, UPDATE_CHECK_INTERVAL);

        // Store as fields for onDisable access
        this.dataManager    = dataManager;
        this.updateFeature  = updateFeature;
    }

    // Held purely so onDisable can call save/shutdown
    private DataManager    dataManager;
    private UpdateFeature  updateFeature;
    private CodexFeature   codexFeature;

    @Override
    public void onDisable() {
        if (dataManager   != null) dataManager.save();
        if (updateFeature != null) updateFeature.onShutdown();
        if (codexFeature  != null) codexFeature.getManager().save();
        getLogger().info("Dashboard disabled.");
    }
}
