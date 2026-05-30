# Functional Programming Design

Medical calculations are pure functions: frequency validation, intensity validation, next intensity, next frequency, tone creation, threshold detection, audiogram point creation, hearing classification, and session completeness validation. State is represented by immutable records; updates return a new `TestState`.

Incoming serial text is processed as: map sanitize -> filter non-empty -> map parse to Optional -> flatMap valid events -> reduce into `TestState`. Error handling uses `Optional`, `Maybe`, `Result`, and `Validation` without null returns in the functional core.
