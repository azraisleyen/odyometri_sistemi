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
import java.util.Map;

public final class AudiogramChartPane extends TitledPane {
    private static final Map<Integer, Integer> FREQUENCY_POSITIONS = Map.of(
            250, 0,
            500, 1,
            1000, 2,
            2000, 3,
            4000, 4,
            8000, 5
    );

    private final LineChart<Number, Number> chart;

    public AudiogramChartPane() {
        NumberAxis x = new NumberAxis(0, 5, 1);
        x.setLabel("Frequency (Hz)");
        x.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number value) {
                int position = value.intValue();
                return FREQUENCY_POSITIONS.entrySet().stream()
                        .filter(entry -> entry.getValue() == position)
                        .map(entry -> String.valueOf(entry.getKey()))
                        .findFirst()
                        .orElse("");
            }

            @Override
            public Number fromString(String string) {
                return FREQUENCY_POSITIONS.getOrDefault(Integer.parseInt(string), 0);
            }
        });

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
                    int x = FREQUENCY_POSITIONS.getOrDefault(point.frequency().value(), point.frequency().value());
                    XYChart.Data<Number, Number> data = new XYChart.Data<>(x, -point.thresholdDbHL().value());
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
