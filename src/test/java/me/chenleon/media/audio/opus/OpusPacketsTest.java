package me.chenleon.media.audio.opus;

import com.google.common.primitives.Bytes;
import me.chenleon.media.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static me.chenleon.media.TestUtil.assertOpusPacketEqual;
import static org.junit.jupiter.api.Assertions.*;

class OpusPacketsTest {
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void should_create_opus_packet_from_given_code(int code) {
        OpusPacket opusPacket = OpusPackets.newPacket(Config.of(12), Channel.STEREO, code);
        assertEquals(code, opusPacket.getCode());
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5, 20})
    void should_throw_exception_when_creating_opus_packet_from_code_more_than_3(int code) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            OpusPackets.newPacket(Config.of(12), Channel.STEREO, code);
        });
        assertEquals("Invalid Opus packet code: " + code, exception.getMessage());
    }

    @Test
    void should_create_opus_packet_from_a_toc_byte() {
        OpusPacket opusPacket = OpusPackets.newPacketOfToc(0);
        assertEquals(Config.of(0), opusPacket.getConfig());
        assertEquals(Channel.MONO, opusPacket.getChannel());
        assertEquals(0, opusPacket.getCode());

        opusPacket = OpusPackets.newPacketOfToc(14);
        assertEquals(Config.of(1), opusPacket.getConfig());
        assertEquals(Channel.STEREO, opusPacket.getChannel());
        assertEquals(2, opusPacket.getCode());

        opusPacket = OpusPackets.newPacketOfToc(184);
        assertEquals(Config.of(23), opusPacket.getConfig());
        assertEquals(Channel.MONO, opusPacket.getChannel());
        assertEquals(0, opusPacket.getCode());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void should_parse_binary_contains_only_one_packet(int code) {
        OpusPacket expectedPacket = OpusPackets.newPacket(Config.of(1), Channel.STEREO, code);
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
        OpusPacket expectedPacket = OpusPackets.newPacket(Config.of(1), Channel.STEREO, code);
        for (int i = 0; i < expectedPacket.getFrameCount(); i++) {
            expectedPacket.addFrame(TestUtil.createBinary(255, (byte) i));
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
        OpusPacket expectedPacket = OpusPackets.newPacket(Config.of(12), Channel.STEREO, 3);
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
        OpusPacket expectedPacket = OpusPackets.newPacket(Config.of(12), Channel.STEREO, 3);
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
        OpusPacket expectedPacket = OpusPackets.newPacket(Config.of(12), Channel.STEREO, 3);
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
        OpusPacket expectedPacket = OpusPackets.newPacket(Config.of(12), Channel.STEREO, 3);
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
        OpusPacket expectedPacket = OpusPackets.newPacket(Config.of(12), Channel.STEREO, 3);
        expectedPacket.setVbr(false);
        expectedPacket.setFrameCount(3);
        expectedPacket.setHasPadding(true);
        expectedPacket.setPadLenBytesSum(paddingLength);

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
        OpusPacket expectedPacket = OpusPackets.newPacket(Config.of(12), Channel.STEREO, 3);
        expectedPacket.setVbr(false);
        expectedPacket.setFrameCount(3);
        expectedPacket.setHasPadding(true);
        expectedPacket.setPadLenBytesSum(paddingLength);

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
        OpusPacket expectedPacket = OpusPackets.newPacket(Config.of(12), Channel.STEREO, 3);
        expectedPacket.setVbr(true);
        expectedPacket.setFrameCount(3);
        expectedPacket.setHasPadding(true);
        expectedPacket.setPadLenBytesSum(paddingLength);

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
        OpusPacket expectedPacket = OpusPackets.newPacket(Config.of(12), Channel.STEREO, 3);
        expectedPacket.setVbr(true);
        expectedPacket.setFrameCount(3);
        expectedPacket.setHasPadding(true);
        expectedPacket.setPadLenBytesSum(paddingLength);

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
}