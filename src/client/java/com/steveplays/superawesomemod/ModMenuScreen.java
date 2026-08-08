package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Main mod menu screen. Buttons flow left-to-right in rows with a search bar
 * at the top-left. Matching buttons are highlighted with a white outline.
 * Open with the G keybind (rebindable in Options → Controls → SuperAwesomeMod).
 */
public class ModMenuScreen extends Screen {

    private EditBox searchField;
    private final List<FeatureButton> featureButtons = new ArrayList<>();

    private static class FeatureButton {
        final String label;
        final Button button;

        FeatureButton(String label, Button button) {
            this.label = label;
            this.button = button;
        }
    }

    public ModMenuScreen() {
        super(Component.literal("SuperAwesomeMod"));
    }

    @Override
    protected void init() {
        featureButtons.clear();

        int padding = 10;
        int btnW = 140;
        int btnH = 20;
        int gapX = 4;
        int gapY = 4;

        // --- Search bar at top-left ---
        searchField = new EditBox(this.font, padding, padding, 200, btnH,
                Component.literal("Search"));
        searchField.setHint(Component.literal("Search..."));
        searchField.setMaxLength(50);
        this.addRenderableWidget(searchField);

        // --- Buttons start below the search bar ---
        int startY = padding + btnH + gapY + 4;
        int availableWidth = this.width - 2 * padding;
        int buttonsPerRow = Math.max(1, (availableWidth + gapX) / (btnW + gapX));

        // Enable All
        addFeature("Enable All", buttonsPerRow, btnW, btnH, gapX, gapY, padding, startY,
                btn -> {
                    FreeLookData.setEnabled(true);
                    ArmorHudData.setEnabled(true);
                    ArmorHudData.setScale(1.3f);
                    CombatHitboxData.setEnabled(true);
                    CombatHitboxData.setInRangeColor(3);    // Green
                    CombatHitboxData.setOutOfRangeColor(9); // Pink
                    CombatCrosshairData.setEnabled(true);
                    CombatCrosshairData.setInRangeColor(3);    // Green
                    CombatCrosshairData.setOutOfRangeColor(9); // Pink
                    CombatPotionEffectsData.setEnabled(true);
                    AppleSkinData.setEnabled(true);
                    ShulkerTooltipData.setEnabled(true);
                    OldPvpData.setSwingWhileUsingEnabled(true);
                    MiniMapData.setEnabled(true);
                    MiniMapData.setHudVisible(false); // corner map off
                    MiniMapData.setMinimapSize(64);
                    MiniMapData.setCorner(3);
                    NametagData.setEnabled(true);
                    MotionBlurData.setEnabled(true);
                    MotionBlurData.setStrength(3);
                    HealthIndicatorData.setEnabled(true);
                    HigherCrouchData.setEnabled(false); // higher crouch off
                    CpsData.setEnabled(true);
                    CpsData.setScale(5);
                    CpsData.setOffset(30); // slider slightly more to the right
                    KeystrokesData.setEnabled(true);
                    KeystrokesData.setScale(4); // size 4 instead of 5
                    KeystrokesData.setCorner(3);
                    NoFogData.setEnabled(true);
                    FlightData.setEnabled(false); // flight off
                    NoFallData.setEnabled(false);
                });

        // Feature buttons
        addFeature("Free Look",             buttonsPerRow, btnW, btnH, gapX, gapY, padding, startY, btn -> this.minecraft.setScreen(new FreeLookScreen(this)));
        addFeature("Armor HUD",             buttonsPerRow, btnW, btnH, gapX, gapY, padding, startY, btn -> this.minecraft.setScreen(new ArmorHudScreen(this)));
        addFeature("Combat Hitboxes",       buttonsPerRow, btnW, btnH, gapX, gapY, padding, startY, btn -> this.minecraft.setScreen(new CombatHitboxScreen(this)));
        addFeature("Combat Crosshair",      buttonsPerRow, btnW, btnH, gapX, gapY, padding, startY, btn -> this.minecraft.setScreen(new CombatCrosshairScreen(this)));
        addFeature("Combat Potion Effects", buttonsPerRow, btnW, btnH, gapX, gapY, padding, startY, btn -> this.minecraft.setScreen(new CombatPotionEffectsScreen(this)));
        addFeature("PvP Cheat Detector",    buttonsPerRow, btnW, btnH, gapX, gapY, padding, startY, btn -> this.minecraft.setScreen(new PvpDetectorScreen(this)));
        addFeature("Autoclicker",           buttonsPerRow, btnW, btnH, gapX, gapY, padding, startY, btn -> this.minecraft.setScreen(new AutoclickerScreen(this)));
        addFeature("Freecam",               buttonsPerRow, btnW, btnH, gapX, gapY, padding, startY, btn -> this.minecraft.setScreen(new FreecamScreen(this)));
        addFeature("Flight",                buttonsPerRow, btnW, btnH, gapX, gapY, padding, startY, btn -> this.minecraft.setScreen(new FlightScreen(this)));
        addFeature("AppleSkin",             buttonsPerRow, btnW, btnH, gapX, gapY, padding, startY, btn -> this.minecraft.setScreen(new AppleSkinScreen(this)));
        addFeature("Shulker Tooltips",      buttonsPerRow, btnW, btnH, gapX, gapY, padding, startY, btn -> this.minecraft.setScreen(new ShulkerTooltipScreen(this)));
        addFeature("Render Distance",       buttonsPerRow, btnW, btnH, gapX, gapY, padding, startY, btn -> this.minecraft.setScreen(new RenderDistanceScreen(this)));
        addFeature("Item Physics",          buttonsPerRow, btnW, btnH, gapX, gapY, padding, startY, btn -> this.minecraft.setScreen(new ItemPhysicsScreen(this)));
        addFeature("1.7 PvP Animations",   buttonsPerRow, btnW, btnH, gapX, gapY, padding, startY, btn -> this.minecraft.setScreen(new OldPvpScreen(this)));
        addFeature("X-Ray",                buttonsPerRow, btnW, btnH, gapX, gapY, padding, startY, btn -> this.minecraft.setScreen(new XrayScreen(this)));
        addFeature("Mini Map",             buttonsPerRow, btnW, btnH, gapX, gapY, padding, startY, btn -> this.minecraft.setScreen(new MiniMapScreen(this)));
        addFeature("Motion Blur",          buttonsPerRow, btnW, btnH, gapX, gapY, padding, startY, btn -> this.minecraft.setScreen(new MotionBlurScreen(this)));
        addFeature("Health Indicators",    buttonsPerRow, btnW, btnH, gapX, gapY, padding, startY, btn -> this.minecraft.setScreen(new HealthIndicatorScreen(this)));
        addFeature("Nametag",              buttonsPerRow, btnW, btnH, gapX, gapY, padding, startY, btn -> this.minecraft.setScreen(new NametagScreen(this)));
        addFeature("Min Ping",             buttonsPerRow, btnW, btnH, gapX, gapY, padding, startY, btn -> this.minecraft.setScreen(new MinPingScreen(this)));
        addFeature("Higher Crouch",        buttonsPerRow, btnW, btnH, gapX, gapY, padding, startY, btn -> this.minecraft.setScreen(new HigherCrouchScreen(this)));
        addFeature("Farther Players",      buttonsPerRow, btnW, btnH, gapX, gapY, padding, startY, btn -> this.minecraft.setScreen(new FartherPlayersScreen(this)));
        addFeature("CPS Counter",          buttonsPerRow, btnW, btnH, gapX, gapY, padding, startY, btn -> this.minecraft.setScreen(new CpsScreen(this)));
        addFeature("Keystrokes",           buttonsPerRow, btnW, btnH, gapX, gapY, padding, startY, btn -> this.minecraft.setScreen(new KeystrokesScreen(this)));
        addFeature("No Fog",              buttonsPerRow, btnW, btnH, gapX, gapY, padding, startY, btn -> this.minecraft.setScreen(new NoFogScreen(this)));
        addFeature("No Fall",             buttonsPerRow, btnW, btnH, gapX, gapY, padding, startY, btn -> this.minecraft.setScreen(new NoFallScreen(this)));
        addFeature("Boat Fly",            buttonsPerRow, btnW, btnH, gapX, gapY, padding, startY, btn -> this.minecraft.setScreen(new BoatFlyScreen(this)));
        addFeature("XP Multiplier",       buttonsPerRow, btnW, btnH, gapX, gapY, padding, startY, btn -> this.minecraft.setScreen(new XpMultiplierScreen(this)));
        addFeature("Potion Saver",        buttonsPerRow, btnW, btnH, gapX, gapY, padding, startY, btn -> this.minecraft.setScreen(new PotionSaverScreen(this)));

        // Close button
        addFeature("Close", buttonsPerRow, btnW, btnH, gapX, gapY, padding, startY, btn -> this.onClose());
    }

