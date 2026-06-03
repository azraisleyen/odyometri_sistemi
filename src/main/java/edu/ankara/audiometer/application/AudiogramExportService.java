package edu.ankara.audiometer.application;

import edu.ankara.audiometer.domain.fp.Result;
import edu.ankara.audiometer.domain.model.TestState;
import edu.ankara.audiometer.infrastructure.export.CsvAudiogramExporter;
import edu.ankara.audiometer.infrastructure.export.JsonSessionExporter;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class AudiogramExportService {
    private final CsvAudiogramExporter csv = new CsvAudiogramExporter();
    private final JsonSessionExporter json = new JsonSessionExporter();

    public Result<Path, String> exportCsv(TestState state, Path path) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, csv.export(state), StandardCharsets.UTF_8);
            return Result.ok(path);
        } catch (Exception exception) {
            return Result.err(exception.getMessage());
        }
    }

    public Result<Path, String> exportJson(TestState state, Path path) {
        var data = json.export(state);
        if (!data.isOk()) {
            return Result.err("JSON export failed");
        }

        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, data.orElse("{}"), StandardCharsets.UTF_8);
            return Result.ok(path);
        } catch (Exception exception) {
            return Result.err(exception.getMessage());
        }
    }

    public String csvString(TestState state) {
        return csv.export(state);
    }
}
