package org.cordell.com.anizottiradiation.common;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;


public class SetupProps {
    public static final List<Material> ANALYZE_BLOCKS = Arrays.asList(
            Material.DIRT,
            Material.MOSS_BLOCK,
            Material.STONE,
            Material.GRASS_BLOCK,
            Material.GRAVEL
    );

    public static final List<Material> CURE_BLOCKS = Arrays.asList(
            Material.SAND,
            Material.COBBLESTONE,
            Material.STONE,
            Material.RED_SAND,
            Material.GRAVEL,
            Material.DIRT,
            Material.GLASS
    );

    public static final List<Material> CURE_ITEMS = Arrays.asList(
            Material.GOLDEN_APPLE,
            Material.MILK_BUCKET,
            Material.POTION,
            Material.ELYTRA,
            Material.LIME_TERRACOTTA,
            Material.APPLE,
            Material.BONE_MEAL
    );

    public static final List<PotionEffectType> CURE_EFFECTS = Arrays.asList(
            PotionEffectType.REGENERATION,
            PotionEffectType.HEALTH_BOOST,
            PotionEffectType.NAUSEA,
            PotionEffectType.SLOWNESS,
            PotionEffectType.SPEED
    );

    public static final List<Material> RADIOACTIVE_PLANTS = List.of(
            Material.BAMBOO,
            Material.MOSS_BLOCK,
            Material.TALL_GRASS
    );
}
