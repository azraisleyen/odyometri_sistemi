package edu.ankara.audiometer.domain.model;
public sealed interface ProtocolEvent permits ProtocolEvent.Response, ProtocolEvent.NoResponse, ProtocolEvent.Ready, ProtocolEvent.Ack, ProtocolEvent.ButtonDown, ProtocolEvent.ButtonUp, ProtocolEvent.ErrorMessage {
 record Response() implements ProtocolEvent {} record NoResponse() implements ProtocolEvent {} record Ready() implements ProtocolEvent {} record Ack() implements ProtocolEvent {} record ButtonDown() implements ProtocolEvent {} record ButtonUp() implements ProtocolEvent {} record ErrorMessage(String message) implements ProtocolEvent {}
}
