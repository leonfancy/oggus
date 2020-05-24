package me.chenleon.media.container.ogg;

import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class OggFileTest {
    @Test
    void shouldReadOggPage() throws IOException {
        OggStream oggStream = new OggStream("audio/technology.opus");

        int pageCount = 0;

        while (oggStream.hasNextPage()) {
            OggPage page = oggStream.nextPage();
            pageCount += 1;

            System.out.println("version: " + page.getVersion());
            System.out.println("flags: " + (page.isContinued() ? "continued " : "") + (page.isBOS() ? "bos " : "") + (page.isEOS() ? "eos" : ""));
            System.out.println("granule pos: " + page.getGranulePosition());
            System.out.println("serial number: " + String.format("0x%x", page.getSerialNum()));
            System.out.println("seq number: " + page.getSeqNum());
            System.out.println("checksum: " + String.format("0x%x", page.getCheckSum()));
            System.out.println("segment count: " + page.getSegCount());
            System.out.print("segment table: ");
            for (byte b : page.getLaceValues()) {
                System.out.print(Byte.toUnsignedInt(b) + " ");
            }
            System.out.println("\n----------------------\n");
        }


        assertEquals(11, pageCount);
    }

    @Test
    void shouldCorrectSearchCapturePattern() throws IOException {
        byte[] bytes = {'O', 'g', 'O', 'g', 'g', 'S'};
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        OggStream oggStream = new OggStream(inputStream);
        assertTrue(oggStream.hasNextPage());
    }
}