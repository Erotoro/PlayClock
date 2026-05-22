package dev.maksg.playclock.mc261x.client;

import dev.maksg.playclock.core.model.TrackedTarget;
import dev.maksg.playclock.core.runtime.PlayClockState;
import dev.maksg.playclock.core.stats.PlaytimeStats;
import dev.maksg.playclock.core.ui.PlayClockTranslationKeys;
import dev.maksg.playclock.core.config.PlayClockColorPalette;
import dev.maksg.playclock.core.ui.ServerListDailyBadgeSnapshot;
import dev.maksg.playclock.core.ui.ServerListDetailSnapshot;
import dev.maksg.playclock.core.ui.ServerListPanelLayout;
import dev.maksg.playclock.core.ui.ServerListSummarySnapshot;
import dev.maksg.playclock.core.ui.ServerListUiFormatter;
import dev.maksg.playclock.core.ui.TooltipLayout;
import java.time.ZoneId;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

@SuppressWarnings("null")
public final class PlayClock261xWorldListOverlayRenderer {
    private static final int DETAIL_WIDTH = 212;
    private static final int DETAIL_HEIGHT = 92;
    private static final int PANEL_BACKGROUND = 0xF0100010;
    private static final int PANEL_BORDER_OUTER = 0x505000FF;
    private static final int PANEL_BORDER_INNER = 0x5028007F;
    private static final int PANEL_SEPARATOR = 0x604E4369;
    private PlayClock261xWorldListOverlayRenderer() {
    }

    public static void beginFrame() {
        PlayClock261xWorldListOverlayState.beginFrame();
    }

    public static void renderDailyBadge(
            GuiGraphicsExtractor graphics,
            int rowX,
            int rowY,
            int rowWidth,
            int rowHeight,
            boolean hovered,
            int mouseX,
            int mouseY,
            TrackedTarget target) {
        PlayClockState snapshot = PlayClock261xClient.runtimeService().snapshot();
        PlayClock261xWorldListOverlayState.recordRowLayout(rowX, rowY, rowWidth, rowHeight);
        if (hovered) {
            PlayClock261xWorldListOverlayState.recordHoveredTarget(target, mouseX, mouseY);
        }

        PlaytimeStats stats = snapshot.stats().get(target.key());
        if (!snapshot.config().badgeEnabled() || stats == null) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        String language = PlayClock261xLocalization.minecraftLanguage(client);
        PlayClockColorPalette colors = snapshot.config().colors();
        ServerListDailyBadgeSnapshot badge = ServerListUiFormatter.createDailyBadge(stats, snapshot.config(), language);
        if (badge == null) {
            return;
        }

        String label = safeText(PlayClock261xLocalization.string(PlayClockTranslationKeys.LABEL_TODAY_BADGE)) + ": ";
        String value = badge.todayText();
        int badgeWidth = client.font.width(label) + client.font.width(value);
        int detailRailX = Math.max(ServerListPanelLayout.screenMargin(), screenWidth(client) - DETAIL_WIDTH - ServerListPanelLayout.screenMargin());
        int badgeX = ServerListPanelLayout.markerX(screenWidth(client), rowX + rowWidth, detailRailX, badgeWidth);
        if (badgeX < 0) {
            return;
        }

        int badgeY = rowY + Math.max(0, (rowHeight - 9) / 2);
        graphics.text(client.font, label, badgeX, badgeY, colors.markerLabelColor(), true);
        graphics.text(client.font, value, badgeX + client.font.width(label), badgeY, colors.markerValueColor(), true);

    }

