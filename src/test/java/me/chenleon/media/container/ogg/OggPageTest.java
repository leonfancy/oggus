package me.chenleon.media.container.ogg;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OggPageTest {
    @Test
    void shouldCorrectlyParseFlag() {
        OggPage oggPage = new OggPage();

        oggPage.setFlag((byte)0x00);
        assertFalse(oggPage.isContinued());
        assertFalse(oggPage.isBOS());
        assertFalse(oggPage.isEOS());

        oggPage.setFlag((byte)0x01);
        assertTrue(oggPage.isContinued());

        oggPage.setFlag((byte)0x02);
        assertTrue(oggPage.isBOS());

        oggPage.setFlag((byte)0x04);
        assertTrue(oggPage.isEOS());
    }
}