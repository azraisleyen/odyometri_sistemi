package edu.ankara.audiometer.domain.algorithm;

import edu.ankara.audiometer.domain.model.AudiogramPoint;
import edu.ankara.audiometer.domain.model.PatientResponse;
import edu.ankara.audiometer.domain.model.PresentationDirection;
import edu.ankara.audiometer.domain.model.TestPhase;
import edu.ankara.audiometer.domain.model.TestState;

import java.util.ArrayList;

public final class HughsonWestlakeEngine {
    private HughsonWestlakeEngine() {
    }

    public static TestState presentTone(TestState state) {
        TestPhase phase = state.direction() == PresentationDirection.INITIAL ? TestPhase.DESCENDING_SEARCH : state.phase();
        return state.withPresentation(AudiometryFunctions.createTonePresentation(state))
                .transition(state.currentEar(), state.currentFrequency(), state.currentIntensity(), phase, state.direction());
    }

    public static TestState applyResponseToLastPresentation(TestState state, PatientResponse response) {
        TestState withPresentation = state.presentations().isEmpty() ? presentTone(state) : state;
        var presentations = new ArrayList<>(withPresentation.presentations());
        var last = presentations.removeLast().withResponse(response);
        presentations.add(last);
        TestState withResponse = withPresentation.withPresentations(presentations);

        var decision = ThresholdDetector.detectThreshold(withResponse.presentations(), withResponse.config().algorithm());
        if (decision.reached()) {
            AudiogramPoint point = AudiometryFunctions.createAudiogramPoint(withResponse, decision.threshold().orElseThrow());
            TestState withPoint = withResponse.withAudiogram(withResponse.audiogram().add(point, withResponse.config().allowRetest()));
            return advanceFrequencyOrEar(withPoint);
        }

        var nextIntensity = AudiometryFunctions.nextIntensityAfterResponse(
                withResponse.currentIntensity(),
                response,
                withResponse.config().algorithm(),
                withResponse.config()
        );
        var direction = response.heard() ? PresentationDirection.DESCENDING : PresentationDirection.ASCENDING;
        var phase = response.heard() ? TestPhase.DESCENDING_SEARCH : TestPhase.ASCENDING_SEARCH;
        return withResponse.transition(withResponse.currentEar(), withResponse.currentFrequency(), nextIntensity, phase, direction);
    }

    private static TestState advanceFrequencyOrEar(TestState state) {
        return AudiometryFunctions.nextFrequencyIndex(state)
                .map(nextIndex -> state.transitionToFrequencyIndex(
                        state.currentEar(),
                        nextIndex,
                        state.config().algorithm().startIntensity(),
                        TestPhase.READY,
                        PresentationDirection.INITIAL
                ))
                .orElseGet(() -> advanceEarOrComplete(state));
    }

    private static TestState advanceEarOrComplete(TestState state) {
        return AudiometryFunctions.nextEar(state.currentEar(), state)
                .map(nextEar -> state.transitionToFrequencyIndex(
                        nextEar,
                        0,
                        state.config().algorithm().startIntensity(),
                        TestPhase.READY,
                        PresentationDirection.INITIAL
                ))
                .orElseGet(() -> state.transition(
                        state.currentEar(),
                        state.currentFrequency(),
                        state.currentIntensity(),
                        TestPhase.COMPLETED,
                        state.direction()
                ));
    }
}
