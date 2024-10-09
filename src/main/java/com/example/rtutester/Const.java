package com.example.rtutester;

import java.util.ArrayList;
import java.util.Arrays;

public class Const {

    public static final int HEADER_LENGTH = 5;
    public static final int ADDR_INDEX = 1;
    public static final int TYPE_INDEX = 3;
    public static final int LENGTH_INDEX = 4;
    public static final int CHECKSUM_LENGTH = 2;
    public static final ArrayList<Integer> RTU_INDEXES = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 224, 225, 226, 227, 228, 229));

    public static final byte[] RFR1 = new byte[]{0x01, 0x01, 0x01, 0x09, 0x00, (byte) 0x6b, (byte) 0xac};
    public static final byte[] RFR2 = new byte[]{0x01, 0x02, 0x01, 0x09, 0x00, (byte) 0x6b, (byte) 0xe8};
    public static final byte[] RFR3 = new byte[]{0x01, 0x03, 0x01, 0x09, 0x00, (byte) 0x6a, (byte) 0x14};
    public static final byte[] RFR4 = new byte[]{0x01, 0x04, 0x01, 0x09, 0x00, (byte) 0x6b, (byte) 0x60};
    public static final byte[] RFR5 = new byte[]{0x01, 0x05, 0x01, 0x09, 0x00, (byte) 0x6a, (byte) 0x9c};
    public static final byte[] RFR6 = new byte[]{0x01, 0x06, 0x01, 0x09, 0x00, (byte) 0x6a, (byte) 0xd8};
    public static final byte[] RFR7 = new byte[]{0x01, 0x07, 0x01, 0x09, 0x00, (byte) 0x6b, (byte) 0x24};
    public static final byte[] RFR8 = new byte[]{0x01, 0x08, 0x01, 0x09, 0x00, (byte) 0x68, (byte) 0x30};
    public static final byte[] RFR9 = new byte[]{0x01, 0x09, 0x01, 0x09, 0x00, (byte) 0x69, (byte) 0xCC};
    public static final byte[] RFR10 = new byte[]{0x01, 0x0A, 0x01, 0x09, 0x00, (byte) 0x69, (byte) 0x88};
    public static final byte[] RFR11 = new byte[]{0x01, 0x0B, 0x01, 0x09, 0x00, (byte) 0x68, (byte) 0x74};
    public static final byte[] RFR12 = new byte[]{0x01, 0x0C, 0x01, 0x09, 0x00, (byte) 0x69, (byte) 0x00};
    public static final byte[] RFR13 = new byte[]{0x01, 0x0D, 0x01, 0x09, 0x00, (byte) 0x68, (byte) 0xFC};
    public static final byte[] RFR14 = new byte[]{0x01, 0x0E, 0x01, 0x09, 0x00, (byte) 0x68, (byte) 0xB8};
    public static final byte[] RFR224 = new byte[]{0x01, (byte) 0xE0, 0x01, 0x09, 0x00, (byte) 0x5D, (byte) 0x90};
    public static final byte[] RFR225 = new byte[]{0x01, (byte) 0xE1, 0x01, 0x09, 0x00, (byte) 0x5C, (byte) 0x6C};
    public static final byte[] RFR226 = new byte[]{0x01, (byte) 0xE2, 0x01, 0x09, 0x00, (byte) 0x5C, (byte) 0x28};
    public static final byte[] RFR227 = new byte[]{0x01, (byte) 0xE3, 0x01, 0x09, 0x00, (byte) 0x5D, (byte) 0xD4};
    public static final byte[] RFR228 = new byte[]{0x01, (byte) 0xE4, 0x01, 0x09, 0x00, (byte) 0x5C, (byte) 0xA0};
    public static final byte[] RFR229 = new byte[]{0x01, (byte) 0xE5, 0x01, 0x09, 0x00, (byte) 0x5D, (byte) 0x5C};
    public static final byte[] MAX_SIZE = new byte[4096];
    //Analog RTU card calibration message                    [           HEADER                  ] Ch   Gain   Tolerance
    public static final byte[] CALIBRATE_CH_MSG = new byte[]{0x01, (byte) 0xE0, 0x01, 0x42, 0x03, 0x0, 0x02, 0x01};
}
