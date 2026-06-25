package space.qclid.dashboard.feature;

import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

import static space.qclid.dashboard.util.TextUtil.*;

/**
 * Handles the /pos command: broadcast your current coordinates to all players
 * with a clickable [track] button.
 */
public class PosFeature {

    public void registerCommands(Commands commands) {
        var builder = Commands.literal("pos")
            .executes(ctx -> {
                if (!(ctx.getSource().getSender() instanceof Player player)) {
                    ctx.getSource().getSender().sendPlainMessage("ᴘʟᴀʏᴇʀѕ ᴏɴʟʏ.");
                    return 1;
                }
                sharePosition(player);
                return 1;
            });
        commands.register(builder.build(), "Share your position globally", List.of("where"));
    }

    private void sharePosition(Player player) {
        Location loc      = player.getLocation();
        String trackCmd   = "/track " + player.getName();
        String posMsg = String.format(
            "%s🗺 %s %s %s 📍 <white>%d, %d, %d</white> %s %s " +
            "<gray>[<click:run_command:'%s'><hover:show_text:'%s'><aqua>%s</aqua></hover></click>]",
            C_GOLD, player.getName(), C_ORANGE, toSmallCaps("is at"),
            loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(),
            toSmallCaps("in"), toSmallCaps(player.getWorld().getName()),
            trackCmd, toSmallCaps("click to track player"), toSmallCaps("track"));
        Bukkit.broadcast(MM.deserialize(posMsg));
    }
}
