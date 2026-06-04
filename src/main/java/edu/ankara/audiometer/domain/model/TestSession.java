package edu.ankara.audiometer.domain.model;

import java.time.Instant;

public record TestSession(
        String sessionId,
        Instant createdAt,
        String mode,
        String softwareVersion
) {
    public static TestSession simulation() {
        return new TestSession("SIM-SESSION", Instant.EPOCH, "simulation", "1.0.0");
    }
}
