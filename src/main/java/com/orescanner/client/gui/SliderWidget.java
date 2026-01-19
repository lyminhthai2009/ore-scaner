package com.orescanner.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

/**
 * Custom slider widget
 * Hiển thị: [Label] [====|====] [Value] [Suffix]
 */
public class SliderWidget extends ClickableWidget {
    private final TextRenderer textRenderer;
    private final Text prefix;
    private final Text suffix;
    private final int minValue;
    private final int maxValue;
    private int value;
    
    private boolean dragging = false;
    
    public SliderWidget(int x, int y, int width, int height, Text prefix, Text suffix, int min, int max, int initial, TextRenderer textRenderer) {
        super(x, y, width, height, Text.empty());
        this.textRenderer = textRenderer;
        this.prefix = prefix;
        this.suffix = suffix;
        this.minValue = min;
        this.maxValue = max;
        this.value = initial;
        updateMessage();
    }
    
    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // Background
        context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xFF000000);
        context.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1, 0xFF444444);
        
        // Fill (progress)
        double progress = (double) (value - minValue) / (maxValue - minValue);
        int fillWidth = (int) ((this.width - 2) * progress);
        context.fill(this.getX() + 1, this.getY() + 1, this.getX() + 1 + fillWidth, this.getY() + this.height - 1, 0xFF00AA00);
        
        // Text - FIXED: Dùng textRenderer thay vì Matrix4f
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            this.getMessage(),
            this.getX() + this.width / 2,
            this.getY() + (this.height - 8) / 2,
            0xFFFFFF
        );
    }
    
    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        this.setValue(mouseX);
    }
    
    @Override
    public void onClick(double mouseX, double mouseY) {
        this.setValue(mouseX);
    }
    
    private void setValue(double mouseX) {
        double progress = (mouseX - this.getX()) / this.width;
        progress = Math.max(0, Math.min(1, progress));
        
        this.value = (int) (minValue + progress * (maxValue - minValue));
        updateMessage();
    }
    
    private void updateMessage() {
        String formattedValue;
        
        if (value >= 1000) {
            formattedValue = String.format("%,d", value);
        } else {
            formattedValue = String.valueOf(value);
        }
        
        this.setMessage(Text.literal(
            prefix.getString() + formattedValue + suffix.getString()
        ));
    }
    
    public int getValueInt() {
        return value;
    }
    
    @Override
    protected void appendClickableNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {
        builder.put(net.minecraft.client.gui.screen.narration.NarrationPart.TITLE, this.getMessage());
    }
}
