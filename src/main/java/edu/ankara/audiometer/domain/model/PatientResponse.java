package edu.ankara.audiometer.domain.model;
public enum PatientResponse { HEARD, NOT_HEARD, TIMEOUT; public boolean heard(){return this==HEARD;} }
