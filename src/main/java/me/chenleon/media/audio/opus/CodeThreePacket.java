package me.chenleon.media.audio.opus;

import me.chenleon.media.container.ogg.DumpException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

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

        try {
            out.write(getTocByte());
            if (hasPadding) {
                out.write(paddingLength);
            }
            if (isVbr) {
                for (int i = 0; i < frames.size() - 1; i++) {
                    out.write(OpusUtil.frameLengthToBytes(frames.get(i).length));
                }
            }
            for (byte[] frame : frames) {
                out.write(frame);
            }
            if (hasPadding) {
                out.writeBytes(new byte[paddingLength]);
            }
        } catch (IOException e) {
            throw new DumpException("OpusPacket dump to byte array error", e);
        }
        return out.toByteArray();
    }

    @Override
    public byte[] dumpToSelfDelimitingFormat() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            out.write(getTocByte());
            if (hasPadding) {
                out.write(paddingLength);
            }
            if (isVbr) {
                for (byte[] frame : frames) {
                    out.write(OpusUtil.frameLengthToBytes(frame.length));
                }
            } else {
                out.write(OpusUtil.frameLengthToBytes(frames.get(0).length));
            }
            for (byte[] frame : frames) {
                out.write(frame);
            }
            if (hasPadding) {
                out.writeBytes(new byte[paddingLength]);
            }
        } catch (IOException e) {
            throw new DumpException("OpusPacket dump to byte array error", e);
        }

        return out.toByteArray();
    }
}
