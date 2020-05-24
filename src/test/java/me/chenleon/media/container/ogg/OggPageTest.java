package me.chenleon.media.container.ogg;

import com.google.common.primitives.Bytes;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class OggPageTest {
    @Test
    void shouldCorrectlyParseFlag() {
        OggPage oggPage = new OggPage();

        oggPage.setFlag((byte)0x00);
        assertFalse(oggPage.isContinued());
        assertFalse(oggPage.isBOS());
        assertFalse(oggPage.isEOS());

        oggPage.setFlag((byte)0x01);
        assertTrue(oggPage.isContinued());

        oggPage.setFlag((byte)0x02);
        assertTrue(oggPage.isBOS());

        oggPage.setFlag((byte)0x04);
        assertTrue(oggPage.isEOS());
    }

    @Test
    void shouldCorrectlySetFlag() {
        OggPage oggPage = new OggPage();
        oggPage.setFlag((byte) 0x00);

        oggPage.setContinued();
        assertTrue(oggPage.isContinued());
        assertFalse(oggPage.isBOS());
        assertFalse(oggPage.isEOS());

        oggPage.setFlag((byte) 0x00);

        oggPage.setBOS();
        assertFalse(oggPage.isContinued());
        assertTrue(oggPage.isBOS());
        assertFalse(oggPage.isEOS());

        oggPage.setFlag((byte) 0x00);

        oggPage.setEOS();
        assertFalse(oggPage.isContinued());
        assertFalse(oggPage.isBOS());
        assertTrue(oggPage.isEOS());
    }

    @Test
    void shouldCorrectlyReturnIsCompletedStatus() {
        OggPage oggPage = new OggPage();
        oggPage.setSegCount(3);
        oggPage.setLaceValues(new byte[]{(byte) 255, (byte) 201, (byte) 255 });

        assertFalse(oggPage.isCompleted());

        for (int laceValue = 0; laceValue < 254; laceValue++) {
            oggPage.setLaceValues(new byte[]{(byte) 255, (byte) 201, (byte) laceValue});

            assertTrue(oggPage.isCompleted());
        }
    }

    @Test
    void shouldDumpOggPageToByteArray() {
        OggPage oggPage = new OggPage();
        oggPage.setVersion(0);
        oggPage.setFlag((byte)0x01);
        oggPage.setGranulePosition(257);
        oggPage.setSerialNum(0xffffffff);
        oggPage.setSeqNum(1025);
        oggPage.setCheckSum(0);
        oggPage.setSegCount(3);
        oggPage.setLaceValues(new byte[]{(byte) 255, (byte) 201, (byte) 255 });

        byte[] dataPacket1 = new byte[456];
        Arrays.fill(dataPacket1, (byte) 1);
        oggPage.addOggDataPacket(dataPacket1);

        byte[] dataPacket2 = new byte[255];
        Arrays.fill(dataPacket2, (byte) 2);
        oggPage.addOggDataPacket(dataPacket2);

        byte[] dumpData = oggPage.dump();

        assertEquals(741, dumpData.length);

        byte[] headerBytes = {'O', 'g', 'g', 'S', 0, 1,
                1, 1, 0, 0, 0, 0, 0, 0,
                (byte) 255, (byte) 255, (byte) 255, (byte) 255,
                1, 4, 0, 0,
                0, 0, 0, 0,
                3, (byte) 255, (byte) 201, (byte) 255};
        byte[] expectedBytes = Bytes.concat(headerBytes, dataPacket1, dataPacket2);

        assertArrayEquals(expectedBytes, dumpData);
    }
}