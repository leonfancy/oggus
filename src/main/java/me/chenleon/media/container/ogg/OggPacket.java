package me.chenleon.media.container.ogg;

public class OggPacket {
    private byte[] data;
    private final boolean isPartial;

    public OggPacket(byte[] data) {
        this(data, false);
    }

    public OggPacket(byte[] data, boolean isPartial) {
        this.data = data;
        this.isPartial = isPartial;
    }
}
