package me.chenleon.media.audio.opus;

import java.util.List;

public class AudioDataPacket {
    private final List<OpusPacket> opusPackets;

    public AudioDataPacket(byte[] data, int streamCount) {
        opusPackets = OpusPackets.from(data, streamCount);
    }

    public List<OpusPacket> getOpusPackets() {
        return opusPackets;
    }
}
