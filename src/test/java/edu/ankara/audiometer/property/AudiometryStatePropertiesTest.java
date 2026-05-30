package edu.ankara.audiometer.property;

import edu.ankara.audiometer.application.SerialMessageProcessor;
import edu.ankara.audiometer.domain.config.AudiometryConfig;
import edu.ankara.audiometer.domain.config.SerialProtocolConfig;
import edu.ankara.audiometer.domain.model.Audiogram;
import edu.ankara.audiometer.domain.model.AudiogramPoint;
import edu.ankara.audiometer.domain.model.Ear;
import edu.ankara.audiometer.domain.model.FrequencyHz;
import edu.ankara.audiometer.domain.model.IntensityDbHL;
import edu.ankara.audiometer.domain.model.TestState;
import edu.ankara.audiometer.domain.model.ThresholdCriterion;
import edu.ankara.audiometer.infrastructure.export.CsvAudiogramExporter;
import edu.ankara.audiometer.infrastructure.serial.SerialProtocol;
import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AudiometryStatePropertiesTest {
    private final AudiometryConfig config = AudiometryConfig.defaults();

    @Property
    void invalidRawMessagesNeverCrashParser(@ForAll String raw) {
        assertDoesNotThrow(() -> SerialProtocol.parseResponseMessage(raw));
    }

    @Property
    void csvRowsMatchAudiogramPointCount(@ForAll @IntRange(min = 0, max = 5) int count) {
        Audiogram audiogram = Audiogram.empty();
        List<Integer> frequencies = List.of(250, 500, 1000, 2000, 4000);
        for (int i = 0; i < count; i++) {
            audiogram = audiogram.add(new AudiogramPoint(
                    i % 2 == 0 ? Ear.RIGHT : Ear.LEFT,
                    new FrequencyHz(frequencies.get(i)),
                    new IntensityDbHL(20 + i),
                    ThresholdCriterion.TWO_OUT_OF_THREE_ASCENDING,
                    3,
                    i,
                    "n"
            ), true);
        }
        TestState state = TestState.initial(config).withAudiogram(audiogram);
        assertEquals(count + 1L, new CsvAudiogramExporter().export(state).lines().count());
    }

    @Property
    void noDuplicateThresholdWithoutRetest(@ForAll @IntRange(min = 0, max = 80) int db) {
        var point = new AudiogramPoint(Ear.RIGHT, new FrequencyHz(1000), new IntensityDbHL(db), ThresholdCriterion.TWO_OUT_OF_THREE_ASCENDING, 3, 1, "a");
        Audiogram audiogram = Audiogram.empty().add(point, false).add(point, false);
        assertEquals(1, audiogram.points().size());
    }

    @Property
    void reducerIsDeterministicForSameMessages(@ForAll @Size(max = 20) List<Boolean> heardFlags) {
        List<String> raw = heardFlags.stream().map(flag -> flag ? " RESPONSE " : "noise").toList();
        var processor = new SerialMessageProcessor(SerialProtocolConfig.defaults());
        TestState initial = TestState.initial(config);
        assertEquals(processor.process(initial, raw), processor.process(initial, raw));
    }

    @Example
    void pureMessageReducerIsDeterministicForKnownInput() {
        var raw = List.of(" RESPONSE ", "bad", "response");
        var processor = new SerialMessageProcessor(SerialProtocolConfig.defaults());
        var state = TestState.initial(config);
        assertEquals(processor.process(state, raw), processor.process(state, raw));
    }
}
