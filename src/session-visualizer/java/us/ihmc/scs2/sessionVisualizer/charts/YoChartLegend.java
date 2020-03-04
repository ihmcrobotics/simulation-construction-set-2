package us.ihmc.scs2.sessionVisualizer.charts;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.gsi.chart.XYChartCss;
import de.gsi.chart.legend.Legend;
import de.gsi.chart.renderer.Renderer;
import de.gsi.chart.renderer.spi.utils.DefaultRenderColorScheme;
import de.gsi.chart.utils.FXUtils;
import de.gsi.chart.utils.StyleParser;
import de.gsi.dataset.DataSet;
import de.gsi.dataset.utils.AssertUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import us.ihmc.yoVariables.variable.YoVariable;

/**
 * A chart legend that displays a list of items with symbols in a box
 *
 * @author rstein
 */
public class YoChartLegend extends FlowPane implements Legend
{
   // TODO: transform static integers to styleable property fields
   private static final int GAP = 5;

   // -------------- PRIVATE FIELDS ------------------------------------------

   private final ListChangeListener<YoLegendItem> itemsListener = c ->
   {
      getChildren().setAll(getItems());
      if (isVisible())
      {
         requestLayout();
      }
   };

   // -------------- PUBLIC PROPERTIES ----------------------------------------
   /**
    * The legend items should be laid out vertically in columns rather than horizontally in rows
    */
   private final BooleanProperty vertical = new SimpleBooleanProperty(this, "vertical", false)
   {
      @Override
      protected void invalidated()
      {
         setOrientation(get() ? Orientation.VERTICAL : Orientation.HORIZONTAL);
      }
   };

   /** The legend items to display in this legend */
   private final ObjectProperty<ObservableList<YoLegendItem>> items = new SimpleObjectProperty<ObservableList<YoLegendItem>>(this, "items")
   {
      private ObservableList<YoLegendItem> oldItems = null;

      @Override
      protected void invalidated()
      {
         if (oldItems != null)
         {
            oldItems.removeListener(itemsListener);
         }

         final ObservableList<YoLegendItem> newItems = get();
         if (newItems == null)
         {
            getChildren().clear();
         }
         else
         {
            newItems.addListener(itemsListener);
            getChildren().setAll(newItems);
         }
         oldItems = get();
         if (isVisible())
         {
            requestLayout();
         }
      }
   };

   public YoChartLegend()
   {
      super(GAP, GAP);
      setItems(FXCollections.<YoLegendItem> observableArrayList());
      getStyleClass().setAll("chart-legend");
      setAlignment(Pos.CENTER);
   }

   @Override
   protected double computePrefHeight(final double forWidth)
   {
      // Legend prefHeight is zero if there are no legend items
      return getItems().isEmpty() ? 0 : super.computePrefHeight(forWidth);
   }

   @Override
   protected double computePrefWidth(final double forHeight)
   {
      // Legend prefWidth is zero if there are no legend items
      return getItems().isEmpty() ? 0 : super.computePrefWidth(forHeight);
   }

   public final ObservableList<YoLegendItem> getItems()
   {
      return items.get();
   }

   public YoLegendItem getNewLegendItem(final Renderer renderer, final YoDoubleDataSet series, final int seriesIndex)
   {
      AssertUtils.gtEqThanZero("setLineScheme dsIndex", seriesIndex);
      String defaultStyle = series.getStyle();

      final Color lineColor = StyleParser.getColorPropertyValue(defaultStyle, XYChartCss.DATASET_STROKE_COLOR);
      final Color rawColor = lineColor == null ? DefaultRenderColorScheme.getStrokeColor(seriesIndex) : lineColor;

      return new YoLegendItem(series, rawColor);
   }

   @Override
   public Node getNode()
   {
      return this;
   }

   /*
    * (non-Javadoc)
    * @see de.gsi.chart.legend.Legend#isVertical()
    */
   @Override
   public final boolean isVertical()
   {
      return vertical.get();
   }

   public final ObjectProperty<ObservableList<YoLegendItem>> itemsProperty()
   {
      return items;
   }

   public final void setItems(final ObservableList<YoLegendItem> value)
   {
      itemsProperty().set(value);
   }

   @Override
   public final void setVertical(final boolean value)
   {
      vertical.set(value);
   }

   public void updateValueFields()
   {
      FXUtils.assertJavaFxThread();
      getItems().forEach(YoLegendItem::updateValueField);
   }

