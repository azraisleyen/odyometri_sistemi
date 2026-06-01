package edu.ankara.audiometer.application;

import edu.ankara.audiometer.domain.config.AudiometryConfig;
import edu.ankara.audiometer.infrastructure.serial.SerialPortGateway;

public final class AudiometryUseCase {
    private final TestSessionService sessions;
    private final SimulationService simulation;
    private final AudiogramExportService exports;

    public AudiometryUseCase(AudiometryConfig config, SerialPortGateway gateway) {
        this.sessions = new TestSessionService(config, gateway);
        this.simulation = new SimulationService();
        this.exports = new AudiogramExportService();
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
