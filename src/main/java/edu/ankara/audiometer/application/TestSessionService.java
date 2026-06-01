package edu.ankara.audiometer.application;

import edu.ankara.audiometer.domain.algorithm.HughsonWestlakeEngine;
import edu.ankara.audiometer.domain.algorithm.TestStateReducer;
import edu.ankara.audiometer.domain.config.AudiometryConfig;
import edu.ankara.audiometer.domain.fp.Result;
import edu.ankara.audiometer.domain.model.EarTestMode;
import edu.ankara.audiometer.domain.model.ProtocolEvent;
import edu.ankara.audiometer.domain.model.TestPhase;
import edu.ankara.audiometer.domain.model.TestState;
import edu.ankara.audiometer.domain.model.ThresholdCriterion;
import edu.ankara.audiometer.infrastructure.serial.SerialCommand;
import edu.ankara.audiometer.infrastructure.serial.SerialPortGateway;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class TestSessionService {
    private TestState state;
    private final SerialPortGateway gateway;
    private final List<Consumer<TestState>> listeners = new ArrayList<>();

    public TestSessionService(AudiometryConfig config, SerialPortGateway gateway) {
        this.state = TestState.initial(config);
        this.gateway = gateway;
    }

    public TestState state() {
        return state;
    }

    public void onState(Consumer<TestState> listener) {
        listeners.add(listener);
    }

    private void set(TestState next) {
        state = next;
        listeners.forEach(listener -> listener.accept(next));
    }

    public void reset() {
        set(TestState.initial(state.config()));
    }

    public void setEarMode(EarTestMode mode) {
        set(TestState.initial(state.config().withEars(mode.ears())));
    }

    public void pause() {
        if (state.phase() != TestPhase.STOPPED && state.phase() != TestPhase.COMPLETED) {
            set(state.transition(
                    state.currentEar(),
                    state.currentFrequencyIndex(),
                    state.currentIntensity(),
                    TestPhase.PAUSED,
                    state.direction()
            ));
        }
    }

    public void resume() {
        if (state.phase() == TestPhase.PAUSED) {
            set(state.transition(
                    state.currentEar(),
                    state.currentFrequencyIndex(),
                    state.currentIntensity(),
                    TestPhase.READY,
                    state.direction()
            ));
        }
    }

    public void stop() {
        set(state.transition(
                state.currentEar(),
                state.currentFrequencyIndex(),
                state.currentIntensity(),
                TestPhase.STOPPED,
                state.direction()
        ));
    }

    public void setCriterion(ThresholdCriterion criterion) {
        set(TestState.initial(state.config().withCriterion(criterion)));
    }

    public Result<String, String> presentTone() {
        if (state.phase() == TestPhase.PAUSED || state.phase() == TestPhase.STOPPED || state.phase() == TestPhase.COMPLETED) {
            return Result.err("Cannot present tone while session is " + state.phase());
        }

        TestState next = HughsonWestlakeEngine.presentTone(state);
        var command = SerialCommand.tone(next.presentations().getLast(), next.config());
        if (!command.isOk()) {
            return Result.err("Tone command validation failed");
        }

        String value = command.orElse(null).value();
        var sendResult = gateway.send(value);
        set(next);
        return sendResult.isOk() ? Result.ok(value) : Result.err("Serial send failed for command: " + value);
    }

    public void response() {
        if (canAcceptResponse()) {
            set(TestStateReducer.updateState(state, new ProtocolEvent.Response()));
        }
    }

    public void noResponse() {
        if (canAcceptResponse()) {
            set(TestStateReducer.updateState(state, new ProtocolEvent.NoResponse()));
        }
    }

    public Result<AutoSimulationStep, String> autoSimulateStep(SimulationService simulation) {
        var command = presentTone();
        if (!command.isOk()) {
            return Result.err(command.orElse("Auto simulation step rejected"));
        }

        var decision = simulation.decide(state);
        if (decision.heard()) {
            response();
        } else {
            noResponse();
        }
        return Result.ok(new AutoSimulationStep(command.orElse(""), decision.eventName(), decision.threshold().value()));
    }

    public void applyRawMessages(List<String> rawMessages) {
        if (state.phase() != TestPhase.STOPPED) {
            set(new SerialMessageProcessor(state.config().serialProtocol()).process(state, rawMessages));
        }
    }

    public SerialPortGateway gateway() {
        return gateway;
    }

    private boolean canAcceptResponse() {
        return state.phase() != TestPhase.PAUSED
                && state.phase() != TestPhase.STOPPED
                && state.phase() != TestPhase.COMPLETED;
    }

    public record AutoSimulationStep(String command, String responseEvent, int simulatedThresholdDbHL) {
    }
}
