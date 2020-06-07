package me.chenleon.media.audio.opus;

import com.google.common.primitives.Bytes;
import me.chenleon.media.TestUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CodeZeroPacketTest {
    @Test
    void should_create_empty_packet_correctly() {
        OpusPacket opusPacket = OpusPackets.newPacketOfCode(0);
        assertEquals(0, opusPacket.getCode());
        assertFalse(opusPacket.isVbr());
        assertFalse(opusPacket.hasPadding());
        assertEquals(1, opusPacket.getFrameCount());
        assertEquals(0, opusPacket.getPaddingLength());
    }

    @Test
    void should_dump_to_standard_and_self_delimiting_format_correctly() {
        OpusPacket opusPacket = OpusPackets.newPacketOfCode(0);
        opusPacket.setConfig(Config.of(12));
        opusPacket.setMono(false);
        byte[] frameData = TestUtil.createFrameData(513, (byte) 1);
        opusPacket.addFrame(frameData);
        byte[] standardBytes = opusPacket.dumpToStandardFormat();
        assertArrayEquals(Bytes.concat(new byte[]{100}, frameData), standardBytes);
        byte[] selfDelimitingBytes = opusPacket.dumpToSelfDelimitingFormat();
        assertArrayEquals(Bytes.concat(new byte[]{100, (byte) 253, 65}, frameData), selfDelimitingBytes);
    }
}