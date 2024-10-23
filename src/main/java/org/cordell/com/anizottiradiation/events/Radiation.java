package org.cordell.com.anizottiradiation.events;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import org.cordell.com.anizottiradiation.common.ArmorManager;
import org.cordell.com.anizottiradiation.common.LocationManager;
import org.cordell.com.anizottiradiation.objects.Area;

import org.j1sk1ss.itemmanager.manager.Manager;

import java.io.IOException;
import java.util.*;

import static org.bukkit.Bukkit.getServer;


public class Radiation {
    public static ArrayList<Area> areas = new ArrayList<>();
    public static Hashtable<Player, Area> players = new Hashtable<>();
    public static final Map<Player, BukkitTask> radiationTasks = new HashMap<>();

    public static void startRadiationEffect(Player player, Area area) {
        var task = Bukkit.getScheduler().runTaskTimer(Objects.requireNonNull(getServer().getPluginManager().getPlugin("AnizottiRadiation")), () -> {
            if (!players.containsKey(player)) return;
            applyRadiation2Player(player, area.getProximityFactor(player));
        }, 0, 200);

        radiationTasks.put(player, task);
    }

    public static void applyRadiation2Player(Player player, double proximityFactor) {
        var isDefence = Infection.infectedPlayers.containsKey(player);
        var helmet = player.getInventory().getHelmet();
        var chestPlate = player.getInventory().getChestplate();
        var leggings = player.getInventory().getLeggings();
        var boots = player.getInventory().getBoots();

        ArmorManager.damageArmor(helmet, player, (int)(6 * proximityFactor));
        ArmorManager.damageArmor(chestPlate, player, (int)(7 * proximityFactor));
        ArmorManager.damageArmor(leggings, player, (int)(5 * proximityFactor));
        ArmorManager.damageArmor(boots, player, (int)(5 * proximityFactor));

        if (helmet != null && chestPlate != null && leggings != null && boots != null) {
            if (helmet.getType() == Material.LEATHER_HELMET && chestPlate.getType() == Material.IRON_CHESTPLATE ||
                    leggings.getType() == Material.IRON_LEGGINGS && boots.getType() == Material.IRON_BOOTS) {
                isDefence = true;
            }
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 300, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 300, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 300, 1));
        Infection.infectedPlayers.merge(player, 1, Math::max);

        if (proximityFactor > .35 && proximityFactor <= .8) {
            player.getWorld().playSound(player.getLocation(), Sound.AMBIENT_BASALT_DELTAS_ADDITIONS, 1.0f, 1.0f);
            player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 300, 0));

            if (!isDefence) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 300, (int) (1 + 5 * proximityFactor)));
                player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 300, 2));

                var maxJump = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)).getBaseValue();
                Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)).setBaseValue(Math.max(0.1, maxJump - (0.1 * proximityFactor)));

                player.sendMessage("Надо уходить.");
                Infection.infectedPlayers.merge(player, 2, Math::max);
                for (var item : player.getInventory()) {
                    if (item == null) continue;
                    if (item.getItemMeta() == null) continue;
                    Manager.setInteger2Container(item, 2, "infected");
                }
            }
        }

        if (proximityFactor > .8 && proximityFactor < 1) {
            player.getWorld().playSound(player.getLocation(), Sound.AMBIENT_CAVE, 1.0f, 1.0f);
            player.getWorld().playSound(player.getLocation(), Sound.AMBIENT_SOUL_SAND_VALLEY_MOOD, 1.0f, 1.0f);
            player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 300, 5));
            player.setCompassTarget(LocationManager.randomLocation(player));

            if (!isDefence) {
                player.damage(2 * proximityFactor);
                player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 300, (int) (1 + 5 * proximityFactor)));
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 300, 5));
                player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 300, 1));

                var maxHealth = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getBaseValue();
                Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(Math.max(5, maxHealth - (2 * proximityFactor)));

                player.sendMessage("Надо бежать!");
                Infection.infectedPlayers.merge(player, 3, Math::max);
                for (var item : player.getInventory()) {
                    if (item == null) continue;
                    if (item.getItemMeta() == null) continue;
                    Manager.setInteger2Container(item, 3, "infected");
                }
            }
        }

        try {
            Infection.startInfection(player);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void clearRadiationEffect(Player player) {
        player.removePotionEffect(PotionEffectType.NAUSEA);
        player.removePotionEffect(PotionEffectType.POISON);
        player.removePotionEffect(PotionEffectType.DARKNESS);
        player.removePotionEffect(PotionEffectType.WEAKNESS);
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.removePotionEffect(PotionEffectType.MINING_FATIGUE);

        var maxHealth = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getDefaultValue();
        Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(maxHealth);

        if (player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH) != null) {
            var defaultJumpStrength = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)).getDefaultValue();
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)).setBaseValue(defaultJumpStrength);
        }
    }

    public static void endRadiationEffect(Player player) {
        clearRadiationEffect(player);
        if (radiationTasks.containsKey(player)) {
            radiationTasks.get(player).cancel();
            radiationTasks.remove(player);
        }
    }

    public static void plantGrowth() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Area area : areas) area.growPlantsInArea();
            }
        }.runTaskTimer(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("AnizottiRadiation")), 0, 60);
    }

    public static void growZones() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (var area : areas) {
                    try {
                        var regenSize = new Random().nextInt(2);
                        area.expandArea(regenSize);
                        area.damageZone(-regenSize);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }.runTaskTimer(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("AnizottiRadiation")), 0, 24000);
    }
}
