package edu.ankara.audiometer.infrastructure;

import edu.ankara.audiometer.application.TestSessionService;
import edu.ankara.audiometer.domain.model.Ear;
import edu.ankara.audiometer.domain.model.EarTestMode;
import edu.ankara.audiometer.infrastructure.config.AudiometryConfigLoader;
import edu.ankara.audiometer.infrastructure.serial.FakeSerialGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigLoaderTest {
    @TempDir
    Path tempDir;

    @Test
    void readsValidConfigAndAppliesStartIntensity() throws Exception {
        Path config = tempDir.resolve("audiometry-config.json");
        Files.writeString(config, """
                {
                  "frequencyRangeHz": { "min": 250, "max": 8000 },
                  "defaultFrequenciesHz": [250, 500, 1000, 2000, 4000, 8000],
                  "clinicalOrderHz": [1000, 2000, 4000, 8000, 1000, 500, 250],
                  "optionalInterOctavesHz": [750, 1500, 3000, 6000],
                  "startIntensityDbHL": 35,
                  "intensityRangeDbHL": { "min": -10, "max": 120 },
                  "heardDecreaseDb": 10,
                  "notHeardIncreaseDb": 5,
                  "responseTimeoutMs": 2500,
                  "defaultCriterion": "THREE_OUT_OF_FIVE_ASCENDING",
                  "serial": { "baudRate": 115200 }
                }
                """);

        var loaded = AudiometryConfigLoader.load(config);
        assertTrue(loaded.warnings().isEmpty());
        assertEquals(35, loaded.config().algorithm().startIntensity().value());
        assertEquals(115200, loaded.serial().baudRate());
        assertEquals(35, new TestSessionService(loaded.config(), new FakeSerialGateway()).state().currentIntensity().value());
    }

    @Test
    void missingConfigFallsBackSafely() {
        var loaded = AudiometryConfigLoader.load(tempDir.resolve("missing.json"));
        assertFalse(loaded.warnings().isEmpty());
        assertEquals(40, loaded.config().algorithm().startIntensity().value());
    }

    @Test
    void earModeStillWorksAfterConfigLoading() throws Exception {
        Path config = tempDir.resolve("audiometry-config.json");
        Files.writeString(config, "{\"startIntensityDbHL\": 45}");
        var service = new TestSessionService(AudiometryConfigLoader.load(config).config(), new FakeSerialGateway());
        service.setEarMode(EarTestMode.LEFT_ONLY);
        assertEquals(Ear.LEFT, service.state().currentEar());
        assertEquals(45, service.state().currentIntensity().value());
    }
}
