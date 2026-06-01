package edu.ankara.audiometer.infrastructure.export;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import edu.ankara.audiometer.domain.fp.Result;
import edu.ankara.audiometer.domain.model.TestState;

import java.util.Map;

public final class JsonSessionExporter {
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public Result<String, String> export(TestState state) {
        try {
            return Result.ok(mapper.writeValueAsString(toDocument(state)));
        } catch (JsonProcessingException ex) {
            return Result.err(ex.getMessage());
        }
    }

    private static Map<String, Object> toDocument(TestState state) {
        return Map.of(
                "software_version", state.session().softwareVersion(),
                "mode", state.session().mode(),
                "session_id", state.session().sessionId(),
                "configuration", Map.of(
                        "min_frequency_hz", state.config().minFrequency().value(),
                        "max_frequency_hz", state.config().maxFrequency().value(),
                        "min_db_hl", state.config().minIntensity().value(),
                        "max_db_hl", state.config().maxIntensity().value(),
                        "criterion", state.config().algorithm().criterion().name(),
                        "clinical_order_hz", state.config().frequencyPlan().activeOrder().stream().map(f -> f.value()).toList(),
                        "ears", state.config().ears().stream().map(Enum::name).toList()
                ),
                "thresholds", state.audiogram().points().stream()
                        .map(point -> Map.of(
                                "ear", point.ear().name(),
                                "frequency_hz", point.frequency().value(),
                                "threshold_db_hl", point.thresholdDbHL().value(),
                                "criterion", point.criterion().name(),
                                "presentation_count", point.presentationCount(),
                                "completed_at_order", point.completedAtOrder(),
                                "notes", point.notes()
                        ))
                        .toList(),
                "presentation_history", state.presentations().stream()
                        .map(presentation -> Map.of(
                                "ear", presentation.ear().name(),
                                "frequency_hz", presentation.frequency().value(),
                                "db_hl", presentation.intensity().value(),
                                "order", presentation.orderIndex(),
                                "direction", presentation.direction().name(),
                                "valid_ascending_trial", presentation.validAscendingTrial(),
                                "response", presentation.response().map(Enum::name).orElse("PENDING")
                        ))
                        .toList()
        );
    }
}
