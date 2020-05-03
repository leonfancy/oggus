package me.chenleon.media.container.ogg;

import java.util.ArrayList;

public class OggPage {
    Integer version;
    Byte flag;
    Long granulePosition;
    Long serialNum;
    Integer seqNum;
    Integer checkSum;
    Integer segCount;
    private byte[] segTable;
    private ArrayList<OggPacket> oggPackets = new ArrayList<>();

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public void setFlag(Byte flag) {
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

    public Long getGranulePosition() {
        return granulePosition;
    }

    public void setGranulePosition(Long granulePosition) {
        this.granulePosition = granulePosition;
    }

    public long getSerialNum() {
        return serialNum;
    }

    public void setSerialNum(Long serialNum) {
        this.serialNum = serialNum;
    }

    public Integer getSeqNum() {
        return seqNum;
    }

    public void setSeqNum(Integer seqNum) {
        this.seqNum = seqNum;
    }

    public Integer getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(Integer checkSum) {
        this.checkSum = checkSum;
    }

    public Integer getSegCount() {
        return segCount;
    }

    public void setSegCount(Integer segCount) {
        this.segCount = segCount;
    }

    public void setSegTable(byte[] segTable) {
        this.segTable = segTable;
    }

    public byte[] getSegTable() {
        return segTable;
    }

    public void addOggPacket(OggPacket oggPacket) {
        this.oggPackets.add(oggPacket);
    }
}
