package org.chenliang.oggus.opus;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class AudioDataPacket {
    private final List<OpusPacket> opusPackets = new ArrayList<>();

    private AudioDataPacket() {
    }

    public static AudioDataPacket from(byte[] data, int streamCount) {
        AudioDataPacket audioDataPacket = new AudioDataPacket();
        audioDataPacket.opusPackets.addAll(OpusPackets.from(data, streamCount));
        return audioDataPacket;
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

    public byte[] dump() {
        int packetCount = opusPackets.size();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (int i = 0; i < packetCount - 1; i++) {
            byte[] data = opusPackets.get(i).dumpToSelfDelimitingFormat();
            outputStream.writeBytes(data);
        }

        byte[] data = opusPackets.get(packetCount - 1).dumpToStandardFormat();
        outputStream.writeBytes(data);

        return outputStream.toByteArray();
    }
}
