package edu.ankara.audiometer.ui.controller;

import edu.ankara.audiometer.application.AudiometryUseCase;
import edu.ankara.audiometer.domain.fp.Result;
import edu.ankara.audiometer.domain.model.TestPhase;
import edu.ankara.audiometer.domain.model.TestState;
import edu.ankara.audiometer.domain.model.ThresholdCriterion;
import edu.ankara.audiometer.ui.view.AudiogramChartPane;
import edu.ankara.audiometer.ui.view.ControlPanel;
import edu.ankara.audiometer.ui.view.LogPanel;
import edu.ankara.audiometer.ui.view.ResultPanel;
import edu.ankara.audiometer.ui.view.SerialPanel;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.VBox;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class MainController {
    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final AudiometryUseCase useCase;
    private final boolean simulationMode;
    private final AudiogramChartPane chart = new AudiogramChartPane();
    private final LogPanel log = new LogPanel();
    private final ResultPanel results = new ResultPanel();
    private final Label mode = new Label();
    private final Label stateLabel = new Label();
    private int lastThresholdCount = 0;

    public MainController(AudiometryUseCase useCase, boolean simulationMode) {
        this.useCase = useCase;
        this.simulationMode = simulationMode;
        this.mode.setText(simulationMode ? "Simulation Mode" : "Serial Mode");
        this.useCase.sessions().onState(state -> Platform.runLater(() -> refresh(state)));
    }

    public Parent root() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app");
        root.setTop(header());

        var controls = new ControlPanel(
                this::start,
                this::pause,
                this::stop,
                this::present,
                this::manualResponse,
                this::autoSimulate,
                this::reset,
                this::criterionChanged
        );
        var serial = new SerialPanel(
                useCase.sessions().gateway(),
                simulationMode,
                log::add,
                line -> Platform.runLater(() -> {
                    log.add("Incoming serial: " + line);
                    useCase.sessions().applyRawMessages(java.util.List.of(line));
                })
        );

        VBox left = new VBox(12, serial, controls, currentState(), exportPanel());
        left.setPadding(new Insets(12));
        left.setPrefWidth(380);

        SplitPane center = new SplitPane(chart, new VBox(10, results, log));
        center.setDividerPositions(.62);
        root.setLeft(left);
        root.setCenter(center);
        refresh(useCase.sessions().state());
        return root;
    }

    private Parent header() {
        Label title = new Label("Odyometre Sistemi Tasarımı ve Testi");
        title.getStyleClass().add("title");
        Label sub = new Label("Functional Programming Based Audiometry Test Software");
        sub.getStyleClass().add("subtitle");
        mode.getStyleClass().add("mode");
        VBox box = new VBox(3, title, sub, mode);
        box.setPadding(new Insets(14));
        return box;
    }

    private Parent currentState() {
        stateLabel.getStyleClass().add("state");
        TitledPane pane = new TitledPane("Current Test State", new VBox(stateLabel));
        pane.setCollapsible(false);
        return pane;
    }

    private Parent exportPanel() {
        Button csv = new Button("Export CSV");
        csv.setOnAction(event -> exportCsv());
        Button json = new Button("Export JSON");
        json.setOnAction(event -> exportJson());
        TitledPane pane = new TitledPane("Export Controls", new FlowPane(8, 8, csv, json));
        pane.setCollapsible(false);
        return pane;
    }

    private void start() {
        log.add("Test started at " + LocalDateTime.now());
        present();
    }

    private void pause() {
        useCase.sessions().pause();
        log.add("Session paused");
    }

    private void stop() {
        useCase.sessions().stop();
        log.add("Session stopped; reset is required before continuing");
    }

    private void present() {
        Result<String, String> result = useCase.sessions().presentTone();
        if (result.isOk()) {
            log.add("Outgoing serial command: " + result.orElse(""));
        } else {
            log.add("Tone presentation rejected or serial send failed");
        }
    }

    private void manualResponse() {
        useCase.sessions().response();
        log.add("Incoming: RESPONSE (manual simulation)");
    }

    private void autoSimulate() {
        var command = useCase.sessions().presentTone();
        if (command.isOk()) {
            log.add("Outgoing serial command: " + command.orElse(""));
        }
        var state = useCase.sessions().state();
        var threshold = useCase.simulation().thresholdFor(state.currentEar(), state.currentFrequency());
        if (state.currentIntensity().value() >= threshold.value()) {
            useCase.sessions().response();
            log.add("Auto simulation: current level >= simulated threshold; RESPONSE applied");
        } else {
            useCase.sessions().noResponse();
            log.add("Auto simulation: current level below simulated threshold; NO_RESPONSE applied");
        }
    }

    private void reset() {
        useCase.sessions().reset();
        lastThresholdCount = 0;
        log.clear();
        log.add("Session reset");
    }

    private void criterionChanged(ThresholdCriterion criterion) {
        useCase.sessions().setCriterion(criterion);
        log.add("Criterion changed to " + criterion);
    }

    private void exportCsv() {
        Path path = Path.of("exports", "audiogram-" + FILE_TIME.format(LocalDateTime.now()) + ".csv");
        var result = useCase.exports().exportCsv(useCase.sessions().state(), path);
        log.add(result.isOk() ? "CSV export written: " + path : "CSV export failed");
    }

    private void exportJson() {
        Path path = Path.of("exports", "session-" + FILE_TIME.format(LocalDateTime.now()) + ".json");
        var result = useCase.exports().exportJson(useCase.sessions().state(), path);
        log.add(result.isOk() ? "JSON export written: " + path : "JSON export failed");
    }

    private void refresh(TestState state) {
        stateLabel.setText("Ear: %s%nFrequency: %d Hz%nIntensity: %d dB HL%nPhase: %s%nResponses/events: %d%nCompleted thresholds: %d%nNext action: %s".formatted(
                state.currentEar(),
                state.currentFrequency().value(),
                state.currentIntensity().value(),
                state.phase(),
                state.eventHistory().size(),
                state.audiogram().points().size(),
                recommendation(state)
        ));
        chart.update(state.audiogram());
        results.update(state.audiogram());
        if (state.audiogram().points().size() > lastThresholdCount) {
            var point = state.audiogram().points().getLast();
            log.add("Threshold detected: " + point.ear() + " " + point.frequency().value() + " Hz = " + point.thresholdDbHL().value() + " dB HL");
            lastThresholdCount = state.audiogram().points().size();
        }
        if (state.phase() == TestPhase.COMPLETED) {
            log.add("Session completed; export results for the report.");
        }
    }

    private static String recommendation(TestState state) {
        return switch (state.phase()) {
            case COMPLETED -> "Export results";
            case PAUSED -> "Resume by resetting or starting a new presentation after reset";
            case STOPPED -> "Reset required";
            default -> "Present tone / wait for RESPONSE";
        };
    }
}
