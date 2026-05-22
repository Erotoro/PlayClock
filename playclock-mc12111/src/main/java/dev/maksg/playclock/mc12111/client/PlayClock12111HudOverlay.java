package dev.maksg.playclock.mc12111.client;

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
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

@SuppressWarnings("null")
final class PlayClock12111HudOverlay {
    private static final int MAX_TEXT_WIDTH = 200;
    private static final int LINE_HEIGHT = 10;
    private static final int STACKED_GAP = 1;
    private static final int SECTION_GAP = 3;

    private final PlayClockRuntimeService runtimeService;

    PlayClock12111HudOverlay(PlayClockRuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @SuppressWarnings("deprecation")
    void register() {
        HudRenderCallback.EVENT.register(this::onHudRender);
    }

    private void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
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

        MinecraftClient client = MinecraftClient.getInstance();
        String language = PlayClock12111Localization.minecraftLanguage(client);
        HudSnapshot hud = HudFormatter.create(activeTarget, stats, snapshot.config(), language, ZoneId.systemDefault());
        if (hud == null) {
            return;
        }

        HudRenderModel model = buildRenderModel(client.textRenderer, hud);
        HudLayout layout = HudLayout.anchored(
                client.getWindow().getScaledWidth(),
                client.getWindow().getScaledHeight(),
                model.width(),
                model.height(),
                snapshot.config().hudAnchor());
        PlayClockColorPalette colors = snapshot.config().colors();
        int x = layout.x();
        int y = layout.y();

        for (HudRenderLine line : model.lines()) {
            drawLine(context, client.textRenderer, line, x, y, colors);
            y += line.height();
        }
    }

    private static HudRenderModel buildRenderModel(TextRenderer textRenderer, HudSnapshot snapshot) {
        return switch (snapshot.variant()) {
            case "minimal" -> buildMinimalModel(textRenderer, snapshot);
            case "stacked" -> buildStackedModel(textRenderer, snapshot);
            default -> buildCompactModel(textRenderer, snapshot);
        };
    }

    private static HudRenderModel buildMinimalModel(TextRenderer textRenderer, HudSnapshot snapshot) {
        String title = trimToWidth(textRenderer, snapshot.title(), MAX_TEXT_WIDTH / 2);
        HudMetric total = snapshot.metrics().getFirst();
        List<HudRenderSpan> spans = new ArrayList<>(3);
        spans.add(new HudRenderSpan(title + " ", SpanRole.VALUE));
        spans.add(new HudRenderSpan(localizedLabel(total.labelKey()) + ": ", SpanRole.LABEL));
        spans.add(new HudRenderSpan(total.value(), SpanRole.VALUE));
        HudRenderLine line = new HudRenderLine(
                List.copyOf(spans),
                LINE_HEIGHT);
        return new HudRenderModel(line.width(textRenderer), line.height(), List.of(line));
    }

    private static HudRenderModel buildCompactModel(TextRenderer textRenderer, HudSnapshot snapshot) {
        String title = trimToWidth(textRenderer, snapshot.title(), MAX_TEXT_WIDTH);
        HudRenderLine titleLine = new HudRenderLine(
                List.of(Objects.requireNonNull(new HudRenderSpan(title, SpanRole.VALUE))),
                LINE_HEIGHT + SECTION_GAP);
        HudRenderLine metricsLine = new HudRenderLine(buildInlineMetricSpans(snapshot.metrics()), LINE_HEIGHT);
        int width = Math.max(titleLine.width(textRenderer), metricsLine.width(textRenderer));
        int height = titleLine.height() + metricsLine.height();
        return new HudRenderModel(width, height, List.of(titleLine, metricsLine));
    }

    private static HudRenderModel buildStackedModel(TextRenderer textRenderer, HudSnapshot snapshot) {
        List<HudRenderLine> lines = new ArrayList<>();
        String title = trimToWidth(textRenderer, snapshot.title(), MAX_TEXT_WIDTH);
        lines.add(new HudRenderLine(List.of(Objects.requireNonNull(new HudRenderSpan(title, SpanRole.VALUE))), LINE_HEIGHT + SECTION_GAP));

        int width = lines.getFirst().width(textRenderer);
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
            width = Math.max(width, line.width(textRenderer));
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
            DrawContext context,
            TextRenderer textRenderer,
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
            context.drawText(textRenderer, span.text(), currentX, y, color, true);
            currentX += textRenderer.getWidth(span.text());
        }
    }

    private static String localizedLabel(String labelKey) {
        return PlayClock12111Localization.string(labelKey);
    }

    private static String trimToWidth(TextRenderer textRenderer, String value, int maxWidth) {
        if (textRenderer.getWidth(value) <= maxWidth) {
            return value;
        }

        String ellipsis = "...";
        int targetWidth = Math.max(0, maxWidth - textRenderer.getWidth(ellipsis));
        int endIndex = value.length();
        while (endIndex > 0 && textRenderer.getWidth(value.substring(0, endIndex)) > targetWidth) {
            endIndex--;
        }
        return value.substring(0, Math.max(0, endIndex)) + ellipsis;
    }

    private record HudRenderModel(int width, int height, List<HudRenderLine> lines) {
    }

    private record HudRenderLine(List<HudRenderSpan> spans, int height) {
        int width(TextRenderer textRenderer) {
            int width = 0;
            for (HudRenderSpan span : spans) {
                width += textRenderer.getWidth(span.text());
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
