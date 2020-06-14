package me.chenleon.media.audio.opus;

import java.io.ByteArrayOutputStream;

class CodeThreePacket extends OpusPacket {
    private boolean isVbr;
    private boolean hasPadding;
    private int frameCount;
    private int padLenBytesSum;

    public void setVbr(boolean isVbr) {
        this.isVbr = isVbr;
    }

    public void setHasPadding(boolean hasPadding) {
        this.hasPadding = hasPadding;
    }

    public void setFrameCount(int frameCount) {
        this.frameCount = frameCount;
    }

    public void setPadLenBytesSum(int padLenBytesSum) {
        this.padLenBytesSum = padLenBytesSum;
    }

    @Override
    public int getCode() {
        return 3;
    }

    @Override
    public boolean isVbr() {
        return isVbr;
    }

    @Override
    public boolean hasPadding() {
        return hasPadding;
    }

    @Override
    public int getFrameCount() {
        return frameCount;
    }

    @Override
    public int getPadLenBytesSum() {
        return padLenBytesSum;
    }

    @Override
    public byte[] dumpToStandardFormat() {
        return dump(true);
    }

    @Override
    public byte[] dumpToSelfDelimitingFormat() {
        return dump(false);
    }

    private byte[] dump(boolean isStandard) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        out.write(getTocByte());
        out.write(getFrameCountByte());
        if (hasPadding) {
            int byteCountOf255 = padLenBytesSum / 255;
            int lastByteValue = padLenBytesSum - byteCountOf255 * 255;
            for (int i = 0; i < byteCountOf255; i++) {
                out.write(255);
            }
            out.write(lastByteValue);
        }
        if (isVbr) {
            int count = isStandard ? frames.size() - 1 : frames.size();
            for (int i = 0; i < count; i++) {
                out.writeBytes(OpusUtil.frameLengthToBytes(frames.get(i).length));
            }
        } else {
            if (!isStandard) {
                out.writeBytes(OpusUtil.frameLengthToBytes(frames.get(0).length));
            }
        }
        for (byte[] frame : frames) {
            out.writeBytes(frame);
        }
        if (hasPadding) {
            out.writeBytes(new byte[getPadDataLen()]);
        }

        return out.toByteArray();
    }

    private byte getFrameCountByte() {
        int b = 0;
        if (isVbr) {
            b = b | 0x80;
        }
        if (hasPadding) {
            b = b | 0x40;
        }
        b = b | frameCount;
        return (byte) b;
    }
}
