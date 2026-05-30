# Odyometre Sistemi Tasarımı ve Testi

Educational Java 21 audiometry simulation for **Audiometer System Design and Testing**. The software follows a functional-core / imperative-shell architecture and is intended for academic demonstration only; it is **not** a clinically certified medical device.

## Commands

```bash
gradle build          # compile, package, and run checks
gradle test           # run unit/property test harness
gradle run            # launch GUI (JavaFX-compatible educational UI)
gradle runSimulation  # launch in simulation mode
gradle exportSample   # write sample CSV/JSON files under exports/
```

## Simulation Mode

1. Run `gradle runSimulation`.
2. Use **Start test** or **Present tone**.
3. Use **Mark RESPONSE** for a heard tone, or **Auto simulate step** to compare current dB HL with simulated thresholds.
4. The audiogram updates with right-ear red `O` and left-ear blue `X` series.

## Serial Mode with Proteus

The Java side uses `SerialPortGateway`; production integration is `JSerialCommGateway` and the demo default is `FakeSerialGateway`. Proteus/Arduino should send `RESPONSE` when the virtual button is pressed. Outgoing tone command format:

```text
TONE;EAR=RIGHT;FREQ=1000;DB=40;DURATION_MS=1000
```

## Exports

CSV columns: `session_id, ear, frequency_hz, threshold_db_hl, criterion, presentation_count, completed_at_order, notes`.
JSON includes configuration, session metadata, thresholds, presentation history, software version, and mode.

## Functional Programming Evidence

* Pure calculations: `AudiometryFunctions`, `ThresholdDetector`, `TestStateReducer`.
* Immutable data: Java records in `domain/model` and `domain/config`.
* Map/filter/reduce RESPONSE pipeline: `SerialMessageProcessor`.
* Optional/Maybe/Result: `domain/fp` plus parser and serial command validation.
