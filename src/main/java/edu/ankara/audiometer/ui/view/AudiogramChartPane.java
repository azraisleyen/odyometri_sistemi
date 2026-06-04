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

public final class AudiogramChartPane extends TitledPane {
    private static final String LINE_STYLE_TEMPLATE = "-fx-stroke: %s; -fx-stroke-width: 2.5px;";
    private static final String MARKER_STYLE = """
            -fx-background-color: transparent;
            -fx-background-insets: 0;
            -fx-background-radius: 0;
            -fx-border-color: transparent;
            -fx-border-width: 0;
            -fx-padding: 0;
            """;

    private final LineChart<Number, Number> chart;

    public AudiogramChartPane() {
        NumberAxis x = new NumberAxis(0, AudiogramAxisMapping.standardFrequencies().size() - 1, 1);
        x.setLabel("Frequency (Hz)");
        x.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number value) {
                int position = value.intValue();
                return position >= 0 && position < AudiogramAxisMapping.standardFrequencies().size()
                        ? String.valueOf(AudiogramAxisMapping.standardFrequencies().get(position))
                        : "";
            }

            @Override
            public Number fromString(String string) {
                return AudiogramAxisMapping.positionForFrequency(Integer.parseInt(string));
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

    public void update(Audiogram audiogram) {
        chart.getData().clear();
        addSeries(AudiogramNotation.RIGHT_SERIES_NAME, Ear.RIGHT, audiogram);
        addSeries(AudiogramNotation.LEFT_SERIES_NAME, Ear.LEFT, audiogram);
    }

    private void addSeries(String name, Ear ear, Audiogram audiogram) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(name);
        audiogram.points().stream()
                .filter(point -> point.ear() == ear)
                .sorted(Comparator.comparingInt(point -> AudiogramAxisMapping.positionForFrequency(point.frequency().value())))
                .forEach(point -> {
                    int x = AudiogramAxisMapping.positionForFrequency(point.frequency().value());
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
        symbol.setStyle("-fx-fill: %s; -fx-font-weight: bold; -fx-font-size: 18px;".formatted(AudiogramNotation.color(ear)));

        StackPane marker = new StackPane(symbol);
        marker.setAlignment(Pos.CENTER);
        marker.setMouseTransparent(true);
        marker.setMinSize(22, 22);
        marker.setPrefSize(22, 22);
        marker.setMaxSize(22, 22);
        marker.setStyle(MARKER_STYLE);
        marker.getStyleClass().addAll("audiogram-marker", ear == Ear.RIGHT ? "right-ear-marker" : "left-ear-marker");
        return marker;
    }

    private static Node customLegend() {
        HBox legend = new HBox(
                18,
                legendItem(Ear.RIGHT, AudiogramNotation.RIGHT_SERIES_NAME),
                legendItem(Ear.LEFT, AudiogramNotation.LEFT_SERIES_NAME)
        );
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
        String inlineStyle = LINE_STYLE_TEMPLATE.formatted(AudiogramNotation.color(ear));
        applyLineStyle(series.getNode(), styleClass, inlineStyle);
        series.nodeProperty().addListener(
                (observable, oldNode, newNode) -> applyLineStyle(newNode, styleClass, inlineStyle)
        );
    }

    private static void applyLineStyle(Node node, String styleClass, String inlineStyle) {
        if (node == null) {
            return;
        }
        if (!node.getStyleClass().contains(styleClass)) {
            node.getStyleClass().add(styleClass);
        }
        node.setStyle(inlineStyle);
    }
}
