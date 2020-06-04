package me.chenleon.media.audio.opus;

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
        return new byte[0];
    }

    @Override
    public byte[] dumpToSelfDelimitedFormat() {
        return new byte[0];
    }
}
