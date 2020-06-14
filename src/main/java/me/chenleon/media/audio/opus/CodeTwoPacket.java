package me.chenleon.media.audio.opus;

/**
 * An Opus packet that described in RFC6716
 */
class CodeTwoPacket extends FixedFrameCountPacket {
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
}
