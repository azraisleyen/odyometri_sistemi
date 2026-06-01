package edu.ankara.audiometer.application;

import edu.ankara.audiometer.infrastructure.config.AudiometryConfigLoader;
import edu.ankara.audiometer.infrastructure.serial.FakeSerialGateway;

import java.nio.file.Path;

public final class ExportSampleCli {
    public static void main(String[] args) {
        var loadedConfig = AudiometryConfigLoader.loadDefault();
        var useCase = new AudiometryUseCase(loadedConfig.config(), new FakeSerialGateway());
        useCase.sessions().presentTone();
        useCase.sessions().response();
        useCase.exports().exportCsv(useCase.sessions().state(), Path.of("exports/sample.csv"));
        useCase.exports().exportJson(useCase.sessions().state(), Path.of("exports/sample.json"));
        loadedConfig.warnings().forEach(warning -> System.out.println("Config warning: " + warning));
        System.out.println("Sample exports written to exports/");
    }
}
