package org.chenliang.oggus.opus;

public class OpusUtil {

    public static byte[] frameLengthToBytes(int n) {
        if (n <= 251) {
            return new byte[]{(byte) n};
        }
        int x = 252 + n % 4;
        int y = (n - x) / 4;
        return new byte[]{(byte) x, (byte) y};
    }
}
