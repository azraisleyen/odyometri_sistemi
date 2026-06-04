# Proteus/COMPIM System-Level Test Evidence Checklist

## Purpose

This document is a practical evidence-collection checklist for the system-level integration test between the Java audiometry software and the Proteus/Arduino virtual hardware through a Virtual COM Port and the COMPIM module.

Use this guide to collect proof that the Java application can send tone commands, the Proteus-side circuit can receive those commands, and the Proteus virtual patient response mechanism can return `RESPONSE` messages to the Java application. This document is a report-support guide; it does **not** claim that the Proteus/COMPIM test has already been performed.

## Required evidence checklist

Complete the checklist during the actual Proteus/COMPIM integration session and attach the referenced screenshots, short videos, or Event Log excerpts to the multidisciplinary project report.

- [ ] Java application launched in Serial Mode or Simulation Mode as appropriate for the test setup.
- [ ] Correct COM port selected in the Java Serial Connection panel.
- [ ] Correct baud rate selected in Java.
- [ ] Proteus COMPIM configured with the matching COM port.
- [ ] Proteus/Arduino circuit running.
- [ ] Java Event Log shows an outgoing serial command.
- [ ] Example command captured: `TONE;EAR=RIGHT;FREQ=1000;DB=40;DURATION_MS=1000`.
- [ ] Proteus receives the command.
- [ ] Proteus generates or simulates the corresponding pure tone.
- [ ] Patient button / virtual button pressed in Proteus.
- [ ] Proteus sends `RESPONSE` message back to Java.
- [ ] Java Event Log shows incoming `RESPONSE`.
- [ ] Java threshold logic continues after `RESPONSE`.
- [ ] Java Event Log shows `Threshold detected`.
- [ ] Results table records Ear, Hz, dB HL, and Notes.
- [ ] Audiogram updates in real time.
- [ ] CSV export generated.
- [ ] JSON export generated.

## Screenshot and video evidence placeholders

| Evidence ID | Screenshot / Video Needed | What It Should Show | Status | Notes |
|---|---|---|---|---|
| E01 | Java GUI before connection | Application open in Serial Mode or the selected test mode before connecting to the COM port. | Not collected | Insert screenshot reference after testing. |
| E02 | Java Serial Connection panel with COM port selected | Selected COM port and baud rate visible in the Java GUI. | Not collected | Insert screenshot reference after testing. |
| E03 | Proteus COMPIM configuration | COMPIM properties showing the matching COM port and serial settings. | Not collected | Insert screenshot reference after testing. |
| E04 | Proteus circuit view | Arduino UNO, COMPIM, MCP4921 DAC, LM358 buffer, tone output path, and virtual patient button. | Not collected | Insert screenshot reference after testing. |
| E05 | Java Event Log outgoing command | Event Log line containing `TONE;EAR=RIGHT;FREQ=1000;DB=40;DURATION_MS=1000` or another documented tone command. | Not collected | Insert screenshot/reference after testing. |
| E06 | RESPONSE returned from Proteus | Proteus button action and Java Event Log showing incoming `RESPONSE`. | Not collected | Insert screenshot/video reference after testing. |
| E07 | Threshold detected log | Java Event Log showing `Threshold detected` after returned responses. | Not collected | Insert screenshot/reference after testing. |
| E08 | Results table after threshold detection | Results table containing Ear, Hz, dB HL, and Notes for detected thresholds. | Not collected | Insert screenshot/reference after testing. |
| E09 | Real-time audiogram update | Audiogram chart updated with right-ear red `O` and/or left-ear blue `X` markers. | Not collected | Insert screenshot/reference after testing. |
| E10 | CSV/JSON exported files | Java Event Log or file explorer showing generated CSV and JSON export files. | Not collected | Insert screenshot/reference after testing. |

## Expected serial protocol

### Outgoing Java tone command

```text
TONE;EAR=RIGHT;FREQ=1000;DB=40;DURATION_MS=1000
```

The actual serial payload appends the command terminator configured in the software. The default terminator is usually a line feed (`\n`).

### Incoming Proteus/Arduino response

```text
RESPONSE
```

The Java side sanitizes line endings and whitespace before parsing the response.

## Pass/fail criteria

### Pass criteria

The Proteus/COMPIM system-level integration test passes when all of the following are demonstrated with evidence:

- Java can connect to the selected COM port.
- Java can send a tone command without a serial error.
- Proteus/COMPIM receives the command.
- Proteus/Arduino sends `RESPONSE` after virtual button interaction.
- Java receives and parses `RESPONSE`.
- The Hughson-Westlake workflow advances after the response.
- A threshold is detected and shown in the Results table.
- The audiogram is updated in real time.
- CSV and JSON export files can be generated.

### Fail examples

The test should be considered failed or incomplete if any of these occur:

- COM port mismatch between Java and Proteus COMPIM.
- Baud rate mismatch.
- No `RESPONSE` returned from Proteus/Arduino.
- Invalid message format returned instead of `RESPONSE`.
- Java serial connection failed.
- Proteus circuit not running.
- Java sends a command, but Proteus/COMPIM does not receive it.
- Proteus sends `RESPONSE`, but Java Event Log does not show the incoming message.

## Report-ready text

> Use this wording only after the actual Proteus/COMPIM test has been performed and evidence screenshots or videos have been collected.

The Java audiometry software was integrated with the Proteus-based virtual hardware through a Virtual COM Port using the COMPIM module. During the system-level test, the Java application transmitted tone commands containing ear, frequency, intensity, and duration parameters. The Proteus-side patient response mechanism returned a `RESPONSE` message to the Java application. The received response was parsed by the software and used to advance the Hughson-Westlake workflow. The detected threshold values were displayed in the Results table and plotted on the real-time audiogram.

## Academic limitation statement

This project is an educational simulation/prototype for a multidisciplinary university course. It does not claim clinical medical-device certification, diagnostic validity, treatment suitability, or IEC 60645-1 certification. Proteus/COMPIM evidence supports academic system-integration verification only.
