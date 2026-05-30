# Serial Protocol

The software uses real jSerialComm through the `JSerialCommGateway` implementation. `FakeSerialGateway` is used for simulation mode and tests. The rest of the application depends only on `SerialPortGateway`.

## Serial Settings

* Baud rate: 9600 by default.
* Data bits: 8.
* Stop bits: 1.
* Parity: none.
* Reading occurs on a background thread so the JavaFX UI thread is not blocked.

## Outgoing Tone Command

```text
TONE;EAR=<RIGHT|LEFT>;FREQ=<Hz>;DB=<dBHL>;DURATION_MS=<ms>
```

Example:

```text
TONE;EAR=RIGHT;FREQ=1000;DB=40;DURATION_MS=1000
```

## Incoming Messages

Required Proteus/Arduino patient-button message:

```text
RESPONSE
```

The parser trims whitespace, removes CR/LF, supports case-insensitive input, safely ignores invalid messages, and supports future messages: `READY`, `ACK`, `BUTTON_DOWN`, `BUTTON_UP`, and `ERROR:<message>`.
