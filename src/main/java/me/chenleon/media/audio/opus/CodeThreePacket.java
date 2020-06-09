package me.chenleon.media.audio.opus;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

class CodeThreePacket extends OpusPacket {
    private boolean isVbr;
    private boolean hasPadding;
    private int frameCount;
    private int paddingLength;

    public void setVbr(boolean isVbr) {
        this.isVbr = isVbr;
    }

    public void setHasPadding(boolean hasPadding) {
        this.hasPadding = hasPadding;
    }

    public void setFrameCount(int frameCount) {
        this.frameCount = frameCount;
    }

    public void setPaddingLength(int paddingLength) {
        this.paddingLength = paddingLength;
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
    public int getPaddingLength() {
        return paddingLength;
    }

    @Override
    public byte[] dumpToStandardFormat() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(getTocByte());
        out.write(getFrameCountByte());
        byte[] padding = new byte[0];
        if (hasPadding) {
            int byteCountOf255 = paddingLength / 255;
            int lastByteValue = paddingLength - byteCountOf255 * 255;
            byte[] paddingLengthBytes = new byte[byteCountOf255 + 1];
            Arrays.fill(paddingLengthBytes, 0, byteCountOf255, (byte) 255);
            paddingLengthBytes[paddingLengthBytes.length - 1] = (byte) lastByteValue;
            out.writeBytes(paddingLengthBytes);
            padding = new byte[paddingLength - byteCountOf255];
        }
        if (isVbr) {
            for (int i = 0; i < frames.size() - 1; i++) {
                out.writeBytes(OpusUtil.frameLengthToBytes(frames.get(i).length));
            }
        }
        for (byte[] frame : frames) {
            out.writeBytes(frame);
        }
        if (hasPadding) {
            out.writeBytes(padding);
        }
        return out.toByteArray();
    }

    @Override
    public byte[] dumpToSelfDelimitingFormat() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        out.write(getTocByte());
        out.write(getFrameCountByte());
        byte[] padding = new byte[0];
        if (hasPadding) {
            int byteCountOf255 = paddingLength / 255;
            int lastByteValue = paddingLength - byteCountOf255 * 255;
            byte[] paddingLengthBytes = new byte[byteCountOf255 + 1];
            Arrays.fill(paddingLengthBytes, 0, byteCountOf255, (byte) 255);
            paddingLengthBytes[paddingLengthBytes.length - 1] = (byte) lastByteValue;
            out.writeBytes(paddingLengthBytes);
            padding = new byte[paddingLength - byteCountOf255];
        }
        if (isVbr) {
            for (byte[] frame : frames) {
                out.writeBytes(OpusUtil.frameLengthToBytes(frame.length));
            }
        } else {
            out.writeBytes(OpusUtil.frameLengthToBytes(frames.get(0).length));
        }
        for (byte[] frame : frames) {
            out.writeBytes(frame);
        }
        if (hasPadding) {
            out.writeBytes(padding);
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
