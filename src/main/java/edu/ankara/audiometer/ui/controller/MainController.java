package edu.ankara.audiometer.ui.controller;

import edu.ankara.audiometer.application.AudiometryUseCase;
import edu.ankara.audiometer.domain.fp.Result;
import edu.ankara.audiometer.domain.model.EarTestMode;
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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class MainController {
    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final AudiometryUseCase useCase;
    private final boolean simulationMode;
    private final String configWarning;
    private final AudiogramChartPane chart = new AudiogramChartPane();
    private final LogPanel log = new LogPanel();
    private final ResultPanel results = new ResultPanel();
    private final Label mode = new Label();
    private final Label stateLabel = new Label();
    private final Label completionBadge = new Label();
    private final Label exportStatus = new Label("No completed thresholds yet.");
    private final Button exportCsvButton = new Button("Export CSV");
    private final Button exportJsonButton = new Button("Export JSON");
    private int lastThresholdCount = 0;
    private int lastEventCount = 0;
    private boolean completionLogged = false;

    public MainController(AudiometryUseCase useCase, boolean simulationMode, String configWarning) {
        this.useCase = useCase;
        this.simulationMode = simulationMode;
        this.configWarning = configWarning == null ? "" : configWarning;
        this.mode.setText("Serial Mode");
        this.useCase.sessions().onState(state -> Platform.runLater(() -> refresh(state)));
    }

    public Parent root() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app");
        root.setTop(header());

        var controls = new ControlPanel(
                this::start,
                this::pause,
                this::resume,
                this::stop,
                this::present,
                this::manualResponse,
                this::autoSimulate,
                this::reset,
                this::criterionChanged,
                this::earModeChanged
        );
        var serial = new SerialPanel(
                useCase.sessions().gateway(),
                simulationMode,
                useCase.sessions().state().config().serialProtocol().baudRate(),
                log::add,
                line -> Platform.runLater(() -> {
                    log.add("Incoming serial: " + line);
                    useCase.sessions().applyRawMessages(java.util.List.of(line));
                })
        );

        VBox left = new VBox(12, serial, controls, currentState(), exportPanel());
        left.setPadding(new Insets(12));
        left.setPrefWidth(380);

        ScrollPane leftScroll = new ScrollPane(left);
        leftScroll.setFitToWidth(true);
        leftScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        leftScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        leftScroll.getStyleClass().add("left-scroll");
        leftScroll.setPrefWidth(400);

        SplitPane center = new SplitPane(chart, new VBox(10, results, log));
        center.setDividerPositions(.60);
        root.setLeft(leftScroll);
        root.setCenter(center);
        refresh(useCase.sessions().state());
        if (!configWarning.isBlank()) {
            log.add("Config warning: " + configWarning);
        } else {
            log.add("Config loaded from config/audiometry-config.json");
        }
        return root;
    }

    private Parent header() {
        Label title = new Label("Audiometer System Design and Testing");
        title.getStyleClass().add("title");
        Label sub = new Label("Functional Programming-Based Audiometry Test Software");
        sub.getStyleClass().add("subtitle");
        mode.getStyleClass().add("mode");
        VBox box = new VBox(3, title, sub, mode);
        box.setPadding(new Insets(14));
        return box;
    }

    private Parent currentState() {
        stateLabel.getStyleClass().add("state");
        completionBadge.getStyleClass().add("completion-badge");
        completionBadge.setVisible(false);
        TitledPane pane = new TitledPane("Current Test State", new VBox(8, completionBadge, stateLabel));
        pane.setCollapsible(false);
        return pane;
    }

    private Parent exportPanel() {
        exportCsvButton.setOnAction(event -> exportCsv());
        exportJsonButton.setOnAction(event -> exportJson());
        exportStatus.getStyleClass().add("export-status");
        TitledPane pane = new TitledPane("Export Controls", new VBox(8, new FlowPane(8, 8, exportCsvButton, exportJsonButton), exportStatus));
        pane.setCollapsible(false);
        return pane;
    }

    private void start() {
        log.add("Test started at " + LocalDateTime.now());
        present();
    }

    private void pause() {
        useCase.sessions().pause();
        log.add("Session paused; Resume continues the same session");
    }

    private void resume() {
        useCase.sessions().resume();
        log.add("Session resumed");
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
        if (useCase.sessions().state().phase() == TestPhase.COMPLETED) {
            log.add("Session already completed. Export results or reset the test.");
            return;
        }
        var result = useCase.sessions().autoSimulateStep(useCase.simulation());
        if (result.isOk()) {
            var step = result.orElse(null);
            log.add("Outgoing serial command: " + step.command());
            log.add("Auto simulation: " + step.responseEvent() + " applied (simulated threshold " + step.simulatedThresholdDbHL() + " dB HL)");
        } else {
            log.add("Auto simulation step rejected");
        }
    }

    private void reset() {
        useCase.sessions().reset();
        lastThresholdCount = 0;
        lastEventCount = 0;
        completionLogged = false;
        log.clear();
        log.add("Session reset");
    }

    private void criterionChanged(ThresholdCriterion criterion) {
        useCase.sessions().setCriterion(criterion);
        lastThresholdCount = 0;
        lastEventCount = 0;
        completionLogged = false;
        log.add("Criterion changed to " + criterion);
    }

    private void earModeChanged(EarTestMode mode) {
        useCase.sessions().setEarMode(mode);
        lastThresholdCount = 0;
        lastEventCount = 0;
        completionLogged = false;
        log.clear();
        log.add("Ear mode changed to " + mode + "; session reset with ears " + useCase.sessions().state().config().ears());
    }

    private void exportCsv() {
        Path path = Path.of("exports", "audiogram-" + FILE_TIME.format(LocalDateTime.now()) + ".csv");
        var result = useCase.exports().exportCsv(useCase.sessions().state(), path);
        log.add(result.isOk() ? "CSV exported: " + path : "CSV export failed");
    }

    private void exportJson() {
        Path path = Path.of("exports", "session-" + FILE_TIME.format(LocalDateTime.now()) + ".json");
        var result = useCase.exports().exportJson(useCase.sessions().state(), path);
        log.add(result.isOk() ? "JSON exported: " + path : "JSON export failed");
    }

    private void refresh(TestState state) {
        int expectedTotal = expectedThresholdTotal(state);
        boolean completed = state.phase() == TestPhase.COMPLETED;
        completionBadge.setText(completed ? "COMPLETED" : "");
        completionBadge.setVisible(completed);
        completionBadge.setManaged(completed);

        String completionText = completed
                ? "Status: COMPLETED%nAll selected ears and frequencies have been tested.%nYou can export CSV/JSON results.%n"
                : "Status: " + state.phase() + "%n";
        stateLabel.setText((completionText + "Ear: %s%nFrequency: %d Hz%nIntensity: %d dB HL%nPhase: %s%nResponses/events: %d%nCompleted thresholds: %d / %d%nNext action: %s").formatted(
                state.currentEar(),
                state.currentFrequency().value(),
                state.currentIntensity().value(),
                state.phase(),
                state.eventHistory().size(),
                state.audiogram().points().size(),
                expectedTotal,
                recommendation(state)
        ));
        chart.update(state.audiogram());
        results.update(state.audiogram(), expectedTotal);
        updateExportControls(state);
        logNewEvents(state);
        if (state.audiogram().points().size() > lastThresholdCount) {
            var point = state.audiogram().points().getLast();
            log.add("Threshold detected: " + point.ear() + " " + point.frequency().value() + " Hz = " + point.thresholdDbHL().value() + " dB HL");
            lastThresholdCount = state.audiogram().points().size();
        }
        if (completed && !completionLogged) {
            log.add("Session completed; export results for the report.");
            completionLogged = true;
        }
    }

    private void updateExportControls(TestState state) {
        boolean hasResults = !state.audiogram().points().isEmpty();
        exportCsvButton.setDisable(!hasResults);
        exportJsonButton.setDisable(!hasResults);
        exportStatus.setText(!hasResults
                ? "Complete at least one threshold before exporting."
                : state.phase() == TestPhase.COMPLETED
                ? "Results are ready for export."
                : "Partial results can be exported.");
    }

    private void logNewEvents(TestState state) {
        for (int i = lastEventCount; i < state.eventHistory().size(); i++) {
            switch (state.eventHistory().get(i)) {
                case edu.ankara.audiometer.domain.model.ProtocolEvent.RetestValidation event -> log.add(
                        event.frequency().value() + " Hz retest validated for " + event.ear() + " at " + event.threshold().value() + " dB HL"
                );
                case edu.ankara.audiometer.domain.model.ProtocolEvent.RetestWarning event -> log.add(
                        "Warning: " + event.frequency().value() + " Hz retest for " + event.ear()
                                + " differed from original threshold (original " + event.originalThreshold().value()
                                + " dB HL, retest " + event.retestThreshold().value() + " dB HL). Final audiogram remains duplicate-free."
                );
                default -> {
                }
            }
        }
        lastEventCount = state.eventHistory().size();
    }

    private static int expectedThresholdTotal(TestState state) {
        return state.config().ears().size() * state.config().frequencyPlan().uniqueThresholdFrequencies().size();
    }

    private static String recommendation(TestState state) {
        return switch (state.phase()) {
            case COMPLETED -> "Export results or reset the test.";
            case PAUSED -> "Press Resume test to continue";
            case STOPPED -> "Reset required";
            default -> "Present tone / wait for RESPONSE";
        };
    }
}
