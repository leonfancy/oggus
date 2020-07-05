package me.chenleon.media.container.ogg;

import com.google.common.io.LittleEndianDataInputStream;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static me.chenleon.media.container.ogg.OggPage.CAPTURE_PATTERN;
import static me.chenleon.media.container.ogg.OggPage.MAX_LACE_VALUE;

public class OggStream {
    private LittleEndianDataInputStream in;

    private OggStream(InputStream inputStream) {
        this.in = new LittleEndianDataInputStream(inputStream);
    }

    /**
     * Create {@code OggStream} from a file
     *
     * @param filePath path of an Ogg file
     * @throws FileNotFoundException if the Ogg file doesn't exist
     */
    public static OggStream from(String filePath) throws FileNotFoundException {
        return new OggStream(new BufferedInputStream(new FileInputStream(filePath)));
    }

    /**
     * Create {@code OggStream} from an {@code InputStream}
     *
     * @param inputStream the underlying input stream
     */
    public static OggStream from(InputStream inputStream) {
        return new OggStream(inputStream);
    }

    /**
     * Read an Ogg page.
     * This method will skip invalid data.
     *
     * @return the next Ogg page, or {@code null} if there isn't page left
     * @throws IOException if an I/O error occurs
     */
    public OggPage readPage() throws IOException {
        if (hasNextPage()) {
            return nextPage();
        }
        return null;
    }

    /**
     * Read an Ogg page with the given serial number
     * This method will skip invalid data.
     *
     * @param serialNum the given serial number
     * @return the next Ogg page, or {@code null} if there isn't page left
     * @throws IOException if an I/O error occurs
     */
    public OggPage readPage(long serialNum) throws IOException {
        while (hasNextPage()) {
            OggPage oggPage = nextPage();
            if (oggPage.getSerialNum() == serialNum) {
                return oggPage;
            }
        }
        return null;
    }

    private boolean hasNextPage() throws IOException {
        int posOfPattern = 0;
        while (posOfPattern < CAPTURE_PATTERN.length) {
            int b = in.read();
            if (b == -1) {
                return false;
            }
            if (b == CAPTURE_PATTERN[posOfPattern]) {
                posOfPattern++;
            } else {
                posOfPattern = (b == CAPTURE_PATTERN[0] ? 1 : 0);
            }
        }
        return true;
    }

    private OggPage nextPage() throws IOException {
        OggPage oggPage = OggPage.empty();
        oggPage.setVersion(in.readUnsignedByte());
        oggPage.setFlag(in.readByte());
        oggPage.setGranulePosition(in.readLong());
        oggPage.setSerialNum(Integer.toUnsignedLong(in.readInt()));
        oggPage.setSeqNum(Integer.toUnsignedLong(in.readInt()));
        oggPage.setCheckSum(in.readInt());
        int segCount = in.readUnsignedByte();
        byte[] laceValues = in.readNBytes(segCount);

        int packetLen = 0;
        for (byte laceValue : laceValues) {
            int segLen = Byte.toUnsignedInt(laceValue);
            packetLen += segLen;
            if (segLen < MAX_LACE_VALUE) {
                byte[] data = in.readNBytes(packetLen);
                oggPage.addDataPacket(data);
                packetLen = 0;
            }
        }
        if (packetLen != 0) {
            byte[] data = in.readNBytes(packetLen);
            oggPage.addPartialDataPacket(data);
        }
        return oggPage;
    }
}
