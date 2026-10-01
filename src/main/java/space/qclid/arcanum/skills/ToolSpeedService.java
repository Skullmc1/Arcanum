package space.qclid.arcanum.skills;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import space.qclid.arcanum.compat.Compat;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.IntToDoubleFunction;
import java.util.function.Predicate;

/**
 * Virtual Efficiency: while a player holds the tool that matches a skill, a block-break-speed
 * modifier scaled by the skill level is applied; it is removed otherwise.
 */
public final class ToolSpeedService implements Listener {

    private static final NamespacedKey KEY = Compat.key("skill_efficiency");

    private record Rule(Predicate<Material> tool, IntToDoubleFunction bonusByLevel) {}

    private final SkillManager manager;
    private final Map<SkillType, Rule> rules = new EnumMap<>(SkillType.class);

    public ToolSpeedService(SkillManager manager) {
        this.manager = manager;
    }

    public void register(SkillType type, Predicate<Material> tool, IntToDoubleFunction bonusByLevel) {
        rules.put(type, new Rule(tool, bonusByLevel));
    }

    /** Re-evaluates the modifier from the held item; cheap enough for a 5-tick scheduler. */
    public void update(Player player) {
        Attribute attribute = Compat.BLOCK_BREAK_SPEED;
        if (attribute == null) return;
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;

        double amount = 0.0;
        if (manager.canEarn(player)) {
            Material held = player.getInventory().getItemInMainHand().getType();
            for (Map.Entry<SkillType, Rule> e : rules.entrySet()) {
                if (manager.isEnabled(e.getKey()) && e.getValue().tool().test(held)) {
                    amount = e.getValue().bonusByLevel().applyAsDouble(manager.level(player.getUniqueId(), e.getKey()));
                    break;
                }
            }
        }

        AttributeModifier existing = find(instance);
        if (existing != null) {
            if (Math.abs(existing.getAmount() - amount) < 1e-9) return;
            instance.removeModifier(existing);
        }
        if (amount > 0) {
            instance.addModifier(new AttributeModifier(KEY, amount, AttributeModifier.Operation.ADD_SCALAR, EquipmentSlotGroup.ANY));
        }
    }

    /** Removes our modifier (on quit and when the plugin shuts down). */
    public void clear(Player player) {
        Attribute attribute = Compat.BLOCK_BREAK_SPEED;
        if (attribute == null) return;
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        AttributeModifier existing = find(instance);
        if (existing != null) instance.removeModifier(existing);
    }

    private AttributeModifier find(AttributeInstance instance) {
        for (AttributeModifier m : instance.getModifiers()) {
            if (KEY.equals(m.getKey())) return m;
        }
        return null;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        clear(event.getPlayer());
    }
}
