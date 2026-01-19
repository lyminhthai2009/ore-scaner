package com.orescanner.scanner;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.*;

/**
 * Quản lý danh sách blocks mà người dùng muốn tìm
 */
public class BlockTargets {
    // Danh sách tất cả khoáng sản có thể tìm
    public static final Map<String, Block> ALL_ORES = new LinkedHashMap<>();
    public static final Map<String, Block> ALL_SPAWNERS = new LinkedHashMap<>();
    
    static {
        // === KHOÁNG SẢN OVERWORLD ===
        ALL_ORES.put("Coal Ore", Blocks.COAL_ORE);
        ALL_ORES.put("Deepslate Coal Ore", Blocks.DEEPSLATE_COAL_ORE);
        
        ALL_ORES.put("Iron Ore", Blocks.IRON_ORE);
        ALL_ORES.put("Deepslate Iron Ore", Blocks.DEEPSLATE_IRON_ORE);
        
        ALL_ORES.put("Copper Ore", Blocks.COPPER_ORE);
        ALL_ORES.put("Deepslate Copper Ore", Blocks.DEEPSLATE_COPPER_ORE);
        
        ALL_ORES.put("Gold Ore", Blocks.GOLD_ORE);
        ALL_ORES.put("Deepslate Gold Ore", Blocks.DEEPSLATE_GOLD_ORE);
        
        ALL_ORES.put("Redstone Ore", Blocks.REDSTONE_ORE);
        ALL_ORES.put("Deepslate Redstone Ore", Blocks.DEEPSLATE_REDSTONE_ORE);
        
        ALL_ORES.put("Lapis Ore", Blocks.LAPIS_ORE);
        ALL_ORES.put("Deepslate Lapis Ore", Blocks.DEEPSLATE_LAPIS_ORE);
        
        ALL_ORES.put("Diamond Ore", Blocks.DIAMOND_ORE);
        ALL_ORES.put("Deepslate Diamond Ore", Blocks.DEEPSLATE_DIAMOND_ORE);
        
        ALL_ORES.put("Emerald Ore", Blocks.EMERALD_ORE);
        ALL_ORES.put("Deepslate Emerald Ore", Blocks.DEEPSLATE_EMERALD_ORE);
        
        // === KHOÁNG SẢN NETHER ===
        ALL_ORES.put("Nether Gold Ore", Blocks.NETHER_GOLD_ORE);
        ALL_ORES.put("Nether Quartz Ore", Blocks.NETHER_QUARTZ_ORE);
        ALL_ORES.put("Ancient Debris", Blocks.ANCIENT_DEBRIS);
        
        // === SPAWNERS ===
        ALL_SPAWNERS.put("Monster Spawner", Blocks.SPAWNER);
        ALL_SPAWNERS.put("Trial Spawner", getBlockSafe("trial_spawner")); // 1.21+
        
        // === STORAGE & SPECIAL ===
        ALL_ORES.put("Chest", Blocks.CHEST);
        ALL_ORES.put("Trapped Chest", Blocks.TRAPPED_CHEST);
        ALL_ORES.put("Ender Chest", Blocks.ENDER_CHEST);
        ALL_ORES.put("Barrel", Blocks.BARREL);
        ALL_ORES.put("Shulker Box", Blocks.SHULKER_BOX);
    }
    
    // Blocks mà người dùng đã chọn để quét
    private final Set<Block> selectedBlocks = new HashSet<>();
    
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
    
    // Helper method để lấy block mới (tránh crash ở các phiên bản cũ)
    private static Block getBlockSafe(String id) {
        try {
            return Registries.BLOCK.get(Identifier.of("minecraft", id));
        } catch (Exception e) {
            return null;
        }
    }
}