package us.ihmc.scs2.sessionVisualizer.charts;

import java.util.List;
import java.util.stream.Collectors;

import us.ihmc.scs2.definition.yoChart.ChartDoubleBoundsDefinition;
import us.ihmc.scs2.definition.yoChart.YoChartGroupModelDefinition;
import us.ihmc.scs2.definition.yoChart.YoChartIdentifierDefinition;

public class YoChartTools
{
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
