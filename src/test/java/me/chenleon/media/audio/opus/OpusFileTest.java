package me.chenleon.media.audio.opus;

import com.google.common.base.Strings;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import me.chenleon.media.container.ogg.OggPage;
import me.chenleon.media.container.ogg.OggStream;
import org.junit.jupiter.api.Disabled;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

class OpusFileTest {
    @Test
    void should_read_valid_ogg_stream_with_one_audio_packet() throws IOException {
        IdHeader idHeader = createIdHeader();
        OggPage oggPage1 = createOggPage(0, 0, idHeader.dump());
        oggPage1.setBOS();

        CommentHeader commentHeader = createCommentHeader();
        OggPage oggPage2 = createOggPage(0, 1, commentHeader.dump());

        OpusPacket opusPacket = OpusPackets.newPacket(Config.of(0), Channel.MONO, 0);
        opusPacket.addFrame(createBinary(100, (byte) 1));
        byte[] audioData = opusPacket.dumpToStandardFormat();
        OggPage oggPage3 = createOggPage(40, 2, audioData);

        byte[] oggStreamData = Bytes.concat(oggPage1.dump(), oggPage2.dump(), oggPage3.dump());

        OpusFile opusFile = OpusFile.from(new ByteArrayInputStream(oggStreamData));

        assertEquals("test vendor", opusFile.getVendor());
        assertEquals(1, opusFile.getTags().size());
        assertEquals("[Test title]", opusFile.getTags().get("TITLE").toString());

        IdHeader actualIdHeader = opusFile.getIdHeader();
        assertArrayEquals(idHeader.dump(), actualIdHeader.dump());

        AudioDataPacket audioDataPacket = opusFile.readAudioPacket();
        assertOpusPacketEqual(opusPacket, audioDataPacket.getOpusPackets().get(0));
    }

    @Test
    void should_throw_exception_if_id_header_page_not_exist() {
        CommentHeader commentHeader = createCommentHeader();
        OggPage oggPage = createOggPage(0, 1, commentHeader.dump());

        InvalidOpusException exception = assertThrows(InvalidOpusException.class, () -> {
            OpusFile.from(new ByteArrayInputStream(oggPage.dump()));
        });

        assertEquals("No ID Header data in this opus file", exception.getMessage());
    }

    @Test
    void should_throw_exception_if_id_header_page_contains_other_data() {
        IdHeader idHeader = createIdHeader();
        OggPage oggPage = createOggPage(0, 0, idHeader.dump(), createBinary(1, (byte) 1));
        oggPage.setBOS();

        InvalidOpusException exception = assertThrows(InvalidOpusException.class, () -> {
            OpusFile.from(new ByteArrayInputStream(oggPage.dump()));
        });

        assertEquals("The ID Header Ogg page must NOT contain other data", exception.getMessage());
    }

    @Test
    void should_read_comment_header_that_spans_two_pages() throws IOException {
        IdHeader idHeader = createIdHeader();
        OggPage oggPage1 = createOggPage(0, 0, idHeader.dump());
        oggPage1.setBOS();

        CommentHeader commentHeader = createCommentHeader();
        String longTagValue = Strings.repeat("a", 255 * 255);
        commentHeader.addTag("LONG_TITLE", longTagValue);
        byte[] commentData = commentHeader.dump();

        OggPage oggPage2 = createOggPage(-1, 1);
        oggPage2.addPartialDataPacket(Arrays.copyOfRange(commentData, 0, 255 * 255));
        OggPage oggPage3 = createOggPage(0, 2);
        oggPage3.addDataPacket(Arrays.copyOfRange(commentData, 255 * 255, commentData.length));

        byte[] oggStreamData = Bytes.concat(oggPage1.dump(), oggPage2.dump(), oggPage3.dump());
        OpusFile opusFile = OpusFile.from(new ByteArrayInputStream(oggStreamData));

        assertEquals("test vendor", opusFile.getVendor());
        assertEquals(2, opusFile.getTags().size());
        assertEquals(longTagValue, String.join("", opusFile.getTags().get("LONG_TITLE")));
    }

    @Test
    void should_throw_exception_if_comment_header_page_contains_other_data() {
        IdHeader idHeader = createIdHeader();
        OggPage oggPage1 = createOggPage(0, 0, idHeader.dump());
        oggPage1.setBOS();

        CommentHeader commentHeader = createCommentHeader();
        OggPage oggPage2 = createOggPage(0, 1, commentHeader.dump(), createBinary(1, (byte) 1));

        byte[] oggStreamData = Bytes.concat(oggPage1.dump(), oggPage2.dump());

        InvalidOpusException exception = assertThrows(InvalidOpusException.class, () -> {
            OpusFile.from(new ByteArrayInputStream(oggStreamData));
        });

        assertEquals("Comment Header Ogg pages must only contain 1 data packet", exception.getMessage());
    }

    private CommentHeader createCommentHeader() {
        CommentHeader commentHeader = CommentHeader.emptyHeader();
        commentHeader.setVendor("test vendor");
        commentHeader.addTag("TITLE", "Test title");
        return commentHeader;
    }

    private OggPage createOggPage(int granulePosition, int seqNum, byte[]... dataPackets) {
        OggPage oggPage = createOggPage(granulePosition, seqNum);
        for (byte[] dataPacket : dataPackets) {
            oggPage.addDataPacket(dataPacket);
        }
        return oggPage;
    }

    private OggPage createOggPage(int granulePosition, int seqNum) {
        OggPage oggPage = new OggPage();
        oggPage.setGranulePosition(granulePosition);
        oggPage.setSerialNum(1);
        oggPage.setSeqNum(seqNum);
        return oggPage;
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

    @Test
    @Disabled
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
                        opusPacket.getCode(), opusPacket.getFrames().size(), opusPacket.getChannel());
            }
            count++;
        }
    }
}