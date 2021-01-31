package us.ihmc.scs2.sessionVisualizer.jfx.charts;

import java.util.List;
import java.util.stream.Collectors;

import de.gsi.chart.axes.Axis;
import de.gsi.chart.plugins.XValueIndicator;
import de.gsi.chart.plugins.YValueIndicator;
import javafx.scene.shape.Line;
import us.ihmc.scs2.definition.yoChart.ChartDoubleBoundsDefinition;
import us.ihmc.scs2.definition.yoChart.YoChartGroupModelDefinition;
import us.ihmc.scs2.definition.yoChart.YoChartIdentifierDefinition;

public class YoChartTools
{
   public static XValueIndicator readOnlyLineOnlyXValueIndicator(Axis axis, double value, String styleClass)
   {
      XValueIndicator xValueIndicator = new XValueIndicator(axis, value)
      {
         @Override
         public void updateStyleClass()
         {
            line.getStyleClass().add(styleClass);
         }
      };
      // The default implementation allows to drag the indicators with the mouse.
      xValueIndicator.setEditable(false);
      // The default implementation comes with an optional label and a top triangle that we won't use.
      xValueIndicator.getChartChildren().removeIf(node -> node.getClass() != Line.class);
      return xValueIndicator;
   }

   public static YValueIndicator readOnlyLineOnlyYValueIndicator(Axis axis, double value, String styleClass)
   {
      YValueIndicator yValueIndicator = new YValueIndicator(axis, value)
      {
         @Override
         public void updateStyleClass()
         {
            line.getStyleClass().add(styleClass);
         }
      };
      // The default implementation allows to drag the indicators with the mouse.
      yValueIndicator.setEditable(false);
      // The default implementation comes with an optional label and a top triangle that we won't use.
      yValueIndicator.getChartChildren().removeIf(node -> node.getClass() != Line.class);
      return yValueIndicator;
   }

   public static ChartIdentifier toChartIdentifier(YoChartIdentifierDefinition xmlChartIdentifier)
   {
      return new ChartIdentifier(xmlChartIdentifier.getRow(), xmlChartIdentifier.getColumn());
   }

   public static ChartGroupModel toChartIdentifierList(YoChartGroupModelDefinition definition)
   {
      return new ChartGroupModel(definition.getName(),
                                 definition.getChartIdentifiers().stream().map(YoChartTools::toChartIdentifier).collect(Collectors.toList()));
   }

   public static ChartDoubleBounds toChartDoubleBounds(ChartDoubleBoundsDefinition definition)
   {
      if (Double.isNaN(definition.getLower()) && Double.isNaN(definition.getUpper()))
         return null;
      else
         return new ChartDoubleBounds(definition.getLower(), definition.getUpper());
   }

   public static List<YoChartGroupModelDefinition> toYoChartGroupModelDefinitions(List<ChartGroupModel> chartGroupModels)
   {
      return chartGroupModels.stream().map(YoChartTools::toYoChartGroupModelDefinition).collect(Collectors.toList());
   }

   public static YoChartGroupModelDefinition toYoChartGroupModelDefinition(ChartGroupModel chartGroupModel)
   {
      YoChartGroupModelDefinition definition = new YoChartGroupModelDefinition();
      definition.setName(chartGroupModel.getName());
      definition.setChartIdentifiers(toYoChartIdentifierDefinitions(chartGroupModel.getChartIdentifiers()));
      return definition;
   }

   public static List<YoChartIdentifierDefinition> toYoChartIdentifierDefinitions(List<ChartIdentifier> chartIdentifiers)
   {
      return chartIdentifiers.stream().map(YoChartTools::toYoChartIdentifierDefinition).collect(Collectors.toList());
   }

   public static YoChartIdentifierDefinition toYoChartIdentifierDefinition(ChartIdentifier chartIdentifier)
   {
      if (chartIdentifier == null)
         return null;
      return new YoChartIdentifierDefinition(chartIdentifier.getRow(), chartIdentifier.getColumn());
   }

   public static ChartDoubleBoundsDefinition toChartDoubleBoundsDefinition(ChartDoubleBounds bounds)
   {
      if (bounds == null)
         return new ChartDoubleBoundsDefinition(Double.NaN, Double.NaN);
      else
         return new ChartDoubleBoundsDefinition(bounds.getLower(), bounds.getUpper());
   }
}
