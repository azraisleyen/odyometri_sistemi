package edu.ankara.audiometer.infrastructure;

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
import edu.ankara.audiometer.infrastructure.json.JsonSupport;
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
    void jsonExportContainsRequiredSessionFieldsAndIsParseable() {
        TestState state = stateWithOnePoint();
        String json = new JsonSessionExporter().export(state).orElse("");
        var parsed = JsonSupport.parse(json);
        assertTrue(parsed.isOk());
        var root = JsonSupport.asObject(parsed.orElse(java.util.Map.of()));
        assertTrue(root.containsKey("software_version"));
        assertTrue(root.containsKey("mode"));
        assertTrue(root.containsKey("session_id"));
        assertTrue(root.containsKey("configuration"));
        assertTrue(root.containsKey("thresholds"));
        assertTrue(root.containsKey("presentation_history"));
    }

    private static TestState stateWithOnePoint() {
        var point = new AudiogramPoint(Ear.RIGHT, new FrequencyHz(1000), new IntensityDbHL(20), ThresholdCriterion.TWO_OUT_OF_THREE_ASCENDING, 3, 3, "ok");
        return TestState.initial(AudiometryConfig.defaults()).withAudiogram(Audiogram.empty().add(point, false));
    }
}
