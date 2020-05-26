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
}
