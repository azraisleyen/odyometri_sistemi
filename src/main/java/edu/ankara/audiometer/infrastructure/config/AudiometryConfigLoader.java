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

public final class AudiometryConfigLoader {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private AudiometryConfigLoader() {
    }

    public static LoadedAudiometryConfig loadDefault() {
        return load(Path.of("config", "audiometry-config.json"));
    }

    public static LoadedAudiometryConfig load(Path path) {
        if (!Files.isRegularFile(path)) {
            return LoadedAudiometryConfig.defaults("Config file not found: " + path + "; using Java defaults.");
        }
        try {
            JsonNode root = MAPPER.readTree(path.toFile());
            return new LoadedAudiometryConfig(parseConfig(root), parseSerial(root), List.of());
        } catch (Exception ex) {
            return LoadedAudiometryConfig.defaults("Config file could not be parsed: " + ex.getMessage() + "; using Java defaults.");
        }
    }

    private static AudiometryConfig parseConfig(JsonNode root) {
        AudiometryConfig defaults = AudiometryConfig.defaults();
        JsonNode frequencyRange = root.path("frequencyRangeHz");
        JsonNode intensityRange = root.path("intensityRangeDbHL");

        FrequencyPlan plan = new FrequencyPlan(
                frequencies(root.path("defaultFrequenciesHz"), defaults.frequencyPlan().requiredFrequencies()),
                frequencies(root.path("clinicalOrderHz"), defaults.frequencyPlan().clinicalOrder()),
                frequencies(root.path("optionalInterOctavesHz"), defaults.frequencyPlan().optionalInterOctaves()),
                root.path("enableInterOctaves").asBoolean(defaults.frequencyPlan().enableInterOctaves()),
                root.path("interOctaveDeltaDb").asInt(defaults.frequencyPlan().interOctaveDeltaDb())
        );

        HughsonWestlakeConfig algorithm = new HughsonWestlakeConfig(
                new IntensityDbHL(root.path("startIntensityDbHL").asInt(defaults.algorithm().startIntensity().value())),
                root.path("heardDecreaseDb").asInt(defaults.algorithm().heardDecreaseDb()),
                root.path("notHeardIncreaseDb").asInt(defaults.algorithm().notHeardIncreaseDb()),
                root.path("toneDurationMs").asInt(defaults.algorithm().toneDurationMs()),
                root.path("responseTimeoutMs").asInt(defaults.algorithm().responseTimeoutMs()),
                criterion(root.path("defaultCriterion").asText(defaults.algorithm().criterion().name()))
        );

        return new AudiometryConfig(
                new FrequencyHz(frequencyRange.path("min").asInt(defaults.minFrequency().value())),
                new FrequencyHz(frequencyRange.path("max").asInt(defaults.maxFrequency().value())),
                new IntensityDbHL(intensityRange.path("min").asInt(defaults.minIntensity().value())),
                new IntensityDbHL(intensityRange.path("max").asInt(defaults.maxIntensity().value())),
                plan,
                algorithm,
                defaultEars(root, defaults.ears()),
                root.path("manualFrequencyOverride").asBoolean(defaults.manualFrequencyOverride()),
                root.path("allowRetest").asBoolean(defaults.allowRetest())
        );
    }

    private static SerialProtocolConfig parseSerial(JsonNode root) {
        SerialProtocolConfig defaults = SerialProtocolConfig.defaults();
        JsonNode serial = root.path("serial");
        return new SerialProtocolConfig(
                serial.path("baudRate").asInt(defaults.baudRate()),
                serial.path("caseInsensitiveInput").asBoolean(defaults.caseInsensitiveInput()),
                serial.path("commandTerminator").asText(defaults.commandTerminator())
        );
    }

    private static List<FrequencyHz> frequencies(JsonNode node, List<FrequencyHz> fallback) {
        if (!node.isArray()) {
            return fallback;
        }
        List<FrequencyHz> values = new ArrayList<>();
        node.forEach(value -> values.add(new FrequencyHz(value.asInt())));
        return values.isEmpty() ? fallback : values;
    }

    private static ThresholdCriterion criterion(String value) {
        try {
            return ThresholdCriterion.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return AudiometryConfig.defaults().algorithm().criterion();
        }
    }

    private static List<Ear> defaultEars(JsonNode root, List<Ear> fallback) {
        JsonNode ears = root.path("ears");
        if (!ears.isArray()) {
            return fallback;
        }
        List<Ear> parsed = new ArrayList<>();
        ears.forEach(value -> parsed.add(Ear.valueOf(value.asText())));
        return parsed.isEmpty() ? fallback : parsed;
    }

    public record LoadedAudiometryConfig(AudiometryConfig config, SerialProtocolConfig serial, List<String> warnings) {
        public LoadedAudiometryConfig {
            warnings = List.copyOf(warnings);
        }

        public static LoadedAudiometryConfig defaults(String warning) {
            return new LoadedAudiometryConfig(AudiometryConfig.defaults(), SerialProtocolConfig.defaults(), List.of(warning));
        }
    }
}
