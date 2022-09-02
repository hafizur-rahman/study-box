package com.jdreamer.study;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.media.*;
import javafx.stage.Stage;

import java.io.FileInputStream;

public class StudyBox extends Application {

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        // Path to the FXML File
        String fxmlDocPath = "C:\\dev\\media-player\\src\\main\\resources\\StudyBox.fxml";
        FileInputStream fxmlStream = new FileInputStream(fxmlDocPath);
        BorderPane root = (BorderPane) loader.load(fxmlStream);

        Scene scene = new Scene(root);

        stage.setTitle("StudyBox");
        stage.setScene(scene);
        stage.show();
    }
}
