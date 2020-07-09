package org.chenliang.oggus.ogg;

import com.google.common.primitives.Bytes;
import org.chenliang.oggus.TestUtil;
import org.chenliang.oggus.opus.InvalidOpusException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OggPageTest {
    @Test
    void should_correctly_parse_flag() {
        OggPage oggPage = OggPage.empty();

        oggPage.setFlag((byte) 0x00);
        assertFalse(oggPage.isContinued());
        assertFalse(oggPage.isBOS());
        assertFalse(oggPage.isEOS());

        oggPage.setFlag((byte) 0x01);
        assertTrue(oggPage.isContinued());

        oggPage.setFlag((byte) 0x02);
        assertTrue(oggPage.isBOS());

        oggPage.setFlag((byte) 0x04);
        assertTrue(oggPage.isEOS());
    }

    @Test
    void should_correctly_set_flag() {
        OggPage oggPage = OggPage.empty();
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
    void should_correctly_return_is_completed_status() {
        OggPage oggPage = OggPage.empty();
        oggPage.addDataPacket(TestUtil.createBinary(256, (byte) 1));

        assertTrue(oggPage.isCompleted());

        oggPage.addPartialDataPacket(TestUtil.createBinary(255, (byte) 1));

        assertFalse(oggPage.isCompleted());
    }

    @Test
    void should_dump_ogg_page_to_byte_array() {
        OggPage oggPage = OggPage.empty();
        oggPage.setVersion(0);
        oggPage.setFlag((byte) 0x01);
        oggPage.setGranulePosition(257);
        oggPage.setSerialNum(0xffffffff);
        oggPage.setSeqNum(1025);
        oggPage.setCheckSum(0);

        byte[] dataPacket1 = TestUtil.createBinary(456, (byte) 1);
        oggPage.addDataPacket(dataPacket1);

        byte[] dataPacket2 = TestUtil.createBinary(255, (byte) 2);
        oggPage.addPartialDataPacket(dataPacket2);

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

    @Test
    void should_gen_lace_values_when_adding_partial_data_packet() {
        OggPage oggPage = OggPage.empty();
        oggPage.addDataPacket(TestUtil.createBinary(256, (byte) 1));

        assertEquals(2, oggPage.getSegCount());
        assertArrayEquals(new byte[]{(byte) 255, 1}, oggPage.getLaceValues());

        oggPage.addDataPacket(TestUtil.createBinary(255, (byte) 2));
        assertEquals(4, oggPage.getSegCount());
        assertArrayEquals(new byte[]{(byte) 255, 1, (byte) 255, 0}, oggPage.getLaceValues());
    }

    @Test
    void should_add_partial_data_packet() {
        OggPage oggPage = OggPage.empty();
        oggPage.addPartialDataPacket(TestUtil.createBinary(510, (byte) 1));

        assertEquals(2, oggPage.getSegCount());
        assertArrayEquals(new byte[]{(byte) 255, (byte) 255}, oggPage.getLaceValues());
    }

    @Test
    void should_throw_exception_when_adding_partial_data_packet_and_the_length_is_not_multiple_of_255() {
        OggPage oggPage = OggPage.empty();
        InvalidOpusException exception = assertThrows(InvalidOpusException.class, () -> {
            oggPage.addPartialDataPacket(TestUtil.createBinary(511, (byte) 1));
        });

        assertEquals("Not a partial data packet", exception.getMessage());
    }
}