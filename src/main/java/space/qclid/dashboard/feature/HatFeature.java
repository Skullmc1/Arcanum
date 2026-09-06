package space.qclid.dashboard.feature;

import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

import static space.qclid.dashboard.util.TextUtil.*;

public class HatFeature {

    public void registerCommands(Commands commands) {
        var builder = Commands.literal("hat")
            .executes(ctx -> {
                if (!(ctx.getSource().getSender() instanceof Player player)) {
                    ctx.getSource().getSender().sendPlainMessage("ᴘʟᴀʏᴇʀѕ ᴏɴʟʏ.");
                    return 1;
                }
                ItemStack held = player.getInventory().getItemInMainHand();
                if (held.getType() == Material.AIR) {
                    player.sendMessage(MM.deserialize(C_RED + toSmallCaps("You must be holding an item!")));
                    return 1;
                }
                ItemStack head = player.getInventory().getHelmet();
                player.getInventory().setHelmet(held);
                player.getInventory().setItemInMainHand(head);
                player.sendMessage(MM.deserialize(C_GREEN + toSmallCaps("Enjoy your new hat!")));
                return 1;
            });
        commands.register(builder.build(), "Wear the item you're holding on your head", List.of());
    }
}
