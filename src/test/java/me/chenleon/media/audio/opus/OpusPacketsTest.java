package me.chenleon.media.audio.opus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class OpusPacketsTest {
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void should_create_opus_packet_from_given_code(int code) {
        OpusPacket opusPacket = OpusPackets.newPacketOfCode(code);
        assertEquals(code, opusPacket.getCode());
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5, 20})
    void should_throw_exception_when_creating_opus_packet_from_code_more_than_3(int code) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            OpusPackets.newPacketOfCode(code);
        });
        assertEquals("Invalid Opus packet code: " + code, exception.getMessage());
    }

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