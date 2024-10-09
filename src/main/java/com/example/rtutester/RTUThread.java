package com.example.rtutester;

import javafx.scene.control.Slider;

import java.util.Arrays;

import static com.example.rtutester.Const.*;
import static com.example.rtutester.Const.HEADER_LENGTH;
import static com.example.rtutester.FunctionHelper.*;
import static com.example.rtutester.HelloController.socket;

public class RTUThread implements Runnable {
    public RTUThread(){
        run=true;
    }
    static boolean run = true;
    @Override
    public void run() {
        try {
            while (run) {
                socket.getOutputStream().write(RFR224);
//                System.out.println("Requested data from RTU 224");
                Thread.sleep(50);
                int n = socket.getInputStream().read(MAX_SIZE);
//                System.out.println("Got " + n + " bytes");
                if (n >= 0) {
                    byte[] readData = Arrays.copyOfRange(MAX_SIZE, 0, n);
                    try {
//                        System.out.println("Got ");
                        printArray(readData);
                        parseMessage(readData); //Pass it to the parser
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    //Should never be less than zero.
                    System.err.println("RTUPT Input stream size <0");
                }
                Thread.sleep(250);
//                waitForRtuResponse();


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void parseMessage(byte[] messageReceived) {
        int rtuAddress = CRC16.byteToUnsignedInt(messageReceived[ADDR_INDEX]);
        try {
//            System.out.println("Received RTU address: " + rtuAddress);
            if (!RTU_INDEXES.contains(rtuAddress)) {
                System.err.println("Invalid RTU address " + rtuAddress);
                return;
            }
            //The length is the header (always 5) plus the specified data length, plus the 2 checksum bytes.
            //Need to do this to account for the random FE that MAY be at the end of the RTU message.
            int length = messageReceived[4] + HEADER_LENGTH + CHECKSUM_LENGTH;
            byte[] noWeirdEnding = Arrays.copyOfRange(messageReceived, 0, length);
            if (!CRC16.verifyCrc(noWeirdEnding)) {
//                System.err.println("CRC mismatch on RTU addr " + rtuAddress);
                return;
            } else {
//                System.out.println("Valid CRC");
            }
            int messageType = CRC16.byteToUnsignedInt(noWeirdEnding[TYPE_INDEX]);
            int dataLength = CRC16.byteToUnsignedInt(noWeirdEnding[LENGTH_INDEX]);
            int dataStart = HEADER_LENGTH + 1;
            int dataEnd = HEADER_LENGTH + dataLength;
            byte[] body = Arrays.copyOfRange(noWeirdEnding, dataStart, dataEnd);
            if (messageType == 0x89) {
                //If the card is a 501
                if (rtuAddress >= 224) {
                    getConsoleAnalogValues(body);
                } else {
                    getAnalogValues(body);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized int[] getAnalogValues(byte[] analogBytes) {
        int analogCnt = 0;
        int[] returnArray = new int[analogBytes.length / 2];
        System.out.print("AnalogBtes [");
        for (byte b : analogBytes) {
            System.out.print(byteToHexString(new byte[]{b}) + "-");
        }
        System.out.println("]");
        for (int i = 0; i < analogBytes.length; i += 2) {
            String first = Integer.toHexString(analogBytes[i]);
            if (first.length() == 1) {
                first = "0" + first;
            }
            String second = Integer.toHexString(analogBytes[i + 1]);
            if (second.length() == 1) {
                second = "0" + second;
            }
            if (i == 0) {
                System.out.println("First: " + first);
                System.out.println("Second: " + second);
            }
            first = first.replaceAll("ffffff", "");
            second = second.replaceAll("ffffff", "");
            second = second.replaceAll("a", "-");
            int total = Integer.parseInt(second + first);
            returnArray[analogCnt] = total;
            if (i == 0) {
                System.out.print("(" + byteToHexString(new byte[]{analogBytes[i]}) + "-" + byteToHexString(new byte[]{analogBytes[i + 1]}) + ")=" + total + " || ");
            }
            analogCnt++;
        }
        return returnArray;
    }

    public static synchronized int[] getConsoleAnalogValues(byte[] analogBytes) {
        int analogCnt = 0;
        int[] returnArray = new int[analogBytes.length / 2];
        for (int i = 0; i < analogBytes.length; i += 2) {
            int firstBitmask = 0xF0;
            int secondBitmask = 0xFF;
            String first = Integer.toBinaryString(analogBytes[i] & firstBitmask);
            String second = Integer.toBinaryString(analogBytes[i + 1] & secondBitmask);
            while (first.length() < 8) {
                first = "0" + first;
            }
            while (second.length() < 8) {
                second = "0" + second;
            }
            first = first.substring(0, 4);
            String fullBinaryValue = second + first;
            int val = getTwosComplement(fullBinaryValue);
            returnArray[analogCnt] = val;
            analogCnt++;
            System.out.print("[" + val + "] ");
        }
        System.out.println("");
        return returnArray;
    }

    public static synchronized void processFullReport(int rtuAddress, int dataLength, byte[] messageBody) {
        int numDigitalBytes = 0;
        byte[] digitalBytes = Arrays.copyOfRange(messageBody, 1, numDigitalBytes + 1); //Skip the status byte
        int analogStart = digitalBytes.length; //Skip the status byte
        int analogEnd = dataLength - 1;
        byte[] analogBytes = Arrays.copyOfRange(messageBody, analogStart, analogEnd);
        if (rtuAddress >= 224) {
            getConsoleAnalogValues(analogBytes);
        } else {
            getAnalogValues(analogBytes);
        }
    }
}
