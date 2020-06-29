package me.chenleon.media;

import me.chenleon.media.audio.opus.OpusPacket;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestUtil {
    public static byte[] createBinary(int length, byte content) {
        byte[] frameData1 = new byte[length];
        Arrays.fill(frameData1, content);
        return frameData1;
    }

    public static void assertOpusPacketEqual(OpusPacket expected, OpusPacket actual) {
        assertEquals(expected.getCode(), actual.getCode());
        assertEquals(expected.getConfig().getId(), actual.getConfig().getId());
        assertEquals(expected.isMono(), actual.isMono());
        assertEquals(expected.isVbr(), actual.isVbr());
        assertEquals(expected.hasPadding(), actual.hasPadding());
        assertEquals(expected.getPadLenBytesSum(), actual.getPadLenBytesSum());
        assertEquals(expected.getFrameCount(), actual.getFrameCount());
        assertEquals(expected.getFrames().size(), actual.getFrames().size());
        for (int i = 0; i < expected.getFrames().size(); i++) {
            assertArrayEquals(expected.getFrames().get(i), actual.getFrames().get(i));
        }
    }
}
