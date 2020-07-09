package org.chenliang.oggus.opus;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OpusPackets {
    /**
     * Create a {@code OpusPacket} with given {@code config}, {@code channel} and {@code code}, see
     * <a href=https://tools.ietf.org/html/rfc6716#section-3.2>RFC6716 Section-3.2</a>.
     *
     * @param config  the config from 0 ~ 31
     * @param channel stereo or mono
     * @param code    the Opus packet code
     * @return the OpusPacket object
     */
    public static OpusPacket newPacket(Config config, Channel channel, int code) {
        OpusPacket opusPacket = newPacketOfCode(code);
        opusPacket.setConfig(config);
        opusPacket.setChannel(channel);
        return opusPacket;
    }

    /**
     * Create empty {@code OpusPacket} from TOC byte.
     *
     * @param toc the TOC byte
     */
    public static OpusPacket newPacketOfToc(int toc) {
        int code = toc & 0x03;
        Config config = Config.of(toc >> 3);
        Channel channel = (toc & 0x04) == 0 ? Channel.MONO : Channel.STEREO;
        return newPacket(config, channel, code);
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
        ByteArrayInputStream in = new ByteArrayInputStream(data);
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

    private static OpusPacket newPacketOfCode(int code) {
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

    private static OpusPacket readStandardOpusPacket(ByteArrayInputStream in) throws IOException {
        int toc = in.read();
        OpusPacket opusPacket = newPacketOfToc(toc);
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
                readCode3PacketHeader(in, opusPacket);
                if (opusPacket.isVbr()) {
                    int[] frameLens = new int[opusPacket.getFrameCount() - 1];
                    for (int k = 0; k < opusPacket.getFrameCount() - 1; k++) {
                        frameLens[k] = readFrameLen(in);
                    }
                    for (int k = 0; k < opusPacket.getFrameCount() - 1; k++) {
                        opusPacket.addFrame(in.readNBytes(frameLens[k]));
                    }
                    int lastFrameLen = in.available() - opusPacket.getPadDataLen();
                    opusPacket.addFrame(in.readNBytes(lastFrameLen));
                } else {
                    int frameLen = (in.available() - opusPacket.getPadDataLen()) / opusPacket.getFrameCount();
                    for (int k = 0; k < opusPacket.getFrameCount(); k++) {
                        opusPacket.addFrame(in.readNBytes(frameLen));
                    }
                }
                if (opusPacket.hasPadding()) {
                    in.skip(opusPacket.getPadDataLen());
                }
                break;
        }
        return opusPacket;
    }

    private static OpusPacket readDelimitedOpusPacket(ByteArrayInputStream in) throws IOException {
        int toc = in.read();
        OpusPacket opusPacket = newPacketOfToc(toc);
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
                readCode3PacketHeader(in, opusPacket);
                if (opusPacket.isVbr()) {
                    int[] frameLens = new int[opusPacket.getFrameCount()];
                    for (int k = 0; k < opusPacket.getFrameCount(); k++) {
                        frameLens[k] = readFrameLen(in);
                    }
                    for (int k = 0; k < opusPacket.getFrameCount(); k++) {
                        opusPacket.addFrame(in.readNBytes(frameLens[k]));
                    }
                } else {
                    frameLen = readFrameLen(in);
                    for (int k = 0; k < opusPacket.getFrameCount(); k++) {
                        opusPacket.addFrame(in.readNBytes(frameLen));
                    }
                }
                if (opusPacket.hasPadding()) {
                    in.skip(opusPacket.getPadDataLen());
                }
                break;
        }
        return opusPacket;
    }

    private static void readCode3PacketHeader(ByteArrayInputStream in, OpusPacket opusPacket) {
        int frameCountByte = in.read();
        boolean isVbr = (frameCountByte & 0x80) != 0;
        boolean hasPadding = (frameCountByte & 0x40) != 0;
        int frameCount = frameCountByte & 0x3F;
        int paddingLenSum = 0;
        if (hasPadding) {
            while (true) {
                int n = in.read();
                paddingLenSum += n;
                if (n < 255) {
                    break;
                }
            }
        }
        opusPacket.setFrameCount(frameCount);
        opusPacket.setVbr(isVbr);
        opusPacket.setHasPadding(hasPadding);
        opusPacket.setPadLenBytesSum(paddingLenSum);
    }

    private static int readFrameLen(ByteArrayInputStream in) {
        int frameLen = in.read();
        if (frameLen > 252) {
            frameLen = in.read() * 4 + frameLen;
        }
        return frameLen;
    }
}
