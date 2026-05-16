package com.steveplays.superawesomemod;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.LinkedHashMap;
import java.util.Map;

public final class MiniMapLegend {

    private MiniMapLegend() {}

    public static final Map<String, Integer> ENTRIES = new LinkedHashMap<>();

    static {
        ENTRIES.put("Grass", 0xFF7FB238);
        ENTRIES.put("Water", 0xFF4040FF);
        ENTRIES.put("Stone", 0xFF707070);
        ENTRIES.put("Sand", 0xFFF7E9A3);
        ENTRIES.put("Wood", 0xFF8F7748);
        ENTRIES.put("Snow/Ice", 0xFFFFFFFF);
        ENTRIES.put("Dirt", 0xFF976D4D);
        ENTRIES.put("Leaves", 0xFF007C00);
        ENTRIES.put("Deepslate", 0xFF646464);
        ENTRIES.put("Lava", 0xFFFF0000);
        ENTRIES.put("Unexplored", 0xFF000000);
    }

    public static void render(GuiGraphics graphics, Font font, int x, int y) {
        int rowH = 14;
        int swatchSize = 10;
        int padding = 4;

        // Background panel
        int panelW = 100;
        int panelH = padding * 2 + ENTRIES.size() * rowH;
        graphics.fill(x, y, x + panelW, y + panelH, 0xC0000000);

        // Title
        graphics.drawString(font, "Legend", x + padding, y + padding, 0xFFFFFF, false);

        int row = 0;
        for (Map.Entry<String, Integer> entry : ENTRIES.entrySet()) {
            int ry = y + padding + 12 + row * rowH;
            // Color swatch
            graphics.fill(x + padding, ry, x + padding + swatchSize, ry + swatchSize, entry.getValue());
            // Border around swatch
            graphics.fill(x + padding, ry, x + padding + swatchSize, ry + 1, 0xFF888888);
            graphics.fill(x + padding, ry + swatchSize - 1, x + padding + swatchSize, ry + swatchSize, 0xFF888888);
            graphics.fill(x + padding, ry, x + padding + 1, ry + swatchSize, 0xFF888888);
            graphics.fill(x + padding + swatchSize - 1, ry, x + padding + swatchSize, ry + swatchSize, 0xFF888888);
            // Label
            graphics.drawString(font, entry.getKey(), x + padding + swatchSize + 4, ry + 1, 0xCCCCCC, false);
            row++;
        }
    }

    public static int getPanelHeight() {
        return 4 * 2 + 12 + ENTRIES.size() * 14;
    }
}
