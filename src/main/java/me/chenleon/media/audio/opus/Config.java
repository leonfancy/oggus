package me.chenleon.media.audio.opus;

import static me.chenleon.media.audio.opus.Bandwidth.*;
import static me.chenleon.media.audio.opus.EncodeMode.*;

public class Config {
    private final EncodeMode encodeMode;
    private final Bandwidth bandwidth;
    private final double frameSize;
    private static Config[] configs = {
            new Config(SILK, NB, 10),
            new Config(SILK, NB, 20),
            new Config(SILK, NB, 40),
            new Config(SILK, NB, 60),

            new Config(SILK, MB, 10),
            new Config(SILK, MB, 20),
            new Config(SILK, MB, 40),
            new Config(SILK, MB, 60),

            new Config(SILK, WB, 10),
            new Config(SILK, WB, 20),
            new Config(SILK, WB, 40),
            new Config(SILK, WB, 60),

            new Config(HYBRID, SWB, 10),
            new Config(HYBRID, SWB, 20),

            new Config(HYBRID, FB, 10),
            new Config(HYBRID, FB, 20),

            new Config(CELT, NB, 2.5),
            new Config(CELT, NB, 5),
            new Config(CELT, NB, 10),
            new Config(CELT, NB, 20),

            new Config(CELT, WB, 2.5),
            new Config(CELT, WB, 5),
            new Config(CELT, WB, 10),
            new Config(CELT, WB, 20),

            new Config(CELT, SWB, 2.5),
            new Config(CELT, SWB, 5),
            new Config(CELT, SWB, 10),
            new Config(CELT, SWB, 20),

            new Config(CELT, FB, 2.5),
            new Config(CELT, FB, 5),
            new Config(CELT, FB, 10),
            new Config(CELT, FB, 20),
    };

    public static Config of(int id) {
        if(id > 32 || id < 0) {
            throw new IllegalArgumentException("Invalid Config ID: " + id);
        }
        return configs[id];
    }

    private Config(EncodeMode encodeMode, Bandwidth bandwidth, double frameSize) {
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
}
