package com.example.rtutester;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class RTUTesterApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(RTUTesterApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1060, 550);
        stage.setTitle("RTU Tester!");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Needed this because the timer threads in the controllers would continue
     * running even while the stage was closed.
     */
    @Override
    public void stop() {
        System.exit(0);
    }

    public static void main(String[] args) {
        launch();
    }
}