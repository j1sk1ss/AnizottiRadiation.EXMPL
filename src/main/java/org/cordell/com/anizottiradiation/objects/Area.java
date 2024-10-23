package org.cordell.com.anizottiradiation.objects;

import lombok.Getter;
import lombok.Setter;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.cordell.com.anizottiradiation.AnizottiRadiation;
import org.cordell.com.anizottiradiation.common.LocationConverter;
import org.cordell.com.anizottiradiation.common.SetupProps;

import java.io.IOException;
import java.util.*;


@Setter
@Getter
public class Area {
    public Area(Location firstBound, Location secondBound, double hp)  {
        firstLocation = firstBound;
        secondLocation = secondBound;
        this.hp = hp;

        hpBar = Bukkit.createBossBar("Хихиканье младшего", BarColor.GREEN, BarStyle.SOLID);
        hpBar.setProgress(1.0);

        var center = getCenter();
        var width  = Math.abs(firstLocation.getBlockX() - secondLocation.getBlockX());
        var height = Math.abs(firstLocation.getBlockZ() - secondLocation.getBlockZ());

        analyzeBlocks = new ArrayList<>();

        var radius = Math.min(width, height) / 2d;
        var spawnedZones = new boolean[4];

        while (analyzeBlocks.size() < 8) {
            var angle = new Random().nextDouble() * 2 * Math.PI;
            var distanceFactor = new Random().nextDouble();
            var distanceFromCenter = (0.3 + distanceFactor * 0.7) * radius;
            var xOffset = distanceFromCenter * Math.cos(angle);
            var zOffset = distanceFromCenter * Math.sin(angle);
            var y = center.getBlockY() + 1;

            Location blockLocation = new Location(center.getWorld(), center.getBlockX() + xOffset, y, center.getBlockZ() + zOffset).toHighestLocation();
            Block homeBlock = blockLocation.getBlock();

            if (homeBlock.getType() == Material.DIRT || homeBlock.getType() == Material.GRASS_BLOCK || homeBlock.getType() == Material.MOSS_BLOCK) {
                var distanceFactorToCenter = blockLocation.distance(center) / radius;

                var zone = -1;
                if (analyzeBlocks.size() < 4) {
                    if (distanceFactorToCenter >= 0.3 && distanceFactorToCenter < 0.5 && !spawnedZones[0]) zone = 0;
                    else if (distanceFactorToCenter >= 0.5 && distanceFactorToCenter < 0.7 && !spawnedZones[1]) zone = 1;
                    else if (distanceFactorToCenter >= 0.7 && distanceFactorToCenter < 0.9 && !spawnedZones[2]) zone = 2;
                    else if (distanceFactorToCenter >= 0.9 && distanceFactorToCenter <= 1.0 && !spawnedZones[3]) zone = 3;
                }
                else zone = 1;

                if (zone != -1) {
                    Block block = blockLocation.add(0, 1, 0).getBlock();
                    block.setType(SetupProps.ANALYZE_BLOCKS.get(new Random().nextInt(SetupProps.ANALYZE_BLOCKS.size())));
                    analyzeBlocks.add(block);
                    spawnedZones[zone] = true;
                }
            }
        }
    }

    private BossBar hpBar;
    private Location firstLocation;
    private Location secondLocation;
    private double hp;

    private ArrayList<Block> analyzeBlocks = null;
    private Material cureBlock = null;

    private boolean firstStage = false;
    private boolean secondStage = false;
    private boolean thirdStage = false;

    public void damageZone(double damage) {
        hp -= damage;
        if (hp > 400 && hp <= 630 && !firstStage) {
            spawnHorde("armored", 25, EntityType.SKELETON);
            firstStage = true;
            secondStage = false;
            thirdStage = false;
        }

        if (hp > 200 && hp <= 400 && !secondStage) {
            spawnHorde("elite", 25, EntityType.ZOMBIE);
            firstStage = false;
            secondStage = true;
            thirdStage = false;
        }

        if (hp > 0 && hp <= 200 && !thirdStage) {
            spawnHorde("boss", 15, EntityType.PILLAGER);
            firstStage = false;
            secondStage = false;
            thirdStage = true;
        }

        if (hp > 640) hp = 640;
        if (hp <= 0) hp = 0;

        hpBar.setProgress(hp / 640);
        if (hp == 0) defeatZone();
    }

