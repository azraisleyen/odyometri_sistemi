package javafx.geometry; public record Insets(double top,double right,double bottom,double left){ public Insets(double all){this(all,all,all,all);} }
