package edu.ankara.audiometer.infrastructure.serial;
import edu.ankara.audiometer.domain.config.SerialProtocolConfig;import edu.ankara.audiometer.domain.model.*;import java.util.*;
public final class SerialProtocol { private SerialProtocol(){}
 public static String sanitizeSerialInput(String rawMessage){ return rawMessage==null?"":rawMessage.strip().replace("\r","").replace("\n",""); }
 public static Optional<ProtocolEvent> parseResponseMessage(String rawMessage){ return parseResponseMessage(rawMessage, SerialProtocolConfig.defaults()); }
 public static Optional<ProtocolEvent> parseResponseMessage(String rawMessage, SerialProtocolConfig config){ String s=sanitizeSerialInput(rawMessage); if(config.caseInsensitiveInput()) s=s.toUpperCase(Locale.ROOT); if(s.isBlank()) return Optional.empty(); if(s.equals("RESPONSE")) return Optional.of(new ProtocolEvent.Response()); if(s.equals("READY")) return Optional.of(new ProtocolEvent.Ready()); if(s.equals("ACK")) return Optional.of(new ProtocolEvent.Ack()); if(s.equals("BUTTON_DOWN")) return Optional.of(new ProtocolEvent.ButtonDown()); if(s.equals("BUTTON_UP")) return Optional.of(new ProtocolEvent.ButtonUp()); if(s.startsWith("ERROR:")) return Optional.of(new ProtocolEvent.ErrorMessage(s.substring(6))); return Optional.empty(); }
}