    private void addFeature(String label, int buttonsPerRow, int btnW, int btnH,
                            int gapX, int gapY, int padding, int startY,
                            Button.OnPress action) {
        int index = featureButtons.size();
        int col = index % buttonsPerRow;
        int row = index / buttonsPerRow;
        int x = padding + col * (btnW + gapX);
        int y = startY + row * (btnH + gapY);

        Button btn = Button.builder(Component.literal(label), action)
                .bounds(x, y, btnW, btnH).build();
        this.addRenderableWidget(btn);
        featureButtons.add(new FeatureButton(label, btn));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Draw a fully opaque background so the menu always covers F3 and other overlays.
        graphics.fill(0, 0, this.width, this.height, 0xFF000000);
        super.render(graphics, mouseX, mouseY, delta);

        // Draw white outline around buttons that match the search query
        String query = searchField.getValue().toLowerCase().trim();
        if (!query.isEmpty()) {
            for (FeatureButton fb : featureButtons) {
                if (fb.label.toLowerCase().contains(query)) {
                    int x = fb.button.getX() - 2;
                    int y = fb.button.getY() - 2;
                    int w = fb.button.getWidth() + 4;
                    int h = fb.button.getHeight() + 4;
                    // White outline (2px border via nested outlines)
                    graphics.renderOutline(x, y, w, h, 0xFFFFFFFF);
                    graphics.renderOutline(x + 1, y + 1, w - 2, h - 2, 0xFFFFFFFF);
                }
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
