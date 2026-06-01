package edu.ankara.audiometer.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ankara.audiometer.domain.config.AudiometryConfig;
import edu.ankara.audiometer.domain.model.Audiogram;
import edu.ankara.audiometer.domain.model.AudiogramPoint;
import edu.ankara.audiometer.domain.model.Ear;
import edu.ankara.audiometer.domain.model.FrequencyHz;
import edu.ankara.audiometer.domain.model.IntensityDbHL;
import edu.ankara.audiometer.domain.model.PresentationDirection;
import edu.ankara.audiometer.domain.model.ProtocolEvent;
import edu.ankara.audiometer.domain.model.TestState;
import edu.ankara.audiometer.domain.model.ThresholdCriterion;
import edu.ankara.audiometer.domain.model.TonePresentation;
import edu.ankara.audiometer.infrastructure.export.CsvAudiogramExporter;
import edu.ankara.audiometer.infrastructure.export.JsonSessionExporter;
import edu.ankara.audiometer.infrastructure.serial.SerialCommand;
import edu.ankara.audiometer.infrastructure.serial.SerialProtocol;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SerialProtocolTest {
    @Test
    void parsesResponseVariantsAndIgnoresInvalid() {
        assertTrue(SerialProtocol.parseResponseMessage(" RESPONSE\r\n").orElseThrow() instanceof ProtocolEvent.Response);
        assertTrue(SerialProtocol.parseResponseMessage("response").orElseThrow() instanceof ProtocolEvent.Response);
        assertTrue(SerialProtocol.parseResponseMessage("nonsense").isEmpty());
    }

    @Test
    void createsValidatedToneCommand() {
        var presentation = new TonePresentation(Ear.RIGHT, new FrequencyHz(1000), new IntensityDbHL(40), 1, Optional.empty(), PresentationDirection.INITIAL, false);
        var command = SerialCommand.tone(presentation, AudiometryConfig.defaults());
        assertTrue(command.isOk());
        assertEquals("TONE;EAR=RIGHT;FREQ=1000;DB=40;DURATION_MS=1000", command.orElse(null).value());
    }

    @Test
    void csvExportRowsMatchPoints() {
        TestState state = stateWithOnePoint();
        String csv = new CsvAudiogramExporter().export(state);
        assertEquals(2, csv.lines().count());
        assertTrue(csv.contains("RIGHT,1000,20"));
    }

    @Test
    void jsonExportContainsRequiredSessionFields() throws Exception {
        TestState state = stateWithOnePoint();
        String json = new JsonSessionExporter().export(state).orElse("");
        var tree = new ObjectMapper().readTree(json);
        assertTrue(tree.has("software_version"));
        assertTrue(tree.has("mode"));
        assertTrue(tree.has("session_id"));
        assertTrue(tree.has("configuration"));
        assertTrue(tree.has("thresholds"));
        assertTrue(tree.has("presentation_history"));
        assertEquals("RIGHT", tree.get("thresholds").get(0).get("ear").asText());
    }

    private static TestState stateWithOnePoint() {
        var point = new AudiogramPoint(Ear.RIGHT, new FrequencyHz(1000), new IntensityDbHL(20), ThresholdCriterion.TWO_OUT_OF_THREE_ASCENDING, 3, 3, "ok");
        return TestState.initial(AudiometryConfig.defaults()).withAudiogram(Audiogram.empty().add(point, false));
    }
}
