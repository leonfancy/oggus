package me.chenleon.media.audio.opus;

import com.google.common.primitives.Bytes;
import me.chenleon.media.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

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

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void should_parse_binary_contains_only_one_packet(int code) {
        OpusPacket expectedPacket = OpusPackets.newPacketOfCode(code);
        expectedPacket.setConfig(Config.of(1));
        expectedPacket.setMono(false);
        for (int i = 0; i < expectedPacket.getFrameCount(); i++) {
            expectedPacket.addFrame(TestUtil.createBinary(10, (byte) i));
        }

        byte[] data = expectedPacket.dumpToStandardFormat();
        OpusPacket parsedPacket = OpusPackets.from(data);

        assertOpusPacketEqual(expectedPacket, parsedPacket);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void should_parse_binary_contains_multiple_packets(int code) {
        OpusPacket expectedPacket = OpusPackets.newPacketOfCode(code);
        expectedPacket.setConfig(Config.of(1));
        expectedPacket.setMono(false);
        for (int i = 0; i < expectedPacket.getFrameCount(); i++) {
            expectedPacket.addFrame(TestUtil.createBinary(10, (byte) i));
        }

        byte[] data = Bytes.concat(expectedPacket.dumpToSelfDelimitingFormat(),
                expectedPacket.dumpToSelfDelimitingFormat(),
                expectedPacket.dumpToStandardFormat());
        List<OpusPacket> opusPackets = OpusPackets.from(data, 3);

        for (OpusPacket opusPacket : opusPackets) {
            assertOpusPacketEqual(expectedPacket, opusPacket);
        }
    }

    @Test
    void should_parse_binary_contains_only_one_cbr_code_3_packet_without_padding() {
        OpusPacket expectedPacket = OpusPackets.newPacketOfCode(3);
        expectedPacket.setConfig(Config.of(12));
        expectedPacket.setMono(false);
        expectedPacket.setVbr(false);
        expectedPacket.setFrameCount(3);
        expectedPacket.setHasPadding(false);

        for (int i = 0; i < expectedPacket.getFrameCount(); i++) {
            expectedPacket.addFrame(TestUtil.createBinary(10, (byte) i));
        }

        byte[] data = expectedPacket.dumpToStandardFormat();
        OpusPacket parsedPacket = OpusPackets.from(data);

        assertOpusPacketEqual(expectedPacket, parsedPacket);
    }

    @Test
    void should_parse_binary_contains_multiple_cbr_code_3_packet_without_padding() {
        OpusPacket expectedPacket = OpusPackets.newPacketOfCode(3);
        expectedPacket.setConfig(Config.of(12));
        expectedPacket.setMono(false);
        expectedPacket.setVbr(false);
        expectedPacket.setFrameCount(3);
        expectedPacket.setHasPadding(false);

        for (int i = 0; i < expectedPacket.getFrameCount(); i++) {
            expectedPacket.addFrame(TestUtil.createBinary(10, (byte) i));
        }

        byte[] data = Bytes.concat(expectedPacket.dumpToSelfDelimitingFormat(),
                expectedPacket.dumpToSelfDelimitingFormat(),
                expectedPacket.dumpToStandardFormat());
        List<OpusPacket> opusPackets = OpusPackets.from(data, 3);

        for (OpusPacket opusPacket : opusPackets) {
            assertOpusPacketEqual(expectedPacket, opusPacket);
        }
    }

    @Test
    void should_parse_binary_contains_only_one_vbr_code_3_packet_without_padding() {
        OpusPacket expectedPacket = OpusPackets.newPacketOfCode(3);
        expectedPacket.setConfig(Config.of(12));
        expectedPacket.setMono(false);
        expectedPacket.setVbr(true);
        expectedPacket.setFrameCount(3);
        expectedPacket.setHasPadding(false);

        for (int i = 0; i < expectedPacket.getFrameCount(); i++) {
            expectedPacket.addFrame(TestUtil.createBinary(10 + i, (byte) i));
        }

        byte[] data = expectedPacket.dumpToStandardFormat();
        OpusPacket parsedPacket = OpusPackets.from(data);

        assertOpusPacketEqual(expectedPacket, parsedPacket);
    }

    @Test
    void should_parse_binary_contains_multiple_vbr_code_3_packet_without_padding() {
        OpusPacket expectedPacket = OpusPackets.newPacketOfCode(3);
        expectedPacket.setConfig(Config.of(12));
        expectedPacket.setMono(false);
        expectedPacket.setVbr(true);
        expectedPacket.setFrameCount(3);
        expectedPacket.setHasPadding(false);

        for (int i = 0; i < expectedPacket.getFrameCount(); i++) {
            expectedPacket.addFrame(TestUtil.createBinary(10 + i, (byte) i));
        }

        byte[] data = Bytes.concat(expectedPacket.dumpToSelfDelimitingFormat(),
                expectedPacket.dumpToSelfDelimitingFormat(),
                expectedPacket.dumpToStandardFormat());
        List<OpusPacket> opusPackets = OpusPackets.from(data, 3);

        for (OpusPacket opusPacket : opusPackets) {
            assertOpusPacketEqual(expectedPacket, opusPacket);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 254, 255, 256, 510, 513})
    void should_parse_binary_contains_only_one_cbr_code_3_packet_with_padding(int paddingLength) {
        OpusPacket expectedPacket = OpusPackets.newPacketOfCode(3);
        expectedPacket.setConfig(Config.of(12));
        expectedPacket.setMono(false);
        expectedPacket.setVbr(false);
        expectedPacket.setFrameCount(3);
        expectedPacket.setHasPadding(true);
        expectedPacket.setPaddingLength(paddingLength);

        for (int i = 0; i < expectedPacket.getFrameCount(); i++) {
            expectedPacket.addFrame(TestUtil.createBinary(10, (byte) i));
        }

        byte[] data = expectedPacket.dumpToStandardFormat();
        OpusPacket parsedPacket = OpusPackets.from(data);

        assertOpusPacketEqual(expectedPacket, parsedPacket);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 254, 255, 256, 510, 513})
    void should_parse_binary_contains_multiple_cbr_code_3_packet_with_padding(int paddingLength) {
        OpusPacket expectedPacket = OpusPackets.newPacketOfCode(3);
        expectedPacket.setConfig(Config.of(12));
        expectedPacket.setMono(false);
        expectedPacket.setVbr(false);
        expectedPacket.setFrameCount(3);
        expectedPacket.setHasPadding(true);
        expectedPacket.setPaddingLength(paddingLength);

        for (int i = 0; i < expectedPacket.getFrameCount(); i++) {
            expectedPacket.addFrame(TestUtil.createBinary(10, (byte) i));
        }

        byte[] data = Bytes.concat(expectedPacket.dumpToSelfDelimitingFormat(),
                expectedPacket.dumpToSelfDelimitingFormat(),
                expectedPacket.dumpToStandardFormat());
        List<OpusPacket> opusPackets = OpusPackets.from(data, 3);

        for (OpusPacket opusPacket : opusPackets) {
            assertOpusPacketEqual(expectedPacket, opusPacket);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 254, 255, 256, 510, 513})
    void should_parse_binary_contains_only_one_vbr_code_3_packet_with_padding(int paddingLength) {
        OpusPacket expectedPacket = OpusPackets.newPacketOfCode(3);
        expectedPacket.setConfig(Config.of(12));
        expectedPacket.setMono(false);
        expectedPacket.setVbr(true);
        expectedPacket.setFrameCount(3);
        expectedPacket.setHasPadding(true);
        expectedPacket.setPaddingLength(paddingLength);

        for (int i = 0; i < expectedPacket.getFrameCount(); i++) {
            expectedPacket.addFrame(TestUtil.createBinary(10 + i, (byte) i));
        }

        byte[] data = expectedPacket.dumpToStandardFormat();
        OpusPacket parsedPacket = OpusPackets.from(data);

        assertOpusPacketEqual(expectedPacket, parsedPacket);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 254, 255, 256, 510, 513})
    void should_parse_binary_contains_multiple_vbr_code_3_packet_with_padding(int paddingLength) {
        OpusPacket expectedPacket = OpusPackets.newPacketOfCode(3);
        expectedPacket.setConfig(Config.of(12));
        expectedPacket.setMono(false);
        expectedPacket.setVbr(true);
        expectedPacket.setFrameCount(3);
        expectedPacket.setHasPadding(true);
        expectedPacket.setPaddingLength(paddingLength);

        for (int i = 0; i < expectedPacket.getFrameCount(); i++) {
            expectedPacket.addFrame(TestUtil.createBinary(10 + i, (byte) i));
        }

        byte[] data = Bytes.concat(expectedPacket.dumpToSelfDelimitingFormat(),
                expectedPacket.dumpToSelfDelimitingFormat(),
                expectedPacket.dumpToStandardFormat());
        List<OpusPacket> opusPackets = OpusPackets.from(data, 3);

        for (OpusPacket opusPacket : opusPackets) {
            assertOpusPacketEqual(expectedPacket, opusPacket);
        }
    }

    private void assertOpusPacketEqual(OpusPacket expected, OpusPacket actual) {
        assertEquals(expected.getCode(), actual.getCode());
        assertEquals(expected.getConfig().getId(), actual.getConfig().getId());
        assertEquals(expected.isMono(), actual.isMono());
        assertEquals(expected.isVbr(), actual.isVbr());
        assertEquals(expected.hasPadding(), actual.hasPadding());
        assertEquals(expected.getPaddingLength(), actual.getPaddingLength());
        assertEquals(expected.getFrameCount(), actual.getFrameCount());
        assertEquals(expected.getFrames().size(), actual.getFrames().size());
        for (int i = 0; i < expected.getFrames().size(); i++) {
            assertArrayEquals(expected.getFrames().get(i), actual.getFrames().get(i));
        }
    }
}