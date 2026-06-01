# Testing Report

The project uses real JUnit 5 and real jqwik through Gradle's JUnit Platform support. Run:

```powershell
gradle --no-daemon test
```

The test suite covers:

* Frequency and intensity validation.
* RESPONSE parsing and invalid message handling.
* Serial command generation.
* Heard => -10 dB and no-response => +5 dB rules.
* 2/3 and 3/5 ascending threshold criteria.
* Independent right/left ear audiogram results.
* Left-only, right-only, and both-ear workflows.
* RIGHT-to-LEFT transition in both mode.
* 500 Hz and 250 Hz reachability.
* Duplicate 1000 Hz no-loop and no duplicate threshold rows when retest rows are disabled.
* Deterministic simulation completion.
* Ear-specific simulation thresholds: RIGHT 25 dB HL and LEFT 30 dB HL.
* Pause/resume and stop/reset behavior.
* Runtime config loader success and safe fallback.
* JSON export parseability and CSV row counts.
* Property-based parser robustness, intensity bounds, and deterministic reducers.

These automated tests provide evidence for the Software Tests section of the academic report.
