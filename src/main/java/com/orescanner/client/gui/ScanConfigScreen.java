package com.orescanner.client.gui;

import com.orescanner.client.OreScannerClient;
import com.orescanner.scanner.BlockTargets;
import com.orescanner.scanner.ScanningTask;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class ScanConfigScreen extends Screen {
    private final BlockTargets targets = new BlockTargets();
    
    private SliderWidget chunkRadiusSlider;
    private SliderWidget blocksPerSecSlider;
    private TextFieldWidget minYField;
    private TextFieldWidget maxYField;
    private int scrollOffset = 0;
    
    public ScanConfigScreen() {
        super(Text.literal("Ore Scanner Configuration"));
    }
    
    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 30;
        
        // --- Sliders ---
        this.chunkRadiusSlider = new SliderWidget(
            centerX - 150, startY, 300, 20,
            Text.literal("Bán kính: "), Text.literal(" chunks"),
            1, 32, 8, this.textRenderer
        );
        this.addDrawableChild(chunkRadiusSlider);
        startY += 30;
        
        this.blocksPerSecSlider = new SliderWidget(
            centerX - 150, startY, 300, 20,
            Text.literal("Tốc độ: "), Text.literal(" blocks/s"),
            1000, 500000, 100000, this.textRenderer
        );
        this.addDrawableChild(blocksPerSecSlider);
        startY += 40;
        
        // --- Y Inputs ---
        this.minYField = new TextFieldWidget(this.textRenderer, centerX - 150, startY, 140, 20, Text.literal("Min Y"));
        this.minYField.setText("-64");
        this.addDrawableChild(minYField);
        
        this.maxYField = new TextFieldWidget(this.textRenderer, centerX + 10, startY, 140, 20, Text.literal("Max Y"));
        this.maxYField.setText("320");
        this.addDrawableChild(maxYField);
        startY += 30;
        
        // --- Selection Buttons ---
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Chọn tất cả"), btn -> targets.selectAll())
            .dimensions(centerX - 150, startY, 95, 20).build());
            
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Bỏ chọn hết"), btn -> targets.clearAll())
            .dimensions(centerX - 50, startY, 95, 20).build());
            
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Diamond Only"), btn -> {
            targets.clearAll();
            targets.toggleBlock(net.minecraft.block.Blocks.DIAMOND_ORE);
            targets.toggleBlock(net.minecraft.block.Blocks.DEEPSLATE_DIAMOND_ORE);
        }).dimensions(centerX + 50, startY, 100, 20).build());
        
        // --- Start & Clear Buttons ---
        int bottomY = this.height - 40;
        
        this.addDrawableChild(ButtonWidget.builder(Text.literal("§a§lBẮT ĐẦU QUÉT"), btn -> startScan())
            .dimensions(centerX - 155, bottomY, 150, 30).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("§cẨn Kết Quả"), btn -> {
            OreScannerClient.clearTask();
            this.close();
            if (client.player != null) client.player.sendMessage(Text.literal("§e[Ore Scanner] Đã ẩn HUD."), false);
        }).dimensions(centerX + 5, bottomY, 150, 30).build());
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "§7Min Y:", this.width / 2 - 150, 95, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "§7Max Y:", this.width / 2 + 10, 95, 0xFFFFFF);
        renderBlockCheckboxes(context, mouseX, mouseY);
        super.render(context, mouseX, mouseY, delta);
    }
    
    private void renderBlockCheckboxes(DrawContext context, int mouseX, int mouseY) {
        int startX = this.width / 2 - 150;
        int startY = 150;
        int y = startY - scrollOffset;
        
        context.enableScissor(startX - 5, startY - 5, startX + 305, this.height - 50);
        
        y = drawCategory(context, "§6§lKHOÁNG SẢN:", BlockTargets.ALL_ORES, startX, y, mouseX, mouseY);
        y = drawCategory(context, "§c§lSPAWNERS & KHÁC:", BlockTargets.ALL_SPAWNERS, startX, y, mouseX, mouseY);
        
        context.disableScissor();
    }
    
    private int drawCategory(DrawContext context, String title, java.util.Map<String, net.minecraft.block.Block> items, int x, int y, int mx, int my) {
        context.drawTextWithShadow(this.textRenderer, title, x, y, 0xFFFFFF);
        y += 15;
        for (var entry : items.entrySet()) {
            if (entry.getValue() == null) continue;
            boolean selected = targets.isSelected(entry.getValue());
            int color = selected ? 0x00FF00 : 0x888888;
            String checkbox = selected ? "[§a✓§r] " : "[ ] ";
            
            context.drawTextWithShadow(this.textRenderer, checkbox + entry.getKey(), x + 10, y, color);
            
            if (mx >= x && mx <= x + 300 && my >= y && my <= y + 10) {
                context.fill(x, y - 1, x + 300, y + 11, 0x33FFFFFF);
            }
            y += 12;
        }
        return y + 10;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int startX = this.width / 2 - 150;
        int startY = 150 - scrollOffset;
        
        if (checkClickCategory(BlockTargets.ALL_ORES, startX, startY + 15, mouseX, mouseY)) return true;
        int oresHeight = BlockTargets.ALL_ORES.size() * 12 + 25;
        if (checkClickCategory(BlockTargets.ALL_SPAWNERS, startX, startY + 15 + oresHeight, mouseX, mouseY)) return true;
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    private boolean checkClickCategory(java.util.Map<String, net.minecraft.block.Block> items, int x, int startY, double mx, double my) {
        int y = startY;
        for (var entry : items.entrySet()) {
            if (entry.getValue() == null) continue;
            if (mx >= x && mx <= x + 300 && my >= y && my <= y + 10) {
                targets.toggleBlock(entry.getValue());
                if (client != null) client.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.master(net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
            y += 12;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollOffset -= (int) (verticalAmount * 15);
        scrollOffset = Math.max(0, Math.min(scrollOffset, 500));
        return true;
    }
    
    private void startScan() {
        if (targets.getSelectedCount() == 0) {
            if (client != null && client.player != null) {
                client.player.sendMessage(Text.literal("§c§l[ERROR] Bạn chưa chọn quặng nào!"), false);
            }
            return;
        }
        
        ScanningTask task = new ScanningTask(
            targets.getSelectedBlocks(),
            chunkRadiusSlider.getValueInt(),
            parseIntSafe(minYField.getText(), -64),
            parseIntSafe(maxYField.getText(), 320),
            blocksPerSecSlider.getValueInt()
        );
        
        if (client != null) {
            task.initialize(client);
            OreScannerClient.startScan(task);
            this.close();
        }
    }
    
    private int parseIntSafe(String text, int def) {
        try { return Integer.parseInt(text); } catch (Exception e) { return def; }
    }
}
