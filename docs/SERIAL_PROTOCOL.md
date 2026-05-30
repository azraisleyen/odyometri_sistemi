# Serial Protocol

Outgoing command:

`TONE;EAR=<RIGHT|LEFT>;FREQ=<Hz>;DB=<dBHL>;DURATION_MS=<ms>`

Example: `TONE;EAR=RIGHT;FREQ=1000;DB=40;DURATION_MS=1000`.

Incoming messages are line based. Required message: `RESPONSE`. Whitespace and CR/LF are sanitized; case-insensitive parsing is configurable. Future messages `READY`, `ACK`, `BUTTON_DOWN`, `BUTTON_UP`, and `ERROR:<message>` are parsed safely.
