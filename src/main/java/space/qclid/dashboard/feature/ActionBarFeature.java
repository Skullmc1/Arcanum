package space.qclid.dashboard.feature;

import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import space.qclid.dashboard.PlayerSettings;
import space.qclid.dashboard.data.DataManager;

import static space.qclid.dashboard.util.TextUtil.*;

/**
 * Renders the HUD action bar and handles all /dashboard toggle subcommands.
 * Also contains /dashboard update, delegating to UpdateFeature.
 */
public class ActionBarFeature {

    private final DataManager data;
    private final UpdateFeature updateFeature;

    public ActionBarFeature(JavaPlugin plugin, DataManager data, UpdateFeature updateFeature) {
        this.data          = data;
        this.updateFeature = updateFeature;
    }

    public void registerCommands(Commands commands) {
        var builder = Commands.literal("dashboard")
            .then(Commands.literal("toggle").executes(ctx -> toggleSetting(ctx.getSource(), "global")))
            .then(Commands.literal("xyz")   .executes(ctx -> toggleSetting(ctx.getSource(), "xyz")))
            .then(Commands.literal("biome") .executes(ctx -> toggleSetting(ctx.getSource(), "biome")))
            .then(Commands.literal("nether").executes(ctx -> toggleSetting(ctx.getSource(), "nether")))
            .then(Commands.literal("update").executes(ctx -> {
                updateFeature.checkForUpdates(ctx.getSource(), false);
                return 1;
            }));
        commands.register(builder.build(), "Control the dashboard", java.util.List.of("db"));
    }

    // ── Per-player update (called by scheduler) ───────────────────────────────

    public void update(Player player) {
        PlayerSettings settings = data.getOrCreate(player.getUniqueId());
        if (!settings.globalEnabled) return;

        Location loc = player.getLocation();
        StringBuilder sb = new StringBuilder();

        if (settings.showXyz) {
            sb.append(C_GOLD).append("<b>🗺</b> ")
              .append(C_ORANGE).append(loc.getBlockX()).append(" ")
              .append(loc.getBlockY()).append(" ").append(loc.getBlockZ()).append(" ");
        }

        if (settings.destination != null && settings.destination.getWorld() != null
                && settings.destination.getWorld().equals(player.getWorld())) {
            double dist = loc.distance(settings.destination);
            String dir  = getDirection(player, settings.destination);
            if (!sb.isEmpty()) sb.append(C_GRAY).append("| ");
            sb.append(C_PURPLE).append("🎯 ")
              .append(C_ORANGE).append((int) dist).append("m ")
              .append(C_YELLOW).append(dir).append(" ");
        }

        if (settings.trackingPlayer != null) {
            Player target = org.bukkit.Bukkit.getPlayer(settings.trackingPlayer);
            if (target != null && target.isOnline() && target.getWorld().equals(player.getWorld())) {
                double dist = loc.distance(target.getLocation());
                String dir  = getDirection(player, target.getLocation());
                if (!sb.isEmpty()) sb.append(C_GRAY).append("| ");
                sb.append(C_PURPLE).append("👤 ")
                  .append(C_ORANGE).append(target.getName()).append(" ")
                  .append((int) dist).append("m ")
                  .append(C_YELLOW).append(dir).append(" ");
            }
        }

        if (settings.showNetherXyz) {
            World.Environment env = player.getWorld().getEnvironment();
            if (env == World.Environment.NORMAL || env == World.Environment.NETHER) {
                double ratio = (env == World.Environment.NORMAL) ? 0.125 : 8.0;
                if (!sb.isEmpty()) sb.append(C_GRAY).append("| ");
                sb.append(env == World.Environment.NORMAL ? C_RED + "<b>🔥</b> " : C_GREEN + "<b>🌳</b> ")
                  .append((int) (loc.getX() * ratio)).append(" ")
                  .append((int) (loc.getZ() * ratio)).append(" ");
            }
        }

        if (settings.showBiome) {
            if (!sb.isEmpty()) sb.append(C_GRAY).append("| ");
            String biome = player.getWorld()
                    .getBiome(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())
                    .key().value().replace("minecraft:", "").replace("_", " ");
            sb.append(C_YELLOW).append("❊ ").append(toSmallCaps(biome));
        }

        if (!sb.isEmpty()) player.sendActionBar(MM.deserialize(sb.toString().trim()));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private int toggleSetting(io.papermc.paper.command.brigadier.CommandSourceStack source, String type) {
        if (!(source.getSender() instanceof Player player)) {
            source.getSender().sendPlainMessage("ᴘʟᴀʏᴇʀѕ ᴏɴʟʏ.");
            return 1;
        }
        PlayerSettings settings = data.getOrCreate(player.getUniqueId());
        String msg;
        switch (type) {
            case "global" -> { settings.toggleGlobal();    msg = settings.globalEnabled ? C_GOLD   + toSmallCaps("Dashboard enabled!")    : "<red>"      + toSmallCaps("Dashboard disabled!"); }
            case "xyz"    -> { settings.toggleXyz();       msg = settings.showXyz       ? C_ORANGE  + toSmallCaps("XYZ display enabled!")  : "<red>"      + toSmallCaps("XYZ display disabled!"); }
            case "biome"  -> { settings.toggleBiome();     msg = settings.showBiome     ? C_YELLOW  + toSmallCaps("Biome display enabled!"): "<red>"      + toSmallCaps("Biome display disabled!"); }
            case "nether" -> { settings.toggleNetherXyz(); msg = settings.showNetherXyz ? C_RED     + toSmallCaps("Nether XYZ enabled!")   : "<dark_red>" + toSmallCaps("Nether XYZ disabled!"); }
            default       -> msg = toSmallCaps("Unknown toggle.");
        }
        player.sendMessage(MM.deserialize(msg));
        if (!settings.globalEnabled) player.sendActionBar(Component.empty());
        return 1;
    }

    private String getDirection(Player player, Location target) {
        double angle       = Math.toDegrees(Math.atan2(
                target.getZ() - player.getLocation().getZ(),
                target.getX() - player.getLocation().getX()));
        angle = (angle + 360) % 360;
        double playerYaw   = (player.getLocation().getYaw() + 90 + 360) % 360;
        double relative    = (angle - playerYaw + 360) % 360;

        if (relative > 337.5 || relative <= 22.5)  return "⬆";
        if (relative > 22.5  && relative <= 67.5)  return "↗";
        if (relative > 67.5  && relative <= 112.5) return "➡";
        if (relative > 112.5 && relative <= 157.5) return "↘";
        if (relative > 157.5 && relative <= 202.5) return "⬇";
        if (relative > 202.5 && relative <= 247.5) return "↙";
        if (relative > 247.5 && relative <= 292.5) return "⬅";
        return "↖";
    }
}
