package org.chenliang.oggus.ogg;

import com.google.common.primitives.Bytes;
import org.chenliang.oggus.TestUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OggStreamTest {
    @Test
    void should_read_ogg_page_from_input_stream() throws IOException {
        OggPage expectedPage = createOggPage();
        expectedPage.setBOS();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(expectedPage.dump());

        OggStream oggStream = OggStream.from(inputStream);

        assertOggPageEquals(expectedPage, oggStream.readPage());

        assertNull(oggStream.readPage());
    }

    @Test
    void should_not_read_ogg_page_from_empty_input_stream() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[0]);
        OggStream oggStream = OggStream.from(inputStream);
        assertNull(oggStream.readPage());
    }

    @Test
    void should_not_read_ogg_page_from_input_stream_with_only_invalid_data() throws IOException {
        byte[] buf = new byte[10];
        Arrays.fill(buf, (byte) 'O');
        ByteArrayInputStream inputStream = new ByteArrayInputStream(buf);
        OggStream oggStream = OggStream.from(inputStream);
        assertNull(oggStream.readPage());
    }

    @Test
    void should_skip_invalid_data_before_valid_page() throws IOException {
        OggPage expectedPage = createOggPage();
        byte[] invalidData = "OgOg".getBytes();
        byte[] data = Bytes.concat(invalidData, expectedPage.dump());
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        OggStream oggStream = OggStream.from(inputStream);

        assertOggPageEquals(expectedPage, oggStream.readPage());
    }

    @Test
    void should_ignore_invalid_data_between_pages() throws IOException {
        OggPage expectedPage1 = createOggPage();
        expectedPage1.setBOS();
        OggPage expectedPage2 = createOggPage();
        byte[] invalidData = "OgOg".getBytes();
        byte[] data = Bytes.concat(expectedPage1.dump(), invalidData, expectedPage2.dump());
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        OggStream oggStream = OggStream.from(inputStream);

        assertOggPageEquals(expectedPage1, oggStream.readPage());
        assertOggPageEquals(expectedPage2, oggStream.readPage());
    }

    @Test
    void should_read_page_with_given_serial_num() throws IOException {
        int serialNum = 100;
        OggPage expectedPage1 = createOggPage();
        OggPage expectedPage2 = createOggPage();
        expectedPage2.setSerialNum(serialNum);
        OggPage expectedPage3 = createOggPage();
        byte[] data = Bytes.concat(expectedPage1.dump(), expectedPage2.dump(), expectedPage3.dump());

        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        OggStream oggStream = OggStream.from(inputStream);

        assertOggPageEquals(expectedPage2, oggStream.readPage(serialNum));
        assertNull(oggStream.readPage(serialNum));
    }

    @Test
    @Disabled
    void should_not_read_if_ogg_page_is_not_completed() throws IOException {
        byte[] unCompletedPageData = {'O', 'g', 'g', 'S', 0, 1};
        ByteArrayInputStream inputStream = new ByteArrayInputStream(unCompletedPageData);
        OggStream oggStream = OggStream.from(inputStream);

        assertNull(oggStream.readPage());
    }

    @Test
    @Disabled
    void should_skip_uncompleted_page() throws IOException {
        byte[] unCompletedPageData = {'O', 'g', 'g', 'S', 0, 1};
        OggPage expectedPage = createOggPage();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Bytes.concat(unCompletedPageData, expectedPage.dump()));
        OggStream oggStream = OggStream.from(inputStream);

        assertOggPageEquals(expectedPage, oggStream.readPage());
    }

    private OggPage createOggPage() {
        OggPage oggPage = OggPage.empty();
        oggPage.setFlag((byte) 0x00);
        oggPage.setGranulePosition(257);
        oggPage.setSerialNum(1);
        oggPage.setSeqNum(1);
        oggPage.setCheckSum(0);

        oggPage.addDataPacket(TestUtil.createBinary(456, (byte) 1));
        oggPage.addPartialDataPacket(TestUtil.createBinary(255, (byte) 2));

        return oggPage;
    }

    private void assertOggPageEquals(OggPage expected, OggPage actual) {
        assertEquals(expected.getVersion(), actual.getVersion());
        assertEquals(expected.isContinued(), actual.isContinued());
        assertEquals(expected.isBOS(), actual.isBOS());
        assertEquals(expected.isEOS(), actual.isEOS());
        assertEquals(expected.getGranulePosition(), actual.getGranulePosition());
        assertEquals(expected.getSerialNum(), actual.getSerialNum());
        assertEquals(expected.getSeqNum(), actual.getSeqNum());
        assertEquals(expected.getCheckSum(), actual.getCheckSum());
        assertEquals(expected.getSegCount(), actual.getSegCount());
        assertArrayEquals(expected.getLaceValues(), actual.getLaceValues());
        assertArrayEquals(expected.getDataPackets().toArray(), actual.getDataPackets().toArray());
    }
}