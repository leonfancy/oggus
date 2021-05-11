package org.chenliang.oggus.util;

public class CRCUtil {
    protected static final int CRC_POLYNOMIAL = 0x04c11db7;
    private static final int[] CRC_TABLE = new int[256];

    static {
        int crc;
        for (int i = 0; i < 256; i++) {
            crc = i << 24;
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x80000000) != 0) {
                    crc = ((crc << 1) ^ CRC_POLYNOMIAL);
                } else {
                    crc <<= 1;
                }
            }
            CRC_TABLE[i] = crc;
        }
    }

    public static int getCRC(byte[] data) {
        int crc = 0;
        int a, b;

        for (byte datum : data) {
            a = crc << 8;
            b = CRC_TABLE[((crc >>> 24) & 0xff) ^ (datum & 0xff)];
            crc = a ^ b;
        }

        return crc;
    }
}
