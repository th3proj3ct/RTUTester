package com.example.rtutester;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.ResourceBundle;
import javax.swing.*;

import static com.example.rtutester.Const.*;
import static com.example.rtutester.FunctionHelper.*;

public class RTUPageController implements Initializable {

    public static Socket socket;
    private static BufferedReader in;

    //    public static String NPORT_IP_ADDR = "192.168.127.254";
    public static String NPORT_IP_ADDR = "192.168.1.250";
    public static int SERIAL_SERVER_PORT_1 = 4001;
    public static int SERIAL_SERVER_PORT_2 = 4002;

    @FXML
    Label calibratedText;
    @FXML
    public CheckBox xbox1, xbox2, xbox3, xbox4, xbox5, xbox6, xbox7, xbox8, xbox9, xbox10, xbox11, xbox12, xbox13, xbox14, xbox15, xbox16;
    @FXML
    public TextField analogCh1, analogCh2, analogCh3, analogCh4, analogCh5, analogCh6, analogCh7, analogCh8, analogCh9, analogCh10,
            analogCh11, analogCh12, analogCh13, analogCh14, analogCh15, analogCh16;

    @FXML
    public VBox statusVbox;
    @FXML
    public AnchorPane anchorPane;
    @FXML
    public HBox hBoxSlider8, hBoxSlider7, hBoxSlider6, analogHbox, discreteHbox;
    @FXML
    public CheckBox statusXbox0, statusXbox1, statusXbox2, statusXbox3, statusXbox4, statusXbox5, statusXbox6, statusXbox7;
    @FXML
    public Slider slider1, slider2, slider3, slider4, slider5, slider6, slider7, slider8;
    @FXML
    public ComboBox<String> comboBoxCard;
    @FXML
    public Button calibrateButton;
    @FXML
    public Label calibrateLabel;
    @FXML
    public ToggleButton startButton, stopButton;
    @FXML
    private Label selectedRTULabel, descrLabel;
    @FXML
    public TextArea textArea;
    @FXML
    public VBox dipSwitchVbox;
    public static int selectedRTU = 0;

    @FXML
    public CheckBox processorCheckbox;
    @FXML
    public Label txLabel, rxLabel;
    private Thread thread;
    static DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    int tx = 0, rx = 0;

    public void incrementTx() {
        Platform.runLater(() -> {
            tx++;
            txLabel.setText("" + tx);
            if (tx >= Integer.MAX_VALUE - 50) {
                tx = 0;
            }
        });
    }

    public void incrementRx() {
        Platform.runLater(() -> {
            rx++;
            rxLabel.setText("" + rx);
            if (rx >= Integer.MAX_VALUE - 50) {
                rx = 0;
            }
        });
    }

    double calibratedTextCapacity = 1;
    Timer calibratedTimer = new Timer(125, new ActionListener() {
        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            calibratedTextCapacity -= 0.05;
            calibratedText.setOpacity(calibratedTextCapacity);
        }

    });

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        processorCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                hBoxSlider6.setVisible(true);
                hBoxSlider7.setVisible(true);
                hBoxSlider8.setVisible(true);
                calibrateButton.setVisible(false);
                calibrateLabel.setVisible(false);
                recalcRTUFromDipswitches(true);
            } else {
                hBoxSlider6.setVisible(false);
                hBoxSlider7.setVisible(false);
                hBoxSlider8.setVisible(false);
                calibrateButton.setVisible(true);
                calibrateLabel.setVisible(true);
                recalcRTUFromDipswitches(false);
            }
        });
        calibratedText.setOpacity(0);
        thread = new Thread(new RTUThread(this));
        Slider[] allSliders = new Slider[]{slider1, slider2, slider3, slider4, slider5, slider6, slider7, slider8};
        for (Slider s : allSliders) {
            s.valueProperty().addListener((observable, oldValue, newValue) ->
                    recalcRTUFromDipswitches(processorCheckbox.isSelected()));
        }
        comboBoxCard.getItems().add("501-101/301");
//        comboBoxCard.getItems().add("501-301");
//        comboBoxCard.getItems().add("502-101");
        comboBoxCard.getItems().add("504-101");
        comboBoxCard.setOnAction(e -> {
            String selectedOption = comboBoxCard.getSelectionModel().getSelectedItem();
            analogHbox.setVisible(!selectedOption.contains("504"));
            discreteHbox.setVisible(selectedOption.contains("504"));
            if (selectedOption.contains("501")) {
                descrLabel.setText("10v or 20mA = 2000 counts, 0v/0mA = 0 counts, linear changes");
            } else {
                descrLabel.setText("");
            }
        });
        ToggleGroup group = new ToggleGroup();
        startButton.setToggleGroup(group);
        stopButton.setToggleGroup(group);
        stopButton.setSelected(true);
        startButton.setOnAction(e -> {
            int port = -1;
            try {
                socket = new Socket();
                if (processorCheckbox.isSelected()) {
                    port = SERIAL_SERVER_PORT_2;
                } else {
                    port = SERIAL_SERVER_PORT_1;
                }
                socket.connect(new InetSocketAddress(NPORT_IP_ADDR, port), 1000);
            } catch (Exception ex) {
                appendText("Could not connect to [" + NPORT_IP_ADDR + ":" + port + "]");
                stopButton.setSelected(true);
                return;
            }
            if (thread.isAlive()) {
                RTUThread.run = true;
            } else {
                thread = new Thread(new RTUThread(this));
                thread.start();
            }
            dipSwitchVbox.setDisable(true);
        });
        stopButton.setOnAction(e -> {
            if (thread.isAlive()) {
                RTUThread.run = false;
                try {
                    socket.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            dipSwitchVbox.setDisable(false);
        });
        comboBoxCard.getSelectionModel().selectFirst();
        recalcRTUFromDipswitches(true);
        calibrateButton.setVisible(false);
        calibrateLabel.setVisible(true);
        descrLabel.setText("10v or 20mA = 2000 counts, 0v/0mA = 0 counts, linear changes");
        discreteHbox.setVisible(false);
    }

    private void recalcRTUFromDipswitches(boolean processor) {
        String binaryString = "";
        Slider[] sliders;
        if (processor) {
            sliders = new Slider[]{slider1, slider2, slider3, slider4, slider5, slider6, slider7, slider8};
        } else {

            sliders = new Slider[]{slider5, slider4, slider3, slider2, slider1};
        }
        for (Slider s : sliders) {
            binaryString += s.getValue() == 0 ? "0" : "1";
        }
        if (processor) {
            selectedRTU = Integer.parseInt(binaryString, 2);
        } else {
            selectedRTU = (224 + Integer.parseInt(binaryString, 2));
        }
        selectedRTULabel.setText("Selected RTU: " + selectedRTU);
    }

    @FXML
    public void calibrateRTU(ActionEvent actionEvent) {
        try {
            appendText("Attempting to create socket ADDRESS = " + NPORT_IP_ADDR + ":" + SERIAL_SERVER_PORT_1);
            if (socket == null || socket.isClosed()) {
                socket = new Socket();
                socket.connect(new InetSocketAddress(NPORT_IP_ADDR, SERIAL_SERVER_PORT_1), 1000);
                appendText("Socket created");
            }
            calibrateRTU(selectedRTU);
            calibratedText.setTextFill(Color.GREEN);
            calibratedText.setText("Calibration Sent!");
            appendText("RTU " + selectedRTU + " message sent");
        } catch (Exception e) {
            appendText("Failed to calibrate RTU");
            calibratedText.setTextFill(Color.RED);
            calibratedText.setText("FAILED!");
            System.err.println("RTURT Main: While loop exception");
            e.printStackTrace();
        }
        calibratedTextCapacity = 1;
        calibratedText.setOpacity(1);
        calibratedTimer.start();
    }

    public void appendText(String s) {
        textArea.appendText(LocalDateTime.now().format(myFormatObj) + ": " + s + "\n\r");
        System.out.println(s);
    }

    boolean areaVisible = true;

    @FXML
    public void checkAddresses(ActionEvent e) {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(NPORT_IP_ADDR, 4002), 1000);
            appendText("Checking Addresses [this will take ~1minute]");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i <= 255; i++) {
                        try {
                            byte[] request = RTUThread.getRFR(i);
                            socket.getOutputStream().write(request);
                            System.out.println("Sending request to Address " + i);
                            printArray(request);
                            Thread.sleep(100);
                            int n = socket.getInputStream().read(MAX_SIZE, 0, socket.getInputStream().available());
                            System.out.println("Got " + n + " bytes");
                            if (n > 0) {
                                byte[] readData = Arrays.copyOfRange(MAX_SIZE, 0, n);
                                appendText("GOT RESPONSE ON ADDRESS [ " + i + " ]");
                                appendText("Got: "+getByteArrayToString(readData));
                                return;
                            }
                        } catch (Exception ed) {
                            ed.printStackTrace();
                        }
                    }
                    appendText("Did not get a response on any RTU 0-255");
                }
            }).start();
            Thread.sleep(1000);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    public void toggleRawData(ActionEvent e) {
        areaVisible = !areaVisible;
        textArea.setVisible(areaVisible);
    }

    @FXML
    public void showHelp(ActionEvent e) {
        JOptionPane.showMessageDialog(
                null,
                "Open a web browser and navigate to the N-port default IP: 192.168.127.254. Default user: admin Default password: moxa",
                "N-port Web Interface", JOptionPane.INFORMATION_MESSAGE);
    }

    @FXML
    public void showOpSettings(ActionEvent e) {
        ImageIcon icon = new ImageIcon(RTUPageController.class.getResource("/img/operatingsettings.png"));
        JOptionPane.showMessageDialog(
                null,
                "",
                "N-port Operating Settings", JOptionPane.INFORMATION_MESSAGE,
                icon);
    }

    @FXML
    public void showSerSettings(ActionEvent e) {
        ImageIcon icon = new ImageIcon(RTUPageController.class.getResource("/img/serialsettings.png"));
        JOptionPane.showMessageDialog(
                null,
                "",
                "N-port Serial Settings", JOptionPane.INFORMATION_MESSAGE,
                icon);
    }

    @FXML
    public void showProcessor9PinSettings(ActionEvent e) {
        ImageIcon icon = new ImageIcon(RTUPageController.class.getResource("/img/moxa485.png"));
        JOptionPane.showMessageDialog(
                null,
                "",
                "N-port Processor Card Port 2 DB9 Pinout", JOptionPane.INFORMATION_MESSAGE,
                icon);
    }

    @FXML
    public void showPinSettings(ActionEvent e) {
        ImageIcon icon = new ImageIcon(RTUPageController.class.getResource("/img/rs485.png"));
        JOptionPane.showMessageDialog(
                null,
                "",
                "N-port Daughterboard Only Port 1 DB9 Pinout", JOptionPane.INFORMATION_MESSAGE,
                icon);
    }

    public void calibrateRTU(int rtuAddress) throws Exception {
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
            appendText("Failed to calibrate RTU");
            e.printStackTrace();
            throw new Exception();
        }
    }

    public String getCard() {
        return comboBoxCard.getSelectionModel().getSelectedItem();
    }

    public void setChannel(int channel, boolean value) {
        switch (channel) {
            case 1 -> xbox1.setSelected(value);
            case 2 -> xbox2.setSelected(value);
            case 3 -> xbox3.setSelected(value);
            case 4 -> xbox4.setSelected(value);
            case 5 -> xbox5.setSelected(value);
            case 6 -> xbox6.setSelected(value);
            case 7 -> xbox7.setSelected(value);
            case 8 -> xbox8.setSelected(value);
            case 9 -> xbox9.setSelected(value);
            case 10 -> xbox10.setSelected(value);
            case 11 -> xbox11.setSelected(value);
            case 12 -> xbox12.setSelected(value);
            case 13 -> xbox13.setSelected(value);
            case 14 -> xbox14.setSelected(value);
            case 15 -> xbox15.setSelected(value);
            case 16 -> xbox16.setSelected(value);
        }
    }

    public void setChannel(int channel, int value) {
        switch (channel) {
            case 1 -> analogCh1.setText("" + value);
            case 2 -> analogCh2.setText("" + value);
            case 3 -> analogCh3.setText("" + value);
            case 4 -> analogCh4.setText("" + value);
            case 5 -> analogCh5.setText("" + value);
            case 6 -> analogCh6.setText("" + value);
            case 7 -> analogCh7.setText("" + value);
            case 8 -> analogCh8.setText("" + value);
            case 9 -> analogCh9.setText("" + value);
            case 10 -> analogCh10.setText("" + value);
            case 11 -> analogCh11.setText("" + value);
            case 12 -> analogCh12.setText("" + value);
            case 13 -> analogCh13.setText("" + value);
            case 14 -> analogCh14.setText("" + value);
            case 15 -> analogCh15.setText("" + value);
            case 16 -> analogCh16.setText("" + value);
        }
    }

    public static void main(String[] args) {
//        printArray(RFR224);
//        byte b = 100;
//        byteToHex(new byte[]{b});
//        try {
//            System.out.println("Attempting to create socket ADDRESS = " + PORT_SERIAL_SERVER_ADDR1 + ":" + SERIAL_SERVER_PORT);
//            socket = new Socket(PORT_SERIAL_SERVER_ADDR1, SERIAL_SERVER_PORT+1);
//            System.out.println("Socket created");
//            calibrateRTU(224);
//            System.out.println("RTU 224 calibrated");
//            while (run) {
//                socket.getOutputStream().write(RFR224);
//                System.out.println("Requested data from RTU 224");
//                waitForRtuResponse();
//
//
//            }
//        } catch (Exception e) {
//            System.err.println("RTURT Main: While loop exception");
//            e.printStackTrace();
//        }
        try {
            int targetRTU = 3;
            byte[] rtuRFRNoCrc = new byte[]{0x01, (byte) targetRTU, 0x00, 0x09, 0x00};
            System.out.print("RFR " + targetRTU + " with no crc: ");
            printArray(rtuRFRNoCrc);
            byte[] rfrCRC = CRC16.addCrc(rtuRFRNoCrc);
            System.out.print("RFR " + targetRTU + " with crc calculated back in: ");
            printArray(rfrCRC);
            int processorPort = SERIAL_SERVER_PORT_1 + 1;
            System.out.println("Attempting to create socket ADDRESS = " + NPORT_IP_ADDR + ":" + processorPort);
            socket = new Socket(NPORT_IP_ADDR, processorPort);
//            socket.setSoTimeout(3000);
//            socket.setTcpNoDelay(true);
            System.out.println("Socket created");
//            while (run) {
            socket.getOutputStream().write(rfrCRC);
            System.out.println("Requested data from RTU " + targetRTU);
            waitForRtuResponse();
            Thread.sleep(500);
//            }
        } catch (Exception e) {
            System.err.println("RTURT Main: While loop exception");
            e.printStackTrace();
        }
    }

    private static void waitForRtuResponse() throws IOException {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        int cnt = 0;
        while (!in.ready()) {
            try {
                if (cnt > 50) {
                    break;
                }
                cnt++;
                Thread.sleep(20);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (cnt > 10) {
            System.err.println("No response received, stop waiting");
            return;
        }
        System.out.println("Starting parsing");
        int n = socket.getInputStream().read(MAX_SIZE);
        System.out.println("Got " + n + " bytes");
        if (n >= 0) {
            byte[] readData = Arrays.copyOfRange(MAX_SIZE, 0, n);
            try {
                System.out.print("Got ");
                printArray(readData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //Should never be less than zero.
            System.err.println("RTUPT Input stream size <0");
        }
    }

    public void processStatusByte(byte statusByte) {
        statusXbox0.setSelected((statusByte & (byte) 0x01) != 0);
        statusXbox0.setSelected((statusByte & (byte) 0x02) != 0);
        statusXbox0.setSelected((statusByte & (byte) 0x04) != 0);
        statusXbox0.setSelected((statusByte & (byte) 0x08) != 0);
        statusXbox0.setSelected((statusByte & (byte) 0x10) != 0);
        statusXbox0.setSelected((statusByte & (byte) 0x20) != 0);
        statusXbox0.setSelected((statusByte & (byte) 0x40) != 0);
        statusXbox0.setSelected((statusByte & (byte) 0x80) != 0);
    }
}