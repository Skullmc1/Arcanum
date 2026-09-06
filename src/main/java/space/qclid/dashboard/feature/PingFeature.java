package space.qclid.dashboard.feature;

import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;

import java.util.List;

import static space.qclid.dashboard.util.TextUtil.*;

public class PingFeature {

    public void registerCommands(Commands commands) {
        var builder = Commands.literal("ping")
            .executes(ctx -> {
                if (!(ctx.getSource().getSender() instanceof Player player)) {
                    ctx.getSource().getSender().sendPlainMessage("ᴘʟᴀʏᴇʀѕ ᴏɴʟʏ.");
                    return 1;
                }
                int ping = player.getPing();
                String color = ping < 100 ? C_GREEN : ping < 200 ? C_YELLOW : C_RED;
                player.sendMessage(MM.deserialize(C_GOLD + toSmallCaps("Your ping: ") + color + ping + "ms"));
                return 1;
            });
        commands.register(builder.build(), "Check your connection latency", List.of());
    }
}
