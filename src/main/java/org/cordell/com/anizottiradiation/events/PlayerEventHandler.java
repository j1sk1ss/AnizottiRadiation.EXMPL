package org.cordell.com.anizottiradiation.events;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static org.cordell.com.anizottiradiation.events.Infection.*;


public class PlayerEventHandler implements Listener {
    private static final List<Material> CURE_ITEMS = Arrays.asList(
            Material.GOLDEN_APPLE,
            Material.MILK_BUCKET,
            Material.POTION,
            Material.ELYTRA,
            Material.LIME_TERRACOTTA,
            Material.APPLE,
            Material.BONE_MEAL
    );

    private static final List<PotionEffectType> CURE_EFFECTS = Arrays.asList(
            PotionEffectType.REGENERATION,
            PotionEffectType.HEALTH_BOOST,
            PotionEffectType.NAUSEA,
            PotionEffectType.SLOWNESS,
            PotionEffectType.SPEED
    );

    private final Material cureItem = CURE_ITEMS.get(new Random().nextInt(CURE_ITEMS.size()));;
    private final PotionEffectType cureEffect = CURE_EFFECTS.get(new Random().nextInt(CURE_EFFECTS.size()));;


    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        var player = event.getEntity();
        infectedPlayers.remove(player);

        if (isCured(player)) {
            if (infectionTasks.containsKey(player)) {
                for (var task : infectionTasks.get(player)) task.cancel();
                infectionTasks.remove(player);
            }
        }

        if (player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH) != null) {
            var defaultJumpStrength = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)).getDefaultValue();
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)).setBaseValue(defaultJumpStrength);
        }
    }

    private boolean isCured(Player player) {
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
