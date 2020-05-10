package me.chenleon.media.audio.opus;

import com.google.common.io.LittleEndianDataInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

public class IdHeader {
    private static final byte[] MAGIC_SIGNATURE = {'O', 'p', 'u', 's', 'H', 'e', 'a', 'd'};
    private final int majorVersion;
    private final int minorVersion;
    private final int channelCount;
    private final int preSkip;
    private final double outputGain;
    private final int coupledCount;
    private final long inputSampleRate;
    private final int channelMappingFamily;
    private final int streamCount;
    private final int[] channelMapping;

    public IdHeader(byte[] data) throws IOException {
        LittleEndianDataInputStream in = new LittleEndianDataInputStream(new ByteArrayInputStream(data));
        if(!Arrays.equals(in.readNBytes(8), MAGIC_SIGNATURE)) {
            throw new InvalidOpusException("Id Header Packet not starts with 'OpusHead'");
        }
        byte version = in.readByte();
        this.majorVersion = version >> 4;
        this.minorVersion = version & 0x0F;
        this.channelCount = in.readUnsignedByte();
        if(channelCount < 1) {
            throw new InvalidOpusException("Invalid channel count: "  + channelCount);
        }
        this.preSkip = in.readUnsignedShort();
        this.inputSampleRate = Integer.toUnsignedLong(in.readInt());
        this.outputGain = in.readUnsignedShort() / 256.0;
        this.channelMappingFamily = in.readUnsignedByte();
        if (this.channelMappingFamily == 0) {
            if(this.channelCount > 2) {
                throw new InvalidOpusException("Channel count must not be more than 2 for channel mapping family 0. Current channel count is: " + channelCount);
            }
            this.streamCount = 1;
            this.coupledCount = this.channelCount - this.streamCount;
            this.channelMapping = this.channelCount == 1 ? new int[]{0} : new int[]{0, 1};
        } else {
            this.streamCount = in.readUnsignedByte();
            this.coupledCount = in.readUnsignedByte();
            channelMapping = new int[this.channelCount];
            for (int i = 0; i < channelCount; i++) {
                channelMapping[i] = in.readUnsignedByte();
            }
        }
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public int getChannelCount() {
        return channelCount;
    }

    public int getPreSkip() {
        return preSkip;
    }

    public long getInputSampleRate() {
        return inputSampleRate;
    }

    public double getOutputGain() {
        return outputGain;
    }

    public int getChannelMappingFamily() {
        return this.channelMappingFamily;
    }

    public int getStreamCount() {
        return this.streamCount;
    }

    public int getCoupledCount() {
        return coupledCount;
    }

    public int[] getChannelMapping() {
        return channelMapping;
    }
}
