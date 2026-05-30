# Software Design

The project uses clean architecture. `domain` contains immutable audiometry types, validation, and the Hughson-Westlake reducer. `application` coordinates sessions, simulation, and exports. `infrastructure` contains serial and export adapters. `ui` contains JavaFX-compatible views/controllers and does not implement medical decision logic.

The design intentionally separates the pure functional core from side effects. Serial I/O, file writes, and GUI updates are performed only outside the domain layer.
