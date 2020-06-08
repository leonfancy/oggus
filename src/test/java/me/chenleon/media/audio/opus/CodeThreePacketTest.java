package me.chenleon.media.audio.opus;

import com.google.common.primitives.Bytes;
import me.chenleon.media.TestUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CodeThreePacketTest {
    @Test
    void should_create_packet_and_set_fields_correctly() {
        OpusPacket opusPacket = OpusPackets.newPacketOfCode(3);
        opusPacket.setVbr(true);
        opusPacket.setFrameCount(10);
        opusPacket.setHasPadding(true);
        opusPacket.setPaddingLength(12);

        assertEquals(3, opusPacket.getCode());
        assertTrue(opusPacket.isVbr());
        assertTrue(opusPacket.hasPadding());
        assertEquals(10, opusPacket.getFrameCount());
        assertEquals(12, opusPacket.getPaddingLength());
    }

    @Test
    void should_dump_to_standard_and_self_delimiting_format_given_vbr_no_padding_packet() {
        OpusPacket opusPacket = OpusPackets.newPacketOfCode(3);
        opusPacket.setConfig(Config.of(12));
        opusPacket.setMono(false);
        opusPacket.setVbr(true);
        opusPacket.setFrameCount(3);
        opusPacket.setHasPadding(false);

        byte[] frameData1 = TestUtil.createFrameData(513, (byte) 1);
        byte[] frameData2 = TestUtil.createFrameData(514, (byte) 2);
        byte[] frameData3 = TestUtil.createFrameData(515, (byte) 3);

        opusPacket.addFrame(frameData1);
        opusPacket.addFrame(frameData2);
        opusPacket.addFrame(frameData3);

        byte[] expectedStandardBytes = Bytes.concat(new byte[]{103, (byte) 253, 65, (byte) 254, 65}, frameData1,
                frameData2, frameData3);
        byte[] expectedSelfDelimitingBytes = Bytes.concat(new byte[]{103, (byte) 253, 65, (byte) 254, 65, (byte) 255,
                65}, frameData1, frameData2, frameData3);

        assertArrayEquals(expectedStandardBytes, opusPacket.dumpToStandardFormat());
        assertArrayEquals(expectedSelfDelimitingBytes, opusPacket.dumpToSelfDelimitingFormat());
    }

    @Test
    void should_dump_to_standard_and_self_delimiting_format_given_cbr_no_padding_packet() {
        OpusPacket opusPacket = OpusPackets.newPacketOfCode(3);
        opusPacket.setConfig(Config.of(12));
        opusPacket.setMono(false);
        opusPacket.setVbr(false);
        opusPacket.setFrameCount(3);
        opusPacket.setHasPadding(false);

        byte[] frameData1 = TestUtil.createFrameData(513, (byte) 1);
        byte[] frameData2 = TestUtil.createFrameData(513, (byte) 2);
        byte[] frameData3 = TestUtil.createFrameData(513, (byte) 3);

        opusPacket.addFrame(frameData1);
        opusPacket.addFrame(frameData2);
        opusPacket.addFrame(frameData3);

        byte[] expectedStandardBytes = Bytes.concat(new byte[]{103}, frameData1, frameData2, frameData3);
        byte[] expectedSelfDelimitingBytes = Bytes.concat(new byte[]{103, (byte) 253, 65}, frameData1, frameData2,
                frameData3);

        assertArrayEquals(expectedStandardBytes, opusPacket.dumpToStandardFormat());
        assertArrayEquals(expectedSelfDelimitingBytes, opusPacket.dumpToSelfDelimitingFormat());
    }
}