package me.chenleon.media.audio.opus;

import com.google.common.io.LittleEndianDataInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedList;

public class AudioDataPacket {
    private final LinkedList<OpusPacket> opusPackets = new LinkedList<>();

    public AudioDataPacket(byte[] data, int streamCount) throws IOException {
        LittleEndianDataInputStream in = new LittleEndianDataInputStream(new ByteArrayInputStream(data));
        for (int i = 0; i < streamCount - 1; i++) {
            opusPackets.add(readDelimitedOpusPacket(in));
        }
        opusPackets.add(readStandardOpusPacket(in));
    }

    public LinkedList<OpusPacket> getOpusPackets() {
        return opusPackets;
    }

    private OpusPacket readStandardOpusPacket(LittleEndianDataInputStream in) throws IOException {
        int toc = in.read();
        Config config = Config.of(toc >> 3);
        boolean isMono = (toc & 0x04) == 0;
        int code = toc & 0x03;
        OpusPacket opusPacket = new OpusPacket(config, isMono, code);
        switch (code) {
            case 0:
                opusPacket.addFrame(in.readAllBytes());
                break;
            case 1:
                int leftDataLen = in.available();
                opusPacket.addFrame(in.readNBytes(leftDataLen/2));
                opusPacket.addFrame(in.readNBytes(leftDataLen/2));
                break;
            case 2:
                int frameLen1 = readFrameLen(in);
                opusPacket.addFrame(in.readNBytes(frameLen1));
                opusPacket.addFrame(in.readAllBytes());
                break;
            case 3:
                int frameCountByte = in.read();
                boolean isVbr = (frameCountByte & 0x80) != 0;
                boolean hasPadding = (frameCountByte & 0x40) != 0;
                int frameCount = frameCountByte & 0x3F;
                int paddingLen = 0;
                if (hasPadding) {
                    while (true) {
                        int n = in.read();
                        if (n > 0) {
                            paddingLen += n - 1;
                        }
                        if (n != 255) {
                            break;
                        }
                    }
                }
                if (isVbr) {
                    int[] frameLens = new int[frameCount - 1];
                    for (int k = 0; k < frameCount - 1; k++) {
                        frameLens[k] = readFrameLen(in);
                    }
                    for (int k = 0; k < frameCount - 1; k++) {
                        opusPacket.addFrame(in.readNBytes(frameLens[k]));
                    }
                    int lastFrameLen = in.available() - paddingLen;
                    opusPacket.addFrame(in.readNBytes(lastFrameLen));
                } else {
                    for (int k = 0; k < frameCount; k++) {
                        int frameLen = (in.available() - paddingLen) / frameCount;
                        opusPacket.addFrame(in.readNBytes(frameLen));
                    }
                }
                if (hasPadding) {
                    // discard padding bytes
                    in.skip(paddingLen);
                }
                break;
        }
        return opusPacket;
    }

    private OpusPacket readDelimitedOpusPacket(LittleEndianDataInputStream in) throws IOException {
        int toc = in.read();
        Config config = Config.of(toc >> 2);
        boolean isMono = (toc & 0x04) == 0;
        int code = toc & 0x03;
        OpusPacket opusPacket = new OpusPacket(config, isMono, code);
        switch (code) {
            case 0:
                int frameLen = readFrameLen(in);
                opusPacket.addFrame(in.readNBytes(frameLen));
                break;
            case 1:
                frameLen = readFrameLen(in);
                opusPacket.addFrame(in.readNBytes(frameLen));
                opusPacket.addFrame(in.readNBytes(frameLen));
                break;
            case 2:
                int frameLen1 = readFrameLen(in);
                int frameLen2 = readFrameLen(in);
                opusPacket.addFrame(in.readNBytes(frameLen1));
                opusPacket.addFrame(in.readNBytes(frameLen2));
                break;
            case 3:
                int frameCountByte = in.read();
                boolean isVbr = (frameCountByte & 0x80) != 0;
                boolean hasPadding = (frameCountByte & 0x40) != 0;
                int frameCount = frameCountByte & 0x3F;
                int paddingLen = 0;
                if (hasPadding) {
                    while (true) {
                        int n = in.read();
                        if (n > 0) {
                            paddingLen += n - 1;
                        }
                        if (n != 255) {
                            break;
                        }
                    }
                }
                if (isVbr) {
                    int[] frameLens = new int[frameCount];
                    for (int k = 0; k < frameCount; k++) {
                        frameLens[k] = readFrameLen(in);
                    }
                    for (int k = 0; k < frameCount; k++) {
                        opusPacket.addFrame(in.readNBytes(frameLens[k]));
                    }
                } else {
                    frameLen = readFrameLen(in);
                    for (int k = 0; k < frameCount; k++) {
                        opusPacket.addFrame(in.readNBytes(frameLen));
                    }
                }
                if (hasPadding) {
                    in.skip(paddingLen);
                }
                break;
        }
        return opusPacket;
    }

    private int readFrameLen(LittleEndianDataInputStream in) throws IOException {
        int frameLen = in.read();
        if (frameLen > 252) {
            frameLen = in.read() * 4 + frameLen;
        }
        return frameLen;
    }
}
