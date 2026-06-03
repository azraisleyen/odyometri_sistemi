package edu.ankara.audiometer.ui.view;

import edu.ankara.audiometer.domain.model.Audiogram;
import edu.ankara.audiometer.domain.model.Ear;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

import java.util.Comparator;
import java.util.List;

public final class AudiogramChartPane extends TitledPane {
    public static final String RIGHT_SERIES_NAME = "RIGHT red O";
    public static final String LEFT_SERIES_NAME = "LEFT blue X";
    private static final List<Integer> STANDARD_FREQUENCIES = List.of(250, 500, 1000, 2000, 4000, 8000);

    private final LineChart<Number, Number> chart;

    public AudiogramChartPane() {
        NumberAxis x = new NumberAxis(0, STANDARD_FREQUENCIES.size() - 1, 1);
        x.setLabel("Frequency (Hz)");
        x.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number value) {
                int position = value.intValue();
                return position >= 0 && position < STANDARD_FREQUENCIES.size()
                        ? String.valueOf(STANDARD_FREQUENCIES.get(position))
                        : "";
            }

            @Override
            public Number fromString(String string) {
                return positionForFrequency(Integer.parseInt(string));
            }
        });

        NumberAxis y = new NumberAxis(-120, 10, 10);
        y.setLabel("Hearing Level (dB HL)");
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
        chart.setTitle("Real-Time Audiogram");
        chart.setLegendVisible(false);
        chart.setCreateSymbols(true);
        chart.setHorizontalGridLinesVisible(true);
        chart.setVerticalGridLinesVisible(true);

        VBox content = new VBox(8, chart, customLegend());
        content.getStyleClass().add("audiogram-content");
        setText("Audiogram Chart");
        setCollapsible(false);
        setContent(content);
    }

    public static List<Integer> standardFrequencies() {
        return STANDARD_FREQUENCIES;
    }

    public static int positionForFrequency(int frequencyHz) {
        int index = STANDARD_FREQUENCIES.indexOf(frequencyHz);
        return index >= 0 ? index : frequencyHz;
    }

    public void update(Audiogram audiogram) {
        chart.getData().clear();
        addSeries(RIGHT_SERIES_NAME, Ear.RIGHT, audiogram);
        addSeries(LEFT_SERIES_NAME, Ear.LEFT, audiogram);
    }

    private void addSeries(String name, Ear ear, Audiogram audiogram) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(name);
        audiogram.points().stream()
                .filter(point -> point.ear() == ear)
                .sorted(Comparator.comparingInt(point -> positionForFrequency(point.frequency().value())))
                .forEach(point -> {
                    int x = positionForFrequency(point.frequency().value());
                    XYChart.Data<Number, Number> data = new XYChart.Data<>(x, -point.thresholdDbHL().value());
                    data.setNode(markerFor(ear));
                    series.getData().add(data);
                });
        chart.getData().add(series);
        applySeriesStyle(series, ear);
    }

    private static StackPane markerFor(Ear ear) {
        Text symbol = new Text(ear.symbol());
        symbol.getStyleClass().add(ear == Ear.RIGHT ? "right-ear-symbol" : "left-ear-symbol");
        StackPane marker = new StackPane(symbol);
        marker.setAlignment(Pos.CENTER);
        marker.setMouseTransparent(true);
        marker.getStyleClass().addAll("audiogram-marker", ear == Ear.RIGHT ? "right-ear-marker" : "left-ear-marker");
        return marker;
    }

    private static Node customLegend() {
        HBox legend = new HBox(18, legendItem(Ear.RIGHT, RIGHT_SERIES_NAME), legendItem(Ear.LEFT, LEFT_SERIES_NAME));
        legend.setAlignment(Pos.CENTER);
        legend.getStyleClass().add("audiogram-legend");
        return legend;
    }

    private static Node legendItem(Ear ear, String labelText) {
        StackPane marker = markerFor(ear);
        marker.getStyleClass().add("legend-marker");
        Label label = new Label(labelText);
        HBox item = new HBox(6, marker, label);
        item.setAlignment(Pos.CENTER);
        item.getStyleClass().add("audiogram-legend-item");
        return item;
    }

    private static void applySeriesStyle(XYChart.Series<Number, Number> series, Ear ear) {
        String styleClass = ear == Ear.RIGHT ? "right-ear-series" : "left-ear-series";
        if (series.getNode() != null) {
            series.getNode().getStyleClass().add(styleClass);
        }
        series.nodeProperty().addListener((observable, oldNode, newNode) -> {
            if (newNode != null) {
                newNode.getStyleClass().add(styleClass);
            }
        });
    }
}
