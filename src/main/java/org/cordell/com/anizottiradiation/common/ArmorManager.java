package org.cordell.com.anizottiradiation.common;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;


public class ArmorManager {
    public static boolean damagePlayerArmor(Player player, int damage) {
        var helmet = player.getInventory().getHelmet();
        var chestPlate = player.getInventory().getChestplate();
        var leggings = player.getInventory().getLeggings();
        var boots = player.getInventory().getBoots();

        var status = ArmorManager.damageArmor(helmet, player, damage);
        status = status || ArmorManager.damageArmor(chestPlate, player, damage);
        status = status || ArmorManager.damageArmor(leggings, player, damage);
        status = status || ArmorManager.damageArmor(boots, player, damage);

        return status;
    }

    public static boolean damageArmor(ItemStack armorPiece, Player player, int damage) {
        if (armorPiece == null) return false;

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

        return true;
    }
}
