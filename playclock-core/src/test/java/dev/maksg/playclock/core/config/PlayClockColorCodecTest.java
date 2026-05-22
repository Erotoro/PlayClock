package dev.maksg.playclock.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PlayClockColorCodecTest {

    @Test
    void formatsArgbColorsAsRgbHex() {
        assertEquals("#FFD75E", PlayClockColorCodec.toHexRgb(0xFFFFD75E));
    }

    @Test
    void parsesHexWithOrWithoutLeadingHash() {
        assertEquals(0xFFFFD75E, PlayClockColorCodec.parseHexRgb("#FFD75E", 0xFFFFFFFF));
        assertEquals(0xFFB7E07A, PlayClockColorCodec.parseHexRgb("B7E07A", 0xFFFFFFFF));
    }

    @Test
    void fallsBackForInvalidHex() {
        assertEquals(0xFF112233, PlayClockColorCodec.parseHexRgb("#oops", 0xFF112233));
    }

    @Test
    void clampsRgbChannels() {
        assertEquals(255, PlayClockColorCodec.parseRgbChannel("999", 10));
        assertEquals(0, PlayClockColorCodec.parseRgbChannel("-2", 10));
        assertEquals(10, PlayClockColorCodec.parseRgbChannel("bad", 10));
    }

    @Test
    void convertsBetweenArgbAndHsvWithoutLosingChannelData() {
        int color = 0xFFB7E07A;
        float[] hsv = PlayClockColorCodec.toHsv(color);
        int reconstructed = PlayClockColorCodec.fromHsv(hsv[0], hsv[1], hsv[2]);

        assertEquals(color, reconstructed);
    }
}
