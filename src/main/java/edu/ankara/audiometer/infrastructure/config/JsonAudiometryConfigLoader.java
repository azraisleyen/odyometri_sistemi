package edu.ankara.audiometer.infrastructure.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ankara.audiometer.domain.config.AudiometryConfig;
import edu.ankara.audiometer.domain.config.FrequencyPlan;
import edu.ankara.audiometer.domain.config.HughsonWestlakeConfig;
import edu.ankara.audiometer.domain.config.SerialProtocolConfig;
import edu.ankara.audiometer.domain.model.Ear;
import edu.ankara.audiometer.domain.model.FrequencyHz;
import edu.ankara.audiometer.domain.model.IntensityDbHL;
import edu.ankara.audiometer.domain.model.ThresholdCriterion;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class JsonAudiometryConfigLoader {
    public static final Path DEFAULT_PATH = Path.of("config", "audiometry-config.json");

    private final ObjectMapper objectMapper;

    public JsonAudiometryConfigLoader() {
        this(new ObjectMapper());
    }

    public JsonAudiometryConfigLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ConfigLoadResult loadDefault() {
        return load(DEFAULT_PATH);
    }

    public ConfigLoadResult load(Path path) {
        if (!Files.isRegularFile(path)) {
            return ConfigLoadResult.fallback("Config file not found: " + path);
        }
        try {
            JsonNode root = objectMapper.readTree(path.toFile());
            return ConfigLoadResult.loaded(fromJson(root));
        } catch (Exception exception) {
            return ConfigLoadResult.fallback("Invalid config file " + path + ": " + exception.getMessage());
        }
    }

    public AudiometryConfig fromJson(JsonNode root) {
        var defaults = AudiometryConfig.defaults();
        JsonNode frequencyRange = node(root, "frequencyRangeHz");
        JsonNode intensityRange = node(root, "intensityRangeDbHL");
        JsonNode serial = node(root, "serial");

        var frequencyPlan = new FrequencyPlan(
                frequencies(node(root, "defaultFrequenciesHz"), defaults.frequencyPlan().requiredFrequencies()),
                frequencies(node(root, "clinicalOrderHz"), defaults.frequencyPlan().clinicalOrder()),
                frequencies(node(root, "optionalInterOctavesHz"), defaults.frequencyPlan().optionalInterOctaves()),
                boolValue(root, "enableInterOctaves", defaults.frequencyPlan().enableInterOctaves()),
                intValue(root, "interOctaveDeltaDb", defaults.frequencyPlan().interOctaveDeltaDb())
        );

        var algorithm = new HughsonWestlakeConfig(
                new IntensityDbHL(intValue(root, "startIntensityDbHL", defaults.algorithm().startIntensity().value())),
                intValue(root, "heardDecreaseDb", defaults.algorithm().heardDecreaseDb()),
                intValue(root, "notHeardIncreaseDb", defaults.algorithm().notHeardIncreaseDb()),
                intValue(root, "toneDurationMs", defaults.algorithm().toneDurationMs()),
                intValue(root, "responseTimeoutMs", defaults.algorithm().responseTimeoutMs()),
                criterion(textValue(root, "defaultCriterion", defaults.algorithm().criterion().name()))
        );

        var serialProtocol = new SerialProtocolConfig(
                intValue(serial, "baudRate", defaults.serialProtocol().baudRate()),
                boolValue(serial, "caseInsensitiveInput", defaults.serialProtocol().caseInsensitiveInput()),
                textValue(serial, "commandTerminator", textValue(root, "commandTerminator", defaults.serialProtocol().commandTerminator()))
        );

        return new AudiometryConfig(
                new FrequencyHz(intValue(frequencyRange, "min", defaults.minFrequency().value())),
                new FrequencyHz(intValue(frequencyRange, "max", defaults.maxFrequency().value())),
                new IntensityDbHL(intValue(intensityRange, "min", defaults.minIntensity().value())),
                new IntensityDbHL(intValue(intensityRange, "max", defaults.maxIntensity().value())),
                frequencyPlan,
                algorithm,
                serialProtocol,
                List.of(Ear.RIGHT, Ear.LEFT),
                boolValue(root, "manualFrequencyOverride", defaults.manualFrequencyOverride()),
                boolValue(root, "allowRetest", defaults.allowRetest())
        );
    }

    private static JsonNode node(JsonNode parent, String fieldName) {
        return parent == null ? null : parent.get(fieldName);
    }

    private static int intValue(JsonNode node, String fieldName, int fallback) {
        JsonNode value = node(node, fieldName);
        return value != null && value.isNumber() ? value.intValue() : fallback;
    }

    private static boolean boolValue(JsonNode node, String fieldName, boolean fallback) {
        JsonNode value = node(node, fieldName);
        return value != null && value.isBoolean() ? value.booleanValue() : fallback;
    }

    private static String textValue(JsonNode node, String fieldName, String fallback) {
        JsonNode value = node(node, fieldName);
        return value != null && value.isTextual() ? value.textValue() : fallback;
    }

    private static List<FrequencyHz> frequencies(JsonNode raw, List<FrequencyHz> fallback) {
        if (raw == null || !raw.isArray()) {
            return fallback;
        }

        List<FrequencyHz> values = new ArrayList<>();
        raw.elements().forEachRemaining(value -> {
            if (value.isNumber()) {
                values.add(new FrequencyHz(value.intValue()));
            }
        });
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
