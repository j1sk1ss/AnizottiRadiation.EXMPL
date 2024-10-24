package org.cordell.com.anizottiradiation.events;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.cordell.com.anizottiradiation.AnizottiRadiation;
import org.cordell.com.anizottiradiation.common.SetupProps;
import org.cordell.com.anizottiradiation.common.StringManager;
import org.j1sk1ss.itemmanager.manager.Item;
import org.j1sk1ss.itemmanager.manager.Manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;


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
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;

        if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            for (var area : Radiation.areas) {
                if (area.isInRegion(player.getLocation())) {
                    if (player.getInventory().getItemInMainHand().getType() == Material.BOOK) {
                        if (Manager.getName(player.getInventory().getItemInMainHand()).equals("Analyzer")) {
                            player.sendMessage("Слишком большие помехи");
                        }
                        else {
                            var value = Math.round(area.getProximityFactor(player) * 100d) / 100d;
                            var message = "";
                            if (value < .4) message = "Приемлемый уровень";
                            else if (value >= .4 && value < .75) message = "Опасный уровень";
                            else if (value >= .75 && value < 1) message = StringManager.getRandomNoiseMessage(new Random().nextInt(20));

                            player.sendMessage("Счётчик Гейгера: " + Math.round(area.getProximityFactor(player) * 100d) / 100d + " (" + message + ")");
                            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CHAIN_PLACE, 1.0f, 1.0f);
                        }
                    }
                    else if (player.getInventory().getItemInMainHand().getType() == Material.COMPASS) {
                        var analyzeBlocks = area.getAnalyzeBlocks();
                        if (analyzeBlocks.isEmpty()) {
                            player.sendMessage("Нет доступных блоков для анализа.");
                            return;
                        }

                        var targetBlock = analyzeBlocks.get(new Random().nextInt(analyzeBlocks.size()));
                        player.setCompassTarget(targetBlock.getLocation());

                        if (player.getLocation().getBlockY() > targetBlock.getLocation().getBlockY()) {
                            player.sendMessage("Вы выше <МАТЕРИАЛА> на " + (player.getLocation().getBlockY() - targetBlock.getLocation().getBlockY()));
                        }
                        else {
                            player.sendMessage("Вы ниже <МАТЕРИАЛА> на " + (targetBlock.getLocation().getBlockY() - player.getLocation().getBlockY()));
                        }
                    }
                }
                else {
                    if (player.getInventory().getItemInMainHand().getType() == Material.BOOK) {
                        if (Manager.getName(player.getInventory().getItemInMainHand()).equals("Analyzer")) {
                            var items = new ArrayList<ItemStack>();
                            var analyzes = new ArrayList<Integer>();
                            for (var item : player.getInventory()) {
                                if (item == null) continue;
                                var analyzeType = Manager.getIntegerFromContainer(item, "dirt_analyze");
                                if (analyzeType != -1) {
                                    analyzes.add(Manager.getIntegerFromContainer(item, "dirt_analyze"));
                                    items.add(item);
                                }
                            }

                            var uniqueGas = new HashSet<>(analyzes);
                            if (uniqueGas.size() < area.getAnalyzeBlocks().size()) {
                                player.sendMessage("Не хватает анализов: " + (area.getAnalyzeBlocks().size() - uniqueGas.size()));
                                return;
                            }

                            Manager.takeItems(items, player);
                            area.setCureBlock(SetupProps.CURE_BLOCKS.get(new Random().nextInt(SetupProps.CURE_BLOCKS.size())));
                            if (area.isInRegion(player.getLocation()))
                                player.sendMessage("Хихиканье уязвимо к: " + area.getCureBlock());
                        }
                    }
                }
            }
        }

        var block = event.getClickedBlock();
        if (block == null) return;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            for (var area : Radiation.areas) {
                if (area.isInRegion(block.getLocation())) {
                    if (player.getInventory().getItemInMainHand().getType() == Material.GLASS_BOTTLE) {
                        if (Infection.infectedPlayers.containsKey(player)) {
                            player.sendMessage("З̧̡̢̟̖̱̳̮̪̭͕̯̫̆ͮ̋̐͐̇ͬ̒͞а̧̝͉̖̜̟̳͚͚̝̫͓̹͔͇̖́̓ͦ̋ͬ̆ͨ̂̋͝р̱̰̥̭͚͔̜̲̭̣̖͎̦̥̮͇̇͊͆͗͘͢а̴̵̘͙̞̥̯̞̗̻̯̖͖͎ͬ͊ͬ̿ͮ̊͐̓ͦ͐ͥ͑̎̍͌̈̈ͭ̀͡ж̴̵̖̝̰͕̤̣̯͓̹̺̇̅͗̋̂ͣ͑̑͐ͬ͒̚̕͠ё̝̼̘̰̱̙̲̭̞̼͙͔̣̍̐ͩ́͆̓͗͐ͥ̑͆̅͂͑ͦ̓̿́̚̚͟͝͝͡н̸̡̠̝̞̘͇̻̖̥̪̗̘̺ͬ̑̓͗ͣ̆ͦ͒͌̋͊ͤ͆ͮͭ͆̚̕н̸̡̱̗̘͓̘͍̙̹͖̗̘̰̟̘̤͕̪́ͨ̇ͣ̔ͫ͐̏̽͂͋̍ͦ̄͂̒͂̕͟ы̋ͮͪͮ͋̋͌ͥ͛̊̂ͪ̌͠҉̨͎͔̯͕͖͖̤̣͈̲̻͍̟͈͓̺̙̟̣й̷̎ͫͯͯ̓̈ͦ͛̇ͤ̆̎͋̉̌͐͗͋͆͜͠͏̟̖̺͍͜");
                            return;
                        }

                        if (!area.getAnalyzeBlocks().contains(event.getClickedBlock())) {
                            player.sendMessage("Необходим образец <МАТЕРИАЛА>");
                            return;
                        }

                        var type = area.getAnalyzeBlocks().indexOf(event.getClickedBlock());
                        var item = new Item("Анализ", "Анализ <МАТЕРИАЛА> №" + type, Material.POTION);
                        Manager.setInteger2Container(item, type, "dirt_analyze");
                        player.getInventory().setItemInMainHand(item);

                        for (int i = 0; i < 2; i++) {
                            var xOffset = (new Random().nextDouble() * 4) - 2;
                            var zOffset = (new Random().nextDouble() * 4) - 2;
                            var spawnLocation = player.getLocation().add(xOffset, 0, zOffset);

                            var zombie = (Zombie)player.getWorld().spawnEntity(spawnLocation, EntityType.ZOMBIE);

                            Objects.requireNonNull(zombie.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(100);
                            zombie.setHealth(100);
                            Objects.requireNonNull(zombie.getEquipment()).setHelmet(new ItemStack(Material.IRON_HELMET));
                        }
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
