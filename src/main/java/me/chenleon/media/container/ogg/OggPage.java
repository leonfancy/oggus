package me.chenleon.media.container.ogg;

import com.google.common.io.LittleEndianDataOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class OggPage {
    public static final byte[] CAPTURE_PATTERN = {'O', 'g', 'g', 'S'};
    private int version;
    private byte flag;
    private long granulePosition;
    private long serialNum;
    private long seqNum;
    private int checkSum;
    private int segCount;
    private byte[] laceValues;
    private final LinkedList<OggPacket> oggPackets = new LinkedList<>();
    private final List<byte[]> oggDataPackets = new LinkedList<>();

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setFlag(byte flag) {
        this.flag = flag;
    }

    public boolean isContinued() {
        return (this.flag & 0x01) != 0;
    }

    public boolean isBOS() {
        return (this.flag & 0x02) != 0;
    }

    public boolean isEOS() {
        return (this.flag & 0x04) != 0;
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
        return segCount;
    }

    public void setSegCount(int segCount) {
        this.segCount = segCount;
    }

    public void setLaceValues(byte[] laceValues) {
        this.laceValues = laceValues;
    }

    public byte[] getLaceValues() {
        return laceValues;
    }

    public void addOggDataPacket(byte[] data) {
        oggDataPackets.add(data);
    }

    public List<byte[]> getOggDataPacket() {
        return oggDataPackets;
    }

    public void addOggPacket(OggPacket oggPacket) {
        this.oggPackets.add(oggPacket);
    }

    public LinkedList<OggPacket> getOggPackets() {
        return oggPackets;
    }

    public byte[] dump() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        LittleEndianDataOutputStream out = new LittleEndianDataOutputStream(byteArrayOutputStream);

        try {
            out.write(CAPTURE_PATTERN);
            out.write(version);
            out.write(flag);
            out.writeLong(granulePosition);
            out.writeInt((int)serialNum);
            out.writeInt((int)seqNum);
            out.writeInt(checkSum);
            out.write(segCount);
            out.write(laceValues);
            for (byte[] oggDataPacket : oggDataPackets) {
                out.write(oggDataPacket);
            }
        } catch (IOException e) {
            throw new DumpException("OggPage dump to byte array error", e);
        }
        return byteArrayOutputStream.toByteArray();
    }
}
