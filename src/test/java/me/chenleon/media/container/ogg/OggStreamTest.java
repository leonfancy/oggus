package me.chenleon.media.container.ogg;

import com.google.common.primitives.Bytes;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class OggStreamTest {
    @Test
    void shouldReadOggPageFromInputStream() throws IOException {
        OggPage expectedPage = createOggPage();
        expectedPage.setBOS();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(expectedPage.dump());

        OggStream oggStream = new OggStream(inputStream);

        assertOggPageEquals(expectedPage, oggStream.readPage());

        assertNull(oggStream.readPage());
    }

    @Test
    void shouldNotReadOggPageFromEmptyInputStream() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[0]);
        OggStream oggStream = new OggStream(inputStream);
        assertNull(oggStream.readPage());
    }

    @Test
    void shouldNotReadOggPageFromInputStreamWithOnlyInvalidData() throws IOException {
        byte[] buf = new byte[10];
        Arrays.fill(buf, (byte) 'O');
        ByteArrayInputStream inputStream = new ByteArrayInputStream(buf);
        OggStream oggStream = new OggStream(inputStream);
        assertNull(oggStream.readPage());
    }

    @Test
    void shouldSkipInvalidDataBeforeValidPage() throws IOException {
        OggPage expectedPage = createOggPage();
        byte[] invalidData = "OgOg".getBytes();
        byte[] data = Bytes.concat(invalidData, expectedPage.dump());
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        OggStream oggStream = new OggStream(inputStream);

        assertOggPageEquals(expectedPage, oggStream.readPage());
    }

    @Test
    void shouldIgnoreInvalidDataBetweenPages() throws IOException {
        OggPage expectedPage1 = createOggPage();
        expectedPage1.setBOS();
        OggPage expectedPage2 = createOggPage();
        byte[] invalidData = "OgOg".getBytes();
        byte[] data = Bytes.concat(expectedPage1.dump(), invalidData, expectedPage2.dump());
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        OggStream oggStream = new OggStream(inputStream);

        assertOggPageEquals(expectedPage1, oggStream.readPage());
        assertOggPageEquals(expectedPage2, oggStream.readPage());
    }

    @Test
    void shouldReadPageWithGivenSerialNum() throws IOException {
        int serialNum = 100;
        OggPage expectedPage1 = createOggPage();
        OggPage expectedPage2 = createOggPage();
        expectedPage2.setSerialNum(serialNum);
        OggPage expectedPage3 = createOggPage();
        byte[] data = Bytes.concat(expectedPage1.dump(), expectedPage2.dump(), expectedPage3.dump());

        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        OggStream oggStream = new OggStream(inputStream);

        assertOggPageEquals(expectedPage2, oggStream.readPage(serialNum));
        assertNull(oggStream.readPage(serialNum));
    }

    @Test
    @Disabled
    void shouldNotReadIfOggPageIsNotCompleted() throws IOException {
        byte[] unCompletedPageData = {'O', 'g', 'g', 'S', 0, 1};
        ByteArrayInputStream inputStream = new ByteArrayInputStream(unCompletedPageData);
        OggStream oggStream = new OggStream(inputStream);

        assertNull(oggStream.readPage());
    }

    @Test
    @Disabled
    void shouldSkipUncompletedPage() throws IOException {
        byte[] unCompletedPageData = {'O', 'g', 'g', 'S', 0, 1};
        OggPage expectedPage = createOggPage();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Bytes.concat(unCompletedPageData, expectedPage.dump()));
        OggStream oggStream = new OggStream(inputStream);

        assertOggPageEquals(expectedPage, oggStream.readPage());
    }

    private OggPage createOggPage() {
        OggPage oggPage = new OggPage();
        oggPage.setVersion(0);
        oggPage.setFlag((byte) 0x00);
        oggPage.setGranulePosition(257);
        oggPage.setSerialNum(1);
        oggPage.setSeqNum(1);
        oggPage.setCheckSum(0);
        oggPage.setSegCount(3);
        oggPage.setLaceValues(new byte[]{(byte) 255, (byte) 201, (byte) 255});

        byte[] dataPacket1 = new byte[456];
        Arrays.fill(dataPacket1, (byte) 1);
        oggPage.addOggDataPacket(dataPacket1);

        byte[] dataPacket2 = new byte[255];
        Arrays.fill(dataPacket2, (byte) 2);
        oggPage.addOggDataPacket(dataPacket2);
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
        assertArrayEquals(expected.getOggDataPackets().toArray(), actual.getOggDataPackets().toArray());
    }
}