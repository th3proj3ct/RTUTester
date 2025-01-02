package com.example.rtutester;

public class FunctionHelper {

    public static String invertDigits(String binaryInt) {
        String result = binaryInt;
        result = result.replace("0", " "); //temp replace 0s
        result = result.replace("1", "0"); //replace 1s with 0s
        result = result.replace(" ", "1"); //put the 1s back in
        return result;
    }

    public static int getTwosComplement(String binaryInt) {
        //Check if the number is negative.
        //We know it's negative if it starts with a 1
        if (binaryInt.charAt(0) == '1') {
            //Call our invert digits method
            String invertedInt = invertDigits(binaryInt);
            //Change this to decimal format.
            int decimalValue = Integer.parseInt(invertedInt, 2);
            //Add 1 to the curernt decimal and multiply it by -1
            //because we know it's a negative number
            decimalValue = (decimalValue + 1) * -1;
            //return the final result
            return decimalValue;
        } else {
            //Else we know it's a positive number, so just convert
            //the number to decimal base.
            return Integer.parseInt(binaryInt, 2);
        }
    }


    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static void printArray(byte[] bytes) {
        String outputString = "";
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            outputString += HEX_ARRAY[v >>> 4] + "" + HEX_ARRAY[v & 0x0F] + "-";
//            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
//            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        outputString += "";
        System.out.println(outputString);
    }
    public static String getByteArrayToString(byte[] bytes) {
        String outputString = "";
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            outputString += HEX_ARRAY[v >>> 4] + "" + HEX_ARRAY[v & 0x0F] + "-";
//            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
//            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        outputString += "";
        return  outputString;
    }

    public static String byteToHexString(byte[] b) {
        int v = b[0] & 0xFF;
        return HEX_ARRAY[v >>> 4] + "" + HEX_ARRAY[v & 0x0F];
    }
    public static String byteToHexString(byte b) {
        String outputString = "";
//        char[] hexChars = new char[bytes.length * 2];
//        for (int j = 0; j < bytes.length; j++) {
        int v = b & 0xFF;
        outputString += HEX_ARRAY[v >>> 4] + "" + HEX_ARRAY[v & 0x0F] + "-";
//            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
//            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
//        }
//        outputString += "]";
        System.out.println(outputString);
        return outputString;
    }
}
