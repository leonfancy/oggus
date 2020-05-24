package me.chenleon.media.container.ogg;

import com.google.common.io.LittleEndianDataInputStream;

import java.io.*;

import static me.chenleon.media.container.ogg.OggPage.CAPTURE_PATTERN;

public class OggStream {
    private static final int MAX_LACE_VALUE = 255;
    private LittleEndianDataInputStream in;

    /**
     * Create {@code OggStream} from a file
     *
     * @param filePath path of an Ogg file
     * @throws FileNotFoundException if the Ogg file doesn't exist
     */
    public OggStream(String filePath) throws FileNotFoundException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(filePath));
        this.in = new LittleEndianDataInputStream(bufferedInputStream);
    }

    /**
     * @param inputStream the underlying input stream
     */
    public OggStream(InputStream inputStream) {
        this.in = new LittleEndianDataInputStream(inputStream);
    }

    /**
     * Read an Ogg page
     *
     * @return the next Ogg page, or {@code null} if there isn't page left
     * @throws IOException if an I/O error occurs
     */
    public OggPage readPage() throws IOException {
        if(hasNextPage()) {
            return nextPage();
        }
        return null;
    }

    /**
     * Read an Ogg page with the given serial number
     *
     * @param serialNum the given serial number
     * @return the next Ogg page, or {@code null} if there isn't page left
     * @throws IOException if an I/O error occurs
     */
    public OggPage readPage(int serialNum) throws IOException {
        while (hasNextPage()) {
            OggPage oggPage = nextPage();
            if(oggPage.getSerialNum() == serialNum) {
                return oggPage;
            }
        }
        return null;
    }

    public boolean hasNextPage() throws IOException {
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

    public OggPage nextPage() throws IOException {
        OggPage oggPage = new OggPage();
        oggPage.setVersion(in.readUnsignedByte());
        oggPage.setFlag(in.readByte());
        oggPage.setGranulePosition(in.readLong());
        oggPage.setSerialNum(Integer.toUnsignedLong(in.readInt()));
        oggPage.setSeqNum(Integer.toUnsignedLong(in.readInt()));
        oggPage.setCheckSum(in.readInt());
        oggPage.setSegCount(in.readUnsignedByte());
        byte[] laceValues = in.readNBytes(oggPage.getSegCount());
        oggPage.setLaceValues(laceValues);

        int packetLen = 0;
        for (byte laceValue : laceValues) {
            int segLen = Byte.toUnsignedInt(laceValue);
            packetLen += segLen;
            if (segLen < MAX_LACE_VALUE) {
                byte[] data = in.readNBytes(packetLen);
                OggPacket oggPacket = new OggPacket(data);
                oggPage.addOggPacket(oggPacket);
                oggPage.addOggDataPacket(data);
                packetLen = 0;
            }
        }
        if (packetLen != 0) {
            byte[] data = in.readNBytes(packetLen);
            OggPacket oggPacket = new OggPacket(data, true);
            oggPage.addOggPacket(oggPacket);
            oggPage.addOggDataPacket(data);
        }
        return oggPage;
    }
}
