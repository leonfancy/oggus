package me.chenleon.media.audio.opus;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdHeaderTest {
    @Test
    void should_dump_id_header_with_mapping_family_zero_to_byte_array() {
        IdHeader idHeader = IdHeader.emptyHeader();
        idHeader.setMajorVersion(0);
        idHeader.setMinorVersion(1);
        idHeader.setChannelCount(2);
        idHeader.setPreSkip(127);
        idHeader.setInputSampleRate(16000);
        idHeader.setOutputGain(0);
        idHeader.setChannelMappingFamily(0);

        byte[] actual = idHeader.dump();
        byte[] expected = {'O', 'p', 'u', 's', 'H', 'e', 'a', 'd', 1, 2, 127, 0, (byte) 128, 62, 0, 0, 0, 0, 0};
        assertArrayEquals(expected, actual);
    }

    @Test
    void should_dump_id_header_with_non_zero_mapping_family_to_byte_array() {
        IdHeader idHeader = IdHeader.emptyHeader();
        idHeader.setMajorVersion(0);
        idHeader.setMinorVersion(1);
        idHeader.setChannelCount(3);
        idHeader.setPreSkip(127);
        idHeader.setInputSampleRate(16000);
        idHeader.setOutputGain(0);
        idHeader.setChannelMappingFamily(1);
        idHeader.setStreamCount(2);
        idHeader.setCoupledCount(1);
        idHeader.setChannelMapping(new int[]{0, 1, 2});

        byte[] actual = idHeader.dump();
        byte[] expected = {'O', 'p', 'u', 's', 'H', 'e', 'a', 'd', 1, 3, 127, 0, (byte) 128, 62, 0, 0, 0, 0, 1, 2, 1,
                0, 1, 2};
        assertArrayEquals(expected, actual);
    }

    @Test
    void should_Create_Non_Zero_Mapping_Family_Id_Header_From_Binary_Data() {
        byte[] data = {'O', 'p', 'u', 's', 'H', 'e', 'a', 'd', 1, 3, 127, 0, (byte) 128, 62, 0, 0, 0, 0, 1, 2, 1,
                0, 1, 2};
        IdHeader idHeader = IdHeader.from(data);
        assertEquals(0, idHeader.getMajorVersion());
        assertEquals(1, idHeader.getMinorVersion());
        assertEquals(3, idHeader.getChannelCount());
        assertEquals(127, idHeader.getPreSkip());
        assertEquals(16000, idHeader.getInputSampleRate());
        assertEquals(0, idHeader.getOutputGain());
        assertEquals(1, idHeader.getChannelMappingFamily());
        assertEquals(2, idHeader.getStreamCount());
        assertEquals(1, idHeader.getCoupledCount());
        assertArrayEquals(new int[]{0, 1, 2}, idHeader.getChannelMapping());
    }

    @Test
    void should_create_zero_mapping_family_id_header_from_binary_data() {
        byte[] data = {'O', 'p', 'u', 's', 'H', 'e', 'a', 'd', 1, 2, 127, 0, (byte) 128, 62, 0, 0, 0, 0, 0};
        IdHeader idHeader = IdHeader.from(data);
        assertEquals(0, idHeader.getMajorVersion());
        assertEquals(1, idHeader.getMinorVersion());
        assertEquals(2, idHeader.getChannelCount());
        assertEquals(127, idHeader.getPreSkip());
        assertEquals(16000, idHeader.getInputSampleRate());
        assertEquals(0, idHeader.getOutputGain());
        assertEquals(0, idHeader.getChannelMappingFamily());
        assertEquals(1, idHeader.getStreamCount());
        assertEquals(1, idHeader.getCoupledCount());
    }

    @Test
    void should_throw_exception_given_binary_not_start_with_correct_signature() {
        byte[] data = {'O', 'p', 'u', 's', 'H', 'e', 'a', 'e'};
        InvalidOpusException exception = assertThrows(InvalidOpusException.class, () -> {
            IdHeader.from(data);
        });
        assertEquals("Id Header packet doesn't start with 'OpusHead'", exception.getMessage());
    }

    @Test
    void should_throw_exception_given_channel_count_less_than_zero() {
        byte[] data = {'O', 'p', 'u', 's', 'H', 'e', 'a', 'd', 1, 0, 127, 0, (byte) 128, 62, 0, 0, 0, 0, 0};
        InvalidOpusException exception = assertThrows(InvalidOpusException.class, () -> {
            IdHeader.from(data);
        });
        assertEquals("Invalid channel count: 0", exception.getMessage());
    }

    @Test
    void should_throw_exception_given_channel_count_great_than_2_for_mapping_family_zero() {
        byte[] data = {'O', 'p', 'u', 's', 'H', 'e', 'a', 'd', 1, 3, 127, 0, (byte) 128, 62, 0, 0, 0, 0, 0};
        InvalidOpusException exception = assertThrows(InvalidOpusException.class, () -> {
            IdHeader.from(data);
        });
        assertEquals("Invalid channel count: 3, for channel mapping family 0", exception.getMessage());
    }

    @Test
    void should_throw_exception_given_channel_count_great_than_8_for_mapping_family_one() {
        byte[] data = {'O', 'p', 'u', 's', 'H', 'e', 'a', 'd', 1, 9, 127, 0, (byte) 128, 62, 0, 0, 0, 0, 1};
        InvalidOpusException exception = assertThrows(InvalidOpusException.class, () -> {
            IdHeader.from(data);
        });
        assertEquals("Invalid channel count: 9, for channel mapping family 1", exception.getMessage());
    }

    @Test
    void should_throw_exception_given_not_enough_binary_data() {
        byte[] data = {'O', 'p', 'u', 's', 'H', 'e', 'a', 'd', 1, 3, 127, 0, (byte) 128, 62, 0, 0, 0, 0};
        InvalidOpusException exception = assertThrows(InvalidOpusException.class, () -> {
            IdHeader.from(data);
        });
        assertEquals("Id Header data is corrupted", exception.getMessage());
    }
}