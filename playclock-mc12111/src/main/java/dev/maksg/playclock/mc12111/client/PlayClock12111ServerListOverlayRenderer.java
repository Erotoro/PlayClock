package dev.maksg.playclock.mc12111.client;

import dev.maksg.playclock.core.model.TrackedTarget;
import dev.maksg.playclock.core.runtime.PlayClockState;
import dev.maksg.playclock.core.stats.PlaytimeStats;
import dev.maksg.playclock.core.config.PlayClockColorPalette;
import dev.maksg.playclock.core.ui.PlayClockTranslationKeys;
import dev.maksg.playclock.core.ui.ServerListDailyBadgeSnapshot;
import dev.maksg.playclock.core.ui.ServerListDetailSnapshot;
import dev.maksg.playclock.core.ui.ServerListPanelLayout;
import dev.maksg.playclock.core.ui.ServerListSummarySnapshot;
import dev.maksg.playclock.core.ui.ServerListUiFormatter;
import dev.maksg.playclock.core.ui.TooltipLayout;
import java.time.ZoneId;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class PlayClock12111ServerListOverlayRenderer {
    private static final int DETAIL_WIDTH = 212;
    private static final int DETAIL_HEIGHT = 92;
    private static final int PANEL_BACKGROUND = 0xF0100010;
    private static final int PANEL_BORDER_OUTER = 0x505000FF;
    private static final int PANEL_BORDER_INNER = 0x5028007F;
    private static final int PANEL_SEPARATOR = 0x604E4369;
    private PlayClock12111ServerListOverlayRenderer() {
    }

    public static void beginFrame() {
        PlayClock12111ServerListOverlayState.beginFrame();
    }

    public static void renderDailyBadge(
            DrawContext context,
            int rowX,
            int rowY,
            int rowWidth,
            int rowHeight,
            boolean hovered,
            int mouseX,
            int mouseY,
            TrackedTarget target) {
        PlayClockState snapshot = PlayClock12111Client.runtimeService().snapshot();
        PlayClock12111ServerListOverlayState.recordRowLayout(rowX, rowY, rowWidth, rowHeight);
        if (hovered) {
            PlayClock12111ServerListOverlayState.recordHoveredTarget(target, mouseX, mouseY);
        }

        PlaytimeStats stats = snapshot.stats().get(target.key());
        if (!snapshot.config().badgeEnabled() || stats == null) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        String language = PlayClock12111Localization.minecraftLanguage(client);
        PlayClockColorPalette colors = snapshot.config().colors();
        ServerListDailyBadgeSnapshot badge = ServerListUiFormatter.createDailyBadge(stats, snapshot.config(), language);
        if (badge == null) {
            return;
        }

        String label = PlayClock12111Localization.string(PlayClockTranslationKeys.LABEL_TODAY_BADGE) + ": ";
        String value = badge.todayText();
        int badgeWidth = client.textRenderer.getWidth(label) + client.textRenderer.getWidth(value);
        int detailRailX = Math.max(ServerListPanelLayout.screenMargin(), screenWidth(client) - DETAIL_WIDTH - ServerListPanelLayout.screenMargin());
        int badgeX = ServerListPanelLayout.markerX(screenWidth(client), rowX + rowWidth, detailRailX, badgeWidth);
        if (badgeX < 0) {
            return;
        }

        int badgeY = rowY + Math.max(0, (rowHeight - 9) / 2);
        context.drawText(client.textRenderer, label, badgeX, badgeY, colors.markerLabelColor(), true);
        context.drawText(client.textRenderer, value, badgeX + client.textRenderer.getWidth(label), badgeY, colors.markerValueColor(), true);

    }

    public static void renderPanels(DrawContext context, int screenWidth, int screenHeight) {
        PlayClockState snapshot = PlayClock12111Client.runtimeService().snapshot();
        MinecraftClient client = MinecraftClient.getInstance();
        String language = PlayClock12111Localization.minecraftLanguage(client);
        PlayClockColorPalette colors = snapshot.config().colors();

        ServerListSummarySnapshot summary = ServerListUiFormatter.createMultiplayerSummary(snapshot, ZoneId.systemDefault(), language);
        renderHeaderSummary(context, client, summary, screenWidth, colors);

        TrackedTarget hoveredTarget = PlayClock12111ServerListOverlayState.hoveredTarget();
        if (hoveredTarget == null) {
            return;
        }

        ServerListDetailSnapshot detail = ServerListUiFormatter.createDetail(
                hoveredTarget,
                snapshot.stats().get(hoveredTarget.key()),
                ZoneId.systemDefault(),
                snapshot.config(),
                language);
        if (detail == null) {
            return;
        }

        TooltipLayout tooltip = TooltipLayout.anchoredBelow(
                screenWidth,
                screenHeight,
                PlayClock12111ServerListOverlayState.hoveredMouseX() + 12,
                PlayClock12111ServerListOverlayState.hoveredMouseY() - 10,
                DETAIL_WIDTH,
                DETAIL_HEIGHT);
        renderDetailTooltip(context, client, tooltip.x(), tooltip.y(), detail, colors);
    }

    private static void renderHeaderSummary(
            DrawContext context,
            MinecraftClient client,
            ServerListSummarySnapshot summary,
            int screenWidth,
            PlayClockColorPalette colors) {
        String label = PlayClock12111Localization.string(PlayClockTranslationKeys.LABEL_TOTAL_PLAYED_ON_SERVERS) + ": ";
        String value = summary.totalText();
        int totalWidth = client.textRenderer.getWidth(label) + client.textRenderer.getWidth(value);
        int x = Math.max(20, screenWidth - totalWidth - 20);
        int y = 12;
        context.drawText(client.textRenderer, trim(client, label, Math.max(40, screenWidth - x - 20)), x, y, colors.headerLabelColor(), true);
        context.drawText(client.textRenderer, value, x + client.textRenderer.getWidth(label), y, colors.headerValueColor(), true);
    }

    private static void renderDetailTooltip(
            DrawContext context,
            MinecraftClient client,
            int x,
            int y,
            ServerListDetailSnapshot detail,
            PlayClockColorPalette colors) {
        int valueX = x + 112;

        context.fill(x, y, x + DETAIL_WIDTH, y + DETAIL_HEIGHT, PANEL_BACKGROUND);
        context.drawHorizontalLine(x, x + DETAIL_WIDTH - 1, y, PANEL_BORDER_OUTER);
        context.drawHorizontalLine(x, x + DETAIL_WIDTH - 1, y + DETAIL_HEIGHT - 1, PANEL_BORDER_OUTER);
        context.drawVerticalLine(x, y, y + DETAIL_HEIGHT - 1, PANEL_BORDER_OUTER);
        context.drawVerticalLine(x + DETAIL_WIDTH - 1, y, y + DETAIL_HEIGHT - 1, PANEL_BORDER_OUTER);
        context.drawHorizontalLine(x + 1, x + DETAIL_WIDTH - 2, y + 1, PANEL_BORDER_INNER);
        context.drawHorizontalLine(x + 1, x + DETAIL_WIDTH - 2, y + DETAIL_HEIGHT - 2, PANEL_BORDER_INNER);
        context.drawVerticalLine(x + 1, y + 1, y + DETAIL_HEIGHT - 2, PANEL_BORDER_INNER);
        context.drawVerticalLine(x + DETAIL_WIDTH - 2, y + 1, y + DETAIL_HEIGHT - 2, PANEL_BORDER_INNER);
        context.drawHorizontalLine(x + 1, x + DETAIL_WIDTH - 2, y + 18, PANEL_SEPARATOR);
        context.drawText(client.textRenderer, trim(client, detail.titleText(), DETAIL_WIDTH - 16), x + 8, y + 6, colors.tooltipTitleColor(), true);
        drawMetricRow(context, client, x + 8, valueX, y + 26, PlayClockTranslationKeys.LABEL_TODAY, detail.todayText(), colors);
        drawMetricRow(context, client, x + 8, valueX, y + 40, PlayClockTranslationKeys.LABEL_TOTAL, detail.totalText(), colors);
        drawMetricRow(context, client, x + 8, valueX, y + 54, PlayClockTranslationKeys.LABEL_SESSION, detail.sessionText(), colors);
        drawMetricRow(
                context,
                client,
                x + 8,
                valueX,
                y + 68,
                PlayClockTranslationKeys.LABEL_LAST_PLAYED,
                trim(client, detail.lastPlayedText(), DETAIL_WIDTH - (valueX - x) - 8),
                colors);
    }

    private static void drawMetricRow(
            DrawContext context,
            MinecraftClient client,
            int labelX,
            int valueX,
            int y,
            String labelKey,
            String value,
            PlayClockColorPalette colors) {
        context.drawText(client.textRenderer, PlayClock12111Localization.string(labelKey), labelX, y, colors.tooltipLabelColor(), true);
        context.drawText(client.textRenderer, value, valueX, y, colors.tooltipValueColor(), true);
    }

    private static String trim(MinecraftClient client, String value, int maxWidth) {
        return client.textRenderer.trimToWidth(value, maxWidth);
    }

    private static int screenWidth(MinecraftClient client) {
        var currentScreen = client.currentScreen;
        if (currentScreen != null) {
            return currentScreen.width;
        }
        return client.getWindow().getScaledWidth();
    }
}
