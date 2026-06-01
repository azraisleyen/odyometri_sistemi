package edu.ankara.audiometer.domain.model;

import edu.ankara.audiometer.domain.config.AudiometryConfig;

import java.util.ArrayList;
import java.util.List;

public record TestState(
        TestSession session,
        AudiometryConfig config,
        Ear currentEar,
        int currentFrequencyIndex,
        FrequencyHz currentFrequency,
        IntensityDbHL currentIntensity,
        TestPhase phase,
        PresentationDirection direction,
        List<TonePresentation> presentations,
        List<ProtocolEvent> eventHistory,
        Audiogram audiogram,
        List<String> errors
) {
    public TestState {
        presentations = List.copyOf(presentations);
        eventHistory = List.copyOf(eventHistory);
        errors = List.copyOf(errors);
    }

    public static TestState initial(AudiometryConfig config) {
        var order = config.frequencyPlan().activeOrder();
        if (order.isEmpty()) {
            throw new IllegalArgumentException("frequency order must not be empty");
        }
        return new TestState(
                TestSession.simulation(),
                config,
                config.ears().getFirst(),
                0,
                order.getFirst(),
                config.algorithm().startIntensity(),
                TestPhase.READY,
                PresentationDirection.INITIAL,
                List.of(),
                List.of(),
                Audiogram.empty(),
                List.of()
        );
    }

    public int nextOrder() {
        return presentations.size() + 1;
    }

    public TestState withPresentation(TonePresentation presentation) {
        var next = new ArrayList<>(presentations);
        next.add(presentation);
        return copy(currentEar, currentFrequencyIndex, currentFrequency, currentIntensity, phase, direction, next, eventHistory, audiogram, errors);
    }

    public TestState withEvent(ProtocolEvent event) {
        var next = new ArrayList<>(eventHistory);
        next.add(event);
        return copy(currentEar, currentFrequencyIndex, currentFrequency, currentIntensity, phase, direction, presentations, next, audiogram, errors);
    }

    public TestState transition(Ear ear, int frequencyIndex, IntensityDbHL intensity, TestPhase nextPhase, PresentationDirection nextDirection) {
        var order = config.frequencyPlan().activeOrder();
        if (frequencyIndex < 0 || frequencyIndex >= order.size()) {
            throw new IllegalArgumentException("frequency index outside active order: " + frequencyIndex);
        }
        return copy(ear, frequencyIndex, order.get(frequencyIndex), intensity, nextPhase, nextDirection, presentations, eventHistory, audiogram, errors);
    }

    public TestState transition(Ear ear, FrequencyHz frequency, IntensityDbHL intensity, TestPhase nextPhase, PresentationDirection nextDirection) {
        int index = config.frequencyPlan().activeOrder().indexOf(frequency);
        return transition(ear, Math.max(index, 0), intensity, nextPhase, nextDirection);
    }

    public TestState withAudiogram(Audiogram nextAudiogram) {
        return copy(currentEar, currentFrequencyIndex, currentFrequency, currentIntensity, phase, direction, presentations, eventHistory, nextAudiogram, errors);
    }

    public TestState withError(String error) {
        var next = new ArrayList<>(errors);
        next.add(error);
        return copy(currentEar, currentFrequencyIndex, currentFrequency, currentIntensity, TestPhase.ERROR, direction, presentations, eventHistory, audiogram, next);
    }

    public TestState withPresentations(List<TonePresentation> nextPresentations) {
        return copy(currentEar, currentFrequencyIndex, currentFrequency, currentIntensity, phase, direction, nextPresentations, eventHistory, audiogram, errors);
    }

    private TestState copy(
            Ear ear,
            int frequencyIndex,
            FrequencyHz frequency,
            IntensityDbHL intensity,
            TestPhase nextPhase,
            PresentationDirection nextDirection,
            List<TonePresentation> nextPresentations,
            List<ProtocolEvent> nextEvents,
            Audiogram nextAudiogram,
            List<String> nextErrors
    ) {
        return new TestState(
                session,
                config,
                ear,
                frequencyIndex,
                frequency,
                intensity,
                nextPhase,
                nextDirection,
                nextPresentations,
                nextEvents,
                nextAudiogram,
                nextErrors
        );
    }
}
