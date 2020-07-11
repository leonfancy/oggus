package org.chenliang.oggus.ogg;

import com.google.common.io.LittleEndianDataOutputStream;
import com.google.common.primitives.Bytes;
import org.chenliang.oggus.opus.InvalidOpusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Ogg page that defined by <a href="https://tools.ietf.org/html/rfc3533">RFC3533</a>.
 *
 * <p>Create a new OggPage object with static method: {@link OggPage#empty()}. Call set...() methods to set fields of
 * Ogg page.
 */
public class OggPage {
    public static final byte[] CAPTURE_PATTERN = {'O', 'g', 'g', 'S'};
    public static final int MAX_LACE_VALUE = 255;
    private int version = 0;
    private byte flag = 0x00;
    private long granulePosition;
    private long serialNum;
    private long seqNum;
    private int checkSum;
    private byte[] laceValues = new byte[0];
    private final List<byte[]> dataPackets = new LinkedList<>();

    private OggPage() {
    }

    /**
     * @return a new {@code OggPage} object with fields unset
     */
    public static OggPage empty() {
        return new OggPage();
    }

    /**
     * Get the Ogg page spec version, currently only version 0 is supported.
     *
     * @return version number
     */
    public int getVersion() {
        return version;
    }

    public void setFlag(byte flag) {
        this.flag = flag;
    }

    public boolean isContinued() {
        return (this.flag & 0x01) != 0;
    }

    public void setContinued() {
        flag = (byte) (flag | 0x01);
    }

    public boolean isBOS() {
        return (this.flag & 0x02) != 0;
    }

    public void setBOS() {
        flag = (byte) (flag | 0x02);
    }

    public boolean isEOS() {
        return (this.flag & 0x04) != 0;
    }

    public void setEOS() {
        flag = (byte) (flag | 0x04);
    }

    public long getGranulePosition() {
        return granulePosition;
    }

    public void setGranulePosition(long granulePosition) {
        this.granulePosition = granulePosition;
    }

    public long getSerialNum() {
        return serialNum;
    }

    public void setSerialNum(long serialNum) {
        this.serialNum = serialNum;
    }

    public long getSeqNum() {
        return seqNum;
    }

    public void setSeqNum(long seqNum) {
        this.seqNum = seqNum;
    }

    public int getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(int checkSum) {
        this.checkSum = checkSum;
    }

    public int getSegCount() {
        return laceValues != null ? laceValues.length : 0;
    }

    public byte[] getLaceValues() {
        return laceValues;
    }

    public boolean isCompleted() {
        return Byte.toUnsignedInt(laceValues[getSegCount() - 1]) < MAX_LACE_VALUE;
    }

    public void addDataPacket(byte[] data) {
        laceValues = Bytes.concat(laceValues, lenToLaceValues(data.length, false));
        dataPackets.add(data);
    }

    public void addPartialDataPacket(byte[] data) {
        if (data.length % 255 != 0) {
            throw new InvalidOpusException("Not a partial data packet");
        }
        laceValues = Bytes.concat(laceValues, lenToLaceValues(data.length, true));
        dataPackets.add(data);
    }

    public List<byte[]> getDataPackets() {
        return dataPackets;
    }

    public byte[] dump() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        LittleEndianDataOutputStream out = new LittleEndianDataOutputStream(byteArrayOutputStream);

        try {
            out.write(CAPTURE_PATTERN);
            out.write(version);
            out.write(flag);
            out.writeLong(granulePosition);
            out.writeInt((int) serialNum);
            out.writeInt((int) seqNum);
            out.writeInt(checkSum);
            out.write(getSegCount());
            out.write(laceValues);
            for (byte[] dataPacket : dataPackets) {
                out.write(dataPacket);
            }
        } catch (IOException e) {
            throw new RuntimeException("OggPage dump to byte array error", e);
        }
        return byteArrayOutputStream.toByteArray();
    }

    private byte[] lenToLaceValues(int len, boolean isPartial) {
        int countOf255 = len / 255;
        if (isPartial) {
            byte[] laceValues = new byte[countOf255];
            Arrays.fill(laceValues, (byte) 255);
            return laceValues;
        } else {
            int lastValue = len % 255;
            byte[] laceValues = new byte[countOf255 + 1];
            Arrays.fill(laceValues, 0, countOf255, (byte) 255);
            laceValues[countOf255] = (byte) lastValue;
            return laceValues;
        }
    }
}
