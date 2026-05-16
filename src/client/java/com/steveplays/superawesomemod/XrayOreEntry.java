package com.steveplays.superawesomemod;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public record XrayOreEntry(String name, int color, List<Block> blocks) {

    public static final List<XrayOreEntry> ALL = List.of(
        new XrayOreEntry("Coal",           0x4D4D4D, List.of(Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE)),
        new XrayOreEntry("Iron",           0xD8AF93, List.of(Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE)),
        new XrayOreEntry("Copper",         0xBF6B3A, List.of(Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE)),
        new XrayOreEntry("Gold",           0xFCDB00, List.of(Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.NETHER_GOLD_ORE)),
        new XrayOreEntry("Redstone",       0xFF0000, List.of(Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE)),
        new XrayOreEntry("Lapis Lazuli",   0x345EC3, List.of(Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE)),
        new XrayOreEntry("Diamond",        0x5DECF5, List.of(Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE)),
        new XrayOreEntry("Emerald",        0x41F384, List.of(Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE)),
        new XrayOreEntry("Nether Quartz",  0xE3D5C9, List.of(Blocks.NETHER_QUARTZ_ORE)),
        new XrayOreEntry("Ancient Debris", 0x96614E, List.of(Blocks.ANCIENT_DEBRIS))
    );

    public float r() { return ((color >> 16) & 0xFF) / 255f; }
    public float g() { return ((color >> 8)  & 0xFF) / 255f; }
    public float b() { return  (color        & 0xFF) / 255f; }
}
