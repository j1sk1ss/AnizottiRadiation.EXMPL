package org.cordell.com.anizottiradiation.common;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

public class ArmorManager {
    public static void damagePlayerArmor(Player player, int damage) {
        var helmet = player.getInventory().getHelmet();
        var chestPlate = player.getInventory().getChestplate();
        var leggings = player.getInventory().getLeggings();
        var boots = player.getInventory().getBoots();

        ArmorManager.damageArmor(helmet, player, damage);
        ArmorManager.damageArmor(chestPlate, player, damage);
        ArmorManager.damageArmor(leggings, player, damage);
        ArmorManager.damageArmor(boots, player, damage);
    }

    public static void damageArmor(ItemStack armorPiece, Player player, int damage) {
        if (armorPiece == null) return;

        var damageable = (Damageable) armorPiece.getItemMeta();
        if (damageable != null) {
            int currentDamage = damageable.getDamage();
            damageable.setDamage(currentDamage + damage);
            armorPiece.setItemMeta(damageable);

            if (damageable.getDamage() >= armorPiece.getType().getMaxDurability()) {
                armorPiece.setAmount(0);
                player.sendMessage("Элемент защины сломан");
            }
        }
    }
}
