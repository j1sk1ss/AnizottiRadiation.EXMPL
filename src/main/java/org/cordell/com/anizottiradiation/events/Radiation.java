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
import org.cordell.com.anizottiradiation.objects.Area;

import org.j1sk1ss.itemmanager.manager.Manager;

import java.io.IOException;
import java.util.*;

import static org.bukkit.Bukkit.getServer;
import static org.cordell.com.anizottiradiation.events.Infection.infectedPlayers;
import static org.cordell.com.anizottiradiation.objects.Area.growPlantsInArea;


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
        var isDefence = false;
        var helmet = player.getInventory().getHelmet();
        var chestPlate = player.getInventory().getChestplate();
        var leggings = player.getInventory().getLeggings();
        var boots = player.getInventory().getBoots();

        ArmorManager.damageArmor(helmet, player, (int)(12 * proximityFactor));
        ArmorManager.damageArmor(chestPlate, player, (int)(15 * proximityFactor));
        ArmorManager.damageArmor(leggings, player, (int)(10 * proximityFactor));
        ArmorManager.damageArmor(boots, player, (int)(5 * proximityFactor));

        if (helmet != null && chestPlate != null && leggings != null && boots != null) {
            if (helmet.getType() == Material.LEATHER_HELMET && chestPlate.getType() == Material.IRON_CHESTPLATE ||
                    leggings.getType() == Material.IRON_LEGGINGS && boots.getType() == Material.IRON_BOOTS) {
                isDefence = true;
            }
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 500, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 500, 0));
        infectedPlayers.merge(player, 1, Math::max);

        if (proximityFactor > .35 && proximityFactor <= .8) {
            player.getWorld().playSound(player.getLocation(), Sound.AMBIENT_BASALT_DELTAS_ADDITIONS, 1.0f, 1.0f);

            if (!isDefence) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 500, (int) (1 + 5 * proximityFactor)));
                player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 200, 1));

                var maxJump = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)).getBaseValue();
                Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)).setBaseValue(Math.max(0.1, maxJump - (0.1 * proximityFactor)));
                infectedPlayers.merge(player, 2, Math::max);
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

            if (!isDefence) {
                player.damage(2 * proximityFactor);
                player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 500, (int) (1 + 5 * proximityFactor)));
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 500, 5));
                player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 1));

                var maxHealth = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getBaseValue();
                Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(Math.max(5, maxHealth - (2 * proximityFactor)));

                player.sendMessage("Надо бежать!");
                infectedPlayers.merge(player, 3, Math::max);
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

    public static void endRadiationEffect(Player player) {
        player.removePotionEffect(PotionEffectType.NAUSEA);
        player.removePotionEffect(PotionEffectType.POISON);
        player.removePotionEffect(PotionEffectType.WEAKNESS);
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.removePotionEffect(PotionEffectType.BLINDNESS);

        var maxHealth = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getDefaultValue();
        Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(maxHealth);

        if (player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH) != null) {
            var defaultJumpStrength = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)).getDefaultValue();
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)).setBaseValue(defaultJumpStrength);
        }

        if (radiationTasks.containsKey(player)) {
            radiationTasks.get(player).cancel();
            radiationTasks.remove(player);
        }
    }

    public static void plantGrowth() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Area area : areas) growPlantsInArea(area);
            }
        }.runTaskTimer(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("AnizottiRadiation")), 0, 1500);
    }

    public static void growZones() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (var area : areas) {
                    try {
                        area.expandArea(new Random().nextInt(2));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }.runTaskTimer(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("AnizottiRadiation")), 0, 24000);
    }
}
