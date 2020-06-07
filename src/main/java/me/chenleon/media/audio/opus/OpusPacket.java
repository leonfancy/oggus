package me.chenleon.media.audio.opus;

import java.util.LinkedList;
import java.util.List;

/**
 * An Opus packet that described in <a href=https://tools.ietf.org/html/rfc6716>RFC6716</a>.
 */
public abstract class OpusPacket {
    protected Config config;
    protected boolean isMono;
    protected final List<byte[]> frames = new LinkedList<>();

    /**
     * Add one frame to this Opus packet.
     *
     * @param frameData the binary data byte array of a frame
     */
    public void addFrame(byte[] frameData) {
        frames.add(frameData);
    }

    /**
     * Get the configuration of this packet that represent the encoding mode, bandwidth, and frame size.
     *
     * @return the configuration
     */
    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    /**
     * @return {@code true} if this is mono stream.
     */
    public boolean isMono() {
        return isMono;
    }

    public void setMono(boolean mono) {
        isMono = mono;
    }

    /**
     * @return the list of frame data in this Opus packet.
     */
    public List<byte[]> getFrames() {
        return frames;
    }

    /**
     * Code should be 0 to 3 which stored in last two bits of TOC byte.
     *
     * @return the code of this packet.
     */
    public abstract int getCode();

    public abstract boolean isVbr();

    public abstract boolean hasPadding();

    public abstract int getFrameCount();

    public abstract int getPaddingLength();

    public void setVbr(boolean isVbr) {
        throw new IllegalStateException("Code 0 to 2 Opus packet doesn't support setting Vbr flag");
    }

    public void setHasPadding(boolean hasPadding) {
        throw new IllegalStateException("Code 0 to 2 packet doesn't support setting padding flag");
    }

    public void setFrameCount(int frameCount) {
        throw new IllegalStateException("Code 0 to 2 packet doesn't support setting frame count");
    }

    public void setPaddingLength(int paddingLength) {
        throw new IllegalStateException("Code 0 to 2 packet doesn't support setting padding length");
    }

    /**
     * Dump Opus packet to standard binary.
     */
    public abstract byte[] dumpToStandardFormat();

    /**
     * Dump Opus packet to self delimited binary.
     */
    public abstract byte[] dumpToSelfDelimitingFormat();

    protected int getTocByte() {
        int toc = config.getId() << 3;
        if (!isMono) {
            toc = toc | 0x04;
        }
        toc = toc | getCode();
        return toc;
    }
}
