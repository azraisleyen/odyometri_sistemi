package edu.ankara.audiometer.infrastructure.serial;

import edu.ankara.audiometer.domain.fp.Result;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class FakeSerialGateway implements SerialPortGateway {
    private SerialConnectionStatus status = new SerialConnectionStatus(true, "SIMULATED-COM", "Simulation mode connected");
    private Consumer<String> onLine = line -> { };
    private final List<String> sent = new CopyOnWriteArrayList<>();

    public List<String> sentCommands() {
        return List.copyOf(sent);
    }

    @Override
    public List<String> listPorts() {
        return List.of("SIMULATED-COM");
    }

    @Override
    public Result<SerialConnectionStatus, String> connect(String port, int baudRate, Consumer<String> onLine) {
        this.onLine = onLine;
        status = new SerialConnectionStatus(true, port, "Simulation connected at " + baudRate);
        return Result.ok(status);
    }

    @Override
    public Result<Void, String> disconnect() {
        status = new SerialConnectionStatus(true, "SIMULATED-COM", "Simulation mode connected");
        return Result.ok(null);
    }

    @Override
    public Result<Void, String> send(String command) {
        sent.add(command);
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
