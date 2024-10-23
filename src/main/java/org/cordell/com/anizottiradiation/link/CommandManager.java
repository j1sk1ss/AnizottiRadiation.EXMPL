package org.cordell.com.anizottiradiation.link;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.bukkit.inventory.ItemStack;
import org.cordell.com.anizottiradiation.AnizottiRadiation;
import org.cordell.com.anizottiradiation.common.SetupProps;
import org.cordell.com.anizottiradiation.events.Infection;
import org.cordell.com.anizottiradiation.events.PlayerEventHandler;
import org.j1sk1ss.itemmanager.manager.Manager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import org.cordell.com.anizottiradiation.common.LocationConverter;
import org.cordell.com.anizottiradiation.events.Radiation;
import org.cordell.com.anizottiradiation.objects.Area;


public class CommandManager implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return false;
        }

        if (label.equalsIgnoreCase("add_radiation_area")) {
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

                var newArea = new Area(firstBound, secondBound, 640);
                Radiation.areas.add(newArea);
                player.sendMessage("New radioactive area added successfully.");

                AnizottiRadiation.dataManager.setString("default_zone_first", LocationConverter.locationToString(firstBound));
                AnizottiRadiation.dataManager.setString("default_zone_second", LocationConverter.locationToString(secondBound));
                AnizottiRadiation.dataManager.setDouble("default_zone_hp", 640);

                return true;
            } catch (NumberFormatException e) {
                sender.sendMessage("Coordinates must be numbers.");
                return false;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else if (label.equalsIgnoreCase("clear_infection")) {
            if (args.length != 1) {
                sender.sendMessage("Usage: /clear_infection <name>");
                return false;
            }

            var player = (Player) Bukkit.getServer().getPlayer(args[0]);
            if (player != null) {
                for (var item : player.getInventory()) {
                    if (item == null) continue;
                    Manager.setInteger2Container(item, -1, "infected");
                }

                Radiation.clearRadiationEffect(player);
                Infection.infectedPlayers.remove(player);
                if (Infection.infectionTasks.containsKey(player)) {
                    for (var task : Infection.infectionTasks.get(player)) task.cancel();
                    Infection.infectionTasks.remove(player);
                }

                try {
                    AnizottiRadiation.dataManager.deleteRecord(player.getName());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                return true;
            }
        }
        else if (label.equalsIgnoreCase("create_antidote")) {
            var player = (Player)sender;
            var analyzes = new ArrayList<ItemStack>();
            for (var item : player.getInventory()) {
                if (item == null) continue;
                var isAnalyze = Manager.getIntegerFromContainer(item, "player_analyze") == 1;
                if (isAnalyze) analyzes.add(item);
            }

            if (!analyzes.isEmpty()) {
                if (PlayerEventHandler.cureItem == null || PlayerEventHandler.cureEffect == null) {
                    PlayerEventHandler.cureItem = SetupProps.CURE_ITEMS.get(new Random().nextInt(SetupProps.CURE_ITEMS.size()));
                    PlayerEventHandler.cureEffect = SetupProps.CURE_EFFECTS.get(new Random().nextInt(SetupProps.CURE_EFFECTS.size()));
                }

                Manager.takeItems(analyzes, player);
                player.sendMessage("Для лечения попробуйте: " + PlayerEventHandler.cureEffect + " " + PlayerEventHandler.cureItem);
            }
        }

        return false;
    }
}
