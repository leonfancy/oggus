package me.chenleon.media.audio.opus;

class CodeOnePacket extends FixedFrameCountPacket {
    @Override
    public int getCode() {
        return 1;
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
        return 2;
    }

    @Override
    public int getPadLenBytesSum() {
        return 0;
    }
}
