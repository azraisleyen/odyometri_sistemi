package edu.ankara.audiometer.domain.config;
import edu.ankara.audiometer.domain.model.*;import java.util.*;
public record AudiometryConfig(FrequencyHz minFrequency, FrequencyHz maxFrequency, IntensityDbHL minIntensity, IntensityDbHL maxIntensity, FrequencyPlan frequencyPlan, HughsonWestlakeConfig algorithm, List<Ear> ears, boolean manualFrequencyOverride, boolean allowRetest) {
 public AudiometryConfig { ears=List.copyOf(ears); }
 public static AudiometryConfig defaults(){ return new AudiometryConfig(new FrequencyHz(250), new FrequencyHz(8000), new IntensityDbHL(-10), new IntensityDbHL(120), FrequencyPlan.defaults(), HughsonWestlakeConfig.defaults(), List.of(Ear.RIGHT,Ear.LEFT), false, false); }
 public AudiometryConfig withCriterion(ThresholdCriterion c){ return new AudiometryConfig(minFrequency,maxFrequency,minIntensity,maxIntensity,frequencyPlan,algorithm.withCriterion(c),ears,manualFrequencyOverride,allowRetest); }
 public AudiometryConfig withEars(List<Ear> e){ return new AudiometryConfig(minFrequency,maxFrequency,minIntensity,maxIntensity,frequencyPlan,algorithm,e,manualFrequencyOverride,allowRetest); }
}
