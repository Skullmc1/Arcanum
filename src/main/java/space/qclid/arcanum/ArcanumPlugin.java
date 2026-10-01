package space.qclid.arcanum;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.bukkit.plugin.java.JavaPlugin;
import space.qclid.arcanum.codex.CodexFeature;
import space.qclid.arcanum.data.DataManager;
import space.qclid.arcanum.feature.*;
import space.qclid.arcanum.skills.SkillsFeature;

/**
 * Arcanum Plugin — main entry point.
 *
 * This class is intentionally slim: it wires together the feature modules,
 * registers the command lifecycle handler, and starts the recurring schedulers.
 * All business logic lives in the feature/* and data/* packages.
 */
public class ArcanumPlugin extends JavaPlugin {

    private static final long ACTION_BAR_INTERVAL = 5L;
    private static final long UPDATE_CHECK_INTERVAL = 6000L;

    @Override
    public void onEnable() {
        getLogger().info("Arcanum v" + getDescription().getVersion() + " enabled!");

        migrateLegacyData();

        // ── Data layer ────────────────────────────────────────────────────────
        DataManager dataManager = new DataManager(this);

        // ── Features (Listener subclasses self-register in their constructors) ─
        UpdateFeature        updateFeature        = new UpdateFeature(this);
        ActionBarFeature     actionBarFeature      = new ActionBarFeature(this, dataManager, updateFeature);
        SkillsFeature        skillsFeature         = new SkillsFeature(this);
        actionBarFeature.setSkillPopup(skillsFeature::popupFor);
        NavigationFeature    navigationFeature     = new NavigationFeature(dataManager);
        WaypointFeature      waypointFeature       = new WaypointFeature(dataManager);
        DestinationFeature   destinationFeature    = new DestinationFeature(dataManager);
        TrackFeature         trackFeature          = new TrackFeature(dataManager);
        PosFeature           posFeature            = new PosFeature();
        SuicideFeature       suicideFeature        = new SuicideFeature();
        HatFeature           hatFeature            = new HatFeature();
        PingFeature          pingFeature           = new PingFeature();
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
            suicideFeature      .registerCommands(commands);
            hatFeature          .registerCommands(commands);
            pingFeature         .registerCommands(commands);
            linkedChestFeature  .registerCommands(commands);
            deathInventoryFeature.registerCommands(commands);
            codexFeature        .registerCommands(commands);
            skillsFeature       .registerCommands(commands);
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

        skillsFeature.start();

        // Store as fields for onDisable access
        this.dataManager    = dataManager;
        this.updateFeature  = updateFeature;
        this.skillsFeature  = skillsFeature;
    }

    /** Skills subsystem (null until enabled); lets Codex tools award skill XP for blocks they break. */
    public SkillsFeature skills() {
        return skillsFeature;
    }

    // Held purely so onDisable can call save/shutdown
    private DataManager    dataManager;
    private UpdateFeature  updateFeature;
    private CodexFeature   codexFeature;
    private SkillsFeature  skillsFeature;

    @Override
    public void onDisable() {
        if (skillsFeature != null) skillsFeature.shutdown();
        if (dataManager   != null) dataManager.save();
        if (updateFeature != null) updateFeature.onShutdown();
        if (codexFeature  != null) codexFeature.getManager().save();
        getLogger().info("Arcanum disabled.");
    }

    /** Arcanum was formerly called Dashboard: adopt its data folder (plugins/Dashboard) on first start. */
    private void migrateLegacyData() {
        File legacy = new File(getDataFolder().getParentFile(), "Dashboard");
        File current = getDataFolder();
        if (!legacy.isDirectory()) return;
        String[] existing = current.list();
        if (existing != null && existing.length > 0) return;
        try {
            if (current.exists()) current.delete();
            Files.move(legacy.toPath(), current.toPath());
            getLogger().info("Migrated data from plugins/Dashboard to plugins/Arcanum.");
        } catch (IOException e) {
            getLogger().warning("Could not migrate legacy Dashboard data: " + e.getMessage());
        }
    }
}
