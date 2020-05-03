package me.chenleon.media.container.ogg;

import com.google.common.io.LittleEndianDataInputStream;

import java.io.IOException;
import java.io.InputStream;

public class OggStream {
    private static final int MAX_SEG_LEN = 255;
    private static final byte[] CAPTURE_PATTERN = {'O', 'g', 'g', 'S'};
    private LittleEndianDataInputStream in;

    public OggStream(InputStream in) {
        this.in = new LittleEndianDataInputStream(in);
    }

    public boolean hasNextPage() throws IOException {
        int i = 0;
        while (i < CAPTURE_PATTERN.length) {
            int b = in.read();
            if (b == -1) {
                return false;
            }
            if (b == CAPTURE_PATTERN[i]) {
                i++;
            } else {
                i = (b == CAPTURE_PATTERN[0] ? 1 : 0);
            }
        }
        return true;
    }

    public OggPage nextPage() throws IOException {
        OggPage oggPage = new OggPage();
        oggPage.setVersion(in.readUnsignedByte());
        oggPage.setFlag(in.readByte());
        oggPage.setGranulePosition(in.readLong());
        oggPage.setSerialNum(Integer.toUnsignedLong(in.readInt()));
        oggPage.setSeqNum(in.readInt());
        oggPage.setCheckSum(in.readInt());
        oggPage.setSegCount(in.readUnsignedByte());
        byte[] segTable = in.readNBytes(oggPage.getSegCount());
        oggPage.setSegTable(segTable);

        int packetLen = 0;
        for (byte laceValue : segTable) {
            int segLen = Byte.toUnsignedInt(laceValue);
            packetLen += segLen;
            if (segLen < MAX_SEG_LEN) {
                byte[] packetBytes = in.readNBytes(packetLen);

                OggPacket oggPacket = new OggPacket(packetBytes);

                oggPage.addOggPacket(oggPacket);
                packetLen = 0;
            }
        }
        if (packetLen != 0) {
            byte[] packetBytes = in.readNBytes(packetLen);
            OggPacket oggPacket = new OggPacket(packetBytes, true);
            oggPage.addOggPacket(oggPacket);
        }
        return oggPage;
    }
}
