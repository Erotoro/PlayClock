package dev.maksg.playclock.mc261x.client;

import dev.maksg.playclock.core.config.PlayClockColorPalette;
import dev.maksg.playclock.core.model.TrackedTarget;
import dev.maksg.playclock.core.runtime.PlayClockRuntimeService;
import dev.maksg.playclock.core.runtime.PlayClockState;
import dev.maksg.playclock.core.stats.PlaytimeStats;
import dev.maksg.playclock.core.ui.HudFormatter;
import dev.maksg.playclock.core.ui.HudLayout;
import dev.maksg.playclock.core.ui.HudMetric;
import dev.maksg.playclock.core.ui.HudSnapshot;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

@SuppressWarnings("null")
final class PlayClock261xHudOverlay {
    private static final int MAX_TEXT_WIDTH = 200;
    private static final int LINE_HEIGHT = 10;
    private static final int STACKED_GAP = 1;
    private static final int SECTION_GAP = 3;
    private static final Identifier HUD_ID = Identifier.fromNamespaceAndPath("playclock", "hud");

    private final PlayClockRuntimeService runtimeService;

    PlayClock261xHudOverlay(PlayClockRuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    void register() {
        HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS, HUD_ID, this::extractRenderState);
    }

    private void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        PlayClockState snapshot = runtimeService.snapshot();
        if (!snapshot.config().hudEnabled()) {
            return;
        }

        TrackedTarget activeTarget = runtimeService.activeTarget();
        if (activeTarget == null) {
            return;
        }

        PlaytimeStats stats = snapshot.stats().get(activeTarget.key());
        if (stats == null) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        String language = PlayClock261xLocalization.minecraftLanguage(client);
        HudSnapshot hud = HudFormatter.create(activeTarget, stats, snapshot.config(), language, ZoneId.systemDefault());
        if (hud == null) {
            return;
        }

        HudRenderModel model = buildRenderModel(client.font, hud);
        if (model == null) {
            return;
        }

        HudLayout layout = HudLayout.anchored(
                client.getWindow().getGuiScaledWidth(),
                client.getWindow().getGuiScaledHeight(),
                model.width(),
                model.height(),
                snapshot.config().hudAnchor());
        PlayClockColorPalette colors = snapshot.config().colors();
        int x = layout.x();
        int y = layout.y();

