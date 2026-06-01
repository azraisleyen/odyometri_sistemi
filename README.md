# Odyometre Sistemi Tasarımı ve Testi

**Audiometer System Design and Testing** is a Java 21 + JavaFX desktop application for an educational audiometry course project. It demonstrates a configurable Hughson-Westlake workflow, serial communication with Proteus/Arduino-style hardware, simulation mode without hardware, real-time audiogram rendering, CSV/JSON export, and automated tests.

> **Academic warning:** This repository is an educational simulation aligned with audiometry logic. It is **not** a certified clinical medical device and must not be used for diagnosis or treatment.

## Required Software

Recommended environment:

* Windows 11
* Eclipse Temurin JDK 21 or another Java 21 JDK
* Gradle 9.5.1 or a compatible installed Gradle version
* Proteus/Arduino virtual COM port only if you want serial mode

## Build, Test, and Run with Gradle

```powershell
gradle --no-daemon clean build
gradle --no-daemon test
gradle --no-daemon runSimulation
gradle --no-daemon run
gradle --no-daemon exportSample
```

`runSimulation` and `run` open a real JavaFX window titled **Odyometre Sistemi Tasarımı ve Testi**. The project intentionally contains no JavaFX, jSerialComm, JUnit, or jqwik stub packages.

## Runtime Configuration

The application loads `config/audiometry-config.json` at startup. If the file is missing or invalid, the GUI falls back safely to `AudiometryConfig.defaults()` and logs a warning. Runtime-loaded values include frequency range, frequency plans, start intensity, intensity limits, 10/5 dB Hughson-Westlake steps, timeout, default criterion, and default serial baud rate.

The configured clinical order may include a 1000 Hz retest for documentation/future validation. Runtime progression is duplicate-safe and uses the educational threshold order:

```text
1000, 2000, 4000, 8000, 500, 250
```

This prevents the old duplicate-1000 loop while ensuring 500 Hz and 250 Hz are reached.

## Simulation Mode and Ear Modes

Use simulation mode when Proteus or a COM port is not connected.

1. Run `gradle --no-daemon runSimulation`.
2. Select **Ear** mode:
   * **Right**: tests only RIGHT and emits only `EAR=RIGHT` commands.
   * **Left**: tests only LEFT and emits only `EAR=LEFT` commands.
   * **Both**: tests RIGHT first, then LEFT.
3. Press **Reset test** after changing mode if needed; changing mode also resets the session.
4. Press **Start test** or **Auto simulate step** repeatedly.

Default simulation thresholds are RIGHT 25 dB HL and LEFT 30 dB HL. Completed sessions reach 1000, 2000, 4000, 8000, 500, and 250 Hz for each selected ear.

## Pause / Resume / Stop

* **Pause test** sets the session to `PAUSED` and blocks further tone presentation or auto-simulation.
* **Resume test** returns the same session to a presentable state.
* **Stop test** sets `STOPPED`; reset is required before continuing.
* **Reset test** clears state and starts again with the currently selected ear mode/configuration.

## Serial / Proteus Mode

Run:

```powershell
gradle --no-daemon run
```

Select the COM port connected to Proteus COMPIM / Arduino, choose the configured baud rate, and press **Connect**. Serial settings are 8 data bits, 1 stop bit, no parity.

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

Use **Export CSV** or **Export JSON** in the GUI. Files are written under `exports/` with timestamps. You can also run:

```powershell
gradle --no-daemon exportSample
```

CSV columns remain:

```text
session_id,ear,frequency_hz,threshold_db_hl,criterion,presentation_count,completed_at_order,notes
```

JSON export is generated through a reusable JSON serializer utility instead of fragile manual string concatenation. The JSON is pretty-printed and parseable; it includes software version, mode, session id, configuration, thresholds, and presentation history.

## Report Relevance

* Software design: `docs/SOFTWARE_DESIGN.md`, `src/main/java/edu/ankara/audiometer/application`, `domain`, `infrastructure`, and `ui`.
* Functional programming: `docs/FUNCTIONAL_PROGRAMMING_DESIGN.md`, immutable records in `domain/model`, pure functions in `domain/algorithm`, and the map/filter/reduce parser pipeline in `application/SerialMessageProcessor.java`.
* Testing evidence: `docs/TESTING_REPORT.md` and tests under `src/test/java` using real JUnit 5 and jqwik dependencies.
* Communication protocol: `docs/SERIAL_PROTOCOL.md` and `infrastructure/serial`.
* Audiogram/results evidence: GUI chart/table plus CSV/JSON exporters under `infrastructure/export`.
