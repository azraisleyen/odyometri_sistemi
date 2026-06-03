package edu.ankara.audiometer.infrastructure.serial;

import edu.ankara.audiometer.domain.config.AudiometryConfig;
import edu.ankara.audiometer.domain.fp.Result;
import edu.ankara.audiometer.domain.model.TonePresentation;
import edu.ankara.audiometer.domain.validation.AudiometryValidators;

public record SerialCommand(String value) {
    public static Result<SerialCommand, String> tone(TonePresentation presentation, AudiometryConfig config) {
        var frequency = AudiometryValidators.validateFrequency(presentation.frequency(), config);
        if (!frequency.valid()) {
            return Result.err(String.join(",", frequency.errors()));
        }

        var intensity = AudiometryValidators.validateIntensity(presentation.intensity(), config);
        if (!intensity.valid()) {
            return Result.err(String.join(",", intensity.errors()));
        }

        return Result.ok(new SerialCommand("TONE;EAR=%s;FREQ=%d;DB=%d;DURATION_MS=%d".formatted(
                presentation.ear(),
                presentation.frequency().value(),
                presentation.intensity().value(),
                config.algorithm().toneDurationMs()
        )));
    }
}
