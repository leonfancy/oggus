package me.chenleon.media.audio.opus;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import me.chenleon.media.container.ogg.OggPage;
import me.chenleon.media.container.ogg.OggStream;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static me.chenleon.media.TestUtil.assertOpusPacketEqual;
import static me.chenleon.media.TestUtil.createBinary;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OpusFileTest {
    @Test
    void should_read_valid_ogg_stream_with_one_audio_packet() throws IOException {
        IdHeader idHeader = createIdHeader();

        byte[] idHeaderData = idHeader.dump();

        OggPage oggPage1 = new OggPage();
        oggPage1.setBOS();
        oggPage1.setGranulePosition(0);
        oggPage1.setSerialNum(1);
        oggPage1.setSeqNum(0);
        oggPage1.setLaceValues(lenToLaceValues(idHeaderData.length));
        oggPage1.addDataPacket(idHeaderData);

        CommentHeader commentHeader = CommentHeader.emptyHeader();
        commentHeader.setVendor("test vendor");
        commentHeader.addTag("TITLE", "Test title");

        byte[] commentHeaderData = commentHeader.dump();

        OggPage oggPage2 = new OggPage();
        oggPage2.setGranulePosition(0);
        oggPage2.setSerialNum(1);
        oggPage2.setSeqNum(1);
        oggPage2.setLaceValues(lenToLaceValues(commentHeaderData.length));
        oggPage2.addDataPacket(commentHeaderData);

        OpusPacket opusPacket = OpusPackets.newPacketOfCode(0);
        opusPacket.setConfig(Config.of(0));
        opusPacket.addFrame(createBinary(100, (byte) 1));
        byte[] audioData = opusPacket.dumpToStandardFormat();

        OggPage oggPage3 = new OggPage();
        oggPage3.setGranulePosition(0);
        oggPage3.setSerialNum(1);
        oggPage3.setSeqNum(2);
        oggPage3.setLaceValues(lenToLaceValues(audioData.length));
        oggPage3.addDataPacket(audioData);

        byte[] oggStreamData = Bytes.concat(oggPage1.dump(), oggPage2.dump(), oggPage3.dump());

        ByteArrayInputStream inputStream = new ByteArrayInputStream(oggStreamData);
        OpusFile opusFile = new OpusFile(new OggStream(inputStream));

        assertEquals("test vendor", opusFile.getVendor());
        assertEquals(1, opusFile.getTags().size());
        assertEquals("[Test title]", opusFile.getTags().get("TITLE").toString());

        IdHeader actualIdHeader = opusFile.getIdHeader();
        assertArrayEquals(idHeaderData, actualIdHeader.dump());

        AudioDataPacket audioDataPacket = opusFile.readAudioPacket();
        assertOpusPacketEqual(opusPacket, audioDataPacket.getOpusPackets().get(0));
    }

    private IdHeader createIdHeader() {
        IdHeader idHeader = IdHeader.emptyHeader();
        idHeader.setMajorVersion(0);
        idHeader.setMinorVersion(1);
        idHeader.setChannelCount(2);
        idHeader.setPreSkip(127);
        idHeader.setInputSampleRate(16000);
        idHeader.setOutputGain(0);
        idHeader.setChannelMappingFamily(0);
        return idHeader;
    }

    private byte[] lenToLaceValues(int len) {
        int countOf255 = len / 255;
        int lastValue = len % 255;
        byte[] laceValues = new byte[countOf255 + 1];
        Arrays.fill(laceValues, 0, countOf255, (byte) 255);
        laceValues[countOf255] = (byte) lastValue;
        return laceValues;
    }

    @Test
    void should_read_ogg_stream() throws IOException {
        OpusFile opusFile = new OpusFile(new OggStream("audio/technology.opus"));
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

        int count = 1;
        while (true) {
            AudioDataPacket audioDataPacket = opusFile.readAudioPacket();
            if (audioDataPacket == null) {
                break;
            }
            System.out.println("Audio Data Packet: " + count);
            for (OpusPacket opusPacket : audioDataPacket.getOpusPackets()) {
                Config config = opusPacket.getConfig();
                System.out.printf("Bandwidth: %d Hz, Mode: %s, Frame Size: %.1f ms, Code: %d, Frame Count: %d, Channel: %s\n",
                        config.getBandwidth().getHz(), config.getEncodeMode(), config.getFrameSize(),
                        opusPacket.getCode(), opusPacket.getFrames().size(), opusPacket.isMono() ? "mono" : "stereo");
            }
            count++;
        }
    }
}