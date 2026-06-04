package edu.ankara.audiometer.infrastructure.serial;

import edu.ankara.audiometer.domain.fp.Result;

import java.util.List;
import java.util.function.Consumer;

public interface SerialPortGateway extends AutoCloseable {
    List<String> listPorts();

    Result<SerialConnectionStatus, String> connect(String port, int baudRate, Consumer<String> onLine);

    Result<Void, String> disconnect();

    default Result<Void, String> send(String command) {
        return send(command, "\n");
    }

    Result<Void, String> send(String command, String terminator);

    SerialConnectionStatus status();

    @Override
    default void close() {
        disconnect();
    }
}
