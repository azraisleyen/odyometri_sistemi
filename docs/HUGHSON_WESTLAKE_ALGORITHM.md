# Hughson-Westlake Algorithm

The implementation models a configurable modified Hughson-Westlake procedure for educational audiometry simulation.

Defaults:

* Frequency range: 250 Hz to 8000 Hz.
* Clinical order: 1000, 2000, 4000, 8000, 1000 retest, 500, 250 Hz.
* Optional inter-octaves: 750, 1500, 3000, 6000 Hz.
* Start intensity: 40 dB HL.
* Intensity range: -10 to 120 dB HL.
* Heard response: decrease 10 dB.
* No response / timeout: increase 5 dB.
* Default threshold criterion: `TWO_OUT_OF_THREE_ASCENDING`.

Only valid ascending trials are counted for threshold decisions. The alternative `THREE_OUT_OF_FIVE_ASCENDING` criterion is also supported through configuration. Biomedical teams can update values in `domain/config` and `config/audiometry-config.json` without changing GUI code.
