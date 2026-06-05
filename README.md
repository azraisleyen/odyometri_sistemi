# Audiometer System Design and Testing

**Audiometer System Design and Testing** is a Java 21 + JavaFX desktop application for an educational audiometry course project. It demonstrates a configurable Hughson-Westlake workflow, serial communication with Proteus/Arduino-style hardware, simulation mode without hardware, real-time audiogram rendering, CSV/JSON export, and automated tests.

> **Academic warning:** This repository is an educational simulation aligned with audiometry logic. It is **not** a certified clinical medical device and must not be used for diagnosis or treatment.

## Required Software

Recommended environment:

* Windows 11
* Eclipse Temurin JDK 21 or another Java 21 JDK
* Internet connection for the first Gradle Wrapper run
* Proteus/Arduino virtual COM port only if you want serial mode

## Build, Test, and Run with Gradle Wrapper

```powershell
.\gradlew.bat clean build
.\gradlew.bat test
.\gradlew.bat runSimulation
.\gradlew.bat run
.\gradlew.bat exportSample
```

`runSimulation` and `run` open a real English JavaFX window titled **Audiometer System Design and Testing**. The project intentionally contains no JavaFX, jSerialComm, JUnit, or jqwik stub packages.

## Runtime Configuration

The application loads `config/audiometry-config.json` at startup with Jackson `ObjectMapper`. If the file is missing or invalid, the GUI falls back safely to `AudiometryConfig.defaults()` and logs a warning. Runtime-loaded values include frequency range, frequency plans, start intensity, intensity limits, 10/5 dB Hughson-Westlake steps, timeout, default criterion, default serial baud rate, and command terminator.

The runtime Hughson-Westlake sequence now implements the configured 1000 Hz retest as a real validation step for each selected ear:

```text
1000, 2000, 4000, 8000, 1000 retest, 500, 250
```

The test flow uses index-based progression through the clinical order, so the duplicate 1000 Hz retest cannot create the old infinite loop. The retest generates tone presentations and event-history validation messages, while the final audiogram/results table keeps one unique threshold per ear/frequency: 1000, 2000, 4000, 8000, 500, and 250 Hz. This is still an educational validation step and does not claim certified clinical retest or IEC 60645-1 compliance.

## Simulation Mode and Ear Modes

Use simulation mode when Proteus or a COM port is not connected. The GUI language is English.

1. Run `.\gradlew.bat runSimulation`.
2. Select **Ear** mode:
   * **Right**: tests only RIGHT and emits only `EAR=RIGHT` commands.
   * **Left**: tests only LEFT and emits only `EAR=LEFT` commands.
   * **Both**: tests RIGHT first, then LEFT.
3. Press **Reset test** after changing mode if needed; changing mode also resets the session.
4. Press **Start test** or **Auto simulate step** repeatedly.

Default simulation thresholds are RIGHT 25 dB HL and LEFT 30 dB HL. No demo profile, random threshold, or artificial audiogram slope is added, so flat horizontal simulated audiogram lines are expected. Completed sessions visit 1000, 2000, 4000, 8000, 1000 retest, 500, and 250 Hz for each selected ear; final results show the six unique threshold rows per ear.

## Procedure and Manual Controls

The current visible procedure is **Procedure: Automatic Hughson-Westlake**. A separate Manual Mode is not exposed because it is not fully implemented. **Present tone** and **Mark RESPONSE** are manual control buttons within the automatic Hughson-Westlake workflow, not a separate Manual Mode. Manual Mode can be considered future work.

## Pause / Resume / Stop

* **Pause test** sets the session to `PAUSED` and blocks further tone presentation or auto-simulation.
* **Resume test** returns the same session to a presentable state.
* **Stop test** sets `STOPPED`; reset is required before continuing.
* **Reset test** clears state and starts again with the currently selected ear mode/configuration.
* After completion, **Current Test State** prominently shows `COMPLETED`, displays `Completed thresholds: X / expectedTotal`, and tells the user to export CSV/JSON or reset the test.
* The left control column is scrollable, so Serial Connection, Test Control & Simulation, Current Test State, and Export Controls remain reachable on smaller screens.

## Serial / Proteus Mode

Run:

```powershell
.\gradlew.bat run
```

Select the COM port connected to Proteus COMPIM / Arduino, choose the configured baud rate, and press **Connect**. Serial settings are 8 data bits, 1 stop bit, no parity. The actual serial bytes append the configured command terminator (`\n` by default), while the GUI log keeps the command body clean.

Outgoing command format is unchanged:

```text
TONE;EAR=RIGHT;FREQ=1000;DB=40;DURATION_MS=1000
```

Incoming button response expected from Proteus/Arduino:

```text
RESPONSE
```

The parser trims whitespace, removes CR/LF, supports case-insensitive `RESPONSE`, safely ignores invalid messages, and parses `READY`, `ACK`, button events, and `ERROR:<message>`.

## Export CSV / JSON

Use **Export CSV** or **Export JSON** in the GUI. Export buttons are enabled after at least one threshold exists, the completed state displays `Results are ready for export`, and files are written under `exports/` with timestamps. The Event Log records clear `CSV exported: ...` and `JSON exported: ...` paths. You can also run:

```powershell
.\gradlew.bat exportSample
```

CSV columns remain:

```text
session_id,ear,frequency_hz,threshold_db_hl,criterion,presentation_count,completed_at_order,notes
```

JSON export is generated with Jackson `ObjectMapper` instead of fragile manual string concatenation or a custom parser. The JSON is pretty-printed and parseable; it includes software version, mode, session id, configuration, thresholds, and presentation history.

## Report Relevance

* Software design: `docs/SOFTWARE_DESIGN.md`, `src/main/java/edu/ankara/audiometer/application`, `domain`, `infrastructure`, and `ui`.
* Functional programming: `docs/FUNCTIONAL_PROGRAMMING_DESIGN.md`, immutable records in `domain/model`, pure functions in `domain/algorithm`, and the map/filter/reduce parser pipeline in `application/SerialMessageProcessor.java`.
* Testing evidence: `docs/TESTING_REPORT.md` and tests under `src/test/java` using real JUnit 5, jqwik, and Jackson JSON assertions.
* Communication protocol: `docs/SERIAL_PROTOCOL.md` and `infrastructure/serial`.
* Audiogram/results evidence: GUI chart/table plus CSV/JSON exporters under `infrastructure/export`. RIGHT thresholds render as red `O` markers connected by a red line; LEFT thresholds render as blue `X` markers connected by a blue line. Both mode produces 12 unique final threshold rows: six for RIGHT and six for LEFT. Simulation mode uses frequency-based educational demo thresholds rather than real patient data. These thresholds are provided only for software demonstration and audiogram visualization, so the demo audiogram can show frequency-varying RIGHT and LEFT profiles.

