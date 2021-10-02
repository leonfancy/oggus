package org.chenliang.oggus.ogg;

import com.google.common.io.LittleEndianDataInputStream;

import java.io.*;

/**
 * An Ogg stream is made up of a sequence of Ogg pages. An Ogg stream stream could be multiplexed by several logical
 * Ogg stream which could be identified with the {@code serialNum}.
 */
public class OggStream implements Closeable {
    private LittleEndianDataInputStream in;

    private OggStream(InputStream inputStream) {
        this.in = new LittleEndianDataInputStream(inputStream);
    }

    /**
     * Create {@code OggStream} from a file.
     *
     * @param filePath path of an Ogg file
     * @throws FileNotFoundException if the Ogg file doesn't exist.
     * @return OggStream
     */
    public static OggStream from(String filePath) throws FileNotFoundException {
        return new OggStream(new BufferedInputStream(new FileInputStream(filePath)));
    }

    /**
     * Create {@code OggStream} from an {@code InputStream}.
     *
     * @param inputStream the underlying input stream.
     * @return OggStream
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

    /**
     * Close the underlying {@link LittleEndianDataInputStream}
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        in.close();
    }

    private boolean hasNextPage() throws IOException {
        int posOfPattern = 0;
        while (posOfPattern < OggPage.CAPTURE_PATTERN.length) {
            int b = in.read();
            if (b == -1) {
                return false;
            }
            if (b == OggPage.CAPTURE_PATTERN[posOfPattern]) {
                posOfPattern++;
            } else {
                posOfPattern = (b == OggPage.CAPTURE_PATTERN[0] ? 1 : 0);
            }
        }
        return true;
    }

    private OggPage nextPage() throws IOException {
        OggPage oggPage = OggPage.empty();
        int version = in.readUnsignedByte();
        if (version != 0) {
            throw new InvalidOggException("Unsupported Ogg page version: " + version);
        }
        oggPage.setFlag(in.readUnsignedByte());
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
            if (segLen < OggPage.MAX_LACE_VALUE) {
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
