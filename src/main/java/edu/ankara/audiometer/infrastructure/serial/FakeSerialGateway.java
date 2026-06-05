package edu.ankara.audiometer.infrastructure.serial;

import edu.ankara.audiometer.domain.fp.Result;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class FakeSerialGateway implements SerialPortGateway {
    private SerialConnectionStatus status = new SerialConnectionStatus(true, "COM21", "Connected to COM21");
    private Consumer<String> onLine = line -> { };
    private final List<String> sent = new CopyOnWriteArrayList<>();
    private final List<String> sentPayloads = new CopyOnWriteArrayList<>();

    public List<String> sentCommands() {
        return List.copyOf(sent);
    }

    public List<String> sentPayloads() {
        return List.copyOf(sentPayloads);
    }

    @Override
    public List<String> listPorts() {
        return List.of("COM21");
    }

    @Override
    public Result<SerialConnectionStatus, String> connect(String port, int baudRate, Consumer<String> onLine) {
        this.onLine = onLine;
        status = new SerialConnectionStatus(true, port, "Connected to " + port + " at " + baudRate);
        return Result.ok(status);
    }

    @Override
    public Result<Void, String> disconnect() {
        status = new SerialConnectionStatus(true, "COM21", "Connected to COM21");
        return Result.ok(null);
    }

    @Override
    public Result<Void, String> send(String command, String terminator) {
        sent.add(command);
        sentPayloads.add(command + terminator);
        return Result.ok(null);
    }

    public void injectLine(String line) {
        onLine.accept(line);
    }

    @Override
    public SerialConnectionStatus status() {
        return status;
    }
}
