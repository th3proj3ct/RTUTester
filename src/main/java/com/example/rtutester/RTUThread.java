package com.example.rtutester;

import javafx.scene.control.Slider;

import java.util.Arrays;

import static com.example.rtutester.Const.*;
import static com.example.rtutester.Const.HEADER_LENGTH;
import static com.example.rtutester.FunctionHelper.*;
import static com.example.rtutester.HelloController.selectedRTU;
import static com.example.rtutester.HelloController.socket;

public class RTUThread implements Runnable {
    public RTUThread(HelloController controller){

        run=true;
        this.controller=controller;
    }
    public static boolean run = false;
    static HelloController controller;

    @Override
    public void run() {
        try {
            while (run) {
                socket.getOutputStream().write(getRFR(selectedRTU));
                controller.incrementTx();
//                System.out.println("Requested data from RTU 224");
                Thread.sleep(50);
                int n = socket.getInputStream().read(MAX_SIZE);
                controller.incrementRx();
//                System.out.println("Got " + n + " bytes");
                if (n >= 0) {
                    byte[] readData = Arrays.copyOfRange(MAX_SIZE, 0, n);
                    try {
//                        System.out.println("Got ");
                        printArrayToTextArea(readData);
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("end of thread");
    }

    public static byte[] getRFR(int rtuAddress){

        byte[] RFR = new byte[]{0x01, (byte) rtuAddress, 0x01, 0x09, 0x00};
        byte[] cpy = CRC16.addCrc(RFR);
        printArray(cpy);
        return cpy;
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
                System.out.println(controller.getCard());
                if(!controller.getCard().contains("504")){
                    System.out.println("Trying to get analog values");
                    //If the card is a 501
                    if (rtuAddress >= 224) {
                        getConsoleAnalogValues(body);
                    } else {
                        getAnalogValues(body);
                    }
                }else{
                    System.out.println("Trying to get digital values");
                    getDigitalValues(body);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void getDigitalValues(byte[] body){
        int numDigitalBytes = 16;
//        SystemLogger.getInstance().printLine("RTUParser: Num digital bytes: " + numDigitalBytes);
        byte[] digitalBytes = Arrays.copyOfRange(body, 1, numDigitalBytes + 1);
//        SystemLogger.getInstance().printLine("RTUParser: Total digital bytes retrieved: " + digitalBytes.length);
        boolean[] bits = getDigitalBits(digitalBytes);
        for (int i = 0; i < numDigitalBytes; i++) {
            for (int j = 0; j < 8; j++) {
                controller.setChannel((i * 8) + j, bits[(i * 8) + j]);
            }
        }
    }

    private static boolean[] getDigitalBits(byte[] digitalBytes) {
        boolean[] bits = new boolean[digitalBytes.length * 8];
        int bitCounter = 0;
        for (int j = 0; j < digitalBytes.length; j++) {
            for (int i = 0; i < 8; i++) {
                bits[bitCounter] = isSet(digitalBytes[j], i);
                bitCounter++;
            }
        }
        return bits;
    }

    private static boolean isSet(byte b, int bit) {
        int bitPosition = bit % 8;  // Position of this bit in a byte
        return (~b >> bitPosition & 1) == 1;
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
        for(int i=0; i<returnArray.length; i++){
            controller.setChannel(i+1,returnArray[i]);
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
        for(int i=0; i<returnArray.length; i++){
            controller.setChannel(i+1,returnArray[i]);
        }
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


    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public void printArrayToTextArea(byte[] bytes) {
        String outputString = "";
        for (byte aByte : bytes) {
            int v = aByte & 0xFF;
            outputString += HEX_ARRAY[v >>> 4] + "" + HEX_ARRAY[v & 0x0F] + "-";
        }
        outputString=outputString.substring(0,outputString.length()-1);
        controller.appendText(outputString);
        System.out.println(outputString);
    }

    public static String byteToHexString(byte[] b) {
        int v = b[0] & 0xFF;
        return HEX_ARRAY[v >>> 4] + "" + HEX_ARRAY[v & 0x0F];
    }
    public static String byteToHexString(byte b) {
        String outputString = "";
        int v = b & 0xFF;
        outputString += HEX_ARRAY[v >>> 4] + "" + HEX_ARRAY[v & 0x0F] + "-";
        outputString=outputString.substring(0,outputString.length()-1);
        return outputString;
    }
}
