package org.cordell.com.anizottiradiation.events;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import org.cordell.com.anizottiradiation.common.ArmorManager;
import org.cordell.com.anizottiradiation.objects.Area;

import java.util.ArrayList;
import java.util.Hashtable;

import static org.cordell.com.anizottiradiation.events.Radiation.endRadiationEffect;
import static org.cordell.com.anizottiradiation.events.Radiation.startRadiationEffect;


public class PlayerMoveHandler implements Listener {
    public static ArrayList<Area> areas = new ArrayList<>();
    public static Hashtable<Player, Area> players = new Hashtable<>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        var player = e.getPlayer();
        for (var area : areas) {
            if (isInRegion(player.getLocation(), area.getFirstLocation(), area.getSecondLocation())) {
                if (!players.containsKey(player)) {
                    players.put(player, area);
                    startRadiationEffect(player, area);
                    player.sendMessage("Что-то нехорошее начинается...");
                }
                else {
                    if (isInWater(player)) {
                        ArmorManager.damagePlayerArmor(player, 5);
                    }
                }
            }
            else {
                if (players.containsKey(player)) {
                    players.remove(player);
                    endRadiationEffect(player);
                    player.sendMessage("Пронесло");

                    Infection.startInfection(player);
                }
            }
        }
    }

    private boolean isInRegion(Location source, Location firstBound, Location secondBound) {
        if (source == null || firstBound == null || secondBound == null) return false;

        var minX = Math.min(firstBound.getX(), secondBound.getX());
        var maxX = Math.max(firstBound.getX(), secondBound.getX());
        var minY = Math.min(firstBound.getY(), secondBound.getY());
        var maxY = Math.max(firstBound.getY(), secondBound.getY());
        var minZ = Math.min(firstBound.getZ(), secondBound.getZ());
        var maxZ = Math.max(firstBound.getZ(), secondBound.getZ());

        return (source.getX() >= minX && source.getX() <= maxX) &&
                (source.getY() >= minY && source.getY() <= maxY) &&
                (source.getZ() >= minZ && source.getZ() <= maxZ);
    }

    private boolean isInWater(Player player) {
        return player.getLocation().getBlock().getType() == Material.WATER;
    }
}
