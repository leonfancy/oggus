package me.chenleon.media.audio.opus;

import com.google.common.primitives.Bytes;
import me.chenleon.media.TestUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CodeTwoPacketTest {
    @Test
    void should_create_empty_packet_correctly() {
        OpusPacket opusPacket = OpusPackets.newPacketOfCode(2);
        assertEquals(2, opusPacket.getCode());
        assertTrue(opusPacket.isVbr());
        assertFalse(opusPacket.hasPadding());
        assertEquals(2, opusPacket.getFrameCount());
        assertEquals(0, opusPacket.getPaddingLength());
    }

    @Test
    void should_dump_to_standard_and_self_delimiting_format_correctly() {
        OpusPacket opusPacket = OpusPackets.newPacketOfCode(2);
        opusPacket.setConfig(Config.of(12));
        opusPacket.setMono(false);
        byte[] frameData1 = TestUtil.createBinary(513, (byte) 1);
        byte[] frameData2 = TestUtil.createBinary(514, (byte) 2);
        opusPacket.addFrame(frameData1);
        opusPacket.addFrame(frameData2);
        byte[] standardBytes = opusPacket.dumpToStandardFormat();
        assertArrayEquals(Bytes.concat(new byte[]{102, (byte) 253, 65}, frameData1, frameData2), standardBytes);
        byte[] selfDelimitingBytes = opusPacket.dumpToSelfDelimitingFormat();
        assertArrayEquals(Bytes.concat(new byte[]{102, (byte) 253, 65, (byte) 254, 65}, frameData1, frameData2),
                selfDelimitingBytes);
    }
}