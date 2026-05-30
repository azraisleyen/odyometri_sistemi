package edu.ankara.audiometer.ui.view;

import edu.ankara.audiometer.domain.model.Audiogram;
import edu.ankara.audiometer.domain.model.Ear;
import javafx.application.Platform;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.util.StringConverter;

import java.util.Comparator;

public final class AudiogramChartPane extends TitledPane {
    private final LineChart<Number, Number> chart;

    public AudiogramChartPane() {
        NumberAxis x = new NumberAxis(125, 9000, 1000);
        x.setLabel("Frequency (Hz)");
        NumberAxis y = new NumberAxis(-120, 10, 10);
        y.setLabel("dB HL (lower values higher)");
        y.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number value) {
                return String.valueOf(-value.intValue());
            }

            @Override
            public Number fromString(String string) {
                return -Integer.parseInt(string);
            }
        });
        chart = new LineChart<>(x, y);
        chart.setTitle("Real-Time Audiogram (Right red O, Left blue X)");
        chart.setLegendVisible(true);
        chart.setCreateSymbols(true);
        chart.setHorizontalGridLinesVisible(true);
        chart.setVerticalGridLinesVisible(true);
        setText("Audiogram Chart");
        setCollapsible(false);
        setContent(chart);
    }

    public void update(Audiogram audiogram) {
        chart.getData().clear();
        addSeries("RIGHT red O", Ear.RIGHT, audiogram);
        addSeries("LEFT blue X", Ear.LEFT, audiogram);
    }

    private void addSeries(String name, Ear ear, Audiogram audiogram) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(name);
        audiogram.points().stream()
                .filter(point -> point.ear() == ear)
                .sorted(Comparator.comparing(point -> point.frequency().value()))
                .forEach(point -> {
                    XYChart.Data<Number, Number> data = new XYChart.Data<>(point.frequency().value(), -point.thresholdDbHL().value());
                    series.getData().add(data);
                    Platform.runLater(() -> {
                        Label symbol = new Label(ear.symbol());
                        symbol.getStyleClass().add(ear == Ear.RIGHT ? "right-ear-symbol" : "left-ear-symbol");
                        data.setNode(symbol);
                    });
                });
        chart.getData().add(series);
    }
}
