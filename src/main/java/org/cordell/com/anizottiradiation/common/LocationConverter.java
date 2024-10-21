package org.cordell.com.anizottiradiation.common;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationConverter {
    public static String locationToString(Location location) {
        if (location == null) return null;
        return location.getWorld().getName() + "," +
                location.getX() + "," +
                location.getY() + "," +
                location.getZ() + "," +
                location.getYaw() + "," +
                location.getPitch();
    }

    public static Location stringToLocation(String str) {
        if (str == null) return null;

        String[] parts = str.split(",");
        if (parts.length != 6) {
            throw new IllegalArgumentException("Invalid location string");
        }
        var world = Bukkit.getWorld(parts[0]);
        if (world == null) {
            throw new IllegalArgumentException("Invalid world: " + parts[0]);
        }

        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        double z = Double.parseDouble(parts[3]);
        float yaw = Float.parseFloat(parts[4]);
        float pitch = Float.parseFloat(parts[5]);

        return new Location(world, x, y, z, yaw, pitch);
    }
}
