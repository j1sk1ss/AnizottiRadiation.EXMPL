package org.cordell.com.anizottiradiation.events;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.cordell.com.anizottiradiation.objects.Area;

import java.util.Objects;

import static org.bukkit.Bukkit.getServer;
import static org.cordell.com.anizottiradiation.events.PlayerMoveHandler.players;


public class Radiation {
    public static void startRadiationEffect(Player player, Area area) {
        Bukkit.getScheduler().runTaskTimer(Objects.requireNonNull(getServer().getPluginManager().getPlugin("AnizottiRadiation")), () -> {
            if (!players.containsKey(player)) return;

            var center = area.getCenter();
            var distance = player.getLocation().distance(center);
            var maxDistance = center.distance(area.getFirstLocation());
            var proximityFactor = 1 - (distance / maxDistance);

            if (proximityFactor <= .5) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 500, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 500, 0));

                var maxJump = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)).getBaseValue();
                Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)).setBaseValue(Math.max(0.1, maxJump - (0.1 * proximityFactor)));
            }

            if (proximityFactor > .5 && proximityFactor <= .8) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 500, (int)(1 + 5 * proximityFactor)));

                var maxJump = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)).getBaseValue();
                Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)).setBaseValue(Math.max(0.1, maxJump - (0.1 * proximityFactor)));
            }

            if (proximityFactor > .8 && proximityFactor < 1) {
                player.damage(2 * proximityFactor);
                player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 500, (int)(1 + 5 * proximityFactor)));
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 500, 5));
                player.getWorld().playSound(player.getLocation(), Sound.AMBIENT_CAVE, 1.0f, 1.0f);

                var maxHealth = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getBaseValue();
                Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(Math.max(5, maxHealth - (2 * proximityFactor)));

                player.sendMessage("Надо бежать!");
            }

        }, 0, 200);
    }

    public static void endRadiationEffect(Player player) {
        player.removePotionEffect(PotionEffectType.NAUSEA);
        player.removePotionEffect(PotionEffectType.WEAKNESS);
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.removePotionEffect(PotionEffectType.BLINDNESS);

        var maxHealth = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getDefaultValue();
        Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(maxHealth);

        if (player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH) != null) {
            var defaultJumpStrength = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)).getDefaultValue();
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)).setBaseValue(defaultJumpStrength);
        }
    }
}
