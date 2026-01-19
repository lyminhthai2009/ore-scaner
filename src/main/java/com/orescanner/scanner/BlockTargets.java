package com.orescanner.scanner;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import java.util.*;

public class BlockTargets {
    public static final Map<String, Block> ALL_ORES = new LinkedHashMap<>();
    public static final Map<String, Block> ALL_SPAWNERS = new LinkedHashMap<>();
    
    // Static để giữ lựa chọn khi đóng menu
    private static final Set<Block> selectedBlocks = new HashSet<>();
    
    static {
        // === ORES ===
        ALL_ORES.put("Diamond Ore", Blocks.DIAMOND_ORE);
        ALL_ORES.put("Deepslate Diamond", Blocks.DEEPSLATE_DIAMOND_ORE);
        ALL_ORES.put("Ancient Debris", Blocks.ANCIENT_DEBRIS);
        ALL_ORES.put("Gold Ore", Blocks.GOLD_ORE);
        ALL_ORES.put("Deepslate Gold", Blocks.DEEPSLATE_GOLD_ORE);
        ALL_ORES.put("Iron Ore", Blocks.IRON_ORE);
        ALL_ORES.put("Deepslate Iron", Blocks.DEEPSLATE_IRON_ORE);
        ALL_ORES.put("Coal Ore", Blocks.COAL_ORE);
        ALL_ORES.put("Deepslate Coal", Blocks.DEEPSLATE_COAL_ORE);
        ALL_ORES.put("Lapis Ore", Blocks.LAPIS_ORE);
        ALL_ORES.put("Redstone Ore", Blocks.REDSTONE_ORE);
        ALL_ORES.put("Emerald Ore", Blocks.EMERALD_ORE);
        ALL_ORES.put("Nether Gold", Blocks.NETHER_GOLD_ORE);
        ALL_ORES.put("Nether Quartz", Blocks.NETHER_QUARTZ_ORE);
        
        // === SPAWNERS & CHESTS ===
        ALL_SPAWNERS.put("Spawner", Blocks.SPAWNER);
        ALL_SPAWNERS.put("Trial Spawner", getBlockSafe("trial_spawner"));
        ALL_SPAWNERS.put("Chest", Blocks.CHEST);
        ALL_SPAWNERS.put("Ender Chest", Blocks.ENDER_CHEST);
        ALL_SPAWNERS.put("Barrel", Blocks.BARREL);
        ALL_SPAWNERS.put("Shulker Box", Blocks.SHULKER_BOX);
    }
    
    public void toggleBlock(Block block) {
        if (selectedBlocks.contains(block)) {
            selectedBlocks.remove(block);
        } else {
            selectedBlocks.add(block);
        }
    }
    
    public void selectAll() {
        selectedBlocks.addAll(ALL_ORES.values());
        selectedBlocks.addAll(ALL_SPAWNERS.values());
        selectedBlocks.removeIf(Objects::isNull);
    }
    
    public void clearAll() {
        selectedBlocks.clear();
    }
    
    public boolean isSelected(Block block) {
        return selectedBlocks.contains(block);
    }
    
    public Set<Block> getSelectedBlocks() {
        return new HashSet<>(selectedBlocks);
    }
    
    public int getSelectedCount() {
        return selectedBlocks.size();
    }
    
    private static Block getBlockSafe(String id) {
        try {
            return Registries.BLOCK.get(Identifier.of("minecraft", id));
        } catch (Exception e) { return null; }
    }
}
