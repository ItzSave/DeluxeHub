package net.zithium.deluxehub.utility.reflection;

import net.zithium.deluxehub.DeluxeHubPlugin;
import org.bukkit.entity.ArmorStand;
import org.bukkit.plugin.java.JavaPlugin;

public class ArmorStandName {

    private static final JavaPlugin PLUGIN = JavaPlugin.getProvidingPlugin(DeluxeHubPlugin.class);

    public static String getName(ArmorStand stand) {
        return stand.getCustomName();
    }
}
