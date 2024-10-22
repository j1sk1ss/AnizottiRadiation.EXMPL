package org.cordell.com.anizottiradiation.events;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffectType;
import org.cordell.com.anizottiradiation.AnizottiRadiation;
import org.j1sk1ss.itemmanager.manager.Item;
import org.j1sk1ss.itemmanager.manager.Manager;

import java.io.IOException;
import java.util.Objects;



public class PlayerEventHandler implements Listener {
    public static Material cureItem = null;
    public static PotionEffectType cureEffect = null;


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) throws IOException {
        var player = event.getPlayer();
        var infectionLevel = AnizottiRadiation.dataManager.getInt(player.getName());
        if (infectionLevel != -1) {
            Infection.infectedPlayers.put(player, infectionLevel);
            Infection.startInfection(player);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) throws IOException {
        var player = event.getEntity();
        if (isCured(player)) {
            if (Infection.infectionTasks.containsKey(player)) {
                for (var task : Infection.infectionTasks.get(player)) task.cancel();
                Infection.infectionTasks.remove(player);
            }

            AnizottiRadiation.dataManager.deleteRecord(player.getName());
        }

        if (player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH) != null) {
            var defaultJumpStrength = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)).getDefaultValue();
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)).setBaseValue(defaultJumpStrength);
        }
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) throws IOException {
        var entity = event.getEntity();
        if (entity instanceof Player player) {
            for (var area : Radiation.areas) {
                if (area.isInRegion(player, .5)) {
                    if (event.getItem().getItemStack().getItemMeta() == null) continue;
                    Manager.setInteger2Container(event.getItem().getItemStack(), 2, "infected");
                }
                else if (area.isInRegion(player, .75)) {
                    if (event.getItem().getItemStack().getItemMeta() == null) continue;
                    Manager.setInteger2Container(event.getItem().getItemStack(), 3, "infected");
                }
            }

            // If infected player pickup item, item will be infected
            if (Infection.infectionTasks.containsKey(player)) {
                Manager.setInteger2Container(event.getItem().getItemStack(), 3, "infected");
                return;
            }

            var infectedFlag = Manager.getIntegerFromContainer(event.getItem().getItemStack(), "infected");
            if (infectedFlag != -1) {
                Infection.infectedPlayers.put(player, infectedFlag);
                Infection.startInfection(player);
            }
        }
    }

    @EventHandler
    public void onItemTake(InventoryClickEvent event) throws IOException {
        var player = (Player)event.getWhoClicked();
        var item = event.getCurrentItem();
        if (item == null) return;
        if (item.getItemMeta() == null) return;

        // If infected player pickup item, item will be infected
        if (Infection.infectionTasks.containsKey(player)) {
            Manager.setInteger2Container(item, 3, "infected");
            return;
        }

        var infectedFlag = Manager.getIntegerFromContainer(item, "infected");
        if (infectedFlag != -1) {
            Infection.infectedPlayers.put(player, infectedFlag);
            Infection.startInfection(player);
        }
    }

    @EventHandler
    public void onBlockPlaced(BlockPlaceEvent event) throws IOException {
        var player = event.getPlayer();
        for (var area : Radiation.areas) {
            if (area.isInRegion(player, .65)) {
                if (area.getCureBlock() == null) continue;
                if (event.getBlock().getType() == area.getCureBlock()) {
                    area.damageZone(1);
                    area.expandArea(-.01);
                    event.getBlock().setType(Material.AIR, false);
                }
            }
        }
    }

    @EventHandler
    public void onBlockClick(PlayerInteractEvent event) {
        var player = event.getPlayer();
        var block = event.getClickedBlock();
        if (block == null) return;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            for (var area : Radiation.areas) {
                if (area.isInRegion(block.getLocation())) {
                    if (player.getInventory().getItemInMainHand().getType() == Material.BOOK) {
                        var value = Math.round(area.getProximityFactor(player) * 100d) / 100d;
                        var message = "";
                        if (value < .4) message = "Приемлемый уровень";
                        else if (value >= .4 && value < .75) message = "Опасный уровень";
                        else if (value >= .75 && value < 1) message = "Критический уровень";

                        player.sendMessage("Счётчик Гейгера: " + Math.round(area.getProximityFactor(player) * 100d) / 100d + " (" + message + ")");
                        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CHAIN_PLACE, 1.0f, 1.0f);
                    }
                    else if (player.getInventory().getItemInMainHand().getType() == Material.GLASS_BOTTLE) {
                        if (Infection.infectedPlayers.containsKey(player)) {
                            player.sendMessage("З̧̡̢̟̖̱̳̮̪̭͕̯̫̆ͮ̋̐͐̇ͬ̒͞а̧̝͉̖̜̟̳͚͚̝̫͓̹͔͇̖́̓ͦ̋ͬ̆ͨ̂̋͝р̱̰̥̭͚͔̜̲̭̣̖͎̦̥̮͇̇͊͆͗͘͢а̴̵̘͙̞̥̯̞̗̻̯̖͖͎ͬ͊ͬ̿ͮ̊͐̓ͦ͐ͥ͑̎̍͌̈̈ͭ̀͡ж̴̵̖̝̰͕̤̣̯͓̹̺̇̅͗̋̂ͣ͑̑͐ͬ͒̚̕͠ё̝̼̘̰̱̙̲̭̞̼͙͔̣̍̐ͩ́͆̓͗͐ͥ̑͆̅͂͑ͦ̓̿́̚̚͟͝͝͡н̸̡̠̝̞̘͇̻̖̥̪̗̘̺ͬ̑̓͗ͣ̆ͦ͒͌̋͊ͤ͆ͮͭ͆̚̕н̸̡̱̗̘͓̘͍̙̹͖̗̘̰̟̘̤͕̪́ͨ̇ͣ̔ͫ͐̏̽͂͋̍ͦ̄͂̒͂̕͟ы̋ͮͪͮ͋̋͌ͥ͛̊̂ͪ̌͠҉̨͎͔̯͕͖͖̤̣͈̲̻͍̟͈͓̺̙̟̣й̷̎ͫͯͯ̓̈ͦ͛̇ͤ̆̎͋̉̌͐͗͋͆͜͠͏̟̖̺͍͜");
                            return;
                        }

                        if (event.getClickedBlock().getType() != Material.DIRT &&
                                event.getClickedBlock().getType() != Material.GRASS_BLOCK &&
                                event.getClickedBlock().getType() != Material.DIRT_PATH) {
                            player.sendMessage("Необходим образец почвы");
                            return;
                        }

                        var type = -1;
                        var distance = area.getProximityFactor(player);
                        if (distance < .3) type = 1;
                        else if (distance >= .3 && distance < .5) type = 2;
                        else if (distance >= .5 && distance < .8) type = 3;
                        else if (distance >= .8 && distance < 1) type = 4;

                        var item = new Item("Анализ", "Анализ почвы " + type, Material.POTION);
                        Manager.setInteger2Container(item, type, "dirt_analyze");
                        player.getInventory().setItemInMainHand(item);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        var player = event.getPlayer();
        var entity = event.getRightClicked();

        if (entity instanceof Player clickedPlayer) {
            if (Infection.infectedPlayers.containsKey(player)) {
                player.sendMessage("З̧̡̢̟̖̱̳̮̪̭͕̯̫̆ͮ̋̐͐̇ͬ̒͞а̧̝͉̖̜̟̳͚͚̝̫͓̹͔͇̖́̓ͦ̋ͬ̆ͨ̂̋͝р̱̰̥̭͚͔̜̲̭̣̖͎̦̥̮͇̇͊͆͗͘͢а̴̵̘͙̞̥̯̞̗̻̯̖͖͎ͬ͊ͬ̿ͮ̊͐̓ͦ͐ͥ͑̎̍͌̈̈ͭ̀͡ж̴̵̖̝̰͕̤̣̯͓̹̺̇̅͗̋̂ͣ͑̑͐ͬ͒̚̕͠ё̝̼̘̰̱̙̲̭̞̼͙͔̣̍̐ͩ́͆̓͗͐ͥ̑͆̅͂͑ͦ̓̿́̚̚͟͝͝͡н̸̡̠̝̞̘͇̻̖̥̪̗̘̺ͬ̑̓͗ͣ̆ͦ͒͌̋͊ͤ͆ͮͭ͆̚̕н̸̡̱̗̘͓̘͍̙̹͖̗̘̰̟̘̤͕̪́ͨ̇ͣ̔ͫ͐̏̽͂͋̍ͦ̄͂̒͂̕͟ы̋ͮͪͮ͋̋͌ͥ͛̊̂ͪ̌͠҉̨͎͔̯͕͖͖̤̣͈̲̻͍̟͈͓̺̙̟̣й̷̎ͫͯͯ̓̈ͦ͛̇ͤ̆̎͋̉̌͐͗͋͆͜͠͏̟̖̺͍͜");
                return;
            }

            if (player.getInventory().getItemInMainHand().getType() == Material.BOOK) {
                player.sendMessage("Уровень заражения игрока: " + Infection.infectedPlayers.get(clickedPlayer));
            }
            else if (player.getInventory().getItemInMainHand().getType() == Material.GLASS_BOTTLE) {
                var item = new Item("Анализ", "Анализ игрока " + player.getName(), Material.POTION);
                Manager.setInteger2Container(item, 1, "player_analyze");
                player.getInventory().setItemInMainHand(item);
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        var player = event.getPlayer();
        if (Infection.infectionTasks.containsKey(player)) {
            for (var area : Radiation.areas) {
                var respawnLocation = area.getCenter().toHighestLocation().add(0, 1, 0);
                event.setRespawnLocation(respawnLocation);
                return;
            }
        }
    }

    private boolean isCured(Player player) {
        if (cureItem == null || cureEffect == null) return false;
        var inventory = player.getInventory().getContents();
        var hasCureItem = false;

        for (var item : inventory) {
            if (item != null && item.getType() == cureItem) {
                hasCureItem = true;
                break;
            }
        }

        var hasHealingEffect = player.getActivePotionEffects().stream().anyMatch(effect -> effect.getType() == cureEffect);
        return hasCureItem && hasHealingEffect;
    }
}
