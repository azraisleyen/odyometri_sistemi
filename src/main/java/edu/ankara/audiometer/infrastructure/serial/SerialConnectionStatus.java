package edu.ankara.audiometer.infrastructure.serial;
public record SerialConnectionStatus(boolean connected, String portName, String message) { public static SerialConnectionStatus disconnected(){return new SerialConnectionStatus(false,"","Disconnected");} }
