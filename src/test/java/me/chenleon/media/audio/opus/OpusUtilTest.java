package me.chenleon.media.audio.opus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OpusUtilTest {
    @Test
    void should_convert_frame_length_to_bytes_presentation_given_length_less_than_252() {
        for (int n = 0; n < 252; n++) {
            byte[] data = OpusUtil.frameLengthToBytes(n);
            assertEquals(1, data.length);
            assertEquals(n, Byte.toUnsignedInt(data[0]));
        }
    }

    @ParameterizedTest
    @CsvSource({
            "252, 252, 0",
            "256, 252, 1",
            "1265, 253, 253",
            "1273, 253, 255",
            "1275, 255, 255"
    })
    void should_convert_frame_length_to_bytes_presentation_given_length_great_than_252(int len, int first, int second) {
        byte[] data = OpusUtil.frameLengthToBytes(len);
        assertEquals(first, Byte.toUnsignedInt(data[0]));
        assertEquals(second, Byte.toUnsignedInt(data[1]));
    }
}