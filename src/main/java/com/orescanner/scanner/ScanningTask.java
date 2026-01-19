package com.orescanner.scanner;

import com.orescanner.OreScannerMod;
import com.orescanner.util.FileExporter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.*;

public class ScanningTask {
    private final Set<Block> targetBlocks;
    private final int blocksPerTick;
    
    // Tọa độ giới hạn
    private int minX, maxX;
    private int minZ, maxZ;
    private final int minY, maxY;

    // Con trỏ quét hiện tại
    private int currentX, currentY, currentZ;

    private final List<ScanResult> results = new ArrayList<>();
    private final long startTime;
    private long totalBlocks; 
    private long scannedBlocks = 0;
    private boolean complete = false;
    
    private final int chunkRadius; // Lưu tạm để init

    public ScanningTask(Set<Block> targetBlocks, int chunkRadius, int minY, int maxY, int blocksPerSecond) {
        this.targetBlocks = targetBlocks;
        this.minY = minY;
        this.maxY = maxY;
        this.blocksPerTick = Math.max(1, blocksPerSecond / 20); // 20 ticks/giây
        this.startTime = System.currentTimeMillis();
        this.chunkRadius = chunkRadius; 
    }

    public void initialize(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            complete = true;
            return;
        }
        
        BlockPos playerPos = client.player.getBlockPos();
        ChunkPos centerChunk = new ChunkPos(playerPos);
        
        // Tính toán giới hạn block dựa trên chunk
        this.minX = (centerChunk.x - chunkRadius) * 16;
        this.maxX = (centerChunk.x + chunkRadius) * 16 + 15;
        this.minZ = (centerChunk.z - chunkRadius) * 16;
        this.maxZ = (centerChunk.z + chunkRadius) * 16 + 15;
        
        // Đặt con trỏ về vị trí bắt đầu
        this.currentX = minX;
        this.currentY = minY;
        this.currentZ = minZ;

        // Tính tổng số block
        long width = (long) maxX - minX + 1;
        long length = (long) maxZ - minZ + 1;
        long height = (long) maxY - minY + 1;
        this.totalBlocks = width * length * height;
        
        OreScannerMod.LOGGER.info("Scan initialized: {} blocks", totalBlocks);
    }
    
    public void tick(MinecraftClient client) {
        if (complete || client.world == null) return;
        
        int blocksProcessed = 0;
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();

        // Chạy vòng lặp quét
        while (blocksProcessed < blocksPerTick && !complete) {
            // Chỉ quét nếu chunk đã load để tránh lag
            if (client.world.isChunkLoaded(currentX >> 4, currentZ >> 4)) {
                mutablePos.set(currentX, currentY, currentZ);
                BlockState state = client.world.getBlockState(mutablePos);
                Block block = state.getBlock();
                
                if (targetBlocks.contains(block)) {
                    // Tìm thấy! Lưu vị trí (phải new BlockPos vì mutablePos sẽ thay đổi)
                    results.add(new ScanResult(block, mutablePos.toImmutable()));
                }
            }

            scannedBlocks++;
            blocksProcessed++;

            // Logic tăng tọa độ (Y -> X -> Z)
            currentY++;
            if (currentY > maxY) {
                currentY = minY;
                currentX++;
                if (currentX > maxX) {
                    currentX = minX;
                    currentZ++;
                    if (currentZ > maxZ) {
                        complete = true;
                        onComplete(client);
                        break;
                    }
                }
            }
        }
    }
    
    private void onComplete(MinecraftClient client) {
        long duration = System.currentTimeMillis() - startTime;
        OreScannerMod.LOGGER.info("Scan complete! Found {} blocks in {}ms", results.size(), duration);
        FileExporter.exportResults(results);
        
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§a[Ore Scanner] Hoàn thành! Tìm thấy " + results.size() + " blocks."), false);
        }
    }
    
    public boolean isComplete() { return complete; }
    public long getTotalBlocks() { return totalBlocks; }
    public long getScannedBlocks() { return scannedBlocks; }
    
    public double getProgress() {
        if (totalBlocks == 0) return 0;
        return (double) scannedBlocks / totalBlocks;
    }
    
    public List<ScanResult> getResults() { return results; }
    
    public int getEstimatedTimeRemaining() {
        if (scannedBlocks == 0 || blocksPerTick == 0) return 0;
        long remainingBlocks = totalBlocks - scannedBlocks;
        long remainingTicks = remainingBlocks / blocksPerTick;
        return (int) (remainingTicks / 20);
    }
}
