package edu.ankara.audiometer.infrastructure.export;

import edu.ankara.audiometer.domain.model.TestState;

import java.util.stream.Collectors;

public final class CsvAudiogramExporter {
    private static final String HEADER = "session_id,ear,frequency_hz,threshold_db_hl,criterion,presentation_count,completed_at_order,notes\n";

    public String export(TestState state) {
        return HEADER + state.audiogram().points().stream()
                .map(point -> String.join(
                        ",",
                        state.session().sessionId(),
                        point.ear().name(),
                        String.valueOf(point.frequency().value()),
                        String.valueOf(point.thresholdDbHL().value()),
                        point.criterion().name(),
                        String.valueOf(point.presentationCount()),
                        String.valueOf(point.completedAtOrder()),
                        quote(point.notes())
                ))
                .collect(Collectors.joining("\n"));
    }

    private static String quote(String value) {
        return '"' + value.replace("\"", "\"\"") + '"';
    }
}
