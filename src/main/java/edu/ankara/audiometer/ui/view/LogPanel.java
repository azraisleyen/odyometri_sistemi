package edu.ankara.audiometer.ui.view;
import javafx.scene.control.*;import java.time.LocalTime;
public final class LogPanel extends TitledPane { private final TextArea area=new TextArea(); public LogPanel(){ setText("Event Log"); setCollapsible(false); area.setEditable(false); area.setPrefRowCount(12); setContent(area);} public void add(String m){ area.appendText("[%s] %s%n".formatted(LocalTime.now().withNano(0),m)); } }
