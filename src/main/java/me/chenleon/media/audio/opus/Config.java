package me.chenleon.media.audio.opus;

import static me.chenleon.media.audio.opus.Bandwidth.*;
import static me.chenleon.media.audio.opus.EncodeMode.*;

public class Config {
    private final int id;
    private final EncodeMode encodeMode;
    private final Bandwidth bandwidth;
    private final double frameSize;
    private static final Config[] configs = {
            new Config(0, SILK, NB, 10),
            new Config(1, SILK, NB, 20),
            new Config(2, SILK, NB, 40),
            new Config(3, SILK, NB, 60),

            new Config(4, SILK, MB, 10),
            new Config(5, SILK, MB, 20),
            new Config(6, SILK, MB, 40),
            new Config(7, SILK, MB, 60),

            new Config(8, SILK, WB, 10),
            new Config(9, SILK, WB, 20),
            new Config(10, SILK, WB, 40),
            new Config(11, SILK, WB, 60),

            new Config(12, HYBRID, SWB, 10),
            new Config(13, HYBRID, SWB, 20),

            new Config(14, HYBRID, FB, 10),
            new Config(15, HYBRID, FB, 20),

            new Config(16, CELT, NB, 2.5),
            new Config(17, CELT, NB, 5),
            new Config(18, CELT, NB, 10),
            new Config(19, CELT, NB, 20),

            new Config(20, CELT, WB, 2.5),
            new Config(21, CELT, WB, 5),
            new Config(22, CELT, WB, 10),
            new Config(23, CELT, WB, 20),

            new Config(24, CELT, SWB, 2.5),
            new Config(25, CELT, SWB, 5),
            new Config(26, CELT, SWB, 10),
            new Config(27, CELT, SWB, 20),

            new Config(28, CELT, FB, 2.5),
            new Config(29, CELT, FB, 5),
            new Config(30, CELT, FB, 10),
            new Config(31, CELT, FB, 20),
    };

    public static Config of(int id) {
        if (id > 32 || id < 0) {
            throw new IllegalArgumentException("Invalid Config ID: " + id);
        }
        return configs[id];
    }

    private Config(int id, EncodeMode encodeMode, Bandwidth bandwidth, double frameSize) {
        this.id = id;
        this.encodeMode = encodeMode;
        this.bandwidth = bandwidth;
        this.frameSize = frameSize;
    }

    public EncodeMode getEncodeMode() {
        return encodeMode;
    }

    public Bandwidth getBandwidth() {
        return bandwidth;
    }

    public double getFrameSize() {
        return frameSize;
    }

    public int getId() {
        return id;
    }
}
