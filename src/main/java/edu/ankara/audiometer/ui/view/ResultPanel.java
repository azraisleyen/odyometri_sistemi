package edu.ankara.audiometer.ui.view;

import edu.ankara.audiometer.domain.model.Audiogram;
import edu.ankara.audiometer.domain.model.AudiogramPoint;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

public final class ResultPanel extends TitledPane {
    private final TableView<AudiogramPoint> table = new TableView<>();
    private final Label count = new Label("Completed thresholds: 0 / 0");

    public ResultPanel() {
        setText("Results / Completed Thresholds");
        setCollapsible(false);
        table.setPrefHeight(360);
        table.setMinHeight(300);

        TableColumn<AudiogramPoint, String> ear = new TableColumn<>("Ear");
        ear.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().ear().name()));
        TableColumn<AudiogramPoint, Number> frequency = new TableColumn<>("Hz");
        frequency.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().frequency().value()));
        TableColumn<AudiogramPoint, Number> db = new TableColumn<>("dB HL");
        db.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().thresholdDbHL().value()));
        TableColumn<AudiogramPoint, String> notes = new TableColumn<>("Notes");
        notes.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().notes()));

        table.getColumns().addAll(ear, frequency, db, notes);
        count.getStyleClass().add("result-count");
        setContent(new VBox(6, count, table));
    }

    public void update(Audiogram audiogram) {
        update(audiogram, audiogram.points().size());
    }

    public void update(Audiogram audiogram, int expectedTotal) {
        table.setItems(FXCollections.observableArrayList(audiogram.points()));
        count.setText("Completed thresholds: " + audiogram.points().size() + " / " + expectedTotal);
    }
}
