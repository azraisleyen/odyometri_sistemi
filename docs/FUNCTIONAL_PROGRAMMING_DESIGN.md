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

## Cursor-Based State and Ear Modes

`TestState` now includes an immutable frequency cursor so progression is index-based rather than value-based. This preserves pure deterministic transitions even when the clinical plan contains duplicate values such as the 1000 Hz retest. Ear modes are represented by `EarTestMode`, and changing mode creates a new initial immutable state with the selected ear list.
