package edu.ankara.audiometer.ui.view;

import edu.ankara.audiometer.infrastructure.serial.SerialPortGateway;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public final class SerialPanel extends TitledPane {
    public SerialPanel(SerialPortGateway gateway, boolean simulationMode, int defaultBaudRate, Consumer<String> log, Consumer<String> onIncomingLine) {
        setText("Serial Connection");
        setCollapsible(false);

        ComboBox<String> ports = new ComboBox<>();
        ports.setPrefWidth(170);
        ports.setMinWidth(170);
        ports.getItems().addAll(gateway.listPorts());
        if (!ports.getItems().isEmpty()) {
            ports.setValue(ports.getItems().getFirst());
        }

        ComboBox<Integer> baud = new ComboBox<>();
        baud.setPrefWidth(120);
        baud.setMinWidth(120);
        baud.getItems().addAll(9600, 19200, 115200);
        baud.setValue(defaultBaudRate);

        Label status = new Label("Disconnected");
        Label modeIndicator = new Label("Mode: Hardware serial");
        modeIndicator.getStyleClass().add("serial-mode-indicator");
        Label modeHelp = new Label("Select a COM port and click Connect.");
        modeHelp.getStyleClass().add("serial-mode-help");

        Button refresh = new Button("Refresh");
        refresh.setOnAction(event -> {
            ports.getItems().setAll(gateway.listPorts());
            if (!ports.getItems().isEmpty()) {
                ports.setValue(ports.getItems().getFirst());
            }
            log.accept("Serial port list refreshed: " + ports.getItems().size() + " port(s)");
        });

        Button connect = new Button("Connect");
        connect.setOnAction(event -> {
            if (ports.getValue() == null) {
                log.accept("No serial port selected");
                return;
            }
            var result = gateway.connect(ports.getValue(), baud.getValue(), onIncomingLine);
            status.setText(gateway.status().message());
            log.accept(result.isOk() ? "Serial connected to " + ports.getValue() : "Serial connection failed");
        });

        Button disconnect = new Button("Disconnect");
        disconnect.setOnAction(event -> {
            gateway.disconnect();
            status.setText("Disconnected");
            log.accept("Serial disconnected");
        });

        setContent(new VBox(
                8,
                new Label("Port"), ports,
                new Label("Baud rate"), baud,
                new FlowPane(8, 8, refresh, connect, disconnect),
                status,
                modeIndicator,
                modeHelp
        ));
    }
}
