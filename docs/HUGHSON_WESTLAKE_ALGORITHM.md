# Hughson-Westlake Algorithm

The implementation models a configurable modified Hughson-Westlake procedure for educational audiometry simulation.

Defaults loaded from Java fallback and mirrored by `config/audiometry-config.json`:

* Frequency range: 250 Hz to 8000 Hz.
* Configured clinical order: 1000, 2000, 4000, 8000, 1000 retest, 500, 250 Hz.
* Runtime duplicate-safe educational progression: 1000, 2000, 4000, 8000, 500, 250 Hz.
* Optional inter-octaves: 750, 1500, 3000, 6000 Hz.
* Start intensity: 40 dB HL.
* Intensity range: -10 to 120 dB HL.
* Heard response: decrease 10 dB.
* No response / timeout: increase 5 dB.
* Default threshold criterion: `TWO_OUT_OF_THREE_ASCENDING`.

Only valid ascending trials are counted for threshold decisions. The alternative `THREE_OUT_OF_FIVE_ASCENDING` criterion is also supported. The duplicate 1000 Hz retest remains documented/configurable, but runtime threshold progression is de-duplicated when `allowRetest=false`, preventing the previous `1000 -> 2000 -> ... -> 1000 -> 2000` loop and ensuring 500 Hz, 250 Hz, RIGHT-to-LEFT transition, and `COMPLETED` are reachable.