        for (HudRenderLine line : model.lines()) {
            drawLine(graphics, client.font, line, x, y, colors);
            y += line.height();
        }
    }

    private static HudRenderModel buildRenderModel(Font font, HudSnapshot snapshot) {
        return switch (snapshot.variant()) {
            case "minimal" -> buildMinimalModel(font, snapshot);
            case "stacked" -> buildStackedModel(font, snapshot);
            default -> buildCompactModel(font, snapshot);
        };
    }

    private static HudRenderModel buildMinimalModel(Font font, HudSnapshot snapshot) {
        String title = trimToWidth(font, snapshot.title(), MAX_TEXT_WIDTH / 2);
        HudMetric total = snapshot.metrics().getFirst();
        List<HudRenderSpan> spans = new ArrayList<>(3);
        spans.add(new HudRenderSpan(title + " ", SpanRole.VALUE));
        spans.add(new HudRenderSpan(localizedLabel(total.labelKey()) + ": ", SpanRole.LABEL));
        spans.add(new HudRenderSpan(total.value(), SpanRole.VALUE));
        HudRenderLine line = new HudRenderLine(
                List.copyOf(spans),
                LINE_HEIGHT);
        return new HudRenderModel(line.width(font), line.height(), List.of(line));
    }

    private static HudRenderModel buildCompactModel(Font font, HudSnapshot snapshot) {
        String title = trimToWidth(font, snapshot.title(), MAX_TEXT_WIDTH);
        HudRenderLine titleLine = new HudRenderLine(
                List.of(Objects.requireNonNull(new HudRenderSpan(title, SpanRole.VALUE))),
                LINE_HEIGHT + SECTION_GAP);
        HudRenderLine metricsLine = new HudRenderLine(buildInlineMetricSpans(snapshot.metrics()), LINE_HEIGHT);
        int width = Math.max(titleLine.width(font), metricsLine.width(font));
        int height = titleLine.height() + metricsLine.height();
        return new HudRenderModel(width, height, List.of(titleLine, metricsLine));
    }

    private static HudRenderModel buildStackedModel(Font font, HudSnapshot snapshot) {
        List<HudRenderLine> lines = new ArrayList<>();
        String title = trimToWidth(font, snapshot.title(), MAX_TEXT_WIDTH);
        lines.add(new HudRenderLine(List.of(Objects.requireNonNull(new HudRenderSpan(title, SpanRole.VALUE))), LINE_HEIGHT + SECTION_GAP));

        int width = lines.getFirst().width(font);
        int height = lines.getFirst().height();
        for (int i = 0; i < snapshot.metrics().size(); i++) {
            HudMetric metric = snapshot.metrics().get(i);
            int lineHeight = LINE_HEIGHT + (i == snapshot.metrics().size() - 1 ? 0 : STACKED_GAP);
            List<HudRenderSpan> spans = new ArrayList<>(2);
            spans.add(new HudRenderSpan(localizedLabel(metric.labelKey()) + ": ", SpanRole.LABEL));
            spans.add(new HudRenderSpan(metric.value(), SpanRole.VALUE));
            HudRenderLine line = new HudRenderLine(
                    List.copyOf(spans),
                    lineHeight);
            lines.add(line);
            width = Math.max(width, line.width(font));
            height += line.height();
        }

        return new HudRenderModel(width, height, List.copyOf(lines));
    }

    private static List<HudRenderSpan> buildInlineMetricSpans(List<HudMetric> metrics) {
        List<HudRenderSpan> spans = new ArrayList<>();
        for (int i = 0; i < metrics.size(); i++) {
            HudMetric metric = metrics.get(i);
            spans.add(new HudRenderSpan(localizedLabel(metric.labelKey()) + ": ", SpanRole.LABEL));
            spans.add(new HudRenderSpan(metric.value(), SpanRole.VALUE));
            if (i < metrics.size() - 1) {
                spans.add(new HudRenderSpan("  ", SpanRole.SPACER));
            }
        }
        return List.copyOf(spans);
    }

    private static void drawLine(
            GuiGraphicsExtractor graphics,
            Font font,
            HudRenderLine line,
            int x,
            int y,
            PlayClockColorPalette colors) {
        int currentX = x;
        for (HudRenderSpan span : line.spans()) {
            int color = switch (span.role()) {
                case LABEL -> colors.hudLabelColor();
                case VALUE -> colors.hudValueColor();
                case SPACER -> colors.hudLabelColor();
            };
            graphics.text(font, span.text(), currentX, y, color, true);
            currentX += font.width(span.text());
        }
    }

    private static String localizedLabel(String labelKey) {
        return PlayClock261xLocalization.string(labelKey);
    }

    private static String trimToWidth(Font font, String value, int maxWidth) {
        if (font.width(value) <= maxWidth) {
            return value;
        }

        String ellipsis = "...";
        int targetWidth = Math.max(0, maxWidth - font.width(ellipsis));
        int endIndex = value.length();
        while (endIndex > 0 && font.width(value.substring(0, endIndex)) > targetWidth) {
            endIndex--;
        }
        return value.substring(0, Math.max(0, endIndex)) + ellipsis;
    }

    private record HudRenderModel(int width, int height, List<HudRenderLine> lines) {
    }

    private record HudRenderLine(List<HudRenderSpan> spans, int height) {
        int width(Font font) {
            int width = 0;
            for (HudRenderSpan span : spans) {
                width += font.width(span.text());
            }
            return width;
        }
    }

    private record HudRenderSpan(String text, SpanRole role) {
    }

    private enum SpanRole {
        LABEL,
        VALUE,
        SPACER
    }
}