   @Override
   public void updateLegend(final List<DataSet> dataSets, final List<Renderer> renderers, final boolean forceUpdate)
   {
      // list of already drawn data sets in the legend
      final List<DataSet> alreadyDrawnDataSets = new ArrayList<>();
      final List<YoLegendItem> legendItems = new ArrayList<>();

      if (forceUpdate)
      {
         this.getItems().clear();
      }

      // process legend items common to all renderer
      int legendItemCount = 0;
      for (int seriesIndex = 0; seriesIndex < dataSets.size(); seriesIndex++)
      {
         final DataSet series = dataSets.get(seriesIndex);
         final String style = series.getStyle();
         final Boolean show = StyleParser.getBooleanPropertyValue(style, XYChartCss.DATASET_SHOW_IN_LEGEND);
         if (show != null && !show.booleanValue())
         {
            continue;
         }

         if (!(alreadyDrawnDataSets.contains(series) || renderers.isEmpty()))
         {
            legendItems.add(getNewLegendItem(renderers.get(0), (YoDoubleDataSet) series, seriesIndex));
            alreadyDrawnDataSets.add(series);
            legendItemCount++;
         }
      }

      // process data sets within the given renderer
      for (final Renderer renderer : renderers)
      {
         if (!renderer.showInLegend())
         {
            continue;
         }
         for (final DataSet series : renderer.getDatasets())
         {
            final String style = series.getStyle();
            final Boolean show = StyleParser.getBooleanPropertyValue(style, XYChartCss.DATASET_SHOW_IN_LEGEND);
            if (show != null && !show.booleanValue())
            {
               continue;
            }

            if (!alreadyDrawnDataSets.contains(series))
            {
               legendItems.add(getNewLegendItem(renderer, (YoDoubleDataSet) series, legendItemCount));
               alreadyDrawnDataSets.add(series);
               legendItemCount++;
            }
         }
      }

      boolean diffLegend = false;
      if (getItems().size() != legendItems.size())
      {
         diffLegend = true;
      }
      else
      {
         final List<String> newItems = legendItems.stream().map(YoLegendItem::getName).collect(Collectors.toList());
         final List<String> oldItems = getItems().stream().map(YoLegendItem::getName).collect(Collectors.toList());

         for (final String item : newItems)
         {
            if (!oldItems.contains(item))
            {
               diffLegend = true;
            }
         }
      }

      if (diffLegend)
      {
         getItems().setAll(legendItems);
      }

      for (YoLegendItem item : getItems())
         item.updateValueField();
   }

   public final BooleanProperty verticalProperty()
   {
      return vertical;
   }

   /** A item to be displayed on a Legend */
   public static class YoLegendItem extends HBox
   {
      private final YoVariable<?> yoVariable;
      private final Label nameLabel = new Label();
      private final Label currentValueLabel = new Label()
      {
         private double previousPrefWidth = -1;

         /**
          * Performs a request to the parent only for growing the size the label. When the text is shorter,
          * only the label is updated not the parent. This is to prevent unnecessary redraws of the chart.
          */
         @Override
         public void requestLayout()
         {
            double newPrefWidth = computePrefWidth(prefHeight(-1));

            if (previousPrefWidth >= newPrefWidth - 1.0e-10)
            {
               setNeedsLayout(true);
               return;
            }

            super.requestLayout();
            previousPrefWidth = newPrefWidth;
         }
      };

      public YoLegendItem(final YoDoubleDataSet series, Paint textFill)
      {
         getChildren().addAll(nameLabel, currentValueLabel);
         getStyleClass().add("chart-legend-item");
         nameLabel.getStyleClass().add("chart-legend-item-name");
         currentValueLabel.getStyleClass().add("chart-legend-item-value");
         setTextFill(textFill);

         yoVariable = series.getYoVariable();
         nameLabel.setText(yoVariable.getName());
      }

      private long lastValueDisplayed = Long.MIN_VALUE;

      public void updateValueField()
      {
         long currentValue = yoVariable.getValueAsLongBits();

         if (currentValue != lastValueDisplayed)
         {
            lastValueDisplayed = currentValue;
            String newValueAsString = LineChartTools.defaultYoVariableValueFormatter(yoVariable);
            currentValueLabel.setText(newValueAsString);
         }
      }

      public final String getName()
      {
         return nameLabel.getText();
      }

      public void setTextFill(Paint paint)
      {
         nameLabel.setTextFill(paint);
         currentValueLabel.setTextFill(paint);
      }
   }
}
