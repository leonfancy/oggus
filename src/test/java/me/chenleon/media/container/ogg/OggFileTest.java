package me.chenleon.media.container.ogg;

import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class OggFileTest {
    @Test
    void shouldReadOggPage() throws IOException {
        OggFile oggFile = new OggFile("audio/technology.opus");

        int pageCount = 0;

        while (oggFile.hasNextPage()) {
            OggPage page = oggFile.nextPage();
            pageCount += 1;

            System.out.println("version: " + page.getVersion());
            System.out.println("flags: " + (page.isContinued() ? "continued " : "") + (page.isBOS() ? "bos " : "") + (page.isEOS() ? "eos" : ""));
            System.out.println("granule pos: " + page.getGranulePosition());
            System.out.println("serial number: " + String.format("0x%x", page.getSerialNum()));
            System.out.println("seq number: " + page.getSeqNum());
            System.out.println("checksum: " + String.format("0x%x", page.getCheckSum()));
            System.out.println("segment count: " + page.getSegCount());
            System.out.print("segment table: ");
            for (byte b : page.getSegTable()) {
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
        OggFile oggFile = new OggFile(inputStream);
        assertTrue(oggFile.hasNextPage());
    }
}