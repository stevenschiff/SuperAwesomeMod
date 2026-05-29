package com.steveplays.superawesomemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Scrollable material list showing blocks required for the loaded schematic.
 * Follows the same scrollable list pattern as {@link XrayScreen}.
 */
public class SchematicMaterialListScreen extends Screen {

    private final Screen parent;
    private EditBox searchBox;
    private String searchText = "";
    private int scrollOffset = 0;
    private List<MaterialEntry> filteredEntries = new ArrayList<>();
    private List<MaterialEntry> allEntries = new ArrayList<>();

    private static final int BTN_W = 260;
    private static final int ROW_HEIGHT = 20;
    private static final int VISIBLE_ROWS = 8;

    private record MaterialEntry(String name, Block block, int required, int inventory) {}

    public SchematicMaterialListScreen(Screen parent) {
        super(Component.literal("Material List"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int topY = this.height / 2 - 100;

        // Build material list from schematic
        buildMaterialList();

        // Search box
        searchBox = new EditBox(this.font, cx - BTN_W / 2, topY, BTN_W, 20,
            Component.literal("Search"));
        searchBox.setHint(Component.literal("Search blocks..."));
        searchBox.setValue(searchText);
        searchBox.setResponder(text -> {
            searchText = text;
            scrollOffset = 0;
            rebuildFilteredList();
            this.rebuildWidgets();
        });
        this.addRenderableWidget(searchBox);

        rebuildFilteredList();

        // Back button
        int listStartY = topY + 28;
        int backY = listStartY + VISIBLE_ROWS * ROW_HEIGHT + 8;
        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, backY, 100, 20).build());
    }

    private void buildMaterialList() {
        allEntries.clear();

        SchematicPlacement placement = SchematicData.getCurrentPlacement();
        if (placement == null) return;

        Map<BlockState, Integer> counts = placement.getSchematic().countBlocks();

        // Group by block (ignoring state variants for the count)
        Map<Block, Integer> blockCounts = new java.util.LinkedHashMap<>();
        for (var entry : counts.entrySet()) {
            Block block = entry.getKey().getBlock();
            blockCounts.merge(block, entry.getValue(), Integer::sum);
        }

        // Count player inventory
        Map<Block, Integer> inventoryCounts = new java.util.LinkedHashMap<>();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!stack.isEmpty()) {
                    Block block = Block.byItem(stack.getItem());
                    if (block != null) {
                        inventoryCounts.merge(block, stack.getCount(), Integer::sum);
                    }
                }
            }
        }

        for (var entry : blockCounts.entrySet()) {
            Block block = entry.getKey();
            String name = block.getName().getString();
            int required = entry.getValue();
            int inventory = inventoryCounts.getOrDefault(block, 0);
            allEntries.add(new MaterialEntry(name, block, required, inventory));
        }

        // Sort by required count descending
        allEntries.sort(Comparator.comparingInt(MaterialEntry::required).reversed());
    }

    private void rebuildFilteredList() {
        String query = searchText.toLowerCase().trim();
        filteredEntries.clear();
        for (MaterialEntry entry : allEntries) {
            if (query.isEmpty() || entry.name.toLowerCase().contains(query)) {
                filteredEntries.add(entry);
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY,
                                  double scrollX, double scrollY) {
        int maxOffset = Math.max(0, filteredEntries.size() - VISIBLE_ROWS);
        scrollOffset = Math.clamp((long) (scrollOffset - (int) scrollY), 0, maxOffset);
        this.rebuildWidgets();
        return true;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        int cx = this.width / 2;
        int topY = this.height / 2 - 100;

        graphics.drawCenteredString(this.font, this.title, cx, topY - 16, 0xFFFFFF);

        // Column headers
        int listStartY = topY + 28;
        int leftX = cx - BTN_W / 2;
        graphics.drawString(this.font, "Block", leftX, listStartY - 10, 0xAAAAAA);
        graphics.drawString(this.font, "Need", leftX + 160, listStartY - 10, 0xAAAAAA);
        graphics.drawString(this.font, "Have", leftX + 200, listStartY - 10, 0xAAAAAA);

        // Render material rows
        int end = Math.min(scrollOffset + VISIBLE_ROWS, filteredEntries.size());
        for (int i = scrollOffset; i < end; i++) {
            MaterialEntry entry = filteredEntries.get(i);
            int rowY = listStartY + (i - scrollOffset) * ROW_HEIGHT;

            // Truncate long names
            String displayName = entry.name;
            if (displayName.length() > 22) {
                displayName = displayName.substring(0, 19) + "...";
            }

            // Color: green if have enough, red if not
            int color = entry.inventory >= entry.required ? 0x55FF55 : 0xFF5555;

            graphics.drawString(this.font, displayName, leftX, rowY + 4, 0xFFFFFF);
            graphics.drawString(this.font, String.valueOf(entry.required), leftX + 160, rowY + 4, 0xFFFFFF);
            graphics.drawString(this.font, String.valueOf(entry.inventory), leftX + 200, rowY + 4, color);
        }

        // Scroll indicator
        if (filteredEntries.size() > VISIBLE_ROWS) {
            String scrollText = (scrollOffset + 1) + "-"
                + Math.min(scrollOffset + VISIBLE_ROWS, filteredEntries.size())
                + " of " + filteredEntries.size();
            graphics.drawCenteredString(this.font,
                Component.literal(scrollText),
                cx, listStartY + VISIBLE_ROWS * ROW_HEIGHT - 6, 0x888888);
        }

        if (filteredEntries.isEmpty()) {
            graphics.drawCenteredString(this.font,
                Component.literal("No blocks in schematic"),
                cx, listStartY + 40, 0xFF5555);
        }

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
