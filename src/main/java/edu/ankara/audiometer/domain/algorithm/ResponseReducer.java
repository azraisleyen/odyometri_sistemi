package edu.ankara.audiometer.domain.algorithm;

import edu.ankara.audiometer.domain.model.PatientResponse;
import edu.ankara.audiometer.domain.model.ProtocolEvent;
import edu.ankara.audiometer.domain.model.TestState;

import java.util.List;

public final class ResponseReducer {
    private ResponseReducer() {
    }

    public static TestState reduceResponses(TestState state, List<PatientResponse> responses) {
        return responses.stream()
                .map(response -> (ProtocolEvent) (response.heard()
                        ? new ProtocolEvent.Response()
                        : new ProtocolEvent.NoResponse()))
                .reduce(state, TestStateReducer::updateState, (left, right) -> right);
    }
}
