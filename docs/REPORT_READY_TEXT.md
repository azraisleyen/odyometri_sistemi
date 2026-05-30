# Report-Ready Software Engineering Text

## 2.4 Software Design
The software was designed using a functional-core / imperative-shell architecture. Audiometric rules are deterministic pure functions over immutable state. GUI, serial communication, and export are isolated in application and infrastructure layers.

## 2.5 Hughson-Westlake Algorithm
The implementation applies the modified Hughson-Westlake rule: heard responses decrease level by 10 dB and no-response/timeouts increase level by 5 dB. Threshold is detected from valid ascending trials using a configurable 2/3 or 3/5 criterion.

## 2.6 Communication Protocol
The Java program sends deterministic tone commands such as `TONE;EAR=RIGHT;FREQ=1000;DB=40;DURATION_MS=1000`. The hardware returns `RESPONSE` when the patient button is pressed.

## 3.2 Software Tests
Automated tests verify validation, parsing, state immutability, reducer determinism, threshold criteria, exports, and transition invariants.

## 3.3 System-Level Test
Simulation mode exercises the same algorithm and protocol path used by serial mode, allowing end-to-end demonstration without Proteus.

## 3.4 Audiogram Results
Thresholds are stored as audiogram points by ear and frequency. Right ear is rendered as red O and left ear as blue X.

## 4.1 Discussion
This is an academic simulation aligned with audiometry logic and configurable for biomedical validation; it is not a certified clinical device.

## 4.2 Multidisciplinary Collaboration
The serial protocol decouples Java software from Proteus, ESP32-S3, or FPGA button implementations as long as `RESPONSE` is transmitted.
