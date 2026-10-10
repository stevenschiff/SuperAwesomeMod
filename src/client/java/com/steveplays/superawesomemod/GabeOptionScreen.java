package com.steveplays.superawesomemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Shared chrome for every feature's settings screen, so a sub-menu looks like the
 * main menu rather than a stock Minecraft dialog.
 *
 * <p>Subclasses declare what they contain — toggles, sliders, cycles, notes — and this
 * handles panel, header, layout, hit-testing, dragging and the purple fade. That keeps
 * the twenty-odd settings screens from each re-implementing their own widget stack,
 * which is how they drifted out of step with the menu in the first place.
 */
public abstract class GabeOptionScreen extends Screen {

    private static final int PANEL_W  = 230;
    private static final int ROW_H    = 22;
    private static final int HEADER_H = 30;
    private static final int PAD      = 8;
    private static final int RADIUS   = 4;

    /** Fade progress per row label, kept between openings like the main menu's. */
    private static final Map<String, Float> ANIM = new HashMap<>();

    protected final Screen parent;
    private final String title;
    private final List<Option> options = new ArrayList<>();
    private int draggingSlider = -1;
    private long lastFrameNanos = 0L;

    private enum Kind { TOGGLE, SLIDER, CYCLE, ACTION, INFO }

    private static final class Option {
        Kind kind;
        String label;
        BooleanSupplier get;
        Consumer<Boolean> set;
        int min, max;
        IntSupplier iget;
        IntConsumer iset;
        IntFunction<String> fmt;
        Supplier<String> value;
        Runnable run;
        int color = GabeTheme.TEXT_DIM;
        int x, y, w, h;

