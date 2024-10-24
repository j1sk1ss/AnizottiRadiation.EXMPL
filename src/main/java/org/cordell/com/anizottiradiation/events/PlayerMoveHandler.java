package org.cordell.com.anizottiradiation.events;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import org.bukkit.scheduler.BukkitRunnable;
import org.cordell.com.anizottiradiation.common.ArmorManager;
import org.cordell.com.anizottiradiation.common.LocationManager;
import org.j1sk1ss.itemmanager.manager.Manager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.cordell.com.anizottiradiation.events.Radiation.endRadiationEffect;
import static org.cordell.com.anizottiradiation.events.Radiation.startRadiationEffect;


public class PlayerMoveHandler implements Listener {
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) throws IOException {
        var player = e.getPlayer();
        for (var area : Radiation.areas) {
            if (area.getHp() <= 0) continue;
            if (area.isInRegion(player.getLocation())) {
                if (!Radiation.players.containsKey(player)) {
                    Radiation.players.put(player, area);
                    startRadiationEffect(player, area);

                    player.sendMessage("Не вышло?");
                    area.getHpBar().addPlayer(player);
                }
                else {
                    if (LocationManager.isInBlock(player, Material.WATER) || LocationManager.isInBlock(player, Material.BAMBOO)) {
                        var hasArmor = ArmorManager.damagePlayerArmor(player, 2);
                        if (hasArmor) player.playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1.0f, 1.0f);
                        else {
                            Infection.infectedPlayers.put(player, 3);
                            Infection.startInfection(player);
                            player.damage(1);
                        }
                    }

                    if (LocationManager.isOnBlock(player, Material.MOSS_BLOCK) || LocationManager.isOnBlock(player, Material.MOSS_CARPET)) {
                        var hasArmor = ArmorManager.damagePlayerArmor(player, 1, 3);
                        if (hasArmor) player.playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1.0f, 1.0f);
                        else {
                            Infection.infectedPlayers.put(player, 3);
                            Infection.startInfection(player);
                            player.damage(.5);
                        }
                    }
                }
            }
            else {
                if (Infection.infectedPlayers.containsKey(player)) {
                    player.damage(.5);
                }

                if (Radiation.players.containsKey(player)) {
                    Radiation.players.remove(player);
                    endRadiationEffect(player);

                    player.sendMessage(Infection.infectedPlayers.containsKey(player) ? "Вернись" : "Что ты с моим радиоктивным фоном сделал?");
                    area.getHpBar().removePlayer(player);
                    Infection.startInfection(player);
                }

                if (LocationManager.isOnBlock(player, Material.REDSTONE_BLOCK)) {
                    if (LocationManager.isInBlock(player, Material.WATER)) {
                        if (!cleaningPlayers.containsKey(player)) {
                            cleaningPlayers.put(player, true);
                            cleanupProcess(player);
                        }
                    }
                }
            }
        }
    }

    public static final Map<Player, Boolean> cleaningPlayers = new HashMap<>();

    private static void cleanupProcess(Player player) {
        new BukkitRunnable() {
            private int ticks = 0;

            @Override
            public void run() {
                if (LocationManager.isOnBlock(player, Material.REDSTONE_BLOCK)
                        && LocationManager.isInBlock(player, Material.WATER)) {
                    if (ticks++ % 20 == 0) {
                        player.sendMessage("Процент: " + (ticks / 7) * 5);
                    }

                    int MAX_TICKS = 7 * 20;
                    if (ticks >= MAX_TICKS) {
                        player.sendMessage("Очистка оконченна");
                        for (var item : player.getInventory()) {
                            if (item == null) continue;
                            Manager.setInteger2Container(item, -1, "infected");
                        }

                        cleaningPlayers.remove(player);
                        this.cancel();
                    }
                } else {
                    player.sendMessage("Очистка прервана.");
                    cleaningPlayers.remove(player);
                    this.cancel();
                }
            }
        }.runTaskTimer(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("AnizottiRadiation")), 0, 1);
    }
}
