package me.chenleon.media.audio.opus;

import java.util.ArrayList;
import java.util.List;

public class AudioDataPacket {
    private final List<OpusPacket> opusPackets = new ArrayList<>();

    public AudioDataPacket(byte[] data, int streamCount) {
        opusPackets.addAll(OpusPackets.from(data, streamCount));
    }

    private AudioDataPacket() {
    }

    public static AudioDataPacket empty() {
        return new AudioDataPacket();
    }

    public void addOpusPacket(OpusPacket opusPacket) {
        opusPackets.add(opusPacket);
    }

    public List<OpusPacket> getOpusPackets() {
        return opusPackets;
    }
}