        boolean hit(double mx, double my) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }
    }

    protected GabeOptionScreen(Screen parent, String title) {
        super(Component.literal(title));
        this.parent = parent;
        this.title = title;
    }

    /** Declare the screen's contents. Called fresh on every init. */
    protected abstract void buildOptions();

    @Override
    protected void init() {
        options.clear();
        buildOptions();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // --- declaration helpers -------------------------------------------

    protected void toggle(String label, BooleanSupplier get, Consumer<Boolean> set) {
        Option o = new Option();
        o.kind = Kind.TOGGLE; o.label = label; o.get = get; o.set = set;
        options.add(o);
    }

    protected void slider(String label, int min, int max,
                          IntSupplier get, IntConsumer set, IntFunction<String> fmt) {
        Option o = new Option();
        o.kind = Kind.SLIDER; o.label = label; o.min = min; o.max = max;
        o.iget = get; o.iset = set; o.fmt = fmt;
        options.add(o);
    }

    protected void cycle(String label, Supplier<String> value, Runnable next) {
        Option o = new Option();
        o.kind = Kind.CYCLE; o.label = label; o.value = value; o.run = next;
        options.add(o);
    }

    protected void action(String label, Runnable run) {
        Option o = new Option();
        o.kind = Kind.ACTION; o.label = label; o.run = run;
        options.add(o);
    }

    protected void info(String text) {
        Option o = new Option();
        o.kind = Kind.INFO; o.label = text;
        options.add(o);
    }

    protected void warn(String text) {
        Option o = new Option();
        o.kind = Kind.INFO; o.label = text; o.color = 0xFFFFAA00;
        options.add(o);
    }

    // --- layout and render ----------------------------------------------

    private int panelH() {
        return HEADER_H + options.size() * ROW_H + ROW_H + PAD * 2;
    }

    private int panelX() { return (this.width - PANEL_W) / 2; }
    private int panelY() { return Math.max(34, (this.height - panelH()) / 2); }

    private void layout() {
        int px = panelX();
        int y = panelY() + HEADER_H + PAD / 2;
        for (Option o : options) {
            o.x = px + PAD / 2;
            o.y = y;
            o.w = PANEL_W - PAD;
            o.h = ROW_H;
            y += ROW_H;
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        layout();

        long now = System.nanoTime();
        float dt = lastFrameNanos == 0L ? 0f : (now - lastFrameNanos) / 1_000_000_000f;
        lastFrameNanos = now;
        dt = Math.min(dt, 0.1f);

        int px = panelX();
        int py = panelY();

        String client = GabeTheme.CLIENT_NAME;
        g.drawString(this.font, client, (this.width - this.font.width(client)) / 2,
                Math.max(6, py - 22), GabeTheme.ACCENT_TEXT, true);

        GabeTheme.roundedRect(g, px, py, PANEL_W, panelH(), RADIUS, GabeTheme.PANEL);
        GabeTheme.roundedRect(g, px, py, PANEL_W, HEADER_H, RADIUS, GabeTheme.PANEL_HEADER);
        g.drawString(this.font, title, px + PAD, py + (HEADER_H - 8) / 2, GabeTheme.TEXT, false);
        g.drawString(this.font, "<", px + PANEL_W - PAD - 4, py + (HEADER_H - 8) / 2,
                GabeTheme.TEXT_FAINT, false);

        for (Option o : options) {
            boolean hovered = o.hit(mouseX, mouseY) && o.kind != Kind.INFO;
            int textY = o.y + (ROW_H - 8) / 2;

            switch (o.kind) {
                case TOGGLE -> {
                    float target = o.get.getAsBoolean() ? 1f : 0f;
                    String key = title + "/" + o.label;
                    float cur = GabeTheme.approach(ANIM.getOrDefault(key, target), target, dt, 14f);
                    ANIM.put(key, cur);

                    int bg = GabeTheme.blend(0x00000000, GabeTheme.ACCENT_SOFT, cur);
                    if (hovered) bg = GabeTheme.blend(bg, 0x33FFFFFF, 0.45f);
                    if (cur > 0.002f || hovered) {
                        GabeTheme.roundedRect(g, o.x, o.y + 1, o.w, ROW_H - 2, 3, bg);
                    }
                    int tc = GabeTheme.blend(GabeTheme.TEXT_DIM, GabeTheme.TEXT, cur);
                    g.drawString(this.font, o.label, o.x + PAD, textY, tc, false);
                    String state = o.get.getAsBoolean() ? "On" : "Off";
                    g.drawString(this.font, state,
                            o.x + o.w - PAD - this.font.width(state), textY, tc, false);
                }
                case SLIDER -> {
                    if (hovered) {
                        GabeTheme.roundedRect(g, o.x, o.y + 1, o.w, ROW_H - 2, 3, GabeTheme.ROW_HOVER);
                    }
                    String text = o.fmt.apply(o.iget.getAsInt());
                    g.drawString(this.font, o.label, o.x + PAD, o.y + 3, GabeTheme.TEXT, false);
                    g.drawString(this.font, text, o.x + o.w - PAD - this.font.width(text), o.y + 3,
                            GabeTheme.ACCENT_TEXT, false);

                    // Track sits under the label so the row stays one line tall.
                    int tx = o.x + PAD;
                    int tw = o.w - PAD * 2;
                    int ty = o.y + ROW_H - 6;
                    g.fill(tx, ty, tx + tw, ty + 2, 0x44FFFFFF);
                    float f = (o.max == o.min) ? 0f
                            : (o.iget.getAsInt() - o.min) / (float) (o.max - o.min);
                    int fill = Math.round(tw * Math.clamp(f, 0f, 1f));
                    g.fill(tx, ty, tx + fill, ty + 2, GabeTheme.ACCENT);
                    g.fill(tx + Math.max(0, fill - 1), ty - 2, tx + Math.min(tw, fill + 1), ty + 4,
                            GabeTheme.TEXT);
                }
                case CYCLE, ACTION -> {
                    if (hovered) {
                        GabeTheme.roundedRect(g, o.x, o.y + 1, o.w, ROW_H - 2, 3, GabeTheme.ROW_HOVER);
                    }
                    g.drawString(this.font, o.label, o.x + PAD, textY, GabeTheme.TEXT, false);
                    if (o.kind == Kind.CYCLE) {
                        String v = o.value.get();
                        g.drawString(this.font, v, o.x + o.w - PAD - this.font.width(v), textY,
                                GabeTheme.ACCENT_TEXT, false);
                    }
                }
                case INFO -> g.drawString(this.font, o.label, o.x + PAD, textY, o.color, false);
            }
        }

        // Back row at the foot of the panel.
        int backY = panelY() + HEADER_H + options.size() * ROW_H + PAD / 2;
        boolean backHover = mouseX >= px && mouseX < px + PANEL_W
                && mouseY >= backY && mouseY < backY + ROW_H;
        if (backHover) {
            GabeTheme.roundedRect(g, px + PAD / 2, backY + 1, PANEL_W - PAD, ROW_H - 2, 3,
                    GabeTheme.ROW_HOVER);
        }
        g.drawString(this.font, "Back", px + PAD + 4, backY + (ROW_H - 8) / 2,
                GabeTheme.TEXT_DIM, false);

        super.render(g, mouseX, mouseY, delta);
    }

    // --- input -----------------------------------------------------------

    private void applySlider(Option o, double mouseX) {
        int tx = o.x + PAD;
        int tw = o.w - PAD * 2;
        float f = (float) ((mouseX - tx) / Math.max(1, tw));
        int v = Math.round(o.min + Math.clamp(f, 0f, 1f) * (o.max - o.min));
        o.iset.accept(v);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            layout();
            double mx = event.x();
            double my = event.y();

            int px = panelX();
            int backY = panelY() + HEADER_H + options.size() * ROW_H + PAD / 2;
            if (mx >= px && mx < px + PANEL_W && my >= backY && my < backY + ROW_H) {
                this.minecraft.setScreen(parent);
                return true;
            }
            // Header doubles as a back button, matching the chevron drawn there.
            if (mx >= px && mx < px + PANEL_W && my >= panelY() && my < panelY() + HEADER_H) {
                this.minecraft.setScreen(parent);
                return true;
            }

            for (int i = 0; i < options.size(); i++) {
                Option o = options.get(i);
                if (!o.hit(mx, my)) continue;
                switch (o.kind) {
                    case TOGGLE -> o.set.accept(!o.get.getAsBoolean());
                    case CYCLE, ACTION -> o.run.run();
                    case SLIDER -> { draggingSlider = i; applySlider(o, mx); }
                    case INFO -> { }
                }
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (draggingSlider >= 0 && draggingSlider < options.size()) {
            layout();
            applySlider(options.get(draggingSlider), event.x());
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        draggingSlider = -1;
        return super.mouseReleased(event);
    }
}
