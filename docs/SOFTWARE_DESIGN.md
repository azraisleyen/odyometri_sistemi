# Software Design

The project uses a clean **functional-core / imperative-shell** architecture.

## Layers

* `domain/model`: immutable Java records and enums for ears, frequencies, intensities, tone presentations, audiogram points, and complete `TestState`.
* `domain/config`: centralized biomedical parameters such as frequency range, intensity range, step sizes, timeout, frequency plan, and threshold criterion.
* `domain/algorithm`: pure Hughson-Westlake functions, threshold detection, and deterministic reducers.
* `application`: session orchestration, simulation, serial-message processing, and export use cases.
* `infrastructure`: real jSerialComm serial gateway, fake simulation gateway, CSV exporter, and JSON exporter.
* `ui`: real JavaFX desktop GUI controllers and views.

The GUI never implements medical decision logic directly. It calls application services, and those services call the pure domain layer. Serial I/O, JavaFX rendering, and filesystem export are isolated from the domain model.

## Desktop GUI

The application uses real JavaFX dependencies from Gradle. `gradle --no-daemon runSimulation` opens the desktop GUI in simulation mode, while `gradle --no-daemon run` opens the same GUI with the real serial gateway available for COM-port integration.

## Corrected Runtime Workflow

The GUI ear selector is wired to `TestSessionService.setEarMode`, which resets the immutable state with `[RIGHT]`, `[LEFT]`, or `[RIGHT, LEFT]`. Frequency progression is cursor-based inside `TestState`, so duplicate 1000 Hz retest entries cannot create a value-based loop. Runtime biomedical parameters are loaded through `AudiometryConfigLoader` from `config/audiometry-config.json`, with safe fallback to defaults.
