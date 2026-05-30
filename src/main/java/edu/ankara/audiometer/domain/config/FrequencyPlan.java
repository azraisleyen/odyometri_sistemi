package edu.ankara.audiometer.domain.config;
import edu.ankara.audiometer.domain.model.FrequencyHz;import java.util.*;
public record FrequencyPlan(List<FrequencyHz> requiredFrequencies, List<FrequencyHz> clinicalOrder, List<FrequencyHz> optionalInterOctaves, boolean enableInterOctaves, int interOctaveDeltaDb) {
 public FrequencyPlan { requiredFrequencies=List.copyOf(requiredFrequencies); clinicalOrder=List.copyOf(clinicalOrder); optionalInterOctaves=List.copyOf(optionalInterOctaves); }
 public static FrequencyPlan defaults(){ return new FrequencyPlan(vals(250,500,1000,2000,4000,8000), vals(1000,2000,4000,8000,1000,500,250), vals(750,1500,3000,6000), false, 20); }
 private static List<FrequencyHz> vals(int... v){ return Arrays.stream(v).mapToObj(FrequencyHz::new).toList(); }
 public List<FrequencyHz> activeOrder(){ return enableInterOctaves? java.util.stream.Stream.concat(clinicalOrder.stream(), optionalInterOctaves.stream()).distinct().toList() : clinicalOrder; }
}
