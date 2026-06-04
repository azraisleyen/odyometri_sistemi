package edu.ankara.audiometer.domain.algorithm;

import edu.ankara.audiometer.domain.model.IntensityDbHL;
import edu.ankara.audiometer.domain.model.PatientResponse;
import edu.ankara.audiometer.domain.model.PresentationDirection;
import edu.ankara.audiometer.domain.model.ProtocolEvent;
import edu.ankara.audiometer.domain.model.TestPhase;
import edu.ankara.audiometer.domain.model.TestState;
import edu.ankara.audiometer.domain.model.TonePresentation;

import java.util.ArrayList;

public final class HughsonWestlakeEngine {
    private HughsonWestlakeEngine() {
    }

    public static TestState presentTone(TestState state) {
        TestPhase nextPhase = state.direction() == PresentationDirection.INITIAL
                ? TestPhase.DESCENDING_SEARCH
                : state.phase();
        return state.withPresentation(AudiometryFunctions.createTonePresentation(state))
                .transition(state.currentEar(), state.currentFrequencyIndex(), state.currentIntensity(), nextPhase, state.direction());
    }

    public static TestState applyResponseToLastPresentation(TestState state, PatientResponse response) {
        TestState withPresentation = state.presentations().isEmpty() ? presentTone(state) : state;
        TestState withResponse = replaceLastPresentationResponse(withPresentation, response);

        var decision = ThresholdDetector.detectThreshold(withResponse.presentations(), withResponse.config().algorithm());
        if (decision.reached()) {
            var point = AudiometryFunctions.createAudiogramPoint(withResponse, decision.threshold().orElseThrow());
            var withPoint = withResponse.withAudiogram(withResponse.audiogram().add(point, false));
            return advanceFrequencyOrEar(withPoint);
        }

        IntensityDbHL nextIntensity = AudiometryFunctions.nextIntensityAfterResponse(
                withResponse.currentIntensity(),
                response,
                withResponse.config().algorithm(),
                withResponse.config()
        );
        var nextDirection = response.heard() ? PresentationDirection.DESCENDING : PresentationDirection.ASCENDING;
        var nextPhase = response.heard() ? TestPhase.DESCENDING_SEARCH : TestPhase.ASCENDING_SEARCH;
        return withResponse.transition(
                withResponse.currentEar(),
                withResponse.currentFrequencyIndex(),
                nextIntensity,
                nextPhase,
                nextDirection
        );
    }

    private static TestState replaceLastPresentationResponse(TestState state, PatientResponse response) {
        var list = new ArrayList<>(state.presentations());
        TonePresentation last = list.removeLast().withResponse(response);
        list.add(last);
        return state.withPresentations(list);
    }

    private static TestState advanceFrequencyOrEar(TestState state) {
        var nextFrequencyIndex = AudiometryFunctions.nextFrequencyIndex(state);
        if (nextFrequencyIndex.isPresent()) {
            return state.transition(
                    state.currentEar(),
                    nextFrequencyIndex.get(),
                    state.config().algorithm().startIntensity(),
                    TestPhase.READY,
                    PresentationDirection.INITIAL
            );
        }

        var nextEar = AudiometryFunctions.nextEar(state.currentEar(), state);
        if (nextEar.isPresent()) {
            return state.transition(
                    nextEar.get(),
                    0,
                    state.config().algorithm().startIntensity(),
                    TestPhase.READY,
                    PresentationDirection.INITIAL
            );
        }

        return state.transition(
                state.currentEar(),
                state.currentFrequencyIndex(),
                state.currentIntensity(),
                TestPhase.COMPLETED,
                state.direction()
        );
    }
}
