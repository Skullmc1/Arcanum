package space.qclid.arcanum.compat;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;

/**
 * Version-agnostic lookups for Bukkit constants that were renamed between
 * 1.21.1 ("generic.max_health") and 1.21.3+ ("max_health").
 *
 * Resolving through the registry avoids referencing the renamed enum/interface
 * fields directly, so one jar runs on every supported server version.
 */
public final class Compat {

    public static final Attribute MAX_HEALTH = attribute("max_health");
    public static final Attribute ARMOR = attribute("armor");

    /** Namespace of every PDC/recipe key. Kept from the plugin's former name (Dashboard) so existing items stay valid. */
    public static final String KEY_NAMESPACE = "dashboard";

    private Compat() {}

    public static NamespacedKey key(String key) {
        return new NamespacedKey(KEY_NAMESPACE, key);
    }

    private static Attribute attribute(String name) {
        Attribute a = Registry.ATTRIBUTE.get(NamespacedKey.minecraft(name));
        if (a == null) {
            a = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("generic." + name));
        }
        if (a == null) {
            throw new IllegalStateException("Unknown attribute: " + name);
        }
        return a;
    }
}
