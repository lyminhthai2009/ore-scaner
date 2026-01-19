package com.orescanner.util;

import com.orescanner.OreScannerMod;
import com.orescanner.scanner.ScanResult;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Export kết quả quét ra file .txt
 */
public class FileExporter {
    private static final Path SCANS_DIR = FabricLoader.getInstance()
        .getGameDir()
        .resolve("scans");
    
    /**
     * Export danh sách kết quả ra file
     */
    public static void exportResults(List<ScanResult> results) {
        // Tạo thư mục scans nếu chưa có
        File dir = SCANS_DIR.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        // Tạo tên file với timestamp
        String timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String filename = "scan_" + timestamp + ".txt";
        File file = new File(dir, filename);
        
        try (FileWriter writer = new FileWriter(file)) {
            // Header
            writer.write("=".repeat(60) + "\n");
            writer.write("        ORE SCANNER - KẾT QUẢ QUÉT\n");
            writer.write("=".repeat(60) + "\n");
            writer.write("Thời gian: " + timestamp + "\n");
            writer.write("Tổng số blocks tìm được: " + results.size() + "\n");
            writer.write("=".repeat(60) + "\n\n");
            
            // Nhóm theo loại block
            Map<String, List<ScanResult>> grouped = results.stream()
                .collect(Collectors.groupingBy(ScanResult::getBlockName));
            
            // Viết từng nhóm
            for (Map.Entry<String, List<ScanResult>> entry : grouped.entrySet()) {
                String blockName = entry.getKey();
                List<ScanResult> blockResults = entry.getValue();
                
                writer.write("\n--- " + blockName + " (" + blockResults.size() + " blocks) ---\n");
                
                for (ScanResult result : blockResults) {
                    writer.write(result.toFormattedString() + "\n");
                }
            }
            
            // Footer
            writer.write("\n" + "=".repeat(60) + "\n");
            writer.write("End of scan results\n");
            writer.write("=".repeat(60) + "\n");
            
            OreScannerMod.LOGGER.info("Results exported to: {}", file.getAbsolutePath());
            
        } catch (IOException e) {
            OreScannerMod.LOGGER.error("Failed to export results", e);
        }
    }
}