module com.example.rtutester {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.example.rtutester to javafx.fxml;
    exports com.example.rtutester;
}