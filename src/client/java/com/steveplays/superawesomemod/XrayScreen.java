package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class XrayScreen extends Screen {

    private final Screen parent;
    private EditBox searchBox;
    private String searchText = "";
    private int scrollOffset = 0;
    private List<XrayOreEntry> filteredOres = new ArrayList<>();

    private static final int BTN_W = 200;
    private static final int BTN_H = 20;
    private static final int ROW_HEIGHT = 24;
    private static final int VISIBLE_ROWS = 6;

    public XrayScreen(Screen parent) {
        super(Component.literal("X-Ray"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int topY = this.height / 2 - 90;

        // Master toggle
        this.addRenderableWidget(Button.builder(
            toggleLabel(),
            btn -> {
                XrayData.setEnabled(!XrayData.isEnabled());
                btn.setMessage(toggleLabel());
            }
        ).bounds(cx - BTN_W / 2, topY, BTN_W, BTN_H).build());

        // Search box
        searchBox = new EditBox(this.font, cx - BTN_W / 2, topY + 28, BTN_W, BTN_H,
            Component.literal("Search"));
        searchBox.setHint(Component.literal("Search ores..."));
        searchBox.setValue(searchText);
        searchBox.setResponder(text -> {
            searchText = text;
            scrollOffset = 0;
            rebuildOreList();
            this.rebuildWidgets();
        });
        this.addRenderableWidget(searchBox);

        // Filter ores based on current search
        rebuildOreList();

        // Ore toggle buttons (scrollable region)
        int listStartY = topY + 56;
        int end = Math.min(scrollOffset + VISIBLE_ROWS, filteredOres.size());
        for (int i = scrollOffset; i < end; i++) {
            XrayOreEntry ore = filteredOres.get(i);
            int rowY = listStartY + (i - scrollOffset) * ROW_HEIGHT;

            this.addRenderableWidget(Button.builder(
                oreLabel(ore),
                btn -> {
                    XrayData.toggleOre(ore.name());
                    btn.setMessage(oreLabel(ore));
                }
            ).bounds(cx - BTN_W / 2, rowY, BTN_W, BTN_H).build());
        }

        // Back button (fixed position below scroll area)
        int backY = listStartY + VISIBLE_ROWS * ROW_HEIGHT + 8;
        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, backY, 100, BTN_H).build());
    }

    private void rebuildOreList() {
        String query = searchText.toLowerCase().trim();
        filteredOres.clear();
        for (XrayOreEntry ore : XrayOreEntry.ALL) {
            if (query.isEmpty() || ore.name().toLowerCase().contains(query)) {
                filteredOres.add(ore);
            }
        }
    }

    private Component toggleLabel() {
        return Component.literal(XrayData.isEnabled()
            ? "X-Ray: Enabled" : "X-Ray: Disabled");
    }

    private Component oreLabel(XrayOreEntry ore) {
        boolean on = XrayData.isOreEnabled(ore.name());
        String prefix = on ? "[x] " : "[ ] ";
        String query = searchText.toLowerCase().trim();
        if (!query.isEmpty()) {
            return Component.literal(prefix + ore.name()).withStyle(s -> s.withColor(0xFFFF55));
        }
        return Component.literal(prefix + ore.name());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY,
                                  double scrollX, double scrollY) {
        int maxOffset = Math.max(0, filteredOres.size() - VISIBLE_ROWS);
        scrollOffset = Math.clamp((long) (scrollOffset - (int) scrollY), 0, maxOffset);
        this.rebuildWidgets();
        return true;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        int cx = this.width / 2;
        int topY = this.height / 2 - 90;

        // Title and subtitle
        graphics.drawCenteredString(this.font, this.title, cx, topY - 20, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
            Component.literal("Highlight ores through walls"),
            cx, topY - 8, 0xAAAAAA);

        // Draw color indicators next to each visible ore button
        int listStartY = topY + 56;
        int end = Math.min(scrollOffset + VISIBLE_ROWS, filteredOres.size());
        for (int i = scrollOffset; i < end; i++) {
            XrayOreEntry ore = filteredOres.get(i);
            int rowY = listStartY + (i - scrollOffset) * ROW_HEIGHT;
            int indicatorX = cx + BTN_W / 2 + 4;
            graphics.fill(indicatorX, rowY + 2, indicatorX + 16, rowY + 18,
                0xFF000000 | ore.color());
        }

        // Scroll indicator
        if (filteredOres.size() > VISIBLE_ROWS) {
            String scrollText = (scrollOffset + 1) + "-"
                + Math.min(scrollOffset + VISIBLE_ROWS, filteredOres.size())
                + " of " + filteredOres.size();
            graphics.drawCenteredString(this.font,
                Component.literal(scrollText),
                cx, listStartY + VISIBLE_ROWS * ROW_HEIGHT - 6, 0x888888);
        }

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
