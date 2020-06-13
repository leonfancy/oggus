package me.chenleon.media.audio.opus;

import com.google.common.io.LittleEndianDataInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class OpusPackets {
    /**
     * Create a {@code OpusPacket} with given code, see
     * <a href=https://tools.ietf.org/html/rfc6716#section-3.2>RFC6716 Section-3.2</a>.
     *
     * @param code the Opus packet code
     * @return the OpusPacket object
     */
    public static OpusPacket newPacketOfCode(int code) {
        switch (code) {
            case 0:
                return new CodeZeroPacket();
            case 1:
                return new CodeOnePacket();
            case 2:
                return new CodeTwoPacket();
            case 3:
                return new CodeThreePacket();
            default:
                throw new IllegalArgumentException("Invalid Opus packet code: " + code);
        }
    }

    /**
     * Create empty {@code OpusPacket} from TOC byte.
     *
     * @param toc the TOC byte
     */
    public static OpusPacket newPacketOfToc(byte toc) {
        int code = toc & 0x03;
        OpusPacket opusPacket = newPacketOfCode(code);
        opusPacket.config = Config.of(toc >> 3);
        opusPacket.isMono = (toc & 0x04) == 0;
        return opusPacket;
    }

    /**
     * Parse Opus packets from the binary data. The first {@code (streamCount - 1)} packets must be in
     * <a href="https://tools.ietf.org/html/rfc6716#appendix-B">self-delimiting format</a> format. The last packet
     * must be in <a href=https://tools.ietf.org/html/rfc6716#section-3.2>standard Opus packet format</a>.
     *
     * @param data        the binary data that contains {@code streamCount} of Opus packet
     * @param streamCount the number of Opus packet in {@code data}
     * @return the list {@code OpusPacket} parsed from the binary data
     */
    public static List<OpusPacket> from(byte[] data, int streamCount) {
        ArrayList<OpusPacket> opusPackets = new ArrayList<>();
        LittleEndianDataInputStream in = new LittleEndianDataInputStream(new ByteArrayInputStream(data));
        try {
            for (int i = 0; i < streamCount - 1; i++) {
                opusPackets.add(OpusPackets.readDelimitedOpusPacket(in));
            }
            opusPackets.add(OpusPackets.readStandardOpusPacket(in));
        } catch (IOException e) {
            throw new InvalidOpusException("Corrupted opus binary data");
        }
        return opusPackets;
    }

    /**
     * Parse a standard Opus packet from the binary data.
     *
     * @param data the binary data that only contains a standard format Opus packet
     * @return the {@code OpusPacket} parsed from the binary data
     */
    public static OpusPacket from(byte[] data) {
        return from(data, 1).get(0);
    }

    private static OpusPacket readStandardOpusPacket(InputStream inputStream) throws IOException {
        LittleEndianDataInputStream in = new LittleEndianDataInputStream(inputStream);
        int toc = in.readByte();
        OpusPacket opusPacket = newPacketOfToc((byte) toc);
        switch (opusPacket.getCode()) {
            case 0:
                opusPacket.addFrame(in.readAllBytes());
                break;
            case 1:
                int leftDataLen = in.available();
                opusPacket.addFrame(in.readNBytes(leftDataLen / 2));
                opusPacket.addFrame(in.readNBytes(leftDataLen / 2));
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
                opusPacket.setFrameCount(frameCount);
                opusPacket.setVbr(isVbr);
                opusPacket.setHasPadding(hasPadding);
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
                    int frameLen = (in.available() - paddingLen) / frameCount;
                    for (int k = 0; k < frameCount; k++) {
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

    private static OpusPacket readDelimitedOpusPacket(InputStream inputStream) throws IOException {
        LittleEndianDataInputStream in = new LittleEndianDataInputStream(inputStream);
        int toc = in.readByte();
        OpusPacket opusPacket = newPacketOfToc((byte) toc);
        switch (opusPacket.getCode()) {
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

    private static int readFrameLen(InputStream in) throws IOException {
        int frameLen = in.read();
        if (frameLen > 252) {
            frameLen = in.read() * 4 + frameLen;
        }
        return frameLen;
    }
}
