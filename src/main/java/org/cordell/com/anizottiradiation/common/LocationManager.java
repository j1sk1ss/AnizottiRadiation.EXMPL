package org.cordell.com.anizottiradiation.common;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class LocationManager {
    public static boolean isInWater(Player player) {
        return player.getLocation().getBlock().getType() == Material.WATER;
    }
}
