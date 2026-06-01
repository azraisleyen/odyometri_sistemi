package edu.ankara.audiometer.infrastructure.config;

import edu.ankara.audiometer.domain.config.AudiometryConfig;
import edu.ankara.audiometer.domain.model.TestState;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonAudiometryConfigLoaderTest {
    @Test
    void readsValidConfigJson() throws Exception {
        var path = Files.createTempFile("audiometry-config", ".json");
        Files.writeString(path, """
                {
                  "frequencyRangeHz": { "min": 250, "max": 8000 },
                  "defaultFrequenciesHz": [250, 500, 1000, 2000, 4000, 8000],
                  "clinicalOrderHz": [1000, 2000, 4000, 8000, 1000, 500, 250],
                  "optionalInterOctavesHz": [750, 1500, 3000, 6000],
                  "startIntensityDbHL": 55,
                  "intensityRangeDbHL": { "min": -10, "max": 120 },
                  "heardDecreaseDb": 10,
                  "notHeardIncreaseDb": 5,
                  "responseTimeoutMs": 2500,
                  "defaultCriterion": "TWO_OUT_OF_THREE_ASCENDING",
                  "serial": { "baudRate": 115200 }
                }
                """);

        var result = new JsonAudiometryConfigLoader().load(path);

        assertTrue(result.loadedFromFile());
        assertEquals(55, result.config().algorithm().startIntensity().value());
        assertEquals(115200, result.config().serialProtocol().baudRate());
        assertEquals(55, TestState.initial(result.config()).currentIntensity().value());
    }

    @Test
    void missingOrInvalidConfigFallsBackToDefaults() throws Exception {
        var loader = new JsonAudiometryConfigLoader();
        var missing = loader.load(Files.createTempDirectory("missing-config").resolve("missing.json"));
        assertFalse(missing.loadedFromFile());
        assertEquals(AudiometryConfig.defaults().algorithm().startIntensity(), missing.config().algorithm().startIntensity());

        var invalidPath = Files.createTempFile("invalid-audiometry-config", ".json");
        Files.writeString(invalidPath, "not-json");
        var invalid = loader.load(invalidPath);
        assertFalse(invalid.loadedFromFile());
        assertEquals(AudiometryConfig.defaults().algorithm().startIntensity(), invalid.config().algorithm().startIntensity());
    }
}
