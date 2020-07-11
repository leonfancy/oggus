package org.chenliang.oggus.opus;

import com.google.common.base.Strings;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import org.chenliang.oggus.TestUtil;
import org.chenliang.oggus.ogg.OggPage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OggOpusStreamTest {
    @Test
    void should_read_valid_ogg_stream_with_one_audio_packet() throws IOException {
        IdHeader idHeader = createIdHeader();
        OggPage oggPage1 = createOggPage(0, 0, idHeader.dump());
        oggPage1.setBOS();

        CommentHeader commentHeader = createCommentHeader();
        OggPage oggPage2 = createOggPage(0, 1, commentHeader.dump());

        OpusPacket opusPacket = OpusPackets.newPacket(Config.of(0), Channel.MONO, 0);
        opusPacket.addFrame(TestUtil.createBinary(100, (byte) 1));
        byte[] audioData = opusPacket.dumpToStandardFormat();
        OggPage oggPage3 = createOggPage(40, 2, audioData);
        oggPage3.setEOS();

        byte[] oggStreamData = Bytes.concat(oggPage1.dump(), oggPage2.dump(), oggPage3.dump());

        OggOpusStream oggOpusStream = OggOpusStream.from(new ByteArrayInputStream(oggStreamData));

        CommentHeader actualCommentHeader = oggOpusStream.getCommentHeader();

        assertEquals("test vendor", actualCommentHeader.getVendor());
        assertEquals(1, actualCommentHeader.getTags().size());
        assertEquals("[Test title]", actualCommentHeader.getTags().get("TITLE").toString());

        IdHeader actualIdHeader = oggOpusStream.getIdHeader();
        assertArrayEquals(idHeader.dump(), actualIdHeader.dump());

        AudioDataPacket audioDataPacket = oggOpusStream.readAudioPacket();
        TestUtil.assertOpusPacketEqual(opusPacket, audioDataPacket.getOpusPackets().get(0));

        assertNull(oggOpusStream.readAudioPacket());
    }

    @Test
    void should_throw_exception_if_id_header_page_not_exist() {
        CommentHeader commentHeader = createCommentHeader();
        OggPage oggPage = createOggPage(0, 1, commentHeader.dump());

        InvalidOpusException exception = assertThrows(InvalidOpusException.class, () -> {
            OggOpusStream.from(new ByteArrayInputStream(oggPage.dump()));
        });

        assertEquals("No ID Header data in this opus file", exception.getMessage());
    }

    @Test
    void should_throw_exception_if_id_header_page_contains_other_data() {
        IdHeader idHeader = createIdHeader();
        OggPage oggPage = createOggPage(0, 0, idHeader.dump(), TestUtil.createBinary(1, (byte) 1));
        oggPage.setBOS();

        InvalidOpusException exception = assertThrows(InvalidOpusException.class, () -> {
            OggOpusStream.from(new ByteArrayInputStream(oggPage.dump()));
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
        OggOpusStream oggOpusStream = OggOpusStream.from(new ByteArrayInputStream(oggStreamData));

        CommentHeader actualCommentHeader = oggOpusStream.getCommentHeader();

        assertEquals("test vendor", actualCommentHeader.getVendor());
        assertEquals(2, actualCommentHeader.getTags().size());
        assertEquals(longTagValue, String.join("", actualCommentHeader.getTags().get("LONG_TITLE")));
    }

    @Test
    void should_throw_exception_if_comment_header_page_contains_other_data() {
        IdHeader idHeader = createIdHeader();
        OggPage oggPage1 = createOggPage(0, 0, idHeader.dump());
        oggPage1.setBOS();

        CommentHeader commentHeader = createCommentHeader();
        OggPage oggPage2 = createOggPage(0, 1, commentHeader.dump(), TestUtil.createBinary(1, (byte) 1));

        byte[] oggStreamData = Bytes.concat(oggPage1.dump(), oggPage2.dump());

        InvalidOpusException exception = assertThrows(InvalidOpusException.class, () -> {
            OggOpusStream.from(new ByteArrayInputStream(oggStreamData));
        });

        assertEquals("Comment Header Ogg pages must only contain 1 data packet", exception.getMessage());
    }

    @Test
    void should_throw_exception_if_ogg_stream_eos_flag_is_not_set() throws IOException {
        IdHeader idHeader = createIdHeader();
        OggPage oggPage1 = createOggPage(0, 0, idHeader.dump());
        oggPage1.setBOS();

        CommentHeader commentHeader = createCommentHeader();
        OggPage oggPage2 = createOggPage(0, 1, commentHeader.dump());

        OpusPacket opusPacket = OpusPackets.newPacket(Config.of(0), Channel.MONO, 0);
        opusPacket.addFrame(TestUtil.createBinary(100, (byte) 1));
        byte[] audioData = opusPacket.dumpToStandardFormat();
        OggPage oggPage3 = createOggPage(40, 2, audioData);

        byte[] oggStreamData = Bytes.concat(oggPage1.dump(), oggPage2.dump(), oggPage3.dump());

        OggOpusStream oggOpusStream = OggOpusStream.from(new ByteArrayInputStream(oggStreamData));

        oggOpusStream.readAudioPacket();

        InvalidOpusException exception = assertThrows(InvalidOpusException.class, () -> {
            oggOpusStream.readAudioPacket();
        });

        assertEquals("Corrupted opus binary data", exception.getMessage());
    }

    @Test
    void should_read_ogg_stream_with_audio_data_packet_spans_two_page() throws IOException {
        IdHeader idHeader = createIdHeader();
        OggPage oggPage1 = createOggPage(0, 0, idHeader.dump());
        oggPage1.setBOS();

        CommentHeader commentHeader = createCommentHeader();
        OggPage oggPage2 = createOggPage(0, 1, commentHeader.dump());

        OpusPacket opusPacket = OpusPackets.newPacket(Config.of(0), Channel.MONO, 0);
        opusPacket.addFrame(TestUtil.createBinary(255 * 255 + 256, (byte) 1));
        byte[] audioData = opusPacket.dumpToStandardFormat();
        OggPage oggPage3 = createOggPage(-1, 2);
        oggPage3.addPartialDataPacket(Arrays.copyOfRange(audioData, 0, 255 * 255));
        OggPage oggPage4 = createOggPage(40, 3);
        oggPage4.addDataPacket(Arrays.copyOfRange(audioData, 255 * 255, audioData.length));
        oggPage4.setEOS();

        byte[] oggStreamData = Bytes.concat(oggPage1.dump(), oggPage2.dump(), oggPage3.dump(), oggPage4.dump());

        OggOpusStream oggOpusStream = OggOpusStream.from(new ByteArrayInputStream(oggStreamData));

        AudioDataPacket audioDataPacket = oggOpusStream.readAudioPacket();
        assertEquals(1, audioDataPacket.getOpusPackets().size());
        TestUtil.assertOpusPacketEqual(opusPacket, audioDataPacket.getOpusPackets().get(0));
    }

    @Test
    void should_read_ogg_stream_with_a_page_that_contains_multiple_packets() throws IOException {
        OggPage oggPage1 = createOggPage(0, 0, createIdHeader().dump());
        oggPage1.setBOS();

        OggPage oggPage2 = createOggPage(0, 1, createCommentHeader().dump());

        OpusPacket opusPacket1 = OpusPackets.newPacket(Config.of(0), Channel.MONO, 0);
        opusPacket1.addFrame(TestUtil.createBinary(100, (byte) 1));
        byte[] audioData1 = opusPacket1.dumpToStandardFormat();

        OpusPacket opusPacket2 = OpusPackets.newPacket(Config.of(0), Channel.MONO, 0);
        opusPacket2.addFrame(TestUtil.createBinary(100, (byte) 2));
        byte[] audioData2 = opusPacket2.dumpToStandardFormat();

        OpusPacket opusPacket3 = OpusPackets.newPacket(Config.of(0), Channel.MONO, 0);
        opusPacket3.addFrame(TestUtil.createBinary(100, (byte) 3));
        byte[] audioData3 = opusPacket2.dumpToStandardFormat();

        OggPage oggPage3 = createOggPage(120, 2, audioData1, audioData2, audioData3);
        oggPage3.setEOS();

        byte[] oggStreamData = Bytes.concat(oggPage1.dump(), oggPage2.dump(), oggPage3.dump());

        OggOpusStream oggOpusStream = OggOpusStream.from(new ByteArrayInputStream(oggStreamData));

        AudioDataPacket audioDataPacket1 = oggOpusStream.readAudioPacket();
        assertEquals(1, audioDataPacket1.getOpusPackets().size());
        TestUtil.assertOpusPacketEqual(opusPacket1, audioDataPacket1.getOpusPackets().get(0));

        AudioDataPacket audioDataPacket2 = oggOpusStream.readAudioPacket();
        TestUtil.assertOpusPacketEqual(opusPacket2, audioDataPacket2.getOpusPackets().get(0));

        oggOpusStream.readAudioPacket();

        assertNull(oggOpusStream.readAudioPacket());
    }

    @Test
    void should_read_ogg_stream_with_a_page_that_contains_a_complete_and_a_partial_packet() throws IOException {
        OggPage oggPage1 = createOggPage(0, 0, createIdHeader().dump());
        oggPage1.setBOS();

        OggPage oggPage2 = createOggPage(0, 1, createCommentHeader().dump());

        OpusPacket opusPacket1 = OpusPackets.newPacket(Config.of(0), Channel.MONO, 0);
        opusPacket1.addFrame(TestUtil.createBinary(100, (byte) 1));
        byte[] audioData1 = opusPacket1.dumpToStandardFormat();

        OpusPacket opusPacket2 = OpusPackets.newPacket(Config.of(0), Channel.MONO, 0);
        opusPacket2.addFrame(TestUtil.createBinary(255 * 255 * 2, (byte) 2));
        byte[] audioData2 = opusPacket2.dumpToStandardFormat();

        OggPage oggPage3 = createOggPage(40, 2, audioData1);
        int leftRoom = (255 - oggPage3.getSegCount()) * 255;
        oggPage3.addPartialDataPacket(Arrays.copyOfRange(audioData2, 0, leftRoom));

        OggPage oggPage4 = createOggPage(-1, 2);
        oggPage4.addPartialDataPacket(Arrays.copyOfRange(audioData2, leftRoom, leftRoom + 255 * 255));

        OggPage oggPage5 = createOggPage(80, 2);
        oggPage5.addDataPacket(Arrays.copyOfRange(audioData2, leftRoom + 255 * 255, audioData2.length));
        oggPage5.setEOS();

        byte[] oggStreamData = Bytes.concat(oggPage1.dump(), oggPage2.dump(), oggPage3.dump(), oggPage4.dump(),
                oggPage5.dump());

        OggOpusStream oggOpusStream = OggOpusStream.from(new ByteArrayInputStream(oggStreamData));

        AudioDataPacket audioDataPacket1 = oggOpusStream.readAudioPacket();
        assertEquals(1, audioDataPacket1.getOpusPackets().size());
        TestUtil.assertOpusPacketEqual(opusPacket1, audioDataPacket1.getOpusPackets().get(0));

        AudioDataPacket audioDataPacket2 = oggOpusStream.readAudioPacket();
        TestUtil.assertOpusPacketEqual(opusPacket2, audioDataPacket2.getOpusPackets().get(0));

        assertNull(oggOpusStream.readAudioPacket());
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
        OggPage oggPage = OggPage.empty();
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
        OggOpusStream oggOpusStream = OggOpusStream.from("audio/technology.opus");
        IdHeader idHeader = oggOpusStream.getIdHeader();

        System.out.printf("Version: %d.%d\n", idHeader.getMajorVersion(), idHeader.getMinorVersion());
        System.out.printf("Channel Count: %d\n", idHeader.getChannelCount());
        System.out.printf("Pre Skip Samples: %d\n", idHeader.getPreSkip());
        System.out.printf("Original Sample Rate: %d Hz\n", idHeader.getInputSampleRate());
        System.out.printf("Output Gain: %.2f dB\n", idHeader.getOutputGain());
        System.out.printf("Channel Mapping Family: %d\n", idHeader.getChannelMappingFamily());
        System.out.printf("Stream Count: %d\n", idHeader.getStreamCount());
        System.out.printf("Coupled Stream Count: %d\n", idHeader.getCoupledCount());
        System.out.printf("Channel Mapping: %s\n", Ints.join(" ", idHeader.getChannelMapping()));

        System.out.printf("Vendor: %s\n", oggOpusStream.getCommentHeader().getVendor());

        System.out.println("Tags: ");
        Map<String, Collection<String>> tags = oggOpusStream.getCommentHeader().getTags();
        for (String key : tags.keySet()) {
            System.out.printf("- %s=%s\n", key, tags.get(key).stream().collect(Collectors.joining(",")));
        }

        int count = 1;
        while (true) {
            AudioDataPacket audioDataPacket = oggOpusStream.readAudioPacket();
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