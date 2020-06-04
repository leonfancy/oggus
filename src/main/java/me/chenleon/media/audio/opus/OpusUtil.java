package me.chenleon.media.audio.opus;

import me.chenleon.media.container.ogg.OggPage;

import java.util.Arrays;

public class OpusUtil {
    public static boolean isIdHeaderPage(OggPage oggPage) {
        if (oggPage.getOggDataPackets().size() == 0) {
            return false;
        }
        byte[] data = oggPage.getOggDataPackets().get(0);
        return Arrays.equals(data, 0, 7, IdHeader.MAGIC_SIGNATURE, 0, 7);
    }

    public static byte[] frameLengthToBytes(int n) {
        if (n <= 251) {
            return new byte[]{(byte) n};
        }
        int x = 252 + n % 4;
        int y = (n - x) / 4;
        return new byte[]{(byte) x, (byte) y};
    }
}
