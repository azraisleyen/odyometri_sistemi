# Functional Programming Design

The audiometry core is deterministic and side-effect-free. Medical calculations are implemented as pure functions for validation, intensity transitions, next frequency/ear selection, tone creation, threshold detection, audiogram point creation, and hearing classification.

Immutable Java records represent state. `TestState` stores current ear, frequency, dB HL, phase, direction, presentation history, event history, audiogram points, and errors. Update methods return new state values rather than mutating lists in place.

Incoming serial messages are processed with an explicit stream pipeline:

```java
rawMessages.stream()
    .map(SerialProtocol::sanitizeSerialInput)
    .filter(s -> !s.isBlank())
    .map(s -> SerialProtocol.parseResponseMessage(s, config))
    .flatMap(Optional::stream)
    .reduce(initial, TestStateReducer::updateState, (a, b) -> b)
```

The code uses `Optional`, `Maybe`, `Result`, and `Validation` to represent missing values, serial failures, and validation errors without returning `null` from the functional core.
