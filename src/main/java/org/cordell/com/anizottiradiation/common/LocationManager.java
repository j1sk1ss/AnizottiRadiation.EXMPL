package org.cordell.com.anizottiradiation.common;

import org.bukkit.Material;
import org.bukkit.entity.Player;


public class LocationManager {
    public static boolean isInBlock(Player player, Material material) {
        return player.getLocation().getBlock().getType() == material;
    }

    public static boolean isOnBlock(Player player, Material material) {
        var location = player.getLocation().subtract(0, 1, 0);
        return location.getBlock().getType() == material;
    }
}
