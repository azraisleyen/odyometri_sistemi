package edu.ankara.audiometer.ui.view;

import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;

import java.time.LocalTime;

public final class LogPanel extends TitledPane {
    private final TextArea area = new TextArea();

    public LogPanel() {
        setText("Event Log");
        setCollapsible(false);
        area.setEditable(false);
        area.setPrefRowCount(12);
        setContent(area);
    }

    public void add(String message) {
        area.appendText("[%s] %s%n".formatted(LocalTime.now().withNano(0), message));
    }

    public void clear() {
        area.clear();
    }
}
