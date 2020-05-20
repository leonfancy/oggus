package me.chenleon.media.audio.opus;

import java.util.LinkedList;

public class OpusPacket {
    private final Config config;
    private final boolean isMono;
    private final int code;
    private final LinkedList<byte[]> frames = new LinkedList<>();

    public OpusPacket(Config config, boolean isMono, int code) {
        this.config = config;
        this.isMono = isMono;
        this.code = code;
    }

    public void addFrame(byte[] frameData) {
        frames.add(frameData);
    }

    public Config getConfig() {
        return config;
    }

    public boolean isMono() {
        return isMono;
    }

    public int getCode() {
        return code;
    }

    public LinkedList<byte[]> getFrames() {
        return frames;
    }
}
