# Testing Report

The project uses real JUnit 5 and real jqwik through Gradle's JUnit Platform support. Run:

```powershell
gradle --no-daemon test
```

The test suite covers:

* Frequency and intensity validation.
* RESPONSE parsing and invalid message handling.
* Serial command generation with unchanged command body format.
* Configurable serial command terminator handling.
* Heard => -10 dB and no-response => +5 dB rules.
* Recent-window 2/3 and 3/5 ascending threshold criteria.
* Older ascending trials outside the criterion window not forcing threshold.
* Independent right/left ear audiogram results.
* Left-only, right-only, and both-ear workflows.
* RIGHT-to-LEFT transition in both mode.
* 500 Hz and 250 Hz reachability.
* Runtime clinical order includes the true 1000 Hz retest while unique final threshold order suppresses duplicate audiogram rows.
* Deterministic simulation completion with no infinite loop through the duplicate 1000 Hz retest.
* Ear-specific simulation thresholds: RIGHT 25 dB HL and LEFT 30 dB HL.
* Pause/resume and stop/reset behavior.
* Jackson runtime config loader success and safe fallback.
* Jackson JSON export parseability and CSV row counts.
* Property-based parser robustness, intensity bounds, and deterministic reducers.

These automated tests provide evidence for the Software Tests section of the academic report. They verify educational algorithm consistency; they do not establish clinical certification.


## Manual report-evidence checklist

For the final report, include screenshots or excerpts showing: successful build/test output, completed Both-mode GUI, audiogram chart, 12-row Results table, Event Log with outgoing tone commands, RESPONSE/NO_RESPONSE events, threshold detection, 1000 Hz retest validation, session completion, CSV/JSON export paths, and Proteus/COMPIM RESPONSE verification evidence if hardware testing is performed.

## Proteus/COMPIM System-Level Evidence

System-level integration evidence should be collected when the Java software is tested with Proteus/Arduino through Virtual COM Port and the COMPIM module. The report evidence should include:

* Java Event Log outgoing command, such as `TONE;EAR=RIGHT;FREQ=1000;DB=40;DURATION_MS=1000`.
* Proteus COMPIM configuration with matching COM port and baud rate.
* `RESPONSE` message returned from Proteus/Arduino to Java after virtual patient-button interaction.
* Java Event Log `Threshold detected` entry after the response is processed.
* Real-time audiogram update.
* CSV/JSON export evidence.

Use `docs/PROTEUS_COMPIM_TEST_EVIDENCE.md` as the checklist and screenshot/video placeholder guide. Do not state that Proteus/COMPIM validation has passed until the actual hardware/simulation evidence has been collected.
