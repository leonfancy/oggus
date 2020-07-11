package org.chenliang.oggus.opus;

import java.io.ByteArrayOutputStream;

/**
 * This is the parent class of {@link CodeZeroPacket}, {@link CodeOnePacket} and {@link CodeTwoPacket}.
 * These Opus packets contains a fixed number of frames.
 */
abstract class FixedFrameCountPacket extends OpusPacket {
    public void setVbr(boolean isVbr) {
        throw new IllegalStateException("Code 0 to 2 Opus packet doesn't support setting Vbr flag");
    }

    public void setHasPadding(boolean hasPadding) {
        throw new IllegalStateException("Code 0 to 2 packet doesn't support setting padding flag");
    }

    public void setFrameCount(int frameCount) {
        throw new IllegalStateException("Code 0 to 2 packet doesn't support setting frame count");
    }

    public void setPadLenBytesSum(int padLenBytesSum) {
        throw new IllegalStateException("Code 0 to 2 packet doesn't support setting padding bytes length sum");
    }

    @Override
    public byte[] dumpToStandardFormat() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(getTocByte());
        if (frames.size() == 0) {
            if (!isVbr()) {
                return out.toByteArray();
            }

            for (int i = 0; i < getFrameCount() - 1; i++) {
                out.write(0);
            }

            return out.toByteArray();
        }

        if (isVbr()) {
            for (int i = 0; i < getFrameCount() - 1; i++) {
                out.writeBytes(OpusUtil.frameLengthToBytes(frames.get(i).length));
            }
        }

        writeFrames(out);

        return out.toByteArray();
    }

    @Override
    public byte[] dumpToSelfDelimitingFormat() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(getTocByte());
        if (frames.size() == 0) {
            int numberOfLength = isVbr() ? getFrameCount() : 1;
            for (int i = 0; i < numberOfLength; i++) {
                out.write(0);
            }
            return out.toByteArray();
        }

        if (isVbr()) {
            for (int i = 0; i < getFrameCount(); i++) {
                out.writeBytes(OpusUtil.frameLengthToBytes(frames.get(i).length));
            }
        } else {
            out.writeBytes(OpusUtil.frameLengthToBytes(frames.get(0).length));
        }

        writeFrames(out);

        return out.toByteArray();
    }

    private void writeFrames(ByteArrayOutputStream out) {
        for (byte[] frame : frames) {
            out.writeBytes(frame);
        }
    }
}
