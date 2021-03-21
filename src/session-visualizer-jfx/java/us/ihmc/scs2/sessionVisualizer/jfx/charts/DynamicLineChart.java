package us.ihmc.scs2.sessionVisualizer.jfx.charts;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.chart.InvisibleNumberAxis;
import javafx.scene.chart.XYChart.Data;
import us.ihmc.javaFXExtensions.chart.DynamicXYChart;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.ChartRenderManager;

public class DynamicLineChart extends DynamicXYChart
{
   public enum ChartStyle
   {
      RAW, NORMALIZED
   };

   private final ObjectProperty<ChartStyle> chartStyleProperty = new SimpleObjectProperty<>(this, "chartStyle", ChartStyle.RAW);
   private final ObservableList<NumberSeriesLayer> seriesLayers = FXCollections.observableArrayList();
   private final ChangeListener<Object> chartUpdaterListener = (o, oldValue, newValue) -> requestChartLayout();
   private final ChangeListener<Object> markerUpdaterListener = (o, oldValue, newValue) -> updateMarkers();
   private final Map<Data<Number, Number>, ChartMarker> markers = new LinkedHashMap<>();
   private final DynamicChartLegend legend = new DynamicChartLegend();

   private final InvisibleNumberAxis xAxis;
   private final InvisibleNumberAxis yAxis;

   private final Executor backgroundExecutor;
   private final ChartRenderManager chartRenderManager;

   public DynamicLineChart(InvisibleNumberAxis xAxis, InvisibleNumberAxis yAxis, Executor backgroundExecutor, ChartRenderManager chartRenderManager)
   {
      super(xAxis, yAxis);

      this.xAxis = xAxis;
      this.chartRenderManager = chartRenderManager;
      if (xAxis.getSide() == null)
         xAxis.setSide(Side.BOTTOM);
      this.yAxis = yAxis;
      if (yAxis.getSide() == null)
         yAxis.setSide(Side.LEFT);

      this.backgroundExecutor = backgroundExecutor;

      xAxis.autoRangingProperty().addListener((ov, t, t1) -> updateAxisRange());
      yAxis.autoRangingProperty().addListener((ov, t, t1) -> updateAxisRange());

      chartStyleProperty.addListener(chartUpdaterListener);

      setLegend(legend);
   }

   public void addSeries(NumberSeries series)
   {
      int seriesIndex = seriesLayers.size();

      series.negatedProperty().addListener(chartUpdaterListener);
      series.customYBoundsProperty().addListener(chartUpdaterListener);
      series.dirtyProperty().addListener(chartUpdaterListener);
      NumberSeriesLayer layer = new NumberSeriesLayer(xAxis, yAxis, series, backgroundExecutor, chartRenderManager);
      layer.chartStyleProperty().bind(chartStyleProperty);
      setSeriesDefaultStyleClass(layer, seriesIndex);
      seriesLayers.add(layer);
      legend.getItems().add(layer.getLegendNode());
      plotContent.getChildren().add(layer);
      chartUpdaterListener.changed(null, null, null);
   }

   public void removeSeries(NumberSeries series)
   {
      Optional<NumberSeriesLayer> containingLayer = seriesLayers.stream().filter(layer -> layer.getNumberSeries() == series).findFirst();

      if (containingLayer.isPresent())
      {
         int indexOf = seriesLayers.indexOf(containingLayer.get());
         seriesLayers.remove(indexOf);
         series.negatedProperty().removeListener(chartUpdaterListener);
         series.customYBoundsProperty().removeListener(chartUpdaterListener);
         series.dirtyProperty().removeListener(chartUpdaterListener);
         containingLayer.get().chartStyleProperty().unbind();
         plotContent.getChildren().remove(containingLayer.get());
         legend.getItems().remove(containingLayer.get().getLegendNode());

         for (int i = indexOf; i < seriesLayers.size(); i++)
            setSeriesDefaultStyleClass(seriesLayers.get(i), i);

         chartUpdaterListener.changed(null, null, null);
      }
   }

