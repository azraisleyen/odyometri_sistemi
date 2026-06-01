package edu.ankara.audiometer.application;

import edu.ankara.audiometer.domain.config.AudiometryConfig;
import edu.ankara.audiometer.infrastructure.serial.SerialPortGateway;

public final class AudiometryUseCase {
    private final TestSessionService sessions;
    private final SimulationService simulation = new SimulationService();
    private final AudiogramExportService exports = new AudiogramExportService();

    public AudiometryUseCase(AudiometryConfig config, SerialPortGateway gateway) {
        sessions = new TestSessionService(config, gateway);
    }

    public TestSessionService sessions() {
        return sessions;
    }

    public SimulationService simulation() {
        return simulation;
    }

    public AudiogramExportService exports() {
        return exports;
    }
}
