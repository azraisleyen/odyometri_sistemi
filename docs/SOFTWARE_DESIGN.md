# Software Design

The project uses a clean **functional-core / imperative-shell** architecture.

## Layers

* `domain/model`: immutable Java records and enums for ears, ear test modes, frequencies, intensities, tone presentations, audiogram points, and complete `TestState`.
* `domain/config`: biomedical parameters such as frequency range, intensity range, step sizes, timeout, serial settings, frequency plan, and threshold criterion.
* `domain/algorithm`: pure Hughson-Westlake functions, duplicate-free educational frequency progression, recent-window threshold detection, and deterministic reducers.
* `application`: session orchestration, ear-mode changes, pause/resume/stop/reset, simulation stepping, serial-message processing, and export use cases.
* `infrastructure`: real jSerialComm serial gateway, fake simulation gateway, Jackson-based runtime JSON config loading, CSV exporter, and Jackson-based parseable JSON export.
* `ui`: real English JavaFX desktop GUI controllers and views with custom audiogram symbols/legend.

The GUI does not implement the medical threshold decision. It calls application services, and those services call the pure domain layer. Serial I/O, JavaFX rendering, config/file loading, Jackson serialization, and filesystem export are isolated from the domain model.

## Ear Modes

The JavaFX ear ComboBox is connected to backend state through `EarTestMode`:

* Right-only configures `[RIGHT]`.
* Left-only configures `[LEFT]`.
* Both configures `[RIGHT, LEFT]` and runs RIGHT first, then LEFT.

Changing ear mode reinitializes the session with the updated immutable configuration. The current visible procedure is Automatic Hughson-Westlake; a separate Manual Mode is not exposed because it is not fully implemented. Present tone and Mark RESPONSE are manual control buttons within the automatic workflow, not a separate mode.

## Audiogram GUI

The audiogram chart uses equally spaced standard frequencies (250, 500, 1000, 2000, 4000, 8000). RIGHT thresholds are red `O` markers connected by a red line, and LEFT thresholds are blue `X` markers connected by a blue line. Constant simulated thresholds can naturally produce flat horizontal lines.

## Runtime Configuration and Export

`config/audiometry-config.json` is loaded at startup with Jackson `ObjectMapper`. Invalid or missing config falls back to `AudiometryConfig.defaults()` with a visible log warning instead of crashing the GUI.

JSON session export also uses Jackson `ObjectMapper` with pretty printing. The export includes software version, mode, session id, configuration, thresholds, and presentation history.

## Educational Limits

This repository is an academic simulation aligned with audiometry logic. It is not a certified clinical medical device, is not for diagnosis/treatment, and does not claim IEC 60645-1 certification.
