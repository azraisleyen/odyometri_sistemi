# Testing Report

The project uses real JUnit 5 and real jqwik through Gradle's JUnit Platform support. Run:

```powershell
gradle test
```

The test suite covers:

* Frequency and intensity validation.
* RESPONSE parsing and invalid message handling.
* Serial command generation.
* Heard => -10 dB and no-response => +5 dB rules.
* 2/3 and 3/5 ascending threshold criteria.
* Independent right/left ear audiogram results.
* Audiogram point creation.
* Immutable state updates.
* Session completeness validation.
* CSV row counts and JSON session fields.
* Property-based parser robustness, intensity bounds, and deterministic reducers.

These automated tests provide evidence for the Software Tests section of the academic report.
