package space.qclid.arcanum.skills.perk;

/** Tiny roman-numeral helper (1..10) that does not depend on Bukkit. */
final class Roman {

    private static final String[] NUMERALS = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};

    private Roman() {}

    static String roman(int n) {
        return n >= 1 && n < NUMERALS.length ? NUMERALS[n] : Integer.toString(n);
    }
}
