package org.chenliang.oggus.opus;

import com.google.common.primitives.Bytes;
import org.chenliang.oggus.TestUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CodeZeroPacketTest {
    @Test
    void should_create_empty_packet_correctly() {
        OpusPacket opusPacket = OpusPackets.newPacket(Config.of(12), Channel.STEREO, 0);
        assertEquals(0, opusPacket.getCode());
        assertFalse(opusPacket.isVbr());
        assertFalse(opusPacket.hasPadding());
        assertEquals(1, opusPacket.getFrameCount());
        assertEquals(0, opusPacket.getPadLenBytesSum());
    }

    @Test
    void should_dump_to_standard_and_self_delimiting_format_correctly() {
        OpusPacket opusPacket = OpusPackets.newPacket(Config.of(12), Channel.STEREO, 0);
        byte[] frameData = TestUtil.createBinary(513, (byte) 1);
        opusPacket.addFrame(frameData);
        byte[] standardBytes = opusPacket.dumpToStandardFormat();
        assertArrayEquals(Bytes.concat(new byte[]{100}, frameData), standardBytes);
        byte[] selfDelimitingBytes = opusPacket.dumpToSelfDelimitingFormat();
        assertArrayEquals(Bytes.concat(new byte[]{100, (byte) 253, 65}, frameData), selfDelimitingBytes);
    }

    @Test
    void should_dump_to_binary_given_a_zero_frame_packet() {
        OpusPacket opusPacket = OpusPackets.newPacket(Config.of(12), Channel.STEREO, 0);
        assertArrayEquals(new byte[]{100}, opusPacket.dumpToStandardFormat());
        assertArrayEquals(new byte[]{100, 0}, opusPacket.dumpToSelfDelimitingFormat());
    }
}