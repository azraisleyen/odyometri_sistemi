package edu.ankara.audiometer.domain.model;
import java.util.*;import java.util.stream.*;
public record Audiogram(List<AudiogramPoint> points) {
 public Audiogram { points = List.copyOf(points); }
 public static Audiogram empty(){return new Audiogram(List.of());}
 public Audiogram add(AudiogramPoint p, boolean allowRetest){ boolean dup=points.stream().anyMatch(x->x.ear()==p.ear()&&x.frequency().equals(p.frequency())); if(dup&&!allowRetest) return this; var n=new ArrayList<>(points); n.add(p); return new Audiogram(n); }
 public Optional<AudiogramPoint> find(Ear e, FrequencyHz f){return points.stream().filter(p->p.ear()==e&&p.frequency().equals(f)).findFirst();}
 public Map<Ear,List<AudiogramPoint>> byEar(){return points.stream().collect(Collectors.groupingBy(AudiogramPoint::ear));}
}
