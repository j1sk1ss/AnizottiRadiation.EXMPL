package org.cordell.com.anizottiradiation;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import org.cordell.com.anizottiradiation.common.LocationConverter;
import org.cordell.com.anizottiradiation.events.PlayerEventHandler;
import org.cordell.com.anizottiradiation.events.PlayerMoveHandler;
import org.cordell.com.anizottiradiation.events.Radiation;
import org.cordell.com.anizottiradiation.link.CommandManager;
import org.cordell.com.anizottiradiation.objects.Area;
import org.cordell.com.cordelldb.manager.Manager;

import java.io.IOException;
import java.util.List;
import java.util.Objects;


public final class AnizottiRadiation extends JavaPlugin {
    public static Manager dataManager;

    @Override
    public void onEnable() {
        dataManager = new Manager("", "anizotti_radiation.txt");
        try {
            var first = LocationConverter.stringToLocation(dataManager.getString("default_zone_first"));
            var second = LocationConverter.stringToLocation(dataManager.getString("default_zone_second"));
            var hp = dataManager.getDouble("default_zone_hp");
            if (first != null && second != null) {
                Radiation.areas.add(new Area(first, second, hp));
                System.out.println("Loaded default location");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Bukkit.getPluginManager().registerEvents(new PlayerMoveHandler(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerEventHandler(), this);

        var command_manager = new CommandManager();
        for (var command : List.of("add_radiation_area", "clear_infection", "create_antidote", "find_cure_block"))
            Objects.requireNonNull(getCommand(command)).setExecutor(command_manager);

        Radiation.growZones();
        Radiation.plantGrowth();

        System.out.println("AnizottiRadiation enabled!");
    }

    @Override
    public void onDisable() {
        System.out.println("AnizottiRadiation disabled!");
        for (var area : Radiation.areas) {
            try {
                dataManager.setString("default_zone_first", LocationConverter.locationToString(area.getFirstLocation()));
                dataManager.setString("default_zone_second", LocationConverter.locationToString(area.getSecondLocation()));
                dataManager.setDouble("default_zone_hp", 640d);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            area.cleanUp();
        }
    }
}
