# Report-Ready Software Engineering Text

## 2.4 Software Design
The software was designed using a functional-core / imperative-shell architecture. Audiometric rules are deterministic pure functions over immutable state. The real JavaFX desktop GUI, real jSerialComm serial communication, runtime JSON configuration, and file export features are isolated in application and infrastructure layers.

## 2.5 Hughson-Westlake Algorithm
The implementation applies the modified Hughson-Westlake rule: heard responses decrease level by 10 dB and no-response/timeouts increase level by 5 dB. Threshold is detected from valid ascending trials using a configurable 2/3 or 3/5 criterion. Runtime frequency progression is duplicate-safe, reaches 500 Hz and 250 Hz, and prevents the duplicate-1000 loop while preserving the 1000 Hz retest in configuration/documentation for future validation.

## 2.6 Communication Protocol
The Java program sends deterministic commands such as `TONE;EAR=RIGHT;FREQ=1000;DB=40;DURATION_MS=1000`. The Proteus/Arduino side returns `RESPONSE` when the patient button is pressed. Input is sanitized and invalid messages are ignored safely.

## 3.2 Software Tests
Automated tests run with real JUnit 5 and jqwik dependencies. They verify validation, parsing, immutable state, reducer determinism, threshold criteria, ear modes, RIGHT-to-LEFT transition, low-frequency reachability, completion, config loading/fallback, JSON parseability, pause/resume, exports, and transition invariants.

## 3.3 System-Level Test
Simulation mode exercises the same application and domain logic used by serial mode. Right-only, left-only, and both-ear workflows can be demonstrated without Proteus hardware, using default simulated thresholds of RIGHT 25 dB HL and LEFT 30 dB HL.

## 3.4 Audiogram Results
Thresholds are stored as audiogram points by ear and frequency. The GUI renders right-ear results as red `O` symbols and left-ear results as blue `X` symbols with standard audiometry frequencies shown in equal visual spacing.

## 4.1 Discussion
This is an academic simulation aligned with audiometry logic and configurable for biomedical validation; it is not a certified clinical device and does not claim IEC 60645-1 certification.

## 4.2 Multidisciplinary Collaboration
The serial protocol decouples Java software from Proteus, ESP32-S3, or FPGA button implementations as long as `RESPONSE` is transmitted.
