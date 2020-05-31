package me.chenleon.media.audio.opus;

import java.util.LinkedList;
import java.util.List;

/**
 * An Opus packet that described in RFC6716
 */
public class OpusPacket {
    private Config config;
    private boolean isMono;
    private int code;
    private final List<byte[]> frames = new LinkedList<>();

    public OpusPacket(Config config, boolean isMono, int code) {
        this.config = config;
        this.isMono = isMono;
        this.code = code;
    }

    private OpusPacket() {
    }

    /**
     * Create {@code OpusPacket} from TOC byte.
     *
     * @param toc the TOC byte
     */
    public static OpusPacket from(byte toc) {
        OpusPacket opusPacket = new OpusPacket();
        opusPacket.config = Config.of(toc >> 3);
        opusPacket.isMono = (toc & 0x04) == 0;
        opusPacket.code = toc & 0x03;
        return opusPacket;
    }

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

    /**
     * @return {@code true} if this is mono stream.
     */
    public boolean isMono() {
        return isMono;
    }

    /**
     * {@code code} should be 0 to 3 which stored in last two bits of TOC byte.
     *
     * @return the code of this packet.
     */
    public int getCode() {
        return code;
    }

    /**
     * @return the list of frame data in this Opus packet.
     */
    public List<byte[]> getFrames() {
        return frames;
    }
}
