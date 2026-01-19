package com.orescanner.scanner;

import com.orescanner.OreScannerMod;
import com.orescanner.util.FileExporter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.*;

/**
 * Task quét blocks - Chạy incremental mỗi tick để tránh lag
 * 
 * Cơ chế hoạt động:
 * 1. Tạo queue chứa tất cả BlockPos cần quét
 * 2. Mỗi tick, poll N blocks từ queue (N = blocksPerSecond / 20)
 * 3. Chỉ quét chunks đã load
 * 4. Lưu kết quả vào List<ScanResult>
 */
public class ScanningTask {
    private final Set<Block> targetBlocks;
    private final int chunkRadius;
    private final int minY;
    private final int maxY;
    private final int blocksPerTick; // Số blocks quét mỗi tick
    
    private final Queue<BlockPos> scanQueue = new LinkedList<>();
    private final List<ScanResult> results = new ArrayList<>();
    
    private final long startTime;
    private int totalBlocks;
    private int scannedBlocks = 0;
    private boolean complete = false;
    
    public ScanningTask(Set<Block> targetBlocks, int chunkRadius, int minY, int maxY, int blocksPerSecond) {
        this.targetBlocks = targetBlocks;
        this.chunkRadius = chunkRadius;
        this.minY = minY;
        this.maxY = maxY;
        this.blocksPerTick = Math.max(1, blocksPerSecond / 20); // 20 ticks/giây
        this.startTime = System.currentTimeMillis();
    }
    
    /**
     * Khởi tạo scan queue - Tạo danh sách tất cả vị trí cần quét
     */
    public void initialize(MinecraftClient client) {
        if (client.player == null || client.world == null) return;
        
        BlockPos playerPos = client.player.getBlockPos();
        ChunkPos centerChunk = new ChunkPos(playerPos);
        
        // Tính toán phạm vi chunks
        int startChunkX = centerChunk.x - chunkRadius;
        int endChunkX = centerChunk.x + chunkRadius;
        int startChunkZ = centerChunk.z - chunkRadius;
        int endChunkZ = centerChunk.z + chunkRadius;
        
        // Thêm tất cả BlockPos vào queue
        for (int chunkX = startChunkX; chunkX <= endChunkX; chunkX++) {
            for (int chunkZ = startChunkZ; chunkZ <= endChunkZ; chunkZ++) {
                // Chỉ quét chunks đã load
                if (!client.world.isChunkLoaded(chunkX, chunkZ)) continue;
                
                // Thêm tất cả blocks trong chunk vào queue
                int startX = chunkX << 4; // chunkX * 16
                int startZ = chunkZ << 4;
                
                for (int x = startX; x < startX + 16; x++) {
                    for (int z = startZ; z < startZ + 16; z++) {
                        for (int y = minY; y <= maxY; y++) {
                            scanQueue.add(new BlockPos(x, y, z));
                        }
                    }
                }
            }
        }
        
        totalBlocks = scanQueue.size();
        OreScannerMod.LOGGER.info("Scan initialized: {} blocks to scan", totalBlocks);
    }
    
    /**
     * Chạy mỗi tick - Quét N blocks rồi dừng
     */
    public void tick(MinecraftClient client) {
        if (complete || client.world == null) return;
        
        int blocksToScan = Math.min(blocksPerTick, scanQueue.size());
        
        for (int i = 0; i < blocksToScan; i++) {
            BlockPos pos = scanQueue.poll();
            if (pos == null) break;
            
            // Quét block tại vị trí này
            BlockState state = client.world.getBlockState(pos);
            Block block = state.getBlock();
            
            if (targetBlocks.contains(block)) {
                results.add(new ScanResult(block, pos));
            }
            
            scannedBlocks++;
        }
        
        // Kiểm tra xem đã quét xong chưa
        if (scanQueue.isEmpty()) {
            complete = true;
            onComplete(client);
        }
    }
    
    /**
     * Gọi khi quét xong - Export file và thông báo
     */
    private void onComplete(MinecraftClient client) {
        long duration = System.currentTimeMillis() - startTime;
        
        OreScannerMod.LOGGER.info("Scan complete! Found {} blocks in {}ms", results.size(), duration);
        
        // Export ra file
        FileExporter.exportResults(results);
        
        // Thông báo cho người chơi
        if (client.player != null) {
            client.player.sendMessage(
                Text.literal("§a[Ore Scanner] Quét xong! Tìm thấy " + results.size() + " blocks."),
                false
            );
            client.player.sendMessage(
                Text.literal("§7Kết quả đã lưu vào .minecraft/scans/"),
                false
            );
        }
    }
    
    // === GETTERS ===
    
    public boolean isComplete() {
        return complete;
    }
    
    public int getTotalBlocks() {
        return totalBlocks;
    }
    
    public int getScannedBlocks() {
        return scannedBlocks;
    }
    
    public double getProgress() {
        if (totalBlocks == 0) return 0;
        return (double) scannedBlocks / totalBlocks;
    }
    
    public List<ScanResult> getResults() {
        return new ArrayList<>(results);
    }
    
    public long getElapsedTime() {
        return System.currentTimeMillis() - startTime;
    }
    
    /**
     * Tính thời gian dự kiến còn lại (giây)
     */
    public int getEstimatedTimeRemaining() {
        if (scannedBlocks == 0 || blocksPerTick == 0) return 0;
        
        int remainingBlocks = totalBlocks - scannedBlocks;
        int remainingTicks = remainingBlocks / blocksPerTick;
        
        return remainingTicks / 20; // Convert ticks sang giây
    }
}