package me.chenleon.media.audio.opus;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpusPacketsTest {
    @Test
    void should_create_opus_packet_from_a_toc_byte() {
        OpusPacket opusPacket = OpusPackets.newPacketOfToc((byte) 0);
        assertEquals(Config.of(0), opusPacket.getConfig());
        assertTrue(opusPacket.isMono());
        assertEquals(0, opusPacket.getCode());

        opusPacket = OpusPackets.newPacketOfToc((byte) 14);
        assertEquals(Config.of(1), opusPacket.getConfig());
        assertFalse(opusPacket.isMono());
        assertEquals(2, opusPacket.getCode());
    }
}