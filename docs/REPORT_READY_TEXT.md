# Report-Ready Software Engineering Text

## 2.4 Software Design
The English GUI exposes Automatic Hughson-Westlake as the current procedure; a separate Manual Mode is not shown because it is future work. The software was designed using a functional-core / imperative-shell architecture. Audiometric rules are deterministic pure functions over immutable state. The real JavaFX desktop GUI, real jSerialComm serial communication, Jackson runtime JSON configuration, Jackson JSON export, and file export features are isolated in application and infrastructure layers.

## 2.5 Hughson-Westlake Algorithm
The implementation applies the modified Hughson-Westlake rule: heard responses decrease level by 10 dB and no-response/timeouts increase level by 5 dB. Threshold is detected from valid ascending trials using a configurable recent-window 2/3 or 3/5 criterion. Runtime frequency progression uses index-based clinical order and implements the true 1000 Hz retest sequence: 1000, 2000, 4000, 8000, 1000 retest, 500, 250. The retest is stored in presentation/event history as validation, while the final audiogram keeps one unique threshold per ear/frequency.

## 2.6 Communication Protocol
The Java program sends deterministic command bodies such as `TONE;EAR=RIGHT;FREQ=1000;DB=40;DURATION_MS=1000`; the configured command terminator is appended to the actual serial bytes. The Proteus/Arduino side returns `RESPONSE` when the patient button is pressed. Input is sanitized and invalid messages are ignored safely.

## 3.2 Software Tests
Automated tests run with real JUnit 5 and jqwik dependencies. They verify validation, parsing, immutable state, reducer determinism, recent-window threshold criteria, ear modes, RIGHT-to-LEFT transition, low-frequency reachability, completion, config loading/fallback with Jackson, JSON parseability with Jackson, pause/resume, exports, serial terminator handling, and transition invariants.

## 3.3 System-Level Test
Simulation mode exercises the same application and domain logic used by serial mode. Right-only, left-only, and both-ear workflows can be demonstrated without Proteus hardware, using default simulated thresholds of RIGHT 25 dB HL and LEFT 30 dB HL. No demo profile, random threshold, or artificial audiogram slope is added.

## 3.4 Audiogram Results
Thresholds are stored as unique final audiogram points by ear and frequency. Both mode produces 12 final rows: six unique frequencies for RIGHT and six for LEFT. The GUI renders right-ear results as red `O` symbols connected by a red line and left-ear results as blue `X` symbols connected by a blue line with standard audiometry frequencies shown in equal visual spacing. Flat lines are expected when simulated thresholds are constant across frequencies.

## 4.1 Discussion
This is an academic simulation aligned with audiometry logic and configurable for biomedical validation; it is not a certified clinical device, is not for diagnosis/treatment, and does not claim IEC 60645-1 certification.

## 4.2 Multidisciplinary Collaboration
The serial protocol decouples Java software from Proteus, ESP32-S3, or FPGA button implementations as long as `RESPONSE` is transmitted.


## Proteus/COMPIM Verification
The Java software sends commands such as `TONE;EAR=RIGHT;FREQ=1000;DB=40;DURATION_MS=1000`. For system-integration evidence, the team should verify that Proteus/Arduino/COMPIM receives the command, that the virtual patient button sends `RESPONSE`, that the Java Event Log displays the incoming response, and that threshold detection proceeds. Evidence should be recorded as screenshots, a short video, and Event Log excerpts. This is a checklist, not a claim that hardware validation has already passed.

## Optional Proteus/COMPIM Integration Evidence Paragraph
Use the following paragraph only after the actual Proteus/COMPIM system-level test has been performed and screenshots, videos, or Event Log excerpts have been inserted into the report:

The Java audiometry software was connected to the Proteus-based virtual hardware through the COMPIM Virtual COM Port interface. During the integration test, the Java Event Log showed outgoing tone commands, Proteus/COMPIM received the serial command, and the Proteus-side virtual patient button returned `RESPONSE` to Java. After the returned response was parsed, the Hughson-Westlake workflow advanced, threshold detection continued, the Results table was updated, and the real-time audiogram displayed the measured threshold point. The supporting report evidence should include the Java Serial Connection panel, Proteus COMPIM configuration, outgoing command log, incoming `RESPONSE` log, threshold-detected log, audiogram update, and CSV/JSON export evidence.
