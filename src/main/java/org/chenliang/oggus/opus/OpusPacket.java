package org.chenliang.oggus.opus;

import java.util.LinkedList;
import java.util.List;

/**
 * An Opus packet that described in <a href=https://tools.ietf.org/html/rfc6716>RFC6716</a>. An Opus packet binary
 * always begins with a TOC byte as follows:
 *
 * <pre>
 *  0 1 2 3 4 5 6 7
 * +-+-+-+-+-+-+-+-+
 * | config  |s| c |
 * +-+-+-+-+-+-+-+-+
 * </pre>
 * <p>
 * This Class is the abstract class to represent a Opus packet. Based on the frame count defined in the TOC byte,
 * 4 concrete classes are defined:
 * <ul>
 *   <li>{@link CodeZeroPacket}</li>
 *   <li>{@link CodeOnePacket}</li>
 *   <li>{@link CodeTwoPacket}</li>
 *   <li>{@link CodeThreePacket}</li>
 * </ul>
 * <p>
 * Users don't care these concrete classes, they just use {@link OpusPackets} factory methods to create OpusPacket
 * objects.
 */
public abstract class OpusPacket {
    protected Config config;
    protected Channel channel;
    protected final List<byte[]> frames = new LinkedList<>();

    /**
     * Add a frame to this Opus packet.
     *
     * <p>It will throw exception if the number of frames is over the allowed frame count, or a different length of
     * frameData is added to a CBR Opus packet.</p>
     *
     * @param frameData the binary data byte array of a frame
     */
    public void addFrame(byte[] frameData) {
        if (getFrameCount() == frames.size()) {
            throw new InvalidOpusException("The number of frames reaches limitation");
        }
        if (!isVbr() && frames.size() != 0 && frameData.length != frames.get(0).length) {
            throw new InvalidOpusException("Frame size must be the same in CBR Opus packet");
        }
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

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
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

    /**
     * @return the allowed frame count in this Opus packet
     */
    public abstract int getFrameCount();

    public abstract void setFrameCount(int frameCount);

    /**
     * @return {@code true} if this packet is variable bitrate
     */
    public abstract boolean isVbr();

    public abstract void setVbr(boolean isVbr);

    /**
     * Only {@link CodeThreePacket} may have padding.
     *
     * @return {@code true} if packet has padding
     */
    public abstract boolean hasPadding();

    public abstract void setHasPadding(boolean hasPadding);

    public abstract int getPadLenBytesSum();

    /**
     * @param padLenBytesSum the sum of bytes that represent the padding length
     */
    public abstract void setPadLenBytesSum(int padLenBytesSum);

    /**
     * @return the length of binary bytes that are padded at the last of this Opus packet
     */
    public int getPadDataLen() {
        return (getPadLenBytesSum() / 255) * 254 + getPadLenBytesSum() % 255;
    }

    /**
     * Dump Opus packet to standard binary.
     */
    public abstract byte[] dumpToStandardFormat();

    /**
     * Dump Opus packet to self delimiting binary.
     */
    public abstract byte[] dumpToSelfDelimitingFormat();

    protected int getTocByte() {
        int toc = config.getId() << 3;
        if (channel == Channel.STEREO) {
            toc = toc | 0x04;
        }
        toc = toc | getCode();
        return toc;
    }
}
