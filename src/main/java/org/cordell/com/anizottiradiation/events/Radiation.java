package org.cordell.com.anizottiradiation.events;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.cordell.com.anizottiradiation.common.ArmorManager;
import org.cordell.com.anizottiradiation.objects.Area;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.bukkit.Bukkit.getServer;
import static org.cordell.com.anizottiradiation.events.Infection.infectedPlayers;
import static org.cordell.com.anizottiradiation.events.PlayerMoveHandler.players;


public class Radiation {
    public static final Map<Player, BukkitTask> radiationTasks = new HashMap<>();

    public static void startRadiationEffect(Player player, Area area) {
        var task = Bukkit.getScheduler().runTaskTimer(Objects.requireNonNull(getServer().getPluginManager().getPlugin("AnizottiRadiation")), () -> {
            if (!players.containsKey(player)) return;
            var center = area.getCenter();
            var distance = player.getLocation().distance(center);
            var maxDistance = center.distance(area.getFirstLocation());
            var proximityFactor = 1 - (distance / maxDistance);

            applyRadiation2Player(player, proximityFactor);
        }, 0, 200);

        radiationTasks.put(player, task);
    }

    public static void applyRadiation2Player(Player player, double proximityFactor) {
        var helmet = player.getInventory().getHelmet();
        var chestPlate = player.getInventory().getChestplate();
        var leggings = player.getInventory().getLeggings();
        var boots = player.getInventory().getBoots();

        ArmorManager.damageArmor(helmet, player, (int)(10 * proximityFactor));
        ArmorManager.damageArmor(chestPlate, player, (int)(10 * proximityFactor));
        ArmorManager.damageArmor(leggings, player, (int)(10 * proximityFactor));
        ArmorManager.damageArmor(boots, player, (int)(10 * proximityFactor));

        if (helmet != null && chestPlate != null && leggings != null && boots != null) {
            if (helmet.getType() == Material.LEATHER_HELMET && chestPlate.getType() == Material.IRON_CHESTPLATE ||
                    leggings.getType() == Material.IRON_LEGGINGS && boots.getType() == Material.IRON_BOOTS) {
                return;
            }
        }

        if (proximityFactor <= .5) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 500, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 500, 0));

            var maxJump = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)).getBaseValue();
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)).setBaseValue(Math.max(0.1, maxJump - (0.1 * proximityFactor)));

            infectedPlayers.merge(player, 1, Math::max);
        }

        if (proximityFactor > .5 && proximityFactor <= .8) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 500, (int)(1 + 5 * proximityFactor)));

            var maxJump = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)).getBaseValue();
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)).setBaseValue(Math.max(0.1, maxJump - (0.1 * proximityFactor)));

            infectedPlayers.merge(player, 2, Math::max);
        }

        if (proximityFactor > .8 && proximityFactor < 1) {
            player.damage(2 * proximityFactor);
            player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 500, (int)(1 + 5 * proximityFactor)));
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 500, 5));
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 1));
            player.getWorld().playSound(player.getLocation(), Sound.AMBIENT_CAVE, 1.0f, 1.0f);

            var maxHealth = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getBaseValue();
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(Math.max(5, maxHealth - (2 * proximityFactor)));

            player.sendMessage("Надо бежать!");
            infectedPlayers.merge(player, 3, Math::max);
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
}
