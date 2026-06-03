# GUI User Guide

## Open Simulation Mode

```powershell
gradle --no-daemon runSimulation
```

A real JavaFX window titled **Odyometre Sistemi Tasarımı ve Testi** opens. Simulation mode does not require Proteus or a COM port.

## Open Serial Mode

```powershell
gradle --no-daemon run
```

Select a COM port, choose the configured baud rate, and press **Connect**. Proteus/Arduino must send `RESPONSE` when the patient button is pressed. The command body logged in the event log remains `TONE;EAR=...`; the configured command terminator is appended only to the bytes sent to the serial port.

## Ear Modes

The **Ear** ComboBox is connected to backend logic:

* **Right** starts at RIGHT 1000 Hz and emits only RIGHT tone commands.
* **Left** starts at LEFT 1000 Hz and emits only LEFT tone commands.
* **Both** starts with RIGHT, completes 1000/2000/4000/8000/500/250 Hz, then switches to LEFT and completes the same frequencies.

Changing ear mode resets the session with the selected ears.

## Running a Test

* **Start test**: starts the workflow by presenting a tone.
* **Present tone**: sends/records the current tone presentation.
* **Mark RESPONSE**: manually applies a heard response in simulation.
* **Auto simulate step**: uses the application simulation service to present a tone and apply RESPONSE/NO_RESPONSE from ear-specific thresholds.
* **Pause test**: changes phase to `PAUSED` and blocks progression.
* **Resume test**: continues the same paused session.
* **Stop test**: changes phase to `STOPPED`; reset before continuing.
* **Reset test**: clears state, audiogram results, and log.

## Results and Export

The audiogram updates in real time. Right ear is rendered as red `O`; left ear is rendered as blue `X`. Standard frequencies are displayed with equal audiometry-style spacing while the results table keeps real Hz and dB HL values.

Use **Export CSV** or **Export JSON** to write report evidence under `exports/`. JSON export is generated with Jackson and includes configuration, thresholds, and presentation history.

This GUI is for educational simulation only and must not be used for diagnosis or treatment.
