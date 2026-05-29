package com.steveplays.superawesomemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Scrollable file browser listing {@code .litematic} files.
 * Follows the same pattern as {@link XrayScreen}.
 */
public class SchematicBrowserScreen extends Screen {

    private final Screen parent;
    private EditBox searchBox;
    private String searchText = "";
    private int scrollOffset = 0;
    private List<Path> filteredFiles = new ArrayList<>();
    private List<Path> allFiles = new ArrayList<>();

    private static final int BTN_W = 200;
    private static final int BTN_H = 20;
    private static final int ROW_HEIGHT = 24;
    private static final int VISIBLE_ROWS = 6;

    public SchematicBrowserScreen(Screen parent) {
        super(Component.literal("Browse Schematics"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int topY = this.height / 2 - 90;

        // Scan for files
        scanFiles();

        // Search box
        searchBox = new EditBox(this.font, cx - BTN_W / 2, topY, BTN_W, BTN_H,
            Component.literal("Search"));
        searchBox.setHint(Component.literal("Search schematics..."));
        searchBox.setValue(searchText);
        searchBox.setResponder(text -> {
            searchText = text;
            scrollOffset = 0;
            rebuildFileList();
            this.rebuildWidgets();
        });
        this.addRenderableWidget(searchBox);

        // Filter based on search
        rebuildFileList();

        // File buttons (scrollable)
        int listStartY = topY + 28;
        int end = Math.min(scrollOffset + VISIBLE_ROWS, filteredFiles.size());
        for (int i = scrollOffset; i < end; i++) {
            Path file = filteredFiles.get(i);
            String fileName = file.getFileName().toString();
            String displayName = fileName.length() > 30
                ? fileName.substring(0, 27) + "..."
                : fileName;
            int rowY = listStartY + (i - scrollOffset) * ROW_HEIGHT;

            this.addRenderableWidget(Button.builder(
                Component.literal(displayName),
                btn -> loadSchematic(file)
            ).bounds(cx - BTN_W / 2, rowY, BTN_W, BTN_H).build());
        }

        // Back button
        int backY = listStartY + VISIBLE_ROWS * ROW_HEIGHT + 8;
        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, backY, 100, BTN_H).build());
    }

    private void scanFiles() {
        allFiles.clear();
        Path dir = getSchematicsDir();
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                SuperAwesomeMod.LOGGER.warn("[Schematic] Failed to create schematics directory", e);
                return;
            }
        }

        try (Stream<Path> stream = Files.list(dir)) {
            stream.filter(p -> p.toString().endsWith(".litematic"))
                  .sorted()
                  .forEach(allFiles::add);
        } catch (IOException e) {
            SuperAwesomeMod.LOGGER.warn("[Schematic] Failed to list schematics", e);
        }
    }

    private void rebuildFileList() {
        String query = searchText.toLowerCase().trim();
        filteredFiles.clear();
        for (Path file : allFiles) {
            String name = file.getFileName().toString().toLowerCase();
            if (query.isEmpty() || name.contains(query)) {
                filteredFiles.add(file);
            }
        }
    }

    private void loadSchematic(Path file) {
        try {
            Schematic schematic = SchematicFormat.load(file);
            Minecraft mc = Minecraft.getInstance();
            BlockPos origin = mc.player != null
                ? mc.player.blockPosition()
                : BlockPos.ZERO;
            SchematicData.setCurrentPlacement(new SchematicPlacement(schematic, origin));
            SchematicData.setEnabled(true);
            SchematicData.setCurrentLayer(0);
            SchematicVerifier.clearCache();
            SuperAwesomeMod.LOGGER.info("[Schematic] Loaded '{}' ({} blocks)",
                schematic.getName(), schematic.getTotalBlocks());
            this.minecraft.setScreen(this.parent);
        } catch (IOException e) {
            SuperAwesomeMod.LOGGER.error("[Schematic] Failed to load: {}", file, e);
        }
    }

    private static Path getSchematicsDir() {
        return Minecraft.getInstance().gameDirectory.toPath()
            .resolve("superawesomemod")
            .resolve("schematics");
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY,
                                  double scrollX, double scrollY) {
        int maxOffset = Math.max(0, filteredFiles.size() - VISIBLE_ROWS);
        scrollOffset = Math.clamp((long) (scrollOffset - (int) scrollY), 0, maxOffset);
        this.rebuildWidgets();
        return true;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        int cx = this.width / 2;
        int topY = this.height / 2 - 90;

        graphics.drawCenteredString(this.font, this.title, cx, topY - 20, 0xFFFFFF);

        String dirHint = "Place .litematic files in /superawesomemod/schematics/";
        graphics.drawCenteredString(this.font, Component.literal(dirHint), cx, topY - 8, 0xAAAAAA);

        // Scroll indicator
        if (filteredFiles.size() > VISIBLE_ROWS) {
            int listStartY = topY + 28;
            String scrollText = (scrollOffset + 1) + "-"
                + Math.min(scrollOffset + VISIBLE_ROWS, filteredFiles.size())
                + " of " + filteredFiles.size();
            graphics.drawCenteredString(this.font,
                Component.literal(scrollText),
                cx, listStartY + VISIBLE_ROWS * ROW_HEIGHT - 6, 0x888888);
        }

        if (filteredFiles.isEmpty()) {
            int listStartY = topY + 28;
            graphics.drawCenteredString(this.font,
                Component.literal("No schematics found"),
                cx, listStartY + 40, 0xFF5555);
        }

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
