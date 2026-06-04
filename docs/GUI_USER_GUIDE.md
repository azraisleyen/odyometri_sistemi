# GUI User Guide

## Open Simulation Mode

```powershell
gradle --no-daemon runSimulation
```

A real English JavaFX window titled **Audiometer System Design and Testing** opens. Simulation mode does not require Proteus or a COM port.

## Open Serial Mode

```powershell
gradle --no-daemon run
```

Select a COM port, choose the configured baud rate, and press **Connect**. Proteus/Arduino must send `RESPONSE` when the patient button is pressed. The command body logged in the event log remains `TONE;EAR=...`; the configured command terminator is appended only to the bytes sent to the serial port.

## Current Procedure

The current educational workflow is **Procedure: Automatic Hughson-Westlake**. A separate Manual Mode is not exposed in the GUI because it is not fully implemented. **Present tone** and **Mark RESPONSE** remain available as manual control buttons inside the automatic Hughson-Westlake workflow; they are not a separate test mode. Manual Mode can be considered future work.

## Ear Modes

The **Ear** ComboBox is connected to backend logic:

* **Right** starts at RIGHT 1000 Hz and emits only RIGHT tone commands.
* **Left** starts at LEFT 1000 Hz and emits only LEFT tone commands.
* **Both** starts with RIGHT, completes 1000/2000/4000/8000/500/250 Hz, then switches to LEFT and completes the same frequencies.

Changing ear mode resets the session with the selected ears.

## Running a Test

* **Start test**: starts the workflow by presenting a tone.
* **Present tone**: sends/records the current tone presentation inside the automatic procedure.
* **Mark RESPONSE**: manually applies a heard response inside the automatic procedure.
* **Auto simulate step**: uses the application simulation service to present a tone and apply RESPONSE/NO_RESPONSE from ear-specific thresholds.
* **Pause test**: changes phase to `PAUSED` and blocks progression.
* **Resume test**: continues the same paused session.
* **Stop test**: changes phase to `STOPPED`; reset before continuing.
* **Reset test**: clears state, audiogram results, and log.

## Audiogram, Results, and Export

The audiogram updates in real time with connected threshold points. RIGHT is a red `O` marker connected by a red line, and LEFT is a blue `X` marker connected by a blue line. Standard frequencies are displayed with equal audiometry-style spacing while the results table keeps actual Hz and dB HL values.

Flat horizontal audiogram lines are expected when simulated thresholds are constant across frequencies; the software does not add artificial slopes or random visualization offsets.

Use **Export CSV** or **Export JSON** to write report evidence under `exports/`. JSON export is generated with Jackson and includes configuration, thresholds, and presentation history.

This GUI is for educational simulation only and must not be used for diagnosis or treatment. It does not claim clinical-device or IEC 60645-1 certification.
