# Functional Programming Design

The audiometry core is deterministic and side-effect-free. Medical/audiometric calculations are implemented as pure functions for validation, intensity transitions, duplicate-free educational frequency cursor progression, next ear selection, tone creation, recent-window threshold detection, audiogram point creation, and hearing classification.

Immutable Java records represent state. `TestState` stores current ear, current frequency index, current frequency, dB HL, phase, direction, presentation history, event history, audiogram points, and errors. Update methods return new state values rather than mutating lists in place.

Incoming serial messages are processed with an explicit stream pipeline:

```java
rawMessages.stream()
    .map(SerialProtocol::sanitizeSerialInput)
    .filter(message -> !message.isBlank())
    .map(message -> SerialProtocol.parseResponseMessage(message, config))
    .flatMap(Optional::stream)
    .reduce(initial, TestStateReducer::updateState, (left, right) -> right)
```

Application services form the imperative shell. They own JavaFX callbacks, serial gateways, runtime config loading, export files, and the mutable session reference. The domain model remains UI-free, filesystem-free, and serial-free.

`Result`, `Validation`, `Maybe`, and `Optional`-style flows are used for side-effect-safe parsing, command generation, config fallback, and export results. Jackson is used in the infrastructure layer for JSON config parsing and JSON export serialization; it is not part of the domain calculation model.

This is an educational simulation and does not claim certified clinical-device or IEC 60645-1 compliance.
