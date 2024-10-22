package org.cordell.com.anizottiradiation.events;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.util.*;

import static org.bukkit.Bukkit.getServer;
import static org.cordell.com.anizottiradiation.AnizottiRadiation.dataManager;


public class Infection {
    public static final Map<Player, Integer> infectedPlayers = new HashMap<>();
    public static final Map<Player, ArrayList<BukkitTask>> infectionTasks = new HashMap<>();

    public static void startInfection(Player player) throws IOException {
        if (player == null) return;
        if (infectionTasks.containsKey(player)) return;

        var list = new ArrayList<BukkitTask>();
        var infectionLevel = infectedPlayers.get(player);

        System.out.println("Start infection for " + player.getName() + "\tLevel " + infectionLevel);
        if (infectionLevel < 3) {
            infectedPlayers.remove(player);
            return;
        }

        dataManager.setInt(player.getName(), infectionLevel);
        list.add(applyInfection(player));
        list.add(applyRadiationParticles(player));
        list.add(applyRadiationToNearby(player));

        infectionTasks.put(player, list);
    }

    private static BukkitTask applyInfection(Player player) {
        return Bukkit.getScheduler().runTaskTimer(Objects.requireNonNull(getServer().getPluginManager().getPlugin("AnizottiRadiation")), () -> {
            if (!infectedPlayers.containsKey(player)) return;

            int infectionLevel = infectedPlayers.get(player);
            infectedPlayers.put(player, infectionLevel + 1);
            try {
                dataManager.setInt(player.getName(), infectionLevel + 1);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (infectionLevel <= 6) {
                var maxHealth = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getBaseValue();
                Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(Math.min(100, maxHealth + infectionLevel));
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 600, infectionLevel));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, infectionLevel));
            }
            else {
                Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(5);
                var highestBlock = player.getWorld().getHighestBlockAt(player.getLocation());
                if (player.getLocation().getY() >= highestBlock.getY() && player.getWorld().getTime() > 0 && player.getWorld().getTime() < 12300) {
                    player.setFireTicks(100);
                }
            }
        }, 0, 12000);
    }

    private static BukkitTask applyRadiationParticles(Player player) {
        return Bukkit.getScheduler().runTaskTimer(Objects.requireNonNull(getServer().getPluginManager().getPlugin("AnizottiRadiation")), () -> {
            if (!infectedPlayers.containsKey(player)) return;

            var location = player.getLocation();
            player.spawnParticle(
                    Particle.DUST, location.getX(), location.getY() + 1, location.getZ(),
                    1, 1, 1, 0, 5, new Particle.DustOptions(Color.fromRGB(0, 255, 0), 2.0F)
            );
        }, 0, 100);
    }

    private static BukkitTask applyRadiationToNearby(Player infectedPlayer) {
        return Bukkit.getScheduler().runTaskTimer(Objects.requireNonNull(getServer().getPluginManager().getPlugin("AnizottiRadiation")), () -> {
            for (var nearbyPlayer : infectedPlayer.getWorld().getPlayers()) {
                if (nearbyPlayer == infectedPlayer) continue;

                var distance = infectedPlayer.getLocation().distance(nearbyPlayer.getLocation());
                if (distance < 10) {
                    var proximityFactor = 1 - (distance / 10);
                    Radiation.applyRadiation2Player(nearbyPlayer, proximityFactor);
                }
            }
        }, 0, 200);
    }
}
