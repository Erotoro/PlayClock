package dev.maksg.playclock.mc12111.client;

import dev.maksg.playclock.core.model.TrackedTarget;
import dev.maksg.playclock.core.runtime.PlayClockState;
import dev.maksg.playclock.core.stats.PlaytimeStats;
import dev.maksg.playclock.core.ui.BadgeFormatter;
import dev.maksg.playclock.core.ui.BadgeSnapshot;
import dev.maksg.playclock.core.ui.PlayClockTranslationKeys;
import dev.maksg.playclock.core.ui.TooltipLayout;
import java.time.ZoneId;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.joml.Vector2i;

public final class PlayClock12111BadgeRenderer {
    private static final int MIN_BADGE_WIDTH = 84;
    private static final int MAX_BADGE_WIDTH = 120;
    private static final int BADGE_HEIGHT = 24;
    private static final int BADGE_PADDING_X = 6;
    private static final int BACKGROUND_COLOR = 0x8F16161C;
    private static final int OUTLINE_COLOR = 0xD0727280;
    private static final int TEXT_COLOR = 0xFFE8E8F0;
    private static final int SUBTEXT_COLOR = 0xFFB8B8C4;

    private PlayClock12111BadgeRenderer() {
    }

    public static void renderBadge(
            DrawContext context,
            int rowX,
            int rowY,
            int rowWidth,
            int rowHeight,
            boolean hovered,
            TrackedTarget target) {
        PlayClockState snapshot = PlayClock12111Client.runtimeService().snapshot();
        if (!snapshot.config().badgeEnabled()) {
            return;
        }

        PlaytimeStats stats = snapshot.stats().get(target.key());
        if (stats == null) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        String language = PlayClock12111Localization.minecraftLanguage(client);
        BadgeSnapshot badge = BadgeFormatter.create(stats, ZoneId.systemDefault(), snapshot.config(), language);
        String todayLine = PlayClock12111Localization.string(PlayClockTranslationKeys.LABEL_TODAY) + " " + badge.todayText();
        int badgeWidth = clamp(
                Math.max(
                        client.textRenderer.getWidth(badge.totalText()),
                        client.textRenderer.getWidth(todayLine)) + BADGE_PADDING_X * 2,
                MIN_BADGE_WIDTH,
                Math.min(MAX_BADGE_WIDTH, Math.max(MIN_BADGE_WIDTH, rowWidth - 12)));

        int x = rowX + rowWidth - badgeWidth - 6;
        int y = rowY + Math.max(0, (rowHeight - BADGE_HEIGHT) / 2);

        context.fill(x, y, x + badgeWidth, y + BADGE_HEIGHT, BACKGROUND_COLOR);
        context.drawHorizontalLine(x, x + badgeWidth - 1, y, OUTLINE_COLOR);
        context.drawHorizontalLine(x, x + badgeWidth - 1, y + BADGE_HEIGHT - 1, OUTLINE_COLOR);
        context.drawVerticalLine(x, y, y + BADGE_HEIGHT - 1, OUTLINE_COLOR);
        context.drawVerticalLine(x + badgeWidth - 1, y, y + BADGE_HEIGHT - 1, OUTLINE_COLOR);
        context.drawText(client.textRenderer, badge.totalText(), x + BADGE_PADDING_X, y + 5, TEXT_COLOR, false);
        context.drawText(
                client.textRenderer,
                todayLine,
                x + BADGE_PADDING_X,
                y + 14,
                SUBTEXT_COLOR,
                false);

        if (hovered && snapshot.config().tooltipsEnabled()) {
            int estimatedWidth = 140;
            int estimatedHeight = 48;
            TooltipLayout tooltipLayout = TooltipLayout.anchoredBelow(
                    client.getWindow().getScaledWidth(),
                    client.getWindow().getScaledHeight(),
                    x + badgeWidth - 4,
                    y + BADGE_HEIGHT + 2,
                    estimatedWidth,
                    estimatedHeight);
            List<OrderedText> tooltipLines = List.of(
                    Text.literal(PlayClock12111Localization.string(PlayClockTranslationKeys.LABEL_TOTAL) + ": " + badge.totalText()).asOrderedText(),
                    Text.literal(PlayClock12111Localization.string(PlayClockTranslationKeys.LABEL_TODAY) + ": " + badge.todayText()).asOrderedText(),
                    Text.literal(PlayClock12111Localization.string(PlayClockTranslationKeys.LABEL_SESSION) + ": " + badge.sessionText()).asOrderedText(),
                    Text.literal(PlayClock12111Localization.string(PlayClockTranslationKeys.LABEL_LAST_PLAYED) + ": " + badge.lastPlayedText()).asOrderedText());
            TooltipPositioner positioner = (screenWidth, screenHeight, mouseX, mouseY, width, height) ->
                    new Vector2i(tooltipLayout.x(), tooltipLayout.y());
            context.drawTooltip(
                    client.textRenderer,
                    tooltipLines,
                    positioner,
                    tooltipLayout.x(),
                    tooltipLayout.y(),
                    false);
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
