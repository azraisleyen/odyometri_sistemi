package edu.ankara.audiometer.application;

import edu.ankara.audiometer.domain.config.AudiometryConfig;
import edu.ankara.audiometer.domain.config.SerialProtocolConfig;
import edu.ankara.audiometer.domain.model.Ear;
import edu.ankara.audiometer.domain.model.EarTestMode;
import edu.ankara.audiometer.domain.model.ProtocolEvent;
import edu.ankara.audiometer.domain.model.TestPhase;
import edu.ankara.audiometer.infrastructure.serial.FakeSerialGateway;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AudiometryWorkflowTest {
    @Test
    void leftOnlyInitialStateStartsWithLeftAndEmitsOnlyLeftCommands() {
        var gateway = new FakeSerialGateway();
        var useCase = useCase(gateway);
        useCase.sessions().setEarMode(EarTestMode.LEFT_ONLY);

        assertEquals(Ear.LEFT, useCase.sessions().state().currentEar());
        assertEquals(1000, useCase.sessions().state().currentFrequency().value());
        assertEquals(40, useCase.sessions().state().currentIntensity().value());

        runToCompletion(useCase);
        assertTrue(gateway.sentCommands().stream().allMatch(command -> command.contains("EAR=LEFT")));
        assertFalse(gateway.sentCommands().stream().anyMatch(command -> command.contains("EAR=RIGHT")));
        assertEquals(TestPhase.COMPLETED, useCase.sessions().state().phase());
    }

    @Test
    void rightOnlyInitialStateStartsWithRightAndEmitsOnlyRightCommands() {
        var gateway = new FakeSerialGateway();
        var useCase = useCase(gateway);
        useCase.sessions().setEarMode(EarTestMode.RIGHT_ONLY);

        assertEquals(Ear.RIGHT, useCase.sessions().state().currentEar());
        runToCompletion(useCase);

        assertTrue(gateway.sentCommands().stream().allMatch(command -> command.contains("EAR=RIGHT")));
        assertFalse(gateway.sentCommands().stream().anyMatch(command -> command.contains("EAR=LEFT")));
        assertEquals(TestPhase.COMPLETED, useCase.sessions().state().phase());
    }

    @Test
    void bothModeStartsRightReachesLowFrequenciesSwitchesLeftAndCompletes() {
        var useCase = useCase(new FakeSerialGateway());
        assertEquals(Ear.RIGHT, useCase.sessions().state().currentEar());

        runToCompletion(useCase);

        var state = useCase.sessions().state();
        assertEquals(TestPhase.COMPLETED, state.phase());
        assertTrue(state.audiogram().points().stream().anyMatch(point -> point.ear() == Ear.LEFT));
        assertFrequenciesReached(state.audiogram().points().stream()
                .filter(point -> point.ear() == Ear.RIGHT)
                .map(point -> point.frequency().value())
                .collect(Collectors.toSet()));
        assertFrequenciesReached(state.audiogram().points().stream()
                .filter(point -> point.ear() == Ear.LEFT)
                .map(point -> point.frequency().value())
                .collect(Collectors.toSet()));
    }

    @Test
    void duplicate1000ClinicalRetestIsVisitedButDoesNotCreateDuplicateThresholdRows() {
        var useCase = useCase(new FakeSerialGateway());
        runToCompletion(useCase);

        var state = useCase.sessions().state();
        assertEquals(2, presentationVisits(state, Ear.RIGHT, 1000));
        assertEquals(2, presentationVisits(state, Ear.LEFT, 1000));
        assertEquals(1, thresholdRows(state, Ear.RIGHT, 1000));
        assertEquals(1, thresholdRows(state, Ear.LEFT, 1000));
        assertEquals(12, state.audiogram().points().size());
        assertTrue(state.eventHistory().stream().anyMatch(event -> event instanceof ProtocolEvent.RetestValidation));
    }

    @Test
    void clinicalRuntimeOrderIncludesTrue1000HzRetestAndUniqueThresholdOrderDoesNot() {
        var plan = AudiometryConfig.defaults().frequencyPlan();
        var runtimeOrder = plan.activeOrder().stream().map(frequency -> frequency.value()).toList();
        var uniqueOrder = plan.uniqueThresholdFrequencies().stream().map(frequency -> frequency.value()).toList();

        assertEquals(List.of(1000, 2000, 4000, 8000, 1000, 500, 250), runtimeOrder);
        assertEquals(List.of(1000, 2000, 4000, 8000, 500, 250), uniqueOrder);
        assertEquals(uniqueOrder.size(), Set.copyOf(uniqueOrder).size());
        assertTrue(plan.isRetestOccurrence(4));
    }

    @Test
    void configuredSerialTerminatorIsUsedWithoutChangingLoggedCommandBody() {
        var gateway = new FakeSerialGateway();
        var defaults = AudiometryConfig.defaults();
        var config = new AudiometryConfig(
                defaults.minFrequency(),
                defaults.maxFrequency(),
                defaults.minIntensity(),
                defaults.maxIntensity(),
                defaults.frequencyPlan(),
                defaults.algorithm(),
                new SerialProtocolConfig(9600, true, "\r\n"),
                defaults.ears(),
                defaults.manualFrequencyOverride(),
                defaults.allowRetest()
        );
        var useCase = new AudiometryUseCase(config, gateway);

        var result = useCase.sessions().presentTone();

        assertTrue(result.isOk());
        assertEquals("TONE;EAR=RIGHT;FREQ=1000;DB=40;DURATION_MS=1000", result.orElse(""));
        assertEquals("TONE;EAR=RIGHT;FREQ=1000;DB=40;DURATION_MS=1000", gateway.sentCommands().getFirst());
        assertEquals("TONE;EAR=RIGHT;FREQ=1000;DB=40;DURATION_MS=1000\r\n", gateway.sentPayloads().getFirst());
    }

    @Test
    void defaultSerialTerminatorIsLineFeed() {
        assertEquals("\n", AudiometryConfig.defaults().serialProtocol().commandTerminator());
    }


    @Test
    void activeThresholdOrderIsDuplicateFreeEducationalRuntimeOrder() {
        var order = AudiometryConfig.defaults().frequencyPlan().activeThresholdOrder().stream()
                .map(frequency -> frequency.value())
                .toList();

        assertEquals(List.of(1000, 2000, 4000, 8000, 500, 250), order);
        assertEquals(order.size(), Set.copyOf(order).size());
    }

    @Test
    void configuredSerialTerminatorIsUsedWithoutChangingLoggedCommandBody() {
        var gateway = new FakeSerialGateway();
        var defaults = AudiometryConfig.defaults();
        var config = new AudiometryConfig(
                defaults.minFrequency(),
                defaults.maxFrequency(),
                defaults.minIntensity(),
                defaults.maxIntensity(),
                defaults.frequencyPlan(),
                defaults.algorithm(),
                new SerialProtocolConfig(9600, true, "\r\n"),
                defaults.ears(),
                defaults.manualFrequencyOverride(),
                defaults.allowRetest()
        );
        var useCase = new AudiometryUseCase(config, gateway);

        var result = useCase.sessions().presentTone();

        assertTrue(result.isOk());
        assertEquals("TONE;EAR=RIGHT;FREQ=1000;DB=40;DURATION_MS=1000", result.orElse(""));
        assertEquals("TONE;EAR=RIGHT;FREQ=1000;DB=40;DURATION_MS=1000", gateway.sentCommands().getFirst());
        assertEquals("TONE;EAR=RIGHT;FREQ=1000;DB=40;DURATION_MS=1000\r\n", gateway.sentPayloads().getFirst());
    }

    @Test
    void defaultSerialTerminatorIsLineFeed() {
        assertEquals("\n", AudiometryConfig.defaults().serialProtocol().commandTerminator());
    }

    @Test
    void pauseResumeAndStopRequireReset() {
        var useCase = useCase(new FakeSerialGateway());
        useCase.sessions().presentTone();
        useCase.sessions().pause();
        assertFalse(useCase.sessions().presentTone().isOk());

        useCase.sessions().resume();
        assertTrue(useCase.sessions().presentTone().isOk());

        useCase.sessions().stop();
        assertFalse(useCase.sessions().presentTone().isOk());
        useCase.sessions().reset();
        assertTrue(useCase.sessions().presentTone().isOk());
    }

    @Test
    void rightOnlySimulationVisits1000RetestAndCompletesWithUniqueFinalRows() {
        var useCase = useCase(new FakeSerialGateway());
        useCase.sessions().setEarMode(EarTestMode.RIGHT_ONLY);
        runToCompletion(useCase);

        var state = useCase.sessions().state();
        assertEquals(TestPhase.COMPLETED, state.phase());
        assertEquals(2, presentationVisits(state, Ear.RIGHT, 1000));
        assertEquals(1, thresholdRows(state, Ear.RIGHT, 1000));
        assertFrequenciesReached(state.audiogram().points().stream()
                .filter(point -> point.ear() == Ear.RIGHT)
                .map(point -> point.frequency().value())
                .collect(Collectors.toSet()));
    }

    @Test
    void leftOnlySimulationVisits1000RetestAndCompletesWithUniqueFinalRows() {
        var useCase = useCase(new FakeSerialGateway());
        useCase.sessions().setEarMode(EarTestMode.LEFT_ONLY);
        runToCompletion(useCase);

        var state = useCase.sessions().state();
        assertEquals(TestPhase.COMPLETED, state.phase());
        assertEquals(2, presentationVisits(state, Ear.LEFT, 1000));
        assertEquals(1, thresholdRows(state, Ear.LEFT, 1000));
        assertFrequenciesReached(state.audiogram().points().stream()
                .filter(point -> point.ear() == Ear.LEFT)
                .map(point -> point.frequency().value())
                .collect(Collectors.toSet()));
    }

    @Test
    void simulationThresholdDefaultsAreEarSpecific() {
        var simulation = new SimulationService();
        var config = AudiometryConfig.defaults();
        assertEquals(25, simulation.thresholdFor(Ear.RIGHT, config.frequencyPlan().activeOrder().getFirst()).value());
        assertEquals(30, simulation.thresholdFor(Ear.LEFT, config.frequencyPlan().activeOrder().getFirst()).value());
    }

    private static AudiometryUseCase useCase(FakeSerialGateway gateway) {
        return new AudiometryUseCase(AudiometryConfig.defaults(), gateway);
    }

    private static void runToCompletion(AudiometryUseCase useCase) {
        int steps = 0;
        while (useCase.sessions().state().phase() != TestPhase.COMPLETED && steps < 300) {
            useCase.sessions().autoSimulateStep(useCase.simulation());
            steps++;
        }
        assertTrue(steps < 300, "workflow should complete without an infinite loop");
    }

    private static long presentationVisits(edu.ankara.audiometer.domain.model.TestState state, Ear ear, int frequency) {
        return state.presentations().stream()
                .filter(presentation -> presentation.ear() == ear)
                .filter(presentation -> presentation.frequency().value() == frequency)
                .map(presentation -> presentation.frequencyOrderIndex())
                .distinct()
                .count();
    }

    private static long thresholdRows(edu.ankara.audiometer.domain.model.TestState state, Ear ear, int frequency) {
        return state.audiogram().points().stream()
                .filter(point -> point.ear() == ear)
                .filter(point -> point.frequency().value() == frequency)
                .count();
    }

    private static void assertFrequenciesReached(Set<Integer> frequencies) {
        assertTrue(frequencies.contains(1000));
        assertTrue(frequencies.contains(2000));
        assertTrue(frequencies.contains(4000));
        assertTrue(frequencies.contains(8000));
        assertTrue(frequencies.contains(500));
        assertTrue(frequencies.contains(250));
    }
}
