# Software Design

The project uses a clean **functional-core / imperative-shell** architecture.

## Layers

* `domain/model`: immutable Java records and enums for ears, ear test modes, frequencies, intensities, tone presentations, audiogram points, and complete `TestState`.
* `domain/config`: biomedical parameters such as frequency range, intensity range, step sizes, timeout, serial settings, frequency plan, and threshold criterion.
* `domain/algorithm`: pure Hughson-Westlake functions, duplicate-safe frequency progression, threshold detection, and deterministic reducers.
* `application`: session orchestration, ear-mode changes, pause/resume/stop/reset, simulation stepping, serial-message processing, and export use cases.
* `infrastructure`: real jSerialComm serial gateway, fake simulation gateway, runtime JSON config loading, CSV exporter, and parseable JSON export.
* `ui`: real JavaFX desktop GUI controllers and views.

The GUI does not implement the medical threshold decision. It calls application services, and those services call the pure domain layer. Serial I/O, JavaFX rendering, config/file loading, and filesystem export are isolated from the domain model.

## Ear Modes

The JavaFX ear ComboBox is connected to backend state through `EarTestMode`:

* Right-only configures `[RIGHT]`.
* Left-only configures `[LEFT]`.
* Both configures `[RIGHT, LEFT]` and runs RIGHT first, then LEFT.

Changing ear mode reinitializes the session with the updated immutable configuration.

## Runtime Configuration

`config/audiometry-config.json` is loaded at startup. Invalid or missing config falls back to `AudiometryConfig.defaults()` with a visible log warning instead of crashing the GUI.
