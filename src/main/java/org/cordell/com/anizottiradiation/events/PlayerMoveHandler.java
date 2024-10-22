package org.cordell.com.anizottiradiation.events;

import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import org.cordell.com.anizottiradiation.common.ArmorManager;
import org.cordell.com.anizottiradiation.common.LocationManager;

import java.io.IOException;

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
                    if (LocationManager.isInWater(player)) {
                        var hasArmor = ArmorManager.damagePlayerArmor(player, 2);
                        if (hasArmor) player.playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1.0f, 1.0f);
                        else player.damage(1);
                    }
                }
            }
            else {
                if (Radiation.players.containsKey(player)) {
                    Radiation.players.remove(player);
                    endRadiationEffect(player);

                    player.sendMessage("Что ты с моим радиоктивным фоном сделал?");
                    area.getHpBar().removePlayer(player);
                    Infection.startInfection(player);
                }
            }
        }
    }
}
