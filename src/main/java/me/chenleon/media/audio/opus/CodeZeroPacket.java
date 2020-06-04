package me.chenleon.media.audio.opus;

import me.chenleon.media.container.ogg.DumpException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

class CodeZeroPacket extends OpusPacket {
    protected CodeZeroPacket() {
    }

    @Override
    public int getCode() {
        return 0;
    }

    @Override
    public boolean isVbr() {
        return false;
    }

    @Override
    public boolean hasPadding() {
        return false;
    }

    @Override
    public int getFrameCount() {
        return 1;
    }

    @Override
    public int getPaddingLength() {
        return 0;
    }

    @Override
    public byte[] dumpToStandardFormat() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            out.write(getTocByte());
            for (byte[] frame : frames) {
                out.write(frame);
            }
        } catch (IOException e) {
            throw new DumpException("OpusPacket dump to byte array error", e);
        }

        return out.toByteArray();
    }

    @Override
    public byte[] dumpToSelfDelimitedFormat() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            out.write(getTocByte());
            out.write(OpusUtil.frameLengthToBytes(frames.get(0).length));
            for (byte[] frame : frames) {
                out.write(frame);
            }
        } catch (IOException e) {
            throw new DumpException("OpusPacket dump to byte array error", e);
        }

        return out.toByteArray();
    }
}
