package me.chenleon.media.audio.opus;

import java.io.ByteArrayOutputStream;

/**
 * An Opus packet that described in RFC6716
 */
class CodeTwoPacket extends OpusPacket {
    @Override
    public int getCode() {
        return 2;
    }

    @Override
    public boolean isVbr() {
        return true;
    }

    @Override
    public boolean hasPadding() {
        return false;
    }

    @Override
    public int getFrameCount() {
        return 2;
    }

    @Override
    public int getPadLenBytesSum() {
        return 0;
    }

    @Override
    public byte[] dumpToStandardFormat() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        out.write(getTocByte());
        if (frames.size() == 0) {
            out.write(0);
        } else {
            out.writeBytes(OpusUtil.frameLengthToBytes(frames.get(0).length));
        }
        for (byte[] frame : frames) {
            out.writeBytes(frame);
        }

        return out.toByteArray();
    }

    @Override
    public byte[] dumpToSelfDelimitingFormat() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        out.write(getTocByte());
        if (frames.size() == 0) {
            out.write(0);
            out.write(0);
        } else {
            out.writeBytes(OpusUtil.frameLengthToBytes(frames.get(0).length));
            out.writeBytes(OpusUtil.frameLengthToBytes(frames.get(1).length));
        }
        for (byte[] frame : frames) {
            out.writeBytes(frame);
        }

        return out.toByteArray();
    }
}
