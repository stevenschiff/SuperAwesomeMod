package com.steveplays.superawesomemod;

import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The menu's view of every feature: what it's called, whether it's on, how to flip
 * it, and which screen holds its settings.
 *
 * <p>Centralised so the menu can draw a toggle row for anything without knowing which
 * {@code XData} class sits behind it. Adding a feature means one line here rather than
 * another hand-written button.
 */
public final class FeatureRegistry {

    /** One toggleable feature. {@code settings} may be null if it has none. */
    public record Entry(String name,
                        BooleanSupplier isOn,
                        Consumer<Boolean> setOn,
                        Function<Screen, Screen> settings) {

        public void toggle() {
            setOn.accept(!isOn.getAsBoolean());
        }
    }

    private static final Map<String, List<Entry>> BY_CATEGORY = new LinkedHashMap<>();

    private FeatureRegistry() {}

    private static void add(String category, String name,
                            BooleanSupplier isOn, Consumer<Boolean> setOn,
                            Function<Screen, Screen> settings) {
        BY_CATEGORY.computeIfAbsent(category, c -> new ArrayList<>())
                   .add(new Entry(name, isOn, setOn, settings));
    }

    static {
        add("Combat", "Combat Hitboxes",
            CombatHitboxData::isEnabled, CombatHitboxData::setEnabled, CombatHitboxScreen::new);
        add("Combat", "Combat Crosshair",
            CombatCrosshairData::isEnabled, CombatCrosshairData::setEnabled, CombatCrosshairScreen::new);
        add("Combat", "Potion Effects",
            CombatPotionEffectsData::isEnabled, CombatPotionEffectsData::setEnabled, CombatPotionEffectsScreen::new);
        add("Combat", "Cheat Detector",
            PvpDetectorData::isEnabled, PvpDetectorData::setEnabled, PvpDetectorScreen::new);
        // Two independent sub-toggles behind one row: on when either is live, and the
        // row flips both. The settings screen still separates them.
        add("Combat", "1.7 Animations",
            () -> OldPvpData.isBlockingEnabled() || OldPvpData.isSwingWhileUsingEnabled(),
            v -> { OldPvpData.setBlockingEnabled(v); OldPvpData.setSwingWhileUsingEnabled(v); },
            OldPvpScreen::new);
        add("Combat", "Shield",
            ShieldData::isEnabled, ShieldData::setEnabled, ShieldScreen::new);
        add("Combat", "Body Twist",
            BodyTwistData::isEnabled, BodyTwistData::setEnabled, BodyTwistScreen::new);

        add("Render", "Free Look",
            FreeLookData::isEnabled, FreeLookData::setEnabled, FreeLookScreen::new);
        add("Render", "Motion Blur",
            MotionBlurData::isEnabled, MotionBlurData::setEnabled, MotionBlurScreen::new);
        add("Render", "No Fog",
            NoFogData::isEnabled, NoFogData::setEnabled, NoFogScreen::new);
        add("Render", "Render Distance",
            RenderDistanceData::isEnabled, RenderDistanceData::setEnabled, RenderDistanceScreen::new);
        add("Render", "Item Physics",
            ItemPhysicsData::isEnabled, ItemPhysicsData::setEnabled, ItemPhysicsScreen::new);
        add("Render", "Nametag",
            NametagData::isEnabled, NametagData::setEnabled, NametagScreen::new);

        add("HUD", "Armor HUD",
            ArmorHudData::isEnabled, ArmorHudData::setEnabled, ArmorHudScreen::new);
        add("HUD", "AppleSkin",
            AppleSkinData::isEnabled, AppleSkinData::setEnabled, AppleSkinScreen::new);
        add("HUD", "CPS Counter",
            CpsData::isEnabled, CpsData::setEnabled, CpsScreen::new);
        add("HUD", "Keystrokes",
            KeystrokesData::isEnabled, KeystrokesData::setEnabled, KeystrokesScreen::new);
        add("HUD", "Min Ping",
            MinPingData::isEnabled, MinPingData::setEnabled, MinPingScreen::new);

        add("World", "Mini Map",
            MiniMapData::isEnabled, MiniMapData::setEnabled, MiniMapScreen::new);

        add("Other", "Shulker Tooltips",
            ShulkerTooltipData::isEnabled, ShulkerTooltipData::setEnabled, ShulkerTooltipScreen::new);
        add("Other", "Higher Crouch",
            HigherCrouchData::isEnabled, HigherCrouchData::setEnabled, HigherCrouchScreen::new);
    }

    public static List<String> categories() {
        return new ArrayList<>(BY_CATEGORY.keySet());
    }

    public static List<Entry> of(String category) {
        return BY_CATEGORY.getOrDefault(category, List.of());
    }

    /** Switches on the set Enable All has always configured. */
    public static void enableAll() {
        CombatHitboxData.setEnabled(true);
        CombatHitboxData.setInRangeColor(0);    // Red
        CombatHitboxData.setOutOfRangeColor(4); // Cyan
        CombatHitboxData.setFullColor(true);
        CombatCrosshairData.setEnabled(true);
        CombatCrosshairData.setInRangeColor(0);
        CombatCrosshairData.setOutOfRangeColor(4);
        CombatPotionEffectsData.setEnabled(true);
        AppleSkinData.setEnabled(true);
        ShulkerTooltipData.setEnabled(true);
        OldPvpData.setSwingWhileUsingEnabled(true);
        FreeLookData.setEnabled(true);
        ArmorHudData.setEnabled(true);
        ArmorHudData.setScale(1.3f);
        MiniMapData.setEnabled(true);
        MiniMapData.setHudVisible(false);
        MiniMapData.setMinimapSize(64);
        MiniMapData.setCorner(3);
        MiniMapData.setSpecificWaypoints(true);
        NametagData.setEnabled(true);
        MotionBlurData.setEnabled(true);
        MotionBlurData.setStrength(3);
        HigherCrouchData.setEnabled(false);
        CpsData.setEnabled(true);
        CpsData.setScale(5);
        CpsData.setOffset(30);
        KeystrokesData.setEnabled(true);
        KeystrokesData.setScale(4);
        KeystrokesData.setCorner(3);
        NoFogData.setEnabled(true);
        ShieldData.setEnabled(true);
        ShieldData.setTransparency(50);
        ShieldData.setUsedOffset(-15);
        ShieldData.setNotUsedOffset(-35);
        BodyTwistData.setEnabled(true);
        BodyTwistData.setLeadLimit(70);
        BodyTwistData.setSnapHeadRotation(true);
    }
}
