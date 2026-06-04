# Hughson-Westlake Algorithm

The implementation models a configurable modified Hughson-Westlake procedure for educational audiometry simulation.

Defaults loaded from Java fallback and mirrored by `config/audiometry-config.json`:

* Frequency range: 250 Hz to 8000 Hz.
* Configured clinical order: 1000, 2000, 4000, 8000, 1000 retest, 500, 250 Hz.
* Runtime Hughson-Westlake sequence: 1000, 2000, 4000, 8000, 1000 retest, 500, 250 Hz.
* Optional inter-octaves: 750, 1500, 3000, 6000 Hz.
* Start intensity: 40 dB HL.
* Intensity range: -10 to 120 dB HL.
* Heard response: decrease 10 dB.
* No response / timeout: increase 5 dB.
* Default threshold criterion: `TWO_OUT_OF_THREE_ASCENDING`.

## 1000 Hz Retest Policy

The JSON configuration preserves the duplicate 1000 Hz retest in `clinicalOrderHz`, and the runtime now uses index-based progression through that order. Therefore each active ear really visits `1000 -> 2000 -> 4000 -> 8000 -> 1000 retest -> 500 -> 250` without using value-based lookup that could loop at 1000 Hz.

The second 1000 Hz occurrence is a validation step: it generates tone presentations and is stored in presentation/event history. If the retest threshold matches the first 1000 Hz threshold, the Event Log records a validation message. If it differs, the Event Log records a warning. In both cases, the final audiogram and Results table suppress duplicate rows and keep one unique threshold per ear/frequency. The report may state: “1000 Hz retest is implemented as a validation step in the Hughson-Westlake test flow, while the final audiogram keeps one unique threshold per ear/frequency.” This remains an educational simulation and does **not** claim certified clinical retest or IEC 60645-1 compliance.

## Threshold Criterion Semantics

Only valid ascending trials for the current ear and frequency are counted. For each intensity level, the detector evaluates the most recent criterion window:

* `TWO_OUT_OF_THREE_ASCENDING`: at least 2 heard responses in the most recent 3 relevant ascending trials.
* `THREE_OUT_OF_FIVE_ASCENDING`: at least 3 heard responses in the most recent 5 relevant ascending trials.

Older trials outside the recent window do not by themselves force a threshold decision. This stricter deterministic rule is still implemented for an educational simulation, not for certified diagnosis or treatment.
