package edu.ankara.audiometer.domain.model;
import java.util.Optional;
public record ThresholdDecision(boolean reached, Optional<IntensityDbHL> threshold, ThresholdCriterion criterion, String explanation) { public static ThresholdDecision notReached(ThresholdCriterion c,String e){return new ThresholdDecision(false,Optional.empty(),c,e);} public static ThresholdDecision reached(IntensityDbHL i,ThresholdCriterion c,String e){return new ThresholdDecision(true,Optional.of(i),c,e);} }
