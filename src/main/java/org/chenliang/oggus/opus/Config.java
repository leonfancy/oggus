package org.chenliang.oggus.opus;

import static org.chenliang.oggus.opus.Config.Bandwidth.FB;
import static org.chenliang.oggus.opus.Config.Bandwidth.MB;
import static org.chenliang.oggus.opus.Config.Bandwidth.NB;
import static org.chenliang.oggus.opus.Config.Bandwidth.SWB;
import static org.chenliang.oggus.opus.Config.Bandwidth.WB;
import static org.chenliang.oggus.opus.Config.EncodeMode.CELT;
import static org.chenliang.oggus.opus.Config.EncodeMode.HYBRID;
import static org.chenliang.oggus.opus.Config.EncodeMode.SILK;

/**
 * Get the configuration of Opus packet that represent the encoding mode, bandwidth, and frame size. The first 5 bits
 * of the TOC byte gives the ID of configuration. {@code Config.of(int id)} method is used to get the Config instance
 * of a given ID.
 *
 * <p>Below are the table of configurations:
 * <pre>
 *    +-----------------------+-----------+-----------+-------------------+
 *    | Configuration  ID     | Mode      | Bandwidth | Frame Sizes       |
 *    +-----------------------+-----------+-----------+-------------------+
 *    | 0...3                 | SILK-only | NB        | 10, 20, 40, 60 ms |
 *    |                       |           |           |                   |
 *    | 4...7                 | SILK-only | MB        | 10, 20, 40, 60 ms |
 *    |                       |           |           |                   |
 *    | 8...11                | SILK-only | WB        | 10, 20, 40, 60 ms |
 *    |                       |           |           |                   |
 *    | 12...13               | Hybrid    | SWB       | 10, 20 ms         |
 *    |                       |           |           |                   |
 *    | 14...15               | Hybrid    | FB        | 10, 20 ms         |
 *    |                       |           |           |                   |
 *    | 16...19               | CELT-only | NB        | 2.5, 5, 10, 20 ms |
 *    |                       |           |           |                   |
 *    | 20...23               | CELT-only | WB        | 2.5, 5, 10, 20 ms |
 *    |                       |           |           |                   |
 *    | 24...27               | CELT-only | SWB       | 2.5, 5, 10, 20 ms |
 *    |                       |           |           |                   |
 *    | 28...31               | CELT-only | FB        | 2.5, 5, 10, 20 ms |
 *    +-----------------------+-----------+-----------+-------------------+
 * </pre>
 */
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

    /**
     * @param id configuration id, should be between [0, 32)
     * @return Config object
     */
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

    /**
     * Opus encoding mode.
     */
    public enum EncodeMode {
        SILK, HYBRID, CELT
    }

    /**
     * Opus supported audio bandwidth and sample rate.
     */
    public enum Bandwidth {
        NB(4000, 8000), MB(6000, 12000), WB(8000, 16000),
        SWB(12000, 24000), FB(20000, 48000);

        private final int hz;
        private final int sampleRate;

        Bandwidth(int hz, int sampleRate) {
            this.hz = hz;
            this.sampleRate = sampleRate;
        }

        /**
         * @return audio bandwidth
         */
        public int getHz() {
            return hz;
        }

        /**
         * @return sample rate for that bandwidth
         */
        public int getSampleRate() {
            return sampleRate;
        }
    }
}
