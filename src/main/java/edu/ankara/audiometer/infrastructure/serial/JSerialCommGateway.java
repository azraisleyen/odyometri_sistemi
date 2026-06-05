package edu.ankara.audiometer.infrastructure.serial;

import com.fazecast.jSerialComm.SerialPort;
import edu.ankara.audiometer.domain.fp.Result;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public final class JSerialCommGateway implements SerialPortGateway {
    private SerialPort port;
    private ExecutorService executor;
    private volatile boolean running;
    private SerialConnectionStatus status = SerialConnectionStatus.disconnected();

    @Override
    public List<String> listPorts() {
        return Arrays.stream(SerialPort.getCommPorts())
                .map(SerialPort::getSystemPortName)
                .toList();
    }

    @Override
    public Result<SerialConnectionStatus, String> connect(String portName, int baudRate, Consumer<String> onLine) {
        try {
            port = SerialPort.getCommPort(portName);
            port.setComPortParameters(baudRate, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
            port.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 250, 0);
            if (!port.openPort()) {
                return Result.err("Cannot open " + portName);
            }

            running = true;
            executor = Executors.newSingleThreadExecutor(runnable -> new Thread(runnable, "serial-reader"));
            executor.submit(() -> readLoop(onLine));
            status = new SerialConnectionStatus(true, portName, "Connected");
            return Result.ok(status);
        } catch (Exception exception) {
            return Result.err(exception.getMessage());
        }
    }

    private void readLoop(Consumer<String> onLine) {
        StringBuilder buffer = new StringBuilder();

        try (var input = port.getInputStream()) {
            while (running) {
                int value = input.read();

                if (value < 0) {
                    continue;
                }

                char ch = (char) value;

                if (ch == '\n' || ch == '\r') {
                    if (buffer.length() > 0) {
                        String message = buffer.toString().trim();
                        buffer.setLength(0);

                        if (!message.isBlank()) {
                            onLine.accept(message);
                        }
                    }
                } else {
                    buffer.append(ch);
                }
            }
        } catch (IOException ignored) {
            if (running) {
                status = new SerialConnectionStatus(false, status.portName(), "Read error");
            }
        }
    }

    @Override
    public Result<Void, String> disconnect() {
        running = false;
        if (executor != null) {
            executor.shutdownNow();
        }
        if (port != null) {
            port.closePort();
        }
        status = SerialConnectionStatus.disconnected();
        return Result.ok(null);
    }

    @Override
    public Result<Void, String> send(String command, String terminator) {
        if (port == null || !port.isOpen()) {
            return Result.err("Serial port not connected");
        }
        try {
            port.getOutputStream().write((command + terminator).getBytes(StandardCharsets.UTF_8));
            port.getOutputStream().flush();
            return Result.ok(null);
        } catch (IOException exception) {
            return Result.err(exception.getMessage());
        }
    }

    @Override
    public SerialConnectionStatus status() {
        return status;
    }
}
