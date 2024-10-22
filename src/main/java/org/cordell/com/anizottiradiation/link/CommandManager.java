package org.cordell.com.anizottiradiation.link;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;
import java.io.IOException;

import org.cordell.com.anizottiradiation.common.LocationConverter;
import org.cordell.com.anizottiradiation.events.Radiation;
import org.cordell.com.anizottiradiation.objects.Area;

import static org.cordell.com.anizottiradiation.AnizottiRadiation.dataManager;


public class CommandManager implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("add_radiation_area")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be used by players.");
                return false;
            }

            if (args.length != 6) {
                sender.sendMessage("Usage: /add_radiation_area <x1> <y1> <z1> <x2> <y2> <z2>");
                return false;
            }

            try {
                var x1 = Double.parseDouble(args[0]);
                var y1 = Double.parseDouble(args[1]);
                var z1 = Double.parseDouble(args[2]);
                var x2 = Double.parseDouble(args[3]);
                var y2 = Double.parseDouble(args[4]);
                var z2 = Double.parseDouble(args[5]);

                var player = (Player) sender;
                var firstBound = new Location(player.getWorld(), x1, y1, z1);
                var secondBound = new Location(player.getWorld(), x2, y2, z2);

                var newArea = new Area(firstBound, secondBound);
                Radiation.areas.add(newArea);
                player.sendMessage("New radioactive area added successfully.");

                dataManager.setString("default_zone_first", LocationConverter.locationToString(firstBound));
                dataManager.setString("default_zone_second", LocationConverter.locationToString(secondBound));

                return true;

            } catch (NumberFormatException e) {
                sender.sendMessage("Coordinates must be numbers.");
                return false;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return false;
    }
}
