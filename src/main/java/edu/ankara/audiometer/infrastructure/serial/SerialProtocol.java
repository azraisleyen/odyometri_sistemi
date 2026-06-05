package edu.ankara.audiometer.infrastructure.serial;

import edu.ankara.audiometer.domain.config.SerialProtocolConfig;
import edu.ankara.audiometer.domain.model.ProtocolEvent;

import java.util.Locale;
import java.util.Optional;

public final class SerialProtocol {
    private SerialProtocol() {
    }

    public static String sanitizeSerialInput(String rawMessage) {
        return rawMessage == null ? "" : rawMessage.strip().replace("\r", "").replace("\n", "");
    }

    public static Optional<ProtocolEvent> parseResponseMessage(String rawMessage) {
        return parseResponseMessage(rawMessage, SerialProtocolConfig.defaults());
    }

    public static Optional<ProtocolEvent> parseResponseMessage(String rawMessage, SerialProtocolConfig config) {
        String message = sanitizeSerialInput(rawMessage);
        if (config.caseInsensitiveInput()) {
            message = message.toUpperCase(Locale.ROOT);
        }
        if (message.isBlank()) {
            return Optional.empty();
        }
        if (message.equals("RESPONSE")) {
            return Optional.of(new ProtocolEvent.Response());
        }
        if (message.equals("NO_RESPONSE") || message.equals("NORESPONSE") || message.equals("NOT_HEARD")) {
            return Optional.of(new ProtocolEvent.NoResponse());
        }
        if (message.equals("READY")) {
            return Optional.of(new ProtocolEvent.Ready());
        }
        if (message.equals("ACK")) {
            return Optional.of(new ProtocolEvent.Ack());
        }
        if (message.equals("BUTTON_DOWN")) {
            return Optional.of(new ProtocolEvent.ButtonDown());
        }
        if (message.equals("BUTTON_UP")) {
            return Optional.of(new ProtocolEvent.ButtonUp());
        }
        if (message.startsWith("ERROR:")) {
            return Optional.of(new ProtocolEvent.ErrorMessage(message.substring(6)));
        }
        return Optional.empty();
    }
}
