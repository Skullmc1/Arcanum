package space.qclid.arcanum.feature;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.entity.Player;
import space.qclid.arcanum.PlayerSettings;
import space.qclid.arcanum.data.DataManager;

import java.util.List;

import static space.qclid.arcanum.util.TextUtil.*;

/**
 * Handles the /track command: point the navigation HUD at another online player.
 */
public class TrackFeature {

    private final DataManager data;

    public TrackFeature(DataManager data) {
        this.data = data;
    }

    public void registerCommands(Commands commands) {
        var builder = Commands.literal("track")
            .then(Commands.literal("clear").executes(ctx -> {
                if (!(ctx.getSource().getSender() instanceof Player player)) return 1;
                PlayerSettings settings = data.get(player.getUniqueId());
                if (settings != null) settings.trackingPlayer = null;
                player.sendMessage(MM.deserialize(C_GOLD + toSmallCaps("Tracking cleared!")));
                return 1;
            }))
            .then(Commands.argument("player", ArgumentTypes.player())
                .executes(ctx -> {
                    if (!(ctx.getSource().getSender() instanceof Player player)) return 1;
                    PlayerSelectorArgumentResolver selector = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
                    List<Player> targets = selector.resolve(ctx.getSource());
                    if (targets.isEmpty()) {
                        player.sendMessage(MM.deserialize(C_RED + toSmallCaps("Player not found!")));
                        return 1;
                    }
                    Player target = targets.get(0);
                    PlayerSettings settings = data.getOrCreate(player.getUniqueId());
                    settings.trackingPlayer = target.getUniqueId();
                    settings.destination    = null;
                    player.sendMessage(MM.deserialize(C_GOLD + toSmallCaps("Tracking ") + target.getName()));
                    return 1;
                }));

        commands.register(builder.build(), "Track a player's location", List.of());
    }
}
