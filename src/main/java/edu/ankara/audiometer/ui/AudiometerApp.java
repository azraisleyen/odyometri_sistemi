package edu.ankara.audiometer.ui;

import edu.ankara.audiometer.application.AudiometryUseCase;
import edu.ankara.audiometer.infrastructure.config.JsonAudiometryConfigLoader;
import edu.ankara.audiometer.infrastructure.serial.FakeSerialGateway;
import edu.ankara.audiometer.infrastructure.serial.JSerialCommGateway;
import edu.ankara.audiometer.infrastructure.serial.SerialPortGateway;
import edu.ankara.audiometer.ui.controller.MainController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class AudiometerApp extends Application {
    @Override
    public void start(Stage stage) {
        boolean simulationMode = getParameters().getRaw().contains("--simulation");
        SerialPortGateway gateway = simulationMode ? new FakeSerialGateway() : new JSerialCommGateway();
        var configResult = new JsonAudiometryConfigLoader().loadDefault();
        var useCase = new AudiometryUseCase(configResult.config(), gateway);
        var root = new MainController(useCase, simulationMode, configResult.warning()).root();
        var scene = new Scene(root, 1280, 820);
        var css = getClass().getResource("/edu/ankara/audiometer/ui/theme/Styles.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
        stage.setTitle("Odyometre Sistemi Tasarımı ve Testi");
        stage.setMinWidth(1100);
        stage.setMinHeight(720);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
