package net.zithium.deluxehub.utility.reflection;

import com.cryptomorin.xseries.reflection.XReflection;
import net.zithium.deluxehub.DeluxeHubPlugin;
import org.bukkit.entity.ArmorStand;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Level;

public class ArmorStandName {

    private static final JavaPlugin PLUGIN = JavaPlugin.getProvidingPlugin(DeluxeHubPlugin.class);

    public static String getName(ArmorStand stand) {
        if (XReflection.supports(8)) {
            return stand.getCustomName();
        }

        String name = null;
        try {
            name = (String) ArmorStand.class.getMethod("getCustomName").invoke(stand);
        } catch (Exception e) {
            PLUGIN.getLogger().log(Level.SEVERE, "Failed to get custom name from ArmorStand", e);
        }

        return name;
    }
}
