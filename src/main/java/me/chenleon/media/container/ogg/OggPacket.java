package me.chenleon.media.container.ogg;

import com.google.common.primitives.Bytes;

public class OggPacket {
    private final byte[] data;
    private final boolean isPartial;

    public OggPacket(byte[] data) {
        this(data, false);
    }

    public OggPacket(byte[] data, boolean isPartial) {
        this.data = data;
        this.isPartial = isPartial;
    }

    public byte[] getData() {
        return data;
    }

    public boolean isPartial() {
        return isPartial;
    }

    public OggPacket concat(OggPacket other) {
        return new OggPacket(Bytes.concat(data, other.data));
    }
}
