package space.qclid.dashboard;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
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
        EnchantFeature       enchantFeature         = new EnchantFeature(this);
        space.qclid.dashboard.codex.CodexFeature codexFeature = new space.qclid.dashboard.codex.CodexFeature(this, dataManager);

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
            enchantFeature      .registerCommands(commands);
            codexFeature        .registerCommands(commands);
        });

        // ── Schedulers ────────────────────────────────────────────────────────
        getServer().getGlobalRegionScheduler().runAtFixedRate(this, task -> {
            for (var player : Bukkit.getOnlinePlayers()) {
                actionBarFeature.update(player);
                navigationFeature.spawnParticles(player);
            }
        }, 1L, 5L);

        // Auto-update check every 5 minutes (6000 ticks)
        getServer().getGlobalRegionScheduler().runAtFixedRate(this,
            task -> updateFeature.checkForUpdates(null, true), 1L, 6000L);

        // Keep a reference for onDisable
        this.getServer().getPluginManager().getPlugin("Dashboard"); // no-op; refs held in lambdas above
        // Store as fields for onDisable access
        _dataManager    = dataManager;
        _updateFeature  = updateFeature;
    }

    // Held purely so onDisable can call save/shutdown
    private DataManager   _dataManager;
    private UpdateFeature _updateFeature;

    @Override
    public void onDisable() {
        if (_dataManager   != null) _dataManager.save();
        if (_updateFeature != null) _updateFeature.onShutdown();
        getLogger().info("Dashboard disabled.");
    }
}
