package edu.ankara.audiometer.infrastructure.config;

import edu.ankara.audiometer.domain.config.AudiometryConfig;
import edu.ankara.audiometer.domain.config.FrequencyPlan;
import edu.ankara.audiometer.domain.config.HughsonWestlakeConfig;
import edu.ankara.audiometer.domain.config.SerialProtocolConfig;
import edu.ankara.audiometer.domain.model.Ear;
import edu.ankara.audiometer.domain.model.FrequencyHz;
import edu.ankara.audiometer.domain.model.IntensityDbHL;
import edu.ankara.audiometer.domain.model.ThresholdCriterion;
import edu.ankara.audiometer.infrastructure.json.JsonSupport;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class JsonAudiometryConfigLoader {
    public static final Path DEFAULT_PATH = Path.of("config", "audiometry-config.json");

    public ConfigLoadResult loadDefault() {
        return load(DEFAULT_PATH);
    }

    public ConfigLoadResult load(Path path) {
        if (!Files.isRegularFile(path)) {
            return ConfigLoadResult.fallback("Config file not found: " + path);
        }
        try {
            var parsed = JsonSupport.parse(Files.readString(path));
            if (!parsed.isOk()) {
                return ConfigLoadResult.fallback("Invalid config file " + path);
            }
            return ConfigLoadResult.loaded(fromObject(JsonSupport.asObject(parsed.orElse(Map.of()))));
        } catch (Exception exception) {
            return ConfigLoadResult.fallback("Invalid config file " + path + ": " + exception.getMessage());
        }
    }

    public AudiometryConfig fromObject(Map<String, Object> root) {
        var defaults = AudiometryConfig.defaults();
        var frequencyRange = JsonSupport.asObject(root.get("frequencyRangeHz"));
        var intensityRange = JsonSupport.asObject(root.get("intensityRangeDbHL"));
        var serial = JsonSupport.asObject(root.get("serial"));

        var frequencyPlan = new FrequencyPlan(
                frequencies(root.get("defaultFrequenciesHz"), defaults.frequencyPlan().requiredFrequencies()),
                frequencies(root.get("clinicalOrderHz"), defaults.frequencyPlan().clinicalOrder()),
                frequencies(root.get("optionalInterOctavesHz"), defaults.frequencyPlan().optionalInterOctaves()),
                JsonSupport.boolValue(root, "enableInterOctaves", defaults.frequencyPlan().enableInterOctaves()),
                JsonSupport.intValue(root, "interOctaveDeltaDb", defaults.frequencyPlan().interOctaveDeltaDb())
        );

        var algorithm = new HughsonWestlakeConfig(
                new IntensityDbHL(JsonSupport.intValue(root, "startIntensityDbHL", defaults.algorithm().startIntensity().value())),
                JsonSupport.intValue(root, "heardDecreaseDb", defaults.algorithm().heardDecreaseDb()),
                JsonSupport.intValue(root, "notHeardIncreaseDb", defaults.algorithm().notHeardIncreaseDb()),
                JsonSupport.intValue(root, "toneDurationMs", defaults.algorithm().toneDurationMs()),
                JsonSupport.intValue(root, "responseTimeoutMs", defaults.algorithm().responseTimeoutMs()),
                criterion(JsonSupport.stringValue(root, "defaultCriterion", defaults.algorithm().criterion().name()))
        );

        var serialProtocol = new SerialProtocolConfig(
                JsonSupport.intValue(serial, "baudRate", defaults.serialProtocol().baudRate()),
                JsonSupport.boolValue(serial, "caseInsensitiveInput", defaults.serialProtocol().caseInsensitiveInput()),
                JsonSupport.stringValue(serial, "commandTerminator", defaults.serialProtocol().commandTerminator())
        );

        return new AudiometryConfig(
                new FrequencyHz(JsonSupport.intValue(frequencyRange, "min", defaults.minFrequency().value())),
                new FrequencyHz(JsonSupport.intValue(frequencyRange, "max", defaults.maxFrequency().value())),
                new IntensityDbHL(JsonSupport.intValue(intensityRange, "min", defaults.minIntensity().value())),
                new IntensityDbHL(JsonSupport.intValue(intensityRange, "max", defaults.maxIntensity().value())),
                frequencyPlan,
                algorithm,
                serialProtocol,
                List.of(Ear.RIGHT, Ear.LEFT),
                JsonSupport.boolValue(root, "manualFrequencyOverride", defaults.manualFrequencyOverride()),
                JsonSupport.boolValue(root, "allowRetest", defaults.allowRetest())
        );
    }

    private static List<FrequencyHz> frequencies(Object raw, List<FrequencyHz> fallback) {
        var values = JsonSupport.asArray(raw).stream()
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .map(Number::intValue)
                .map(FrequencyHz::new)
                .toList();
        return values.isEmpty() ? fallback : values;
    }

    private static ThresholdCriterion criterion(String raw) {
        try {
            return ThresholdCriterion.valueOf(raw);
        } catch (IllegalArgumentException exception) {
            return AudiometryConfig.defaults().algorithm().criterion();
        }
    }

    public record ConfigLoadResult(AudiometryConfig config, boolean loadedFromFile, String warning) {
        static ConfigLoadResult loaded(AudiometryConfig config) {
            return new ConfigLoadResult(config, true, "");
        }

        static ConfigLoadResult fallback(String warning) {
            return new ConfigLoadResult(AudiometryConfig.defaults(), false, warning);
        }
    }
}
