package dev.maksg.playclock.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PlayClockConfigDraftTest {

    @Test
    void roundTripsExtendedConfigFields() {
        PlayClockConfigDraft draft = PlayClockConfigDraft.from(PlayClockConfig.defaults());
        draft.setHudEnabled(false);
        draft.setShowTodayInHud(false);
        draft.nextHudVariant();
        draft.nextColorMode();
        draft.setColor(PlayClockColorSlot.HEADER_VALUE, 0xFF123456);

        PlayClockConfig config = draft.toConfig();

        assertEquals("stacked", config.hudVariant());
        assertTrue("custom".equals(config.colorMode()));
        assertEquals(0xFF123456, config.headerValueColor());
        assertEquals(false, config.hudEnabled());
        assertEquals(false, config.showTodayInHud());
    }

    @Test
    void applyingPresetSwitchesToPresetModeAndCopiesEverySlot() {
        PlayClockConfigDraft draft = PlayClockConfigDraft.from(PlayClockConfig.defaults());
        draft.nextColorMode();
        draft.setColor(PlayClockColorSlot.HUD_VALUE, 0xFF123456);

        draft.nextColorPresetAndApply();

        PlayClockColorPalette palette = PlayClockColorPresets.palette(draft.colorPreset());
        assertEquals("preset", draft.colorMode());
        assertEquals(palette.hudLabelColor(), draft.color(PlayClockColorSlot.HUD_LABEL));
        assertEquals(palette.hudValueColor(), draft.color(PlayClockColorSlot.HUD_VALUE));
        assertEquals(palette.markerLabelColor(), draft.color(PlayClockColorSlot.MARKER_LABEL));
        assertEquals(palette.markerValueColor(), draft.color(PlayClockColorSlot.MARKER_VALUE));
        assertEquals(palette.tooltipTitleColor(), draft.color(PlayClockColorSlot.TOOLTIP_TITLE));
        assertEquals(palette.tooltipLabelColor(), draft.color(PlayClockColorSlot.TOOLTIP_LABEL));
        assertEquals(palette.tooltipValueColor(), draft.color(PlayClockColorSlot.TOOLTIP_VALUE));
        assertEquals(palette.headerLabelColor(), draft.color(PlayClockColorSlot.HEADER_LABEL));
        assertEquals(palette.headerValueColor(), draft.color(PlayClockColorSlot.HEADER_VALUE));
        assertEquals(palette, draft.effectiveColors());
    }
}
