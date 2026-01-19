package com.orescanner.scanner;

import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;

/**
 * Lưu trữ 1 kết quả tìm được
 */
public class ScanResult {
    private final Block block;
    private final BlockPos pos;
    private final long timestamp;
    
    public ScanResult(Block block, BlockPos pos) {
        this.block = block;
        this.pos = pos;
        this.timestamp = System.currentTimeMillis();
    }
    
    public Block getBlock() {
        return block;
    }
    
    public BlockPos getPos() {
        return pos;
    }
    
    public String getBlockName() {
        return Registries.BLOCK.getId(block).getPath()
            .replace("_", " ")
            .toUpperCase();
    }
    
    /**
     * Format: [DIAMOND ORE] | X: 123 | Y: -45 | Z: 678
     */
    public String toFormattedString() {
        return String.format("[%s] | X: %d | Y: %d | Z: %d", 
            getBlockName(), 
            pos.getX(), 
            pos.getY(), 
            pos.getZ()
        );
    }
    
    @Override
    public String toString() {
        return toFormattedString();
    }
}