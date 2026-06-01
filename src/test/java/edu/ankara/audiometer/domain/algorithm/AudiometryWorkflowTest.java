package edu.ankara.audiometer.domain.algorithm;

import edu.ankara.audiometer.application.SimulationService;
import edu.ankara.audiometer.application.TestSessionService;
import edu.ankara.audiometer.domain.config.AudiometryConfig;
import edu.ankara.audiometer.domain.model.Ear;
import edu.ankara.audiometer.domain.model.EarTestMode;
import edu.ankara.audiometer.domain.model.IntensityDbHL;
import edu.ankara.audiometer.domain.model.TestPhase;
import edu.ankara.audiometer.infrastructure.serial.FakeSerialGateway;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AudiometryWorkflowTest {
    @Test
    void earModesResetSessionToExpectedInitialEar() {
        var service = service();
        service.setEarMode(EarTestMode.RIGHT_ONLY);
        assertEquals(Ear.RIGHT, service.state().currentEar());
        service.setEarMode(EarTestMode.LEFT_ONLY);
        assertEquals(Ear.LEFT, service.state().currentEar());
        service.setEarMode(EarTestMode.BOTH);
        assertEquals(Ear.RIGHT, service.state().currentEar());
    }

    @Test
    void leftOnlyEmitsOnlyLeftCommands() {
        var gateway = new FakeSerialGateway();
        var service = new TestSessionService(AudiometryConfig.defaults(), gateway);
        service.setEarMode(EarTestMode.LEFT_ONLY);
        runUntilComplete(service, 30, 300);
        assertFalse(gateway.sentCommands().isEmpty());
        assertTrue(gateway.sentCommands().stream().allMatch(command -> command.contains("EAR=LEFT")));
        assertTrue(service.state().audiogram().points().stream().allMatch(point -> point.ear() == Ear.LEFT));
    }

    @Test
    void rightOnlyEmitsOnlyRightCommands() {
        var gateway = new FakeSerialGateway();
        var service = new TestSessionService(AudiometryConfig.defaults(), gateway);
        service.setEarMode(EarTestMode.RIGHT_ONLY);
        runUntilComplete(service, 25, 300);
        assertFalse(gateway.sentCommands().isEmpty());
        assertTrue(gateway.sentCommands().stream().allMatch(command -> command.contains("EAR=RIGHT")));
        assertTrue(service.state().audiogram().points().stream().allMatch(point -> point.ear() == Ear.RIGHT));
    }

    @Test
    void bothModeReachesLeftAndCompletesWithoutFrequencyLoop() {
        var service = service();
        boolean reachedLeft = false;
        for (int i = 0; i < 700 && service.state().phase() != TestPhase.COMPLETED; i++) {
            oneSimulatedStep(service);
            reachedLeft = reachedLeft || service.state().currentEar() == Ear.LEFT;
        }
        assertTrue(reachedLeft, "Both-ear mode should advance from RIGHT to LEFT");
        assertEquals(TestPhase.COMPLETED, service.state().phase());
        Set<Integer> rightFrequencies = frequenciesFor(service, Ear.RIGHT);
        Set<Integer> leftFrequencies = frequenciesFor(service, Ear.LEFT);
        assertTrue(rightFrequencies.containsAll(Set.of(250, 500, 1000, 2000, 4000, 8000)));
        assertTrue(leftFrequencies.containsAll(Set.of(250, 500, 1000, 2000, 4000, 8000)));
    }

    @Test
    void duplicateOneThousandRetestDoesNotCreateDuplicateRowsWhenRetestDisabled() {
        var service = service();
        runUntilComplete(service, 25, 700);
        long rightOneThousandRows = service.state().audiogram().points().stream()
                .filter(point -> point.ear() == Ear.RIGHT)
                .filter(point -> point.frequency().value() == 1000)
                .count();
        assertEquals(1, rightOneThousandRows);
    }

    @Test
    void simulationThresholdDefaultsAreEarSpecific() {
        var simulation = new SimulationService();
        assertEquals(25, simulation.thresholdFor(Ear.RIGHT, new edu.ankara.audiometer.domain.model.FrequencyHz(1000)).value());
        assertEquals(30, simulation.thresholdFor(Ear.LEFT, new edu.ankara.audiometer.domain.model.FrequencyHz(1000)).value());
    }

    @Test
    void pauseResumeStopAndResetHaveClearStateTransitions() {
        var service = service();
        service.presentTone();
        service.pause();
        assertEquals(TestPhase.PAUSED, service.state().phase());
        var rejected = service.presentTone();
        assertFalse(rejected.isOk());
        service.resume();
        assertEquals(TestPhase.READY, service.state().phase());
        assertTrue(service.presentTone().isOk());
        service.stop();
        assertEquals(TestPhase.STOPPED, service.state().phase());
        assertFalse(service.presentTone().isOk());
        service.reset();
        assertEquals(TestPhase.READY, service.state().phase());
    }

    private static TestSessionService service() {
        return new TestSessionService(AudiometryConfig.defaults(), new FakeSerialGateway());
    }

    private static void runUntilComplete(TestSessionService service, int threshold, int maxSteps) {
        for (int i = 0; i < maxSteps && service.state().phase() != TestPhase.COMPLETED; i++) {
            service.presentTone();
            if (service.state().currentIntensity().value() >= threshold) {
                service.response();
            } else {
                service.noResponse();
            }
        }
        assertEquals(TestPhase.COMPLETED, service.state().phase());
    }

    private static void oneSimulatedStep(TestSessionService service) {
        service.presentTone();
        int threshold = service.state().currentEar() == Ear.RIGHT ? 25 : 30;
        if (service.state().currentIntensity().value() >= threshold) {
            service.response();
        } else {
            service.noResponse();
        }
    }

    private static Set<Integer> frequenciesFor(TestSessionService service, Ear ear) {
        return service.state().audiogram().points().stream()
                .filter(point -> point.ear() == ear)
                .map(point -> point.frequency().value())
                .collect(Collectors.toSet());
    }
}
