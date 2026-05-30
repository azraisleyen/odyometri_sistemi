# Odyometre Sistemi Tasarımı ve Testi

**Audiometer System Design and Testing** is a Java 21 + JavaFX desktop application for an educational audiometry course project. It demonstrates a configurable Hughson-Westlake workflow, serial communication with Proteus/Arduino-style hardware, simulation mode without hardware, real-time audiogram rendering, CSV/JSON export, and automated tests.

> **Academic warning:** This repository is an educational simulation aligned with audiometry logic. It is **not** a certified clinical medical device and must not be used for diagnosis or treatment.

## Required Software

Recommended environment:

* Windows 11
* Eclipse Temurin JDK 21 or another Java 21 JDK
* Gradle 9.5.1
* Proteus/Arduino virtual COM port only if you want serial mode

Check Java in PowerShell:

```powershell
java -version
javac -version
```

Both commands should report Java 21.

## Build, Test, and Run with Gradle

```powershell
gradle clean build
gradle test
gradle runSimulation
gradle run
gradle exportSample
```

`runSimulation` and `run` should open a real JavaFX window titled **Odyometre Sistemi Tasarımı ve Testi**. If you only see console text, verify that no local stub JavaFX classes are present and that Gradle downloaded the JavaFX dependencies.

## Simulation Mode

Use simulation mode when Proteus or a COM port is not connected.

1. Run `gradle runSimulation`.
2. Press **Start test** or **Present tone**.
3. Press **Mark RESPONSE** to simulate a heard tone.
4. Press **Auto simulate step** to compare the current dB HL against simulated true thresholds:
   * RIGHT: 25 dB HL
   * LEFT: 30 dB HL
5. Watch the current-state panel, event log, audiogram chart, and results table update.

## Serial / Proteus Mode

Run:

```powershell
gradle run
```

Then:

1. Select the COM port connected to Proteus COMPIM / Arduino.
2. Select baud rate `9600`.
3. Press **Connect**.
4. Start the test and present tones.

Serial settings are 9600 baud, 8 data bits, 1 stop bit, no parity.

Outgoing command format:

```text
TONE;EAR=RIGHT;FREQ=1000;DB=40;DURATION_MS=1000
```

Incoming button response expected from Proteus/Arduino:

```text
RESPONSE
```

The parser trims whitespace, removes CR/LF, supports case-insensitive `RESPONSE`, safely ignores invalid messages, and parses `ERROR:<message>`.

## Export CSV / JSON

Use **Export CSV** or **Export JSON** in the GUI. Files are written under `exports/` with timestamps. You can also run:

```powershell
gradle exportSample
```

CSV columns:

```text
session_id,ear,frequency_hz,threshold_db_hl,criterion,presentation_count,completed_at_order,notes
```

JSON includes software version, mode, session id, configuration, thresholds, and presentation history.

## Troubleshooting

* **Java 17 instead of Java 21:** install JDK 21 and ensure `JAVA_HOME` and `Path` point to it.
* **Gradle not recognized:** install Gradle 9.5.1 or ensure Gradle is on your Windows `Path`.
* **No COM port found:** use simulation mode, check Windows Device Manager, and verify Proteus COMPIM/virtual COM configuration.
* **JavaFX window does not open:** run `gradle clean runSimulation`; ensure Maven Central is reachable so Gradle can download JavaFX. The project intentionally contains no JavaFX stubs.
* **OneDrive path issues:** if Windows locks files under OneDrive, copy the project to a short path such as `C:\dev\odyometri_sistemi` and rerun Gradle.

## Report Relevance

* Software design: `docs/SOFTWARE_DESIGN.md`, `src/main/java/edu/ankara/audiometer/application`, `domain`, `infrastructure`, and `ui`.
* Functional programming: `docs/FUNCTIONAL_PROGRAMMING_DESIGN.md`, immutable records in `domain/model`, pure functions in `domain/algorithm`, and the map/filter/reduce parser pipeline in `application/SerialMessageProcessor.java`.
* Testing evidence: `docs/TESTING_REPORT.md` and tests under `src/test/java` using real JUnit 5 and jqwik.
* Communication protocol: `docs/SERIAL_PROTOCOL.md` and `infrastructure/serial`.
* Audiogram/results evidence: GUI chart/table plus CSV/JSON exporters under `infrastructure/export`.
