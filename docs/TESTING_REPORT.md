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
* Duplicate-free educational active threshold order and no duplicate 1000 Hz final threshold rows when retest rows are disabled.
* Deterministic simulation completion.
* Ear-specific simulation thresholds: RIGHT 25 dB HL and LEFT 30 dB HL.
* Pause/resume and stop/reset behavior.
* Jackson runtime config loader success and safe fallback.
* Jackson JSON export parseability and CSV row counts.
* Property-based parser robustness, intensity bounds, and deterministic reducers.

These automated tests provide evidence for the Software Tests section of the academic report. They verify educational algorithm consistency; they do not establish clinical certification.
