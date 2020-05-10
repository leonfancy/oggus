package me.chenleon.media.audio.opus;

import com.google.common.primitives.Ints;
import me.chenleon.media.container.ogg.OggFile;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

class OpusFileTest {
    @Test
    void shouldReadOggStream() throws IOException {
        OpusFile opusFile = new OpusFile(new OggFile("audio/technology.opus"));
        IdHeader idHeader = opusFile.getIdHeader();

        System.out.printf("Version: %d.%d\n", idHeader.getMajorVersion(), idHeader.getMinorVersion());
        System.out.printf("Channel Count: %d\n", idHeader.getChannelCount());
        System.out.printf("Pre Skip Samples: %d\n", idHeader.getPreSkip());
        System.out.printf("Original Sample Rate: %d Hz\n", idHeader.getInputSampleRate());
        System.out.printf("Output Gain: %.2f dB\n", idHeader.getOutputGain());
        System.out.printf("Channel Mapping Family: %d\n", idHeader.getChannelMappingFamily());
        System.out.printf("Stream Count: %d\n", idHeader.getStreamCount());
        System.out.printf("Coupled Stream Count: %d\n", idHeader.getCoupledCount());
        System.out.printf("Channel Mapping: %s\n", Ints.join(" ", idHeader.getChannelMapping()));

        System.out.printf("Vendor: %s\n", opusFile.getVendor());

        System.out.println("Tags: ");
        Map<String, Collection<String>> tags = opusFile.getTags();
        for (String key : tags.keySet()) {
            System.out.printf("- %s=%s\n", key, tags.get(key).stream().collect(Collectors.joining(",")));
        }
    }
}