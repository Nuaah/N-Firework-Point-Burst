package nuaah.plugin.fireworkpointburst;

import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

public class FireworkUtil {

    //花火爆発
    public static void burst(JavaPlugin plugin, Location loc, ItemStack item) {
        Firework fw = loc.getWorld().spawn(loc.clone().add(0.5,0.5,0.5), Firework.class);

        FireworkMeta meta = (FireworkMeta) item.getItemMeta();
        fw.setFireworkMeta(meta);

        fw.setMetadata("FPB", new FixedMetadataValue(plugin, true));

        fw.setSilent(true);
        fw.detonate();
    }

}
