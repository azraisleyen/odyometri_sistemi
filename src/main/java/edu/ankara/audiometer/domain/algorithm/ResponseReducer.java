package edu.ankara.audiometer.domain.algorithm;
import edu.ankara.audiometer.domain.model.*;import java.util.*;
public final class ResponseReducer { private ResponseReducer(){} public static TestState reduceResponses(TestState state,List<PatientResponse> responses){ return responses.stream().map(r -> (ProtocolEvent)(r.heard()?new ProtocolEvent.Response():new ProtocolEvent.NoResponse())).reduce(state, TestStateReducer::updateState, (a,b)->b); } }
