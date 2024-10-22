package org.cordell.com.anizottiradiation.objects;

import lombok.Getter;
import lombok.Setter;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.cordell.com.anizottiradiation.common.LocationConverter;

import java.io.IOException;
import java.util.Random;

import static org.cordell.com.anizottiradiation.AnizottiRadiation.dataManager;
import static org.cordell.com.anizottiradiation.common.Plants.PLANTS;


@Setter
@Getter
public class Area {
    public Area(Location firstBound, Location secondBound)  {
        firstLocation = firstBound;
        secondLocation = secondBound;
    }

    private Location firstLocation;
    private Location secondLocation;

    public Location getCenter() {
        double centerX = (firstLocation.getX() + secondLocation.getX()) / 2;
        double centerY = (firstLocation.getY() + secondLocation.getY()) / 2;
        double centerZ = (firstLocation.getZ() + secondLocation.getZ()) / 2;
        return new Location(firstLocation.getWorld(), centerX, centerY, centerZ);
    }

    public boolean isInRegion(Location source) {
        if (source == null) return false;

        var minX = Math.min(firstLocation.getX(), secondLocation.getX());
        var maxX = Math.max(firstLocation.getX(), secondLocation.getX());
        var minY = Math.min(firstLocation.getY(), secondLocation.getY());
        var maxY = Math.max(firstLocation.getY(), secondLocation.getY());
        var minZ = Math.min(firstLocation.getZ(), secondLocation.getZ());
        var maxZ = Math.max(firstLocation.getZ(), secondLocation.getZ());

        return (source.getX() >= minX && source.getX() <= maxX) &&
                (source.getY() >= minY && source.getY() <= maxY) &&
                (source.getZ() >= minZ && source.getZ() <= maxZ);
    }

    public void expandArea(double expansion) throws IOException {
        var firstLocation = getFirstLocation();
        var secondLocation = getSecondLocation();

        if (firstLocation.getX() < secondLocation.getX()) {
            firstLocation.subtract(expansion, 0, 0);
            secondLocation.add(expansion, 0, 0);
        } else {
            firstLocation.add(expansion, 0, 0);
            secondLocation.subtract(expansion, 0, 0);
        }

        if (firstLocation.getY() < secondLocation.getY()) {
            firstLocation.subtract(0, expansion, 0);
            secondLocation.add(0, expansion, 0);
        } else {
            firstLocation.add(0, expansion, 0);
            secondLocation.subtract(0, expansion, 0);
        }

        if (firstLocation.getZ() < secondLocation.getZ()) {
            firstLocation.subtract(0, 0, expansion);
            secondLocation.add(0, 0, expansion);
        } else {
            firstLocation.add(0, 0, expansion);
            secondLocation.subtract(0, 0, expansion);
        }

        dataManager.setString("default_zone_first", LocationConverter.locationToString(firstLocation));
        dataManager.setString("default_zone_second", LocationConverter.locationToString(secondLocation));

        setFirstLocation(firstLocation);
        setSecondLocation(secondLocation);
    }

    public static void growPlantsInArea(Area area) {
        var world = area.getFirstLocation().getWorld();
        if (world == null) return;

        var first = area.getFirstLocation();
        var second = area.getSecondLocation();

        var minX = Math.min(first.getBlockX(), second.getBlockX());
        var maxX = Math.max(first.getBlockX(), second.getBlockX());
        var minY = Math.min(first.getBlockY(), second.getBlockY());
        var maxY = Math.max(first.getBlockY(), second.getBlockY());
        var minZ = Math.min(first.getBlockZ(), second.getBlockZ());
        var maxZ = Math.max(first.getBlockZ(), second.getBlockZ());

        var random = new Random();
        for (int i = 0; i < 10; i++) { // Посадим до 10 растений за один цикл
            int x = random.nextInt(maxX - minX + 1) + minX;
            int y = random.nextInt(maxY - minY + 1) + minY;
            int z = random.nextInt(maxZ - minZ + 1) + minZ;

            var plantLocation = new Location(world, x, y, z);
            var block = plantLocation.getBlock();

            if (canPlacePlant(block)) {
                block.setType(PLANTS.get(random.nextInt(PLANTS.size())));
            }
        }
    }

    private static boolean canPlacePlant(Block block) {
        var blockType = block.getType();
        var blockBelow = block.getRelative(0, -1, 0);
        return (blockType == Material.AIR || blockType == Material.WATER) &&
                (blockBelow.getType() == Material.GRASS_BLOCK ||
                        blockBelow.getType() == Material.DIRT ||
                        blockBelow.getType() == Material.SAND ||
                        blockBelow.getType() == Material.PODZOL ||
                        blockBelow.getType() == Material.STONE);
    }
}
