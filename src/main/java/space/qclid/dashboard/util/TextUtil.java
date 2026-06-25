package space.qclid.dashboard.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;

/**
 * Shared text utilities: MiniMessage singleton, colour constants, and toSmallCaps().
 * Import statically in feature classes: import static space.qclid.dashboard.util.TextUtil.*;
 */
public final class TextUtil {

    private TextUtil() {}

    public static final MiniMessage MM = MiniMessage.miniMessage();

    // Solid colours
    public static final String C_GOLD   = "<#FFD700>";
    public static final String C_ORANGE = "<#FFA500>";
    public static final String C_YELLOW = "<#FFFF00>";
    public static final String C_RED    = "<#FF4500>";
    public static final String C_GREEN  = "<#55FF55>";
    public static final String C_GRAY   = "<#AAAAAA>";
    public static final String C_PURPLE = "<#DA70D6>";

    // Gradients
    public static final String G_GOLD = "<gradient:#FFD700:#FFA500>";

    /** Deserializes a MiniMessage string and explicitly disables italics. */
    public static Component parse(String input) {
        if (input == null) return Component.empty();
        return MM.deserialize(input).decoration(TextDecoration.ITALIC, false);
    }

    /** Converts a string to Unicode small-caps characters. */
    public static String toSmallCaps(String input) {
        if (input == null) return "";
        String normal = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String small  = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀѕᴛᴜᴠᴡхʏᴢᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀѕᴛᴜᴠᴡхʏᴢ";
        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            int index = normal.indexOf(c);
            result.append(index != -1 ? small.charAt(index) : c);
        }
        return result.toString();
    }
}

