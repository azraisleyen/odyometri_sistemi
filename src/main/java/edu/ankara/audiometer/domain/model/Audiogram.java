package edu.ankara.audiometer.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public record Audiogram(List<AudiogramPoint> points) {
    public Audiogram {
        points = List.copyOf(points);
    }

    public static Audiogram empty() {
        return new Audiogram(List.of());
    }

    public Audiogram add(AudiogramPoint point, boolean allowRetest) {
        boolean duplicate = points.stream()
                .anyMatch(existing -> existing.ear() == point.ear() && existing.frequency().equals(point.frequency()));
        if (duplicate && !allowRetest) {
            return this;
        }

        var next = new ArrayList<>(points);
        next.add(point);
        return new Audiogram(next);
    }

    public Optional<AudiogramPoint> find(Ear ear, FrequencyHz frequency) {
        return points.stream()
                .filter(point -> point.ear() == ear && point.frequency().equals(frequency))
                .findFirst();
    }

    public Map<Ear, List<AudiogramPoint>> byEar() {
        return points.stream().collect(Collectors.groupingBy(AudiogramPoint::ear));
    }
}