    public Location getCenter() {
        double centerX = (firstLocation.getX() + secondLocation.getX()) / 2;
        double centerY = (firstLocation.getY() + secondLocation.getY()) / 2;
        double centerZ = (firstLocation.getZ() + secondLocation.getZ()) / 2;
        return new Location(firstLocation.getWorld(), centerX, centerY, centerZ);
    }

    public boolean isInRegion(Player player, double min) {
        return getProximityFactor(player) > min;
    }

    public double getProximityFactor(Player player) {
        var center = getCenter();
        var distance = player.getLocation().distance(center);
        var maxDistance = center.distance(getFirstLocation());
        return (1 - (distance / maxDistance));
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
        }
        else {
            firstLocation.add(0, 0, expansion);
            secondLocation.subtract(0, 0, expansion);
        }

        AnizottiRadiation.dataManager.setString("default_zone_first", LocationConverter.locationToString(firstLocation));
        AnizottiRadiation.dataManager.setString("default_zone_second", LocationConverter.locationToString(secondLocation));

        setFirstLocation(firstLocation);
        setSecondLocation(secondLocation);
    }

    public void growPlantsInArea() {
        var world = getFirstLocation().getWorld();
        if (world == null) return;

        var first = getFirstLocation();
        var second = getSecondLocation();

        var minX = Math.min(first.getBlockX(), second.getBlockX());
        var maxX = Math.max(first.getBlockX(), second.getBlockX());
        var minY = Math.min(first.getBlockY(), second.getBlockY());
        var maxY = Math.max(first.getBlockY(), second.getBlockY());
        var minZ = Math.min(first.getBlockZ(), second.getBlockZ());
        var maxZ = Math.max(first.getBlockZ(), second.getBlockZ());

        var random = new Random();
        for (int i = 0; i < 10; i++) {
            int x = random.nextInt(maxX - minX + 1) + minX;
            int y = random.nextInt(maxY - minY + 1) + minY;
            int z = random.nextInt(maxZ - minZ + 1) + minZ;

            var plantLocation = new Location(world, x, y, z).toHighestLocation();
            var block = plantLocation.getBlock();
            if (canPlacePlant(block)) block.setType(SetupProps.PLANTS.get(random.nextInt(SetupProps.PLANTS.size())));
        }
    }

    public void cleanUp() {
        for (var player : Bukkit.getOnlinePlayers())
            getHpBar().removePlayer(player);

        for (var block : getAnalyzeBlocks())
            block.setType(Material.AIR);
    }

    private void spawnHorde(String type, int count, EntityType entity) {
        for (int i = 0; i < count; i++) {
            var world = Bukkit.getServer().getWorlds().get(0);

            var center = getCenter();
            var xOffset = (new Random().nextDouble() - 0.5) * 10;
            var zOffset = (new Random().nextDouble() - 0.5) * 10;
            var randomLoc = new Location(world, center.getX() + xOffset, 100, center.getZ() + zOffset);
            var loc = new Location(world, randomLoc.getX(), randomLoc.toHighestLocation().getY() + 1, randomLoc.getZ());

            assert world != null;
            var spawnedEntity = (LivingEntity)world.spawnEntity(loc, entity);
            Objects.requireNonNull(spawnedEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(100);
            spawnedEntity.setHealth(100);

            switch (type) {
                case "armored":
                    Objects.requireNonNull(spawnedEntity.getEquipment()).setHelmet(new ItemStack(Material.IRON_HELMET));
                    break;
                case "elite":
                    Objects.requireNonNull(spawnedEntity.getEquipment()).setHelmet(new ItemStack(Material.DIAMOND_HELMET));
                    spawnedEntity.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
                    spawnedEntity.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
                    break;
                case "boss":
                    Objects.requireNonNull(spawnedEntity.getEquipment()).setHelmet(new ItemStack(Material.NETHERITE_HELMET));
                    spawnedEntity.getEquipment().setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
                    spawnedEntity.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_AXE));
                    break;
            }
        }
    }


    private void defeatZone() {
        hpBar.setColor(BarColor.RED);
        for (var player : Bukkit.getServer().getOnlinePlayers())
            if (isInRegion(player.getLocation())) player.sendMessage("Вы победили зону!");

        hpBar.removeAll();
    }

    private static boolean canPlacePlant(Block block) {
        var blockType = block.getType();
        var blockBelowType = block.getRelative(0, -1, 0).getType();

        return (blockType == Material.AIR || blockType == Material.WATER) &&
                (blockBelowType == Material.GRASS_BLOCK ||
                        blockBelowType == Material.DIRT ||
                        blockBelowType == Material.SAND ||
                        blockBelowType == Material.PODZOL);
    }
}
