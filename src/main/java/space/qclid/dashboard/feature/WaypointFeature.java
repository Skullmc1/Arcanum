package space.qclid.dashboard.feature;

import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import space.qclid.dashboard.PlayerSettings;
import space.qclid.dashboard.data.DataManager;

import java.util.List;

import static space.qclid.dashboard.util.TextUtil.*;

/**
 * Manages player waypoints:
 * create, tp, delete, navigate (set as destination), share (broadcast with clickable buttons).
 */
public class WaypointFeature {

    private final DataManager data;

    public WaypointFeature(DataManager data) {
        this.data = data;
    }

    public void registerCommands(Commands commands) {
        var builder = Commands.literal("waypoint")
            .then(Commands.literal("create")
                .then(Commands.argument("name", com.mojang.brigadier.arguments.StringArgumentType.word())
                    .executes(ctx -> {
                        if (!(ctx.getSource().getSender() instanceof Player player)) return 1;
                        String name = ctx.getArgument("name", String.class);

                        if (name.contains(".")) {
                            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Waypoint names cannot contain dots!")));
                            return 1;
                        }

                        ItemStack cost = new ItemStack(org.bukkit.Material.DIAMOND, 5);
                        if (!player.getInventory().containsAtLeast(cost, 5)) {
                            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("You need 5 diamonds to create a waypoint!")));
                            return 1;
                        }

                        PlayerSettings settings = data.getOrCreate(player.getUniqueId());
                        if (settings.waypoints.containsKey(name)) {
                            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Waypoint already exists!")));
                            return 1;
                        }

                        player.getInventory().removeItem(cost);
                        settings.waypoints.put(name, player.getLocation().clone());
                        data.save();

                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1.2f);
                        player.sendMessage(MM.deserialize(C_GREEN + toSmallCaps("Created waypoint: ") + name));
                        return 1;
                    })))
            .then(Commands.literal("tp")
                .then(Commands.argument("name", com.mojang.brigadier.arguments.StringArgumentType.word())
                    .suggests((ctx, builder2) -> {
                        if (!(ctx.getSource().getSender() instanceof Player player)) return builder2.buildFuture();
                        PlayerSettings settings = data.get(player.getUniqueId());
                        if (settings != null) {
                            for (String wpName : settings.waypoints.keySet()) {
                                if (wpName.toLowerCase().startsWith(builder2.getRemaining().toLowerCase()))
                                    builder2.suggest(wpName);
                            }
                        }
                        if ("respawn".startsWith(builder2.getRemaining().toLowerCase())) builder2.suggest("respawn");
                        return builder2.buildFuture();
                    })
                    .executes(ctx -> {
                        if (!(ctx.getSource().getSender() instanceof Player player)) return 1;
                        String name = ctx.getArgument("name", String.class);

                        Location loc = null;
                        if (name.equalsIgnoreCase("respawn")) {
                            loc = player.getRespawnLocation();
                            if (loc == null) loc = player.getWorld().getSpawnLocation();
                        } else {
                            PlayerSettings settings = data.get(player.getUniqueId());
                            if (settings != null) loc = settings.waypoints.get(name);
                        }

                        if (loc == null) {
                            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Waypoint not found!")));
                            return 1;
                        }
                        player.teleport(loc);
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                        player.sendMessage(MM.deserialize(C_GREEN + toSmallCaps("Teleported to ") + name));
                        return 1;
                    })))
            .then(Commands.literal("delete")
                .then(Commands.argument("name", com.mojang.brigadier.arguments.StringArgumentType.word())
                    .suggests((ctx, builder2) -> {
                        if (!(ctx.getSource().getSender() instanceof Player player)) return builder2.buildFuture();
                        PlayerSettings settings = data.get(player.getUniqueId());
                        if (settings != null) {
                            for (String wpName : settings.waypoints.keySet()) {
                                if (wpName.toLowerCase().startsWith(builder2.getRemaining().toLowerCase()))
                                    builder2.suggest(wpName);
                            }
                        }
                        return builder2.buildFuture();
                    })
                    .executes(ctx -> {
                        if (!(ctx.getSource().getSender() instanceof Player player)) return 1;
                        String name = ctx.getArgument("name", String.class);
                        PlayerSettings settings = data.get(player.getUniqueId());
                        if (settings == null || !settings.waypoints.containsKey(name)) {
                            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Waypoint not found!")));
                            return 1;
                        }
                        settings.waypoints.remove(name);
                        data.save();
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1.3f);
                        player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Deleted waypoint: ") + name));
                        return 1;
                    })))
            .then(Commands.literal("navigate")
                .then(Commands.argument("name", com.mojang.brigadier.arguments.StringArgumentType.word())
                    .suggests((ctx, builder2) -> {
                        if (!(ctx.getSource().getSender() instanceof Player player)) return builder2.buildFuture();
                        PlayerSettings settings = data.get(player.getUniqueId());
                        if (settings != null) {
                            for (String wpName : settings.waypoints.keySet()) {
                                if (wpName.toLowerCase().startsWith(builder2.getRemaining().toLowerCase()))
                                    builder2.suggest(wpName);
                            }
                        }
                        return builder2.buildFuture();
                    })
                    .executes(ctx -> {
                        if (!(ctx.getSource().getSender() instanceof Player player)) return 1;
                        String name = ctx.getArgument("name", String.class);
                        PlayerSettings settings = data.get(player.getUniqueId());
                        if (settings == null || !settings.waypoints.containsKey(name)) {
                            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Waypoint not found!")));
                            return 1;
                        }
                        Location dest = settings.waypoints.get(name);
                        settings.destination    = dest.clone();
                        settings.trackingPlayer = null;
                        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.5f);
                        player.sendMessage(MM.deserialize(C_GOLD + "🧭 " + toSmallCaps("Navigating to ") + C_ORANGE + name
                                + C_GRAY + " (" + dest.getBlockX() + ", " + dest.getBlockY() + ", " + dest.getBlockZ() + ")"));
                        return 1;
                    })))
            .then(Commands.literal("share")
                .then(Commands.argument("name", com.mojang.brigadier.arguments.StringArgumentType.word())
                    .suggests((ctx, builder2) -> {
                        if (!(ctx.getSource().getSender() instanceof Player player)) return builder2.buildFuture();
                        PlayerSettings settings = data.get(player.getUniqueId());
                        if (settings != null) {
                            for (String wpName : settings.waypoints.keySet()) {
                                if (wpName.toLowerCase().startsWith(builder2.getRemaining().toLowerCase()))
                                    builder2.suggest(wpName);
                            }
                        }
                        return builder2.buildFuture();
                    })
                    .executes(ctx -> {
                        if (!(ctx.getSource().getSender() instanceof Player player)) return 1;
                        String name = ctx.getArgument("name", String.class);
                        PlayerSettings settings = data.get(player.getUniqueId());
                        if (settings == null || !settings.waypoints.containsKey(name)) {
                            player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Waypoint not found!")));
                            return 1;
                        }
                        shareWaypoint(player, name, settings.waypoints.get(name));
                        return 1;
                    })));

        commands.register(builder.build(), "Manage your waypoints", List.of("wp"));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void shareWaypoint(Player player, String name, Location loc) {
        String destCmd = String.format("/destination %d %d %d", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        String navCmd  = "/waypoint navigate " + name;
        String msg = String.format(
            "%s📍 %s %s %s \"%s\" %s <white>%d, %d, %d</white> %s %s " +
            "<gray>[<click:run_command:'%s'><hover:show_text:'%s'><green>%s</green></hover></click>] " +
            "[<click:run_command:'%s'><hover:show_text:'%s'><aqua>%s</aqua></hover></click>]",
            C_GOLD, player.getName(), C_ORANGE, toSmallCaps("shared waypoint"),
            name, toSmallCaps("at"),
            loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(),
            toSmallCaps("in"), toSmallCaps(loc.getWorld() != null ? loc.getWorld().getName() : "unknown"),
            destCmd, toSmallCaps("set as your destination"), toSmallCaps("set destination"),
            navCmd,  toSmallCaps("navigate here (particle trail)"),  toSmallCaps("navigate"));
        Bukkit.broadcast(MM.deserialize(msg));
    }
}
