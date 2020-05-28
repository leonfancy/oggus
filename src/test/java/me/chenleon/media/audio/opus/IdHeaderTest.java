package me.chenleon.media.audio.opus;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdHeaderTest {
    @Test
    void shouldDumpIdHeaderWithMappingFamilyZeroToByteArray() {
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
}