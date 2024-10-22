package org.cordell.com.anizottiradiation.objects;

import lombok.Getter;
import lombok.Setter;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.cordell.com.anizottiradiation.common.LocationConverter;

import java.io.IOException;
import java.util.Objects;
import java.util.Random;

import static org.cordell.com.anizottiradiation.AnizottiRadiation.dataManager;
import static org.cordell.com.anizottiradiation.common.Plants.PLANTS;


@Setter
@Getter
public class Area {
    public Area(Location firstBound, Location secondBound, double hp)  {
        firstLocation = firstBound;
        secondLocation = secondBound;
        this.hp = hp;

        hpBar = Bukkit.createBossBar("Зона", BarColor.GREEN, BarStyle.SOLID);
        hpBar.setProgress(1.0);
    }

    private BossBar hpBar;
    private Location firstLocation;
    private Location secondLocation;
    private double hp;

    private boolean firstStage = false;
    private boolean secondStage = false;
    private boolean thirdStage = false;

    public void damageZone(double damage) {
        hp -= damage;
        if (hp > 400 && hp <= 630 && !firstStage) {
            spawnHorde("armored", 25, EntityType.SKELETON);
            Bukkit.broadcastMessage("Зона активирует защиту, готовьтесь к атаке!");

            firstStage = true;
            secondStage = false;
            thirdStage = false;
        }

        if (hp > 200 && hp <= 400 && !secondStage) {
            spawnHorde("elite", 25, EntityType.ZOMBIE);
            Bukkit.broadcastMessage("Зона вызывает элитные силы, будьте осторожны!");

            firstStage = false;
            secondStage = true;
            thirdStage = false;
        }

        if (hp > 0 && hp <= 200 && !thirdStage) {
            spawnHorde("boss", 15, EntityType.PILLAGER);
            Bukkit.broadcastMessage("Зона в отчаянии вызывает босса! Приготовьтесь к бою!");

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
        var blockBelow = block.getRelative(0, -1, 0);
        return (blockType == Material.AIR || blockType == Material.WATER) &&
                (blockBelow.getType() == Material.GRASS_BLOCK ||
                        blockBelow.getType() == Material.DIRT ||
                        blockBelow.getType() == Material.SAND ||
                        blockBelow.getType() == Material.PODZOL ||
                        blockBelow.getType() == Material.STONE);
    }
}
