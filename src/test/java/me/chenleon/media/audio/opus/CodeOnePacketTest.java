package me.chenleon.media.audio.opus;

import com.google.common.primitives.Bytes;
import me.chenleon.media.TestUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CodeOnePacketTest {
    @Test
    void should_create_empty_packet_correctly() {
        OpusPacket opusPacket = OpusPackets.newPacketOfCode(1);
        assertEquals(1, opusPacket.getCode());
        assertFalse(opusPacket.isVbr());
        assertFalse(opusPacket.hasPadding());
        assertEquals(2, opusPacket.getFrameCount());
        assertEquals(0, opusPacket.getPaddingLength());
    }

    @Test
    void should_dump_to_standard_and_self_delimiting_format_correctly() {
        OpusPacket opusPacket = OpusPackets.newPacketOfCode(1);
        opusPacket.setConfig(Config.of(12));
        opusPacket.setMono(false);
        byte[] frameData1 = TestUtil.createFrameData(513, (byte) 1);
        byte[] frameData2 = TestUtil.createFrameData(513, (byte) 2);
        opusPacket.addFrame(frameData1);
        opusPacket.addFrame(frameData2);
        byte[] standardBytes = opusPacket.dumpToStandardFormat();
        assertArrayEquals(Bytes.concat(new byte[]{101}, frameData1, frameData2), standardBytes);
        byte[] selfDelimitingBytes = opusPacket.dumpToSelfDelimitingFormat();
        assertArrayEquals(Bytes.concat(new byte[]{101, (byte) 253, 65}, frameData1, frameData2), selfDelimitingBytes);
    }
}