package edu.ankara.audiometer.domain.validation;

import edu.ankara.audiometer.domain.config.AudiometryConfig;
import edu.ankara.audiometer.domain.model.ValidationResult;

import java.util.ArrayList;
import java.util.List;

public final class ClinicalRuleValidator {
    private ClinicalRuleValidator() {
    }

    public static ValidationResult validateAlgorithmConfig(AudiometryConfig config) {
        List<String> errors = new ArrayList<>();
        if (config.algorithm().heardDecreaseDb() != 10) {
            errors.add("Heard response must decrease by 10 dB by default");
        }
        if (config.algorithm().notHeardIncreaseDb() != 5) {
            errors.add("No response must increase by 5 dB by default");
        }
        if (config.minFrequency().value() != 250 || config.maxFrequency().value() != 8000) {
            errors.add("Course frequency range must be 250-8000 Hz");
        }
        return errors.isEmpty() ? ValidationResult.ok() : new ValidationResult(false, errors);
    }
}