   public ChartMarker addMarker(Data<Number, Number> markerCoordinates)
   {
      Objects.requireNonNull(markerCoordinates, "The marker must not be null.");
      if (markers.containsKey(markerCoordinates))
         return markers.get(markerCoordinates);

      ChartMarker marker = new ChartMarker(markerCoordinates);
      plotContent.getChildren().add(marker.getNode());
      markers.put(markerCoordinates, marker);
      marker.addListener(markerUpdaterListener);

      markerUpdaterListener.changed(null, null, null);
      return marker;
   }

   public void removeMarker(Data<Number, Number> markerCoordinates)
   {
      Objects.requireNonNull(markerCoordinates, "The marker must not be null.");

      ChartMarker marker = markers.remove(markerCoordinates);

      if (marker.getNode() != null)
      {
         plotContent.getChildren().remove(marker.getNode());
         marker.destroy();
      }
   }

   private void updateSeriesList(double top, double left, double width, double height)
   {
      for (NumberSeriesLayer seriesLayer : seriesLayers)
      {
         seriesLayer.scheduleRender();
      }
   }

   @Override
   protected void layoutPlotChildren(double top, double left, double width, double height)
   {
      updateSeriesList(top, left, width, height);
      updateMarkers();
   }

   private void updateMarkers()
   {
      markers.values().forEach(marker -> marker.updateMarker(xAxis, yAxis));
   }

   @Override
   protected void updateAxisRange()
   {
      updateXAxisRange();
      updateYAxisRange();
   }

   protected void updateXAxisRange()
   {
      ChartIntegerBounds xBounds = null;

      if (xAxis.isAutoRanging())
      {
         for (NumberSeriesLayer layer : seriesLayers)
         {
            NumberSeries series = layer.getNumberSeries();

            ChartIntegerBounds dataXBounds = series.xBoundsProperty().getValue();

            if (dataXBounds == null)
               continue;

            if (xBounds == null)
               xBounds = new ChartIntegerBounds(dataXBounds);
            else
               xBounds = xBounds.union(dataXBounds);
         }

         if (xBounds != null)
            xAxis.invalidateRange(Arrays.asList(xBounds.getLower(), xBounds.getUpper()));
      }
   }

   protected void updateYAxisRange()
   {
      ChartDoubleBounds yBounds = null;

      if (yAxis.isAutoRanging())
      {
         if (chartStyleProperty.get() == ChartStyle.NORMALIZED)
         {
            yAxis.invalidateRange(0.0, 1.0);
         }
         else
         {
            for (NumberSeriesLayer layer : seriesLayers)
            {
               NumberSeries series = layer.getNumberSeries();

               ChartDoubleBounds dataYBounds = series.yBoundsProperty().getValue();

               if (dataYBounds == null)
                  continue;

               if (series.getCustomYBounds() != null)
                  dataYBounds = series.getCustomYBounds();
               if (series.isNegated())
                  dataYBounds = dataYBounds.negate();

               if (yBounds == null)
                  yBounds = dataYBounds;
               else
                  yBounds = yBounds.union(dataYBounds);
            }

            if (yBounds != null)
            {
               yAxis.invalidateRange(yBounds.getLower(), yBounds.getUpper());
            }
         }
      }
   }

   /** Get the X axis, by default it is along the bottom of the plot */
   public InvisibleNumberAxis getXAxis()
   {
      return xAxis;
   }

   /** Get the Y axis, by default it is along the left of the plot */
   public InvisibleNumberAxis getYAxis()
   {
      return yAxis;
   }

   public void setChartStyle(ChartStyle style)
   {
      chartStyleProperty.set(style);
   }

   public ChartStyle getChartStyle()
   {
      return chartStyleProperty.get();
   }

   public ObjectProperty<ChartStyle> chartStyleProperty()
   {
      return chartStyleProperty;
   }

   private static void setSeriesDefaultStyleClass(NumberSeriesLayer seriesLayer, int seriesIndex)
   {
      seriesLayer.getStyleClass().setAll("chart-series-line", "series" + seriesIndex, getDefaultColorStyle(seriesIndex));
   }

   public static String getDefaultColorStyle(int index)
   {
      return "default-color" + index % 8;
   }
}