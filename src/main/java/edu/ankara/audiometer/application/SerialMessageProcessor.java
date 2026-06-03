package edu.ankara.audiometer.application;

import edu.ankara.audiometer.domain.algorithm.TestStateReducer;
import edu.ankara.audiometer.domain.config.SerialProtocolConfig;
import edu.ankara.audiometer.domain.model.TestState;
import edu.ankara.audiometer.infrastructure.serial.SerialProtocol;

import java.util.List;
import java.util.Optional;

public final class SerialMessageProcessor {
    private final SerialProtocolConfig config;

    public SerialMessageProcessor(SerialProtocolConfig config) {
        this.config = config;
    }

    public TestState process(TestState initial, List<String> rawMessages) {
        return rawMessages.stream()
                .map(SerialProtocol::sanitizeSerialInput)
                .filter(message -> !message.isBlank())
                .map(message -> SerialProtocol.parseResponseMessage(message, config))
                .flatMap(Optional::stream)
                .reduce(initial, TestStateReducer::updateState, (left, right) -> right);
    }
}
