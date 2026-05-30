# GUI User Guide

## Open Simulation Mode

```powershell
gradle runSimulation
```

A real JavaFX window titled **Odyometre Sistemi Tasarımı ve Testi** opens. Simulation mode does not require Proteus or a COM port.

## Open Serial Mode

```powershell
gradle run
```

Select a COM port, choose baud rate 9600, and press **Connect**. Proteus/Arduino must send `RESPONSE` when the patient button is pressed.

## Running a Test

* **Start test**: starts the workflow by presenting a tone.
* **Present tone**: sends/records the current tone presentation.
* **Mark RESPONSE**: manually applies a heard response in simulation.
* **Auto simulate step**: compares current dB HL to simulated true threshold and applies heard/no-response.
* **Pause test**: changes phase to `PAUSED`.
* **Stop test**: changes phase to `STOPPED`; reset before continuing.
* **Reset test**: clears state, audiogram results, and log.

## Results and Export

The audiogram updates in real time. Right ear is rendered as red `O`; left ear is rendered as blue `X`. Use **Export CSV** or **Export JSON** to write report evidence under `exports/`.
