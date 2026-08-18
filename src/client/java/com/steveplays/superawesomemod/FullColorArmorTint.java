package com.steveplays.superawesomemod;

/**
 * The tint in flight while one entity's armour is being submitted.
 *
 * <p>Armour is a separate model from the body and gets its colour from
 * {@code EquipmentLayerRenderer.getColorForLayer}, which is static and receives no
 * entity or render state. Handing it the value through a field works because the
 * chain from {@code HumanoidArmorLayer.submit} down to it is synchronous — verified
 * in the 1.21.11 source as submit -> renderArmorPiece -> renderLayers ->
 * getColorForLayer — even though this version submits the actual draw calls through a
 * deferred collector afterwards.
 *
 * <p>Plain static rather than a ThreadLocal: entity rendering runs on the render
 * thread alone, and this is set and cleared inside a single call on that thread.
 */
public final class FullColorArmorTint {

    private static int tint = 0;

    private FullColorArmorTint() {}

    public static void set(int argb) { tint = argb; }
    public static void clear()       { tint = 0; }
    public static int  get()         { return tint; }
}
