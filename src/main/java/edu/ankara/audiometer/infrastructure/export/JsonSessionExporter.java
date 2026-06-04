package edu.ankara.audiometer.infrastructure.export;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ankara.audiometer.domain.fp.Result;
import edu.ankara.audiometer.domain.model.TestState;

import java.util.LinkedHashMap;
import java.util.Map;

public final class JsonSessionExporter {
    private final ObjectMapper objectMapper;

    public JsonSessionExporter() {
        this(new ObjectMapper());
    }

    public JsonSessionExporter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Result<String, String> export(TestState state) {
        try {
            return Result.ok(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(sessionDocument(state)));
        } catch (JsonProcessingException exception) {
            return Result.err(exception.getMessage());
        }
    }

    private static Map<String, Object> sessionDocument(TestState state) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("software_version", state.session().softwareVersion());
        root.put("mode", state.session().mode());
        root.put("session_id", state.session().sessionId());
        root.put("configuration", configuration(state));
        root.put("thresholds", state.audiogram().points().stream().map(point -> {
            Map<String, Object> threshold = new LinkedHashMap<>();
            threshold.put("ear", point.ear().name());
            threshold.put("frequency_hz", point.frequency().value());
            threshold.put("threshold_db_hl", point.thresholdDbHL().value());
            threshold.put("criterion", point.criterion().name());
            threshold.put("presentation_count", point.presentationCount());
            threshold.put("completed_at_order", point.completedAtOrder());
            threshold.put("notes", point.notes());
            return threshold;
        }).toList());
        root.put("presentation_history", state.presentations().stream().map(presentation -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("ear", presentation.ear().name());
            item.put("frequency_hz", presentation.frequency().value());
            item.put("db_hl", presentation.intensity().value());
            item.put("order", presentation.orderIndex());
            item.put("direction", presentation.direction().name());
            item.put("response", presentation.response().map(Enum::name).orElse("PENDING"));
            return item;
        }).toList());
        return root;
    }

    private static Map<String, Object> configuration(TestState state) {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("min_frequency_hz", state.config().minFrequency().value());
        config.put("max_frequency_hz", state.config().maxFrequency().value());
        config.put("required_frequencies_hz", state.config().frequencyPlan().requiredFrequencies().stream().map(f -> f.value()).toList());
        config.put("clinical_order_hz", state.config().frequencyPlan().clinicalOrder().stream().map(f -> f.value()).toList());
        config.put("active_threshold_order_hz", state.config().frequencyPlan().activeThresholdOrder().stream().map(f -> f.value()).toList());
        config.put("min_db_hl", state.config().minIntensity().value());
        config.put("max_db_hl", state.config().maxIntensity().value());
        config.put("start_db_hl", state.config().algorithm().startIntensity().value());
        config.put("heard_decrease_db", state.config().algorithm().heardDecreaseDb());
        config.put("not_heard_increase_db", state.config().algorithm().notHeardIncreaseDb());
        config.put("criterion", state.config().algorithm().criterion().name());
        config.put("serial_baud_rate", state.config().serialProtocol().baudRate());
        config.put("command_terminator", state.config().serialProtocol().commandTerminator());
        config.put("ears", state.config().ears().stream().map(Enum::name).toList());
        return config;
    }
}
