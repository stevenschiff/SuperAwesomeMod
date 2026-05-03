package com.steveplays.superawesomemod;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PvpDetectorScreen extends Screen {

    private final Screen parent;

    private static final int LEGEND_LABEL_W = 60;

    private record LegendRow(String label, String oneLine, List<Component> tooltip) {}

    private static List<Component> lines(String... ls) {
        List<Component> out = new ArrayList<>(ls.length);
        for (String s : ls) out.add(Component.literal(s));
        return out;
    }

    private static final LegendRow[] LEGEND = new LegendRow[] {
        new LegendRow(
            "Ping",
            "server latency for that player (ms)",
            lines(
                "Round-trip latency between the server and that player.",
                "Green <100ms, yellow <250ms, red beyond.",
                "High ping dampens the suspicion score, since lag can",
                "make a legitimate hit look like extra reach.")),
        new LegendRow(
            "H:",
            "hits landed in the last 60 seconds",
            lines(
                "Number of attacks this player has landed (on anyone)",
                "inside a rolling 60-second window. Old hits expire",
                "and drop out automatically.")),
        new LegendRow(
            "Sus:",
            "times this player was flagged this session",
            lines(
                "Cumulative count of attacks this player has landed",
                "this session that scored above zero suspicion —",
                "long reach (>3.5 blocks) or through-wall hits.",
                "",
                "Unlike H: and Max:, this counter does NOT decay with",
                "the 60-second window. It's the at-a-glance answer to",
                "\"how many times has this user been suspected of",
                "cheating?\". Color-coded: gray=0, yellow 1-2,",
                "orange 3-5, red 6+.",
                "",
                "Use the Reset Stats button to zero it out.")),
        new LegendRow(
            "Max:",
            "longest reach distance observed (blocks)",
            lines(
                "Greatest hit distance recorded for this player, in blocks,",
                "measured from their eye to the nearest point of the",
                "victim's hitbox.",
                "",
                "Vanilla reach is 3.0. Anything past ~3.5 starts adding",
                "to the suspicion score. 5.5+ is almost certainly cheating.")),
        new LegendRow(
            "Band",
            "suspicion level: NONE / LOW / MED / HIGH",
            lines(
                "Rolling suspicion score over the last 60 seconds:",
                "  gray   NONE — score <5    (clean)",
                "  yellow LOW  — score 5-15  (one-off odd hit)",
                "  orange MED  — score 15-30 (sustained reach)",
                "  red    HIGH — score 30+   (likely cheating)",
                "",
                "Through-wall hits add +5. High ping dampens the score."))
    };

    public PvpDetectorScreen(Screen parent) {
        super(Component.literal("PvP Cheat Detector"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx   = this.width / 2;
        int cy   = this.height / 2;
        int btnW = 200;
        int btnH = 20;

        Button toggle = Button.builder(
            toggleLabel(),
            btn -> {
                PvpDetectorData.setEnabled(!PvpDetectorData.isEnabled());
                btn.setMessage(toggleLabel());
            }
        ).bounds(cx - btnW / 2, cy - 30, btnW, btnH).build();
        toggle.setTooltip(Tooltip.create(Component.literal(
            "Turn the top-right HUD on or off. When on, every PvP hit you\n"
            + "witness is recorded for 60 seconds and scored for suspicion.")));
        this.addRenderableWidget(toggle);

        Button reset = Button.builder(
            Component.literal("Reset Stats"),
            btn -> PvpDetectorTracker.clear()
        ).bounds(cx - btnW / 2, cy, btnW, btnH).build();
        reset.setTooltip(Tooltip.create(Component.literal(
            "Drop all currently-tracked players and their hit history.\n"
            + "Useful after a fight or when changing servers.")));
        this.addRenderableWidget(reset);

        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(cx - 50, cy + 30, 100, btnH).build());
    }

    private Component toggleLabel() {
        return Component.literal(PvpDetectorData.isEnabled()
            ? "PvP Detector: Enabled"
            : "PvP Detector: Disabled");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        int cx = this.width / 2;
        int cy = this.height / 2;
        graphics.drawCenteredString(this.font, this.title, cx, cy - 80, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
            Component.literal("Watches other players' attack reach, ping, and line-of-sight."),
            cx, cy - 66, 0xAAAAAA);
        graphics.drawCenteredString(this.font,
            Component.literal("Heuristic — long reach + many hits raises the suspicion band."),
            cx, cy - 54, 0xAAAAAA);

        super.render(graphics, mouseX, mouseY, delta);

        // Legend below the buttons. Hover any row to see a longer tooltip.
        int legendW = 280;
        int legendX = cx - legendW / 2;
        int legendY = cy + 60;
        int rowH    = 11;

        graphics.drawString(this.font,
            Component.literal("HUD legend (hover for details):"),
            legendX, legendY, 0xCCCCCC, false);

        List<Component> hoverTooltip = null;
        int rowY = legendY + 14;
        for (LegendRow row : LEGEND) {
            graphics.drawString(this.font,
                Component.literal(row.label()),
                legendX, rowY, 0xFFFFFF, false);
            graphics.drawString(this.font,
                Component.literal(row.oneLine()),
                legendX + LEGEND_LABEL_W, rowY, 0xAAAAAA, false);

            if (mouseY >= rowY - 1 && mouseY < rowY + rowH - 1
                && mouseX >= legendX && mouseX < legendX + legendW) {
                hoverTooltip = row.tooltip();
            }
            rowY += rowH;
        }

        if (hoverTooltip != null) {
            graphics.setComponentTooltipForNextFrame(this.font, hoverTooltip, mouseX, mouseY);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
