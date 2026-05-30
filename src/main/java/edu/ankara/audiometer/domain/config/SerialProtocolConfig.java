package edu.ankara.audiometer.domain.config;
public record SerialProtocolConfig(int baudRate, boolean caseInsensitiveInput, String commandTerminator) { public static SerialProtocolConfig defaults(){return new SerialProtocolConfig(9600,true,"\n");} }
