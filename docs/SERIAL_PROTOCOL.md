# Serial Protocol

The software uses real jSerialComm through the `JSerialCommGateway` implementation. `FakeSerialGateway` is used for simulation mode and tests. The rest of the application depends only on `SerialPortGateway`.

## Serial Settings

* Baud rate: 9600 by default, loaded from `config/audiometry-config.json` when present.
* Data bits: 8.
* Stop bits: 1.
* Parity: none.
* Command terminator: `\n` by default, configurable with `serial.commandTerminator` or root-level `commandTerminator`.
* Reading occurs on a background thread so the JavaFX UI thread is not blocked.

## Outgoing Tone Command

The logged command body remains terminator-free and deterministic:

```text
TONE;EAR=<RIGHT|LEFT>;FREQ=<Hz>;DB=<dBHL>;DURATION_MS=<ms>
```

Example:

```text
TONE;EAR=RIGHT;FREQ=1000;DB=40;DURATION_MS=1000
```

The actual bytes written to the serial port append the configured command terminator. This keeps GUI/event-log output clean while allowing Proteus/Arduino serial line-ending configuration.

## Incoming Messages

Required Proteus/Arduino patient-button message:

```text
RESPONSE
```

The parser trims whitespace, removes CR/LF, supports case-insensitive input, safely ignores invalid messages, and supports future messages: `READY`, `ACK`, `BUTTON_DOWN`, `BUTTON_UP`, and `ERROR:<message>`.
