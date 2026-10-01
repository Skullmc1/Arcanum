package space.qclid.arcanum.feature;

import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import space.qclid.arcanum.PlayerSettings;
import space.qclid.arcanum.data.DataManager;

import java.util.List;

import static space.qclid.arcanum.util.TextUtil.*;

/**
 * Handles the /destination command: set a coordinate target shown on the action bar
 * and followed by the navigation particle trail.
 */
public class DestinationFeature {

    private final DataManager data;

    public DestinationFeature(DataManager data) {
        this.data = data;
    }

    public void registerCommands(Commands commands) {
        var builder = Commands.literal("destination")
            .then(Commands.literal("clear").executes(ctx -> {
                if (!(ctx.getSource().getSender() instanceof Player player)) return 1;
                PlayerSettings settings = data.get(player.getUniqueId());
                if (settings != null) settings.destination = null;
                player.sendMessage(MM.deserialize(C_GOLD + toSmallCaps("Destination cleared!")));
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
                            PlayerSettings settings = data.getOrCreate(player.getUniqueId());
                            settings.destination    = new org.bukkit.Location(player.getWorld(), x, y, z);
                            settings.trackingPlayer = null;
                            player.sendMessage(MM.deserialize(C_GOLD + toSmallCaps("Destination set to ")
                                    + (int) x + ", " + (int) y + ", " + (int) z));
                            return 1;
                        }))));

        commands.register(builder.build(), "Set a navigation destination", List.of("dest"));
    }
}
