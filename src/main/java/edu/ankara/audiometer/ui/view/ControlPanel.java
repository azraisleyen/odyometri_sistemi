package edu.ankara.audiometer.ui.view;

import edu.ankara.audiometer.domain.model.ThresholdCriterion;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public final class ControlPanel extends TitledPane {
    public ControlPanel(
            Runnable start,
            Runnable pause,
            Runnable stop,
            Runnable present,
            Runnable response,
            Runnable autoSimulate,
            Runnable reset,
            Consumer<ThresholdCriterion> criterion
    ) {
        setText("Test Control & Simulation");
        setCollapsible(false);

        ComboBox<String> ear = new ComboBox<>();
        ear.getItems().addAll("Both", "Right", "Left");
        ear.setValue("Both");

        ComboBox<String> mode = new ComboBox<>();
        mode.getItems().addAll("Automatic Hughson-Westlake", "Manual");
        mode.setValue("Automatic Hughson-Westlake");

        ComboBox<ThresholdCriterion> criteria = new ComboBox<>();
        criteria.getItems().addAll(ThresholdCriterion.values());
        criteria.setValue(ThresholdCriterion.TWO_OUT_OF_THREE_ASCENDING);
        criteria.setOnAction(event -> criterion.accept(criteria.getValue()));

        Button startButton = new Button("Start test");
        startButton.setOnAction(event -> start.run());
        Button pauseButton = new Button("Pause test");
        pauseButton.setOnAction(event -> pause.run());
        Button stopButton = new Button("Stop test");
        stopButton.setOnAction(event -> stop.run());
        Button resetButton = new Button("Reset test");
        resetButton.setOnAction(event -> reset.run());
        Button presentButton = new Button("Present tone");
        presentButton.setOnAction(event -> present.run());
        Button responseButton = new Button("Mark RESPONSE");
        responseButton.getStyleClass().add("response");
        responseButton.setOnAction(event -> response.run());
        Button autoButton = new Button("Auto simulate step");
        autoButton.setOnAction(event -> autoSimulate.run());

        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(8);
        form.addRow(0, new Label("Ear"), ear);
        form.addRow(1, new Label("Mode"), mode);
        form.addRow(2, new Label("Criterion"), criteria);

        FlowPane buttons = new FlowPane(8, 8, startButton, pauseButton, stopButton, resetButton, presentButton, responseButton, autoButton);
        setContent(new VBox(10, form, buttons, new Label("Simulation thresholds: RIGHT 25 dB HL, LEFT 30 dB HL.")));
    }
}
