package dev.maksg.playclock.mc261x.client;

import dev.maksg.playclock.core.model.TrackedTarget;
import dev.maksg.playclock.core.runtime.PlayClockState;
import dev.maksg.playclock.core.stats.PlaytimeStats;
import dev.maksg.playclock.core.ui.BadgeFormatter;
import dev.maksg.playclock.core.ui.BadgeSnapshot;
import dev.maksg.playclock.core.ui.PlayClockTranslationKeys;
import dev.maksg.playclock.core.ui.TooltipLayout;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Vector2i;

@SuppressWarnings("null")
public final class PlayClock261xBadgeRenderer {
    private static final int MIN_BADGE_WIDTH = 84;
    private static final int MAX_BADGE_WIDTH = 120;
    private static final int BADGE_HEIGHT = 24;
    private static final int BADGE_PADDING_X = 6;
    private static final int BACKGROUND_COLOR = 0x8F16161C;
    private static final int OUTLINE_COLOR = 0xD0727280;
    private static final int TEXT_COLOR = 0xFFE8E8F0;
    private static final int SUBTEXT_COLOR = 0xFFB8B8C4;

    private PlayClock261xBadgeRenderer() {
    }

    public static void renderBadge(
            GuiGraphicsExtractor graphics,
            int rowX,
            int rowY,
            int rowWidth,
            int rowHeight,
            boolean hovered,
            TrackedTarget target) {
        PlayClockState snapshot = PlayClock261xClient.runtimeService().snapshot();
        if (!snapshot.config().badgeEnabled()) {
            return;
        }

        PlaytimeStats stats = snapshot.stats().get(target.key());
        if (stats == null) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        String language = Objects.requireNonNull(PlayClock261xLocalization.minecraftLanguage(client));
        BadgeSnapshot badge = BadgeFormatter.create(stats, ZoneId.systemDefault(), snapshot.config(), language);
        String todayLine = safeText(PlayClock261xLocalization.string(PlayClockTranslationKeys.LABEL_TODAY)) + " " + badge.todayText();
        int badgeWidth = clamp(
                Math.max(
                        client.font.width(badge.totalText()),
                        client.font.width(todayLine)) + BADGE_PADDING_X * 2,
                MIN_BADGE_WIDTH,
                Math.min(MAX_BADGE_WIDTH, Math.max(MIN_BADGE_WIDTH, rowWidth - 12)));
        int x = rowX + rowWidth - badgeWidth - 6;
        int y = rowY + Math.max(0, (rowHeight - BADGE_HEIGHT) / 2);

        graphics.fill(x, y, x + badgeWidth, y + BADGE_HEIGHT, BACKGROUND_COLOR);
        graphics.outline(x, y, badgeWidth, BADGE_HEIGHT, OUTLINE_COLOR);
        graphics.text(client.font, badge.totalText(), x + BADGE_PADDING_X, y + 5, TEXT_COLOR, false);
        graphics.text(
                client.font,
                todayLine,
                x + BADGE_PADDING_X,
                y + 14,
                SUBTEXT_COLOR,
                false);

        if (hovered && snapshot.config().tooltipsEnabled()) {
            int estimatedWidth = 140;
            int estimatedHeight = 48;
            TooltipLayout tooltipLayout = TooltipLayout.anchoredBelow(
                    client.getWindow().getGuiScaledWidth(),
                    client.getWindow().getGuiScaledHeight(),
                    x + badgeWidth - 4,
                    y + BADGE_HEIGHT + 2,
                    estimatedWidth,
                    estimatedHeight);
            List<FormattedCharSequence> tooltipLines = new ArrayList<>(4);
            tooltipLines.add(tooltipText(PlayClockTranslationKeys.LABEL_TOTAL, badge.totalText()));
            tooltipLines.add(tooltipText(PlayClockTranslationKeys.LABEL_TODAY, badge.todayText()));
            tooltipLines.add(tooltipText(PlayClockTranslationKeys.LABEL_SESSION, badge.sessionText()));
            tooltipLines.add(tooltipText(PlayClockTranslationKeys.LABEL_LAST_PLAYED, badge.lastPlayedText()));
            ClientTooltipPositioner positioner = (screenWidth, screenHeight, mouseX, mouseY, width, height) ->
                    new Vector2i(tooltipLayout.x(), tooltipLayout.y());
            graphics.setTooltipForNextFrame(
                    client.font,
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

    private static String safeText(String value) {
        return Objects.requireNonNull(value, "Tooltip text must not be null");
    }

    private static FormattedCharSequence tooltipText(String labelKey, String value) {
        return Objects.requireNonNull(
                Component.literal(safeText(PlayClock261xLocalization.string(labelKey)) + ": " + safeText(value)).getVisualOrderText(),
                "Tooltip line must not be null");
    }
}
