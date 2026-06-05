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

The mode indicator in the Serial Connection panel is read-only because the serial gateway is selected at launch: `runSimulation` uses `SIMULATED-COM` without hardware, while `run` uses real COM ports through jSerialComm.

## Current Procedure

The current educational workflow is **Procedure: Automatic Hughson-Westlake**. A separate Manual Mode is not exposed in the GUI because it is not fully implemented. **Present tone** and **Mark RESPONSE** remain available as manual control buttons inside the automatic Hughson-Westlake workflow; they are not a separate test mode. Manual Mode can be considered future work.

## Ear Modes

The **Ear** ComboBox is connected to backend logic:

* **Right** starts at RIGHT 1000 Hz and emits only RIGHT tone commands.
* **Left** starts at LEFT 1000 Hz and emits only LEFT tone commands.
* **Both** starts with RIGHT, visits 1000/2000/4000/8000/1000 retest/500/250 Hz, then switches to LEFT and completes the same sequence.

Changing ear mode resets the session with the selected ears.

## Running a Test

* **Start test**: starts the workflow by presenting a tone.
* **Present tone**: sends/records the current tone presentation inside the automatic procedure.
* **Mark RESPONSE**: manually applies a heard response inside the automatic procedure.
* **Auto simulate step**: uses the application simulation service to present a tone and apply RESPONSE/NO_RESPONSE from ear-specific thresholds. After completion, the app reports that the session is already complete and tells the user to export or reset.
* **Pause test**: changes phase to `PAUSED` and blocks progression.
* **Resume test**: continues the same paused session.
* **Stop test**: changes phase to `STOPPED`; reset before continuing.
* **Reset test**: clears state, audiogram results, and log.

## Audiogram, Results, and Export

The audiogram updates in real time with connected threshold points. RIGHT is a red `O` marker connected by a red line, and LEFT is a blue `X` marker connected by a blue line. Standard frequencies are displayed with equal audiometry-style spacing while the results table keeps actual Hz and dB HL values.

Flat horizontal audiogram lines are expected when simulated thresholds are constant across frequencies; the software does not add artificial slopes, random thresholds, or selectable demo profiles.

The left panel is scrollable, so **Export Controls** remain reachable on smaller screens. Use **Export CSV** or **Export JSON** to write report evidence under `exports/`; buttons are enabled after at least one threshold exists, and completed sessions show `Results are ready for export`. JSON export is generated with Jackson and includes configuration, thresholds, and presentation history.

When the session reaches `COMPLETED`, Current Test State shows a green completed badge, explains that all selected ears/frequencies have been tested, and displays `Completed thresholds: X / expectedTotal`. Both mode final Results contain 12 unique rows: RIGHT and LEFT each have 1000, 2000, 4000, 8000, 500, and 250 Hz. The 1000 Hz retest is stored in presentation/event history as validation but does not create a duplicate final threshold row.

This GUI is for educational simulation only and must not be used for diagnosis or treatment. It does not claim clinical-device or IEC 60645-1 certification.
