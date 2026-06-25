package space.qclid.dashboard;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerSettings {
    public boolean showXyz = true;
    public boolean showBiome = true;
    public boolean showNetherXyz = true;
    public boolean globalEnabled = true;
    public final Map<String, Location> waypoints = new LinkedHashMap<>();
    public Location destination = null;
    public UUID trackingPlayer = null;
    public Location linkedChest = null;
    public ItemStack[] deathItems = null;

    public void toggleGlobal()     { globalEnabled    = !globalEnabled; }
    public void toggleXyz()        { showXyz          = !showXyz; }
    public void toggleBiome()      { showBiome        = !showBiome; }
    public void toggleNetherXyz()  { showNetherXyz    = !showNetherXyz; }
}
