package edu.ankara.audiometer.domain.algorithm;

import edu.ankara.audiometer.domain.model.PatientResponse;
import edu.ankara.audiometer.domain.model.ProtocolEvent;
import edu.ankara.audiometer.domain.model.TestState;

public final class TestStateReducer {
    private TestStateReducer() {
    }

    public static TestState updateState(TestState state, ProtocolEvent event) {
        TestState withEvent = state.withEvent(event);
        return switch (event) {
            case ProtocolEvent.Response ignored ->
                    HughsonWestlakeEngine.applyResponseToLastPresentation(withEvent, PatientResponse.HEARD);
            case ProtocolEvent.NoResponse ignored ->
                    HughsonWestlakeEngine.applyResponseToLastPresentation(withEvent, PatientResponse.NOT_HEARD);
            case ProtocolEvent.ErrorMessage error -> withEvent.withError(error.message());
            default -> withEvent;
        };
    }
}
