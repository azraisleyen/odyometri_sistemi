package edu.ankara.audiometer.ui.view;

import edu.ankara.audiometer.domain.model.EarTestMode;
import edu.ankara.audiometer.domain.model.ThresholdCriterion;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.function.Consumer;

public final class ControlPanel extends TitledPane {
    public static final String PROCEDURE_LABEL = "Procedure: Automatic Hughson-Westlake";

    public ControlPanel(
            Runnable start,
            Runnable pause,
            Runnable resume,
            Runnable stop,
            Runnable present,
            Runnable response,
            Runnable autoSimulate,
            Runnable reset,
            Consumer<ThresholdCriterion> criterion,
            Consumer<EarTestMode> earMode
    ) {
        setText("Test Control & Simulation");
        setCollapsible(false);

        ComboBox<EarTestMode> ear = new ComboBox<>();
        ear.getItems().addAll(EarTestMode.BOTH, EarTestMode.RIGHT_ONLY, EarTestMode.LEFT_ONLY);
        ear.setValue(EarTestMode.BOTH);
        ear.setOnAction(event -> earMode.accept(ear.getValue()));

        Label procedureValue = new Label(AudiometerUiLabels.PROCEDURE_VALUE);
        procedureValue.getStyleClass().add("procedure-label");
        procedureValue.setMinWidth(205);

        ComboBox<ThresholdCriterion> criteria = new ComboBox<>();
        criteria.getItems().addAll(ThresholdCriterion.values());
        criteria.setConverter(thresholdCriterionConverter());
        criteria.setPrefWidth(205);
        criteria.setValue(ThresholdCriterion.TWO_OUT_OF_THREE_ASCENDING);
        criteria.setOnAction(event -> criterion.accept(criteria.getValue()));

        Button startButton = new Button("Start test");
        startButton.setOnAction(event -> start.run());
        Button pauseButton = new Button("Pause test");
        pauseButton.setOnAction(event -> pause.run());
        Button resumeButton = new Button("Resume test");
        resumeButton.setOnAction(event -> resume.run());
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
        form.addRow(1, new Label(AudiometerUiLabels.PROCEDURE_LABEL), procedureValue);
        form.addRow(2, new Label("Criterion"), criteria);

        FlowPane buttons = new FlowPane(
                8,
                8,
                startButton,
                pauseButton,
                resumeButton,
                stopButton,
                resetButton,
                presentButton,
                responseButton,
                autoButton
        );
        setContent(new VBox(10, form, buttons, new Label("Simulation thresholds: RIGHT 25 dB HL, LEFT 30 dB HL.")));
    }

    private static StringConverter<ThresholdCriterion> thresholdCriterionConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(ThresholdCriterion criterion) {
                if (criterion == null) {
                    return "";
                }
                return switch (criterion) {
                    case TWO_OUT_OF_THREE_ASCENDING -> "2 of 3 Ascending";
                    case THREE_OUT_OF_FIVE_ASCENDING -> "3 of 5 Ascending";
                };
            }

            @Override
            public ThresholdCriterion fromString(String value) {
                if ("3 of 5 Ascending".equals(value)) {
                    return ThresholdCriterion.THREE_OUT_OF_FIVE_ASCENDING;
                }
                return ThresholdCriterion.TWO_OUT_OF_THREE_ASCENDING;
            }
        };
    }
}
