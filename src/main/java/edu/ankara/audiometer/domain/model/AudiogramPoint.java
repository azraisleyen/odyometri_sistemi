package edu.ankara.audiometer.domain.model;
public record AudiogramPoint(Ear ear, FrequencyHz frequency, IntensityDbHL thresholdDbHL, ThresholdCriterion criterion, int presentationCount, int completedAtOrder, String notes) {}
