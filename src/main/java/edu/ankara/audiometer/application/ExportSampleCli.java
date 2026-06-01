package edu.ankara.audiometer.application;

import edu.ankara.audiometer.infrastructure.config.JsonAudiometryConfigLoader;
import edu.ankara.audiometer.infrastructure.serial.FakeSerialGateway;

import java.nio.file.Path;

public final class ExportSampleCli {
    private ExportSampleCli() {
    }

    public static void main(String[] args) {
        var config = new JsonAudiometryConfigLoader().loadDefault().config();
        var useCase = new AudiometryUseCase(config, new FakeSerialGateway());
        useCase.sessions().autoSimulateStep(useCase.simulation());
        useCase.exports().exportCsv(useCase.sessions().state(), Path.of("exports/sample.csv"));
        useCase.exports().exportJson(useCase.sessions().state(), Path.of("exports/sample.json"));
        System.out.println("Sample exports written to exports/");
    }
}
