package space.qclid.dashboard.feature;

import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.List;

import static space.qclid.dashboard.util.TextUtil.*;

public class SuicideFeature {

    public void registerCommands(Commands commands) {
        var builder = Commands.literal("suicide")
            .executes(ctx -> {
                if (!(ctx.getSource().getSender() instanceof Player player)) {
                    ctx.getSource().getSender().sendPlainMessage("ᴘʟᴀʏᴇʀѕ ᴏɴʟʏ.");
                    return 1;
                }
                player.setLastDamageCause(new EntityDamageEvent(player, EntityDamageEvent.DamageCause.SUICIDE, 0));
                player.setHealth(0);
                return 1;
            });
        commands.register(builder.build(), "Kill yourself to get out of sticky situations", List.of());
    }
}
