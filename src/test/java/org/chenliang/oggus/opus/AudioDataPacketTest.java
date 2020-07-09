package org.chenliang.oggus.opus;

import com.google.common.primitives.Bytes;
import org.chenliang.oggus.TestUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AudioDataPacketTest {
    @Test
    void should_create_audio_data_packet_and_dump_to_binary() {
        AudioDataPacket audioDataPacket = AudioDataPacket.empty();
        OpusPacket opusPacket1 = OpusPackets.newPacket(Config.of(12), Channel.STEREO, 0);
        opusPacket1.addFrame(TestUtil.createBinary(100, (byte) 1));

        OpusPacket opusPacket2 = OpusPackets.newPacket(Config.of(12), Channel.STEREO, 0);
        opusPacket2.addFrame(TestUtil.createBinary(100, (byte) 2));

        audioDataPacket.addOpusPacket(opusPacket1);
        audioDataPacket.addOpusPacket(opusPacket2);

        byte[] expected = Bytes.concat(opusPacket1.dumpToSelfDelimitingFormat(), opusPacket2.dumpToStandardFormat());
        assertArrayEquals(expected, audioDataPacket.dump());
    }
}