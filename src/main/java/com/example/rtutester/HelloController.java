package com.example.rtutester;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

import static com.example.rtutester.Const.*;
import static com.example.rtutester.FunctionHelper.*;

public class HelloController {

    public static Socket socket;
    private static BufferedReader in;

    public static String PORT_SERIAL_SERVER_ADDR1 = "192.168.127.254";
    public static int SERIAL_SERVER_PORT = 4001;


    @FXML
    public Slider slider1, slider2, slider3, slider4, slider5;
    @FXML
    private Label welcomeText;

    @FXML
    public void testSliders(ActionEvent actionEvent){
        String binaryString = "";
        Slider[] sliders = new Slider[]{slider5,slider4,slider3,slider2,slider1};
        for (Slider s : sliders) {
            binaryString += s.getValue() == 0 ? "0" : "1";
            System.out.println(s.getValue());
        }
        System.out.println("Binary string [" + binaryString + "]");
        System.out.println(Integer.parseInt(binaryString,2));
        System.out.println((224+Integer.parseInt(binaryString,2)));
    }
    @FXML
    public void testCal(ActionEvent actionEvent) {
        printArray(RFR224);
        byte b = 100;
        byteToHexString(new byte[]{b});
        try {
            System.out.println("Attempting to create socket ADDRESS = " + PORT_SERIAL_SERVER_ADDR1 + ":" + SERIAL_SERVER_PORT);
            socket = new Socket(PORT_SERIAL_SERVER_ADDR1, SERIAL_SERVER_PORT);
            System.out.println("Socket created");
            calibrateRTU(224);
            Thread t = new Thread(new RTUThread());
            t.start();
//            System.out.println("RTU 224 calibrated");
        } catch (Exception e) {
            System.err.println("RTURT Main: While loop exception");
            e.printStackTrace();
        }
    }

    @FXML
    public void testRFR(ActionEvent e) {
        System.out.println("Ahh RFR");
    }

    public static void calibrateRTU(int rtuAddress) {
        try {
            for (int i = 0; i < 16; i++) {
                byte[] cpy = Arrays.copyOf(CALIBRATE_CH_MSG, CALIBRATE_CH_MSG.length);
                cpy[1] = (byte) rtuAddress;
                cpy[5] = (byte) i;
                cpy = CRC16.addCrc(cpy);
                socket.getOutputStream().write(cpy);
                Thread.sleep(150);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}