package com.orescanner.scanner;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

public class BlockTargets {
    private final Set<Block> selectedBlocks = new HashSet<>();

    public static final Map<String, Block> ALL_ORES = new HashMap<>();
    public static final Map<String, Block> ALL_SPAWNERS = new HashMap<>();

    static {
        // Khoáng sản
        ALL_ORES.put("Kim cương", Blocks.DIAMOND_ORE);
        ALL_ORES.put("Kim cương (Đá sâu)", Blocks.DEEPSLATE_DIAMOND_ORE);
        ALL_ORES.put("Vàng", Blocks.GOLD_ORE);
        ALL_ORES.put("Vàng (Đá sâu)", Blocks.DEEPSLATE_GOLD_ORE);
        ALL_ORES.put("Sắt", Blocks.IRON_ORE);
        ALL_ORES.put("Sắt (Đá sâu)", Blocks.DEEPSLATE_IRON_ORE);
        ALL_ORES.put("Than", Blocks.COAL_ORE);
        ALL_ORES.put("Đá đỏ", Blocks.REDSTONE_ORE);
        ALL_ORES.put("Ngọc lục bảo", Blocks.EMERALD_ORE);
        ALL_ORES.put("Thạch anh Nether", Blocks.NETHER_QUARTZ_ORE);
        ALL_ORES.put("Vàng Nether", Blocks.NETHER_GOLD_ORE);
        ALL_ORES.put("Cổ vật (Netherite)", Blocks.ANCIENT_DEBRIS);

        // Spawners & Chests
        ALL_SPAWNERS.put("Lồng quái", Blocks.SPAWNER);
        ALL_SPAWNERS.put("Rương", Blocks.CHEST);
        ALL_SPAWNERS.put("Rương Bẫy", Blocks.TRAPPED_CHEST);
        ALL_SPAWNERS.put("Thùng gỗ (Barrel)", Blocks.BARREL);
    }

    public void toggleBlock(Block block) {
        if (selectedBlocks.contains(block)) {
            selectedBlocks.remove(block);
        } else {
            selectedBlocks.add(block);
        }
    }

    public boolean isSelected(Block block) {
        return selectedBlocks.contains(block);
    }

    public void selectAll() {
        selectedBlocks.addAll(ALL_ORES.values());
        selectedBlocks.addAll(ALL_SPAWNERS.values());
    }

    public void clearAll() {
        selectedBlocks.clear();
    }

    public int getSelectedCount() {
        return selectedBlocks.size();
    }

    // THÊM PHƯƠNG THỨC NÀY ĐỂ SỬA LỖI COMPILER
    public Set<Block> getSelectedBlocksCopy() {
        return new HashSet<>(this.selectedBlocks);
    }
}
