package us.ihmc.scs2.sessionVisualizer.jfx.charts;

import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;

public class DynamicChartLegendItem extends HBox
{
   private final Label seriesNameLabel = new Label();
   private final Label currentValueLabel = new Label();

   public DynamicChartLegendItem()
   {
      getChildren().addAll(seriesNameLabel, currentValueLabel);
      getStyleClass().add("chart-legend-item");
      seriesNameLabel.getStyleClass().add("chart-legend-item-name");
      currentValueLabel.getStyleClass().add("chart-legend-item-value");
   }

   public void setTextFill(Paint paint)
   {
      seriesNameLabel.setTextFill(paint);
      currentValueLabel.setTextFill(paint);
   }

   public StringProperty seriesNameProperty()
   {
      return seriesNameLabel.textProperty();
   }

   public StringProperty currentValueProperty()
   {
      return currentValueLabel.textProperty();
   }
}