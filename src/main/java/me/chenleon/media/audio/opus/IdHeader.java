package me.chenleon.media.audio.opus;

import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static java.lang.String.format;

@Setter
@Getter
public class IdHeader {
    public static final byte[] MAGIC_SIGNATURE = {'O', 'p', 'u', 's', 'H', 'e', 'a', 'd'};
    private int majorVersion;
    private int minorVersion;
    private int channelCount;
    private int preSkip;
    private double outputGain;
    private int coupledCount;
    private long inputSampleRate;
    private int channelMappingFamily;
    private int streamCount;
    private int[] channelMapping;

    private IdHeader() {
    }

    public static IdHeader from(byte[] data) {
        IdHeader idHeader = new IdHeader();
        LittleEndianDataInputStream in = new LittleEndianDataInputStream(new ByteArrayInputStream(data));
        try {
            if (!Arrays.equals(in.readNBytes(8), MAGIC_SIGNATURE)) {
                throw new InvalidOpusException("Id Header packet doesn't start with 'OpusHead'");
            }
            byte version = in.readByte();
            idHeader.majorVersion = version >> 4;
            idHeader.minorVersion = version & 0x0F;
            idHeader.channelCount = in.readUnsignedByte();
            if (idHeader.channelCount < 1) {
                throw new InvalidOpusException("Invalid channel count: " + idHeader.channelCount);
            }
            idHeader.preSkip = in.readUnsignedShort();
            idHeader.inputSampleRate = Integer.toUnsignedLong(in.readInt());
            idHeader.outputGain = in.readUnsignedShort() / 256.0;
            idHeader.channelMappingFamily = in.readUnsignedByte();
            if (idHeader.channelMappingFamily == 0) {
                if (idHeader.channelCount > 2) {
                    throw new InvalidOpusException(format("Invalid channel count: %d, for channel mapping family 0",
                            idHeader.channelCount));
                }
                idHeader.streamCount = 1;
                idHeader.coupledCount = idHeader.channelCount - idHeader.streamCount;
                idHeader.channelMapping = idHeader.channelCount == 1 ? new int[]{0} : new int[]{0, 1};
            } else {
                if (idHeader.channelMappingFamily == 1 && idHeader.channelCount > 8) {
                    throw new InvalidOpusException(format("Invalid channel count: %d, for channel mapping family 1",
                            idHeader.channelCount));
                }
                idHeader.streamCount = in.readUnsignedByte();
                idHeader.coupledCount = in.readUnsignedByte();
                idHeader.channelMapping = new int[idHeader.channelCount];
                for (int i = 0; i < idHeader.channelCount; i++) {
                    idHeader.channelMapping[i] = in.readUnsignedByte();
                }
            }
            return idHeader;
        } catch (IOException e) {
            throw new InvalidOpusException("Id Header data is corrupted");
        }
    }

    public static IdHeader emptyHeader() {
        return new IdHeader();
    }

    public byte[] dump() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        LittleEndianDataOutputStream out = new LittleEndianDataOutputStream(byteArrayOutputStream);

        try {
            out.write(MAGIC_SIGNATURE);
            out.writeByte(majorVersion << 4 | minorVersion);
            out.writeByte(channelCount);
            out.writeShort(preSkip);
            out.writeInt((int) inputSampleRate);
            out.writeShort((int) (outputGain * 256));
            out.writeByte(channelMappingFamily);

            if (channelMappingFamily > 0) {
                out.writeByte(streamCount);
                out.writeByte(coupledCount);
                for (int i = 0; i < channelCount; i++) {
                    out.writeByte(channelMapping[i]);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("IdHeader dump to byte array error", e);
        }

        return byteArrayOutputStream.toByteArray();
    }
}