    public static void renderPanels(GuiGraphicsExtractor graphics, int screenWidth, int screenHeight) {
        PlayClockState snapshot = PlayClock261xClient.runtimeService().snapshot();
        Minecraft client = Minecraft.getInstance();
        String language = PlayClock261xLocalization.minecraftLanguage(client);
        PlayClockColorPalette colors = snapshot.config().colors();

        ServerListSummarySnapshot summary = ServerListUiFormatter.createSingleplayerSummary(snapshot, ZoneId.systemDefault(), language);
        renderHeaderSummary(graphics, client, summary, screenWidth, colors);

        TrackedTarget hoveredTarget = PlayClock261xWorldListOverlayState.hoveredTarget();
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
                PlayClock261xWorldListOverlayState.hoveredMouseX() + 12,
                PlayClock261xWorldListOverlayState.hoveredMouseY() - 10,
                DETAIL_WIDTH,
                DETAIL_HEIGHT);
        renderDetailTooltip(graphics, client, tooltip.x(), tooltip.y(), detail, colors);
    }

    private static void renderHeaderSummary(
            GuiGraphicsExtractor graphics,
            Minecraft client,
            ServerListSummarySnapshot summary,
            int screenWidth,
            PlayClockColorPalette colors) {
        String label = safeText(PlayClock261xLocalization.string(PlayClockTranslationKeys.LABEL_TOTAL_PLAYED_ON_WORLDS)) + ": ";
        String value = summary.totalText();
        int totalWidth = client.font.width(label) + client.font.width(value);
        int x = Math.max(20, screenWidth - totalWidth - 20);
        int y = 12;
        graphics.text(client.font, trim(client, label, Math.max(40, screenWidth - x - 20)), x, y, colors.headerLabelColor(), true);
        graphics.text(client.font, value, x + client.font.width(label), y, colors.headerValueColor(), true);
    }

    private static void renderDetailTooltip(
            GuiGraphicsExtractor graphics,
            Minecraft client,
            int x,
            int y,
            ServerListDetailSnapshot detail,
            PlayClockColorPalette colors) {
        int valueX = x + 112;

        graphics.fill(x, y, x + DETAIL_WIDTH, y + DETAIL_HEIGHT, PANEL_BACKGROUND);
        graphics.outline(x, y, DETAIL_WIDTH, DETAIL_HEIGHT, PANEL_BORDER_OUTER);
        graphics.outline(x + 1, y + 1, DETAIL_WIDTH - 2, DETAIL_HEIGHT - 2, PANEL_BORDER_INNER);
        graphics.fill(x + 1, y + 18, x + DETAIL_WIDTH - 1, y + 19, PANEL_SEPARATOR);
        graphics.text(client.font, trim(client, detail.titleText(), DETAIL_WIDTH - 16), x + 8, y + 6, colors.tooltipTitleColor(), true);
        drawMetricRow(graphics, client, x + 8, valueX, y + 26, PlayClockTranslationKeys.LABEL_TODAY, detail.todayText(), colors);
        drawMetricRow(graphics, client, x + 8, valueX, y + 40, PlayClockTranslationKeys.LABEL_TOTAL, detail.totalText(), colors);
        drawMetricRow(graphics, client, x + 8, valueX, y + 54, PlayClockTranslationKeys.LABEL_SESSION, detail.sessionText(), colors);
        drawMetricRow(
                graphics,
                client,
                x + 8,
                valueX,
                y + 68,
                PlayClockTranslationKeys.LABEL_LAST_PLAYED,
                trim(client, detail.lastPlayedText(), DETAIL_WIDTH - (valueX - x) - 8),
                colors);
    }

    private static void drawMetricRow(
            GuiGraphicsExtractor graphics,
            Minecraft client,
            int labelX,
            int valueX,
            int y,
            String labelKey,
            String value,
            PlayClockColorPalette colors) {
        graphics.text(client.font, safeText(PlayClock261xLocalization.string(labelKey)), labelX, y, colors.tooltipLabelColor(), true);
        graphics.text(client.font, value, valueX, y, colors.tooltipValueColor(), true);
    }

    private static String trim(Minecraft client, String value, int maxWidth) {
        return client.font.plainSubstrByWidth(value, maxWidth);
    }

    private static int screenWidth(Minecraft client) {
        var currentScreen = client.screen;
        if (currentScreen != null) {
            return currentScreen.width;
        }
        return client.getWindow().getGuiScaledWidth();
    }

    private static String safeText(String value) {
        return Objects.requireNonNull(value, "Rendered text must not be null");
    }
}
