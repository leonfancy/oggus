package me.chenleon.media.audio.opus;

class CodeZeroPacket extends FixedFrameCountPacket {
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
    public int getPadLenBytesSum() {
        return 0;
    }
}
