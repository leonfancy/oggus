package me.chenleon.media;

import java.util.Arrays;

public class TestUtil {
    public static byte[] createBinary(int length, byte content) {
        byte[] frameData1 = new byte[length];
        Arrays.fill(frameData1, content);
        return frameData1;
    }
}
