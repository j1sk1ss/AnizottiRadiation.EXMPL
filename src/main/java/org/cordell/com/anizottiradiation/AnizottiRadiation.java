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

import static org.cordell.com.anizottiradiation.events.Radiation.areas;


public final class AnizottiRadiation extends JavaPlugin {
    public static Manager dataManager;

    @Override
    public void onEnable() {
        dataManager = new Manager("", "anizotti_radiation.txt");
        try {
            var first = LocationConverter.stringToLocation(dataManager.getString("default_zone_first"));
            var second = LocationConverter.stringToLocation(dataManager.getString("default_zone_second"));
            if (first != null && second != null) {
                areas.add(new Area(first, second));
                System.out.println("Loaded default location");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Bukkit.getPluginManager().registerEvents(new PlayerMoveHandler(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerEventHandler(), this);

        var command_manager = new CommandManager();
        for (var command : List.of("add_radiation_area"))
            Objects.requireNonNull(getCommand(command)).setExecutor(command_manager);

        Radiation.growZones();
        Radiation.plantGrowth();

        System.out.println("AnizottiRadiation enabled!");
    }

    @Override
    public void onDisable() {
        System.out.println("AnizottiRadiation disabled!");
    }
}
