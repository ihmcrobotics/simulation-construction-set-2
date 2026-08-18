package us.ihmc.scs2.sessionVisualizer.jfx.charts;

import java.util.Optional;
import java.util.concurrent.Executor;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.chart.FastAxisBase;
import javafx.scene.shape.Rectangle;
import us.ihmc.javaFXExtensions.chart.DynamicXYChart;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.ChartRenderManager;

public class DynamicLineChart extends DynamicXYChart
{
   public enum ChartStyle
   {
      RAW, NORMALIZED
   }

   private final Group seriesGroup = new Group()
   {
      @Override
      public void requestLayout()
      {
      } // suppress layout requests
   };
   private final Group markerGroup = new Group()
   {
      @Override
      public void requestLayout()
      {
      } // suppress layout requests
   };

   private final BooleanProperty markerAutoUpdateProperty = new SimpleBooleanProperty(this, "markerAutoUpdate", true);
   private final BooleanProperty showYAxisProperty = new SimpleBooleanProperty(this, "showYAxis", false);
   private final ObjectProperty<ChartStyle> chartStyleProperty = new SimpleObjectProperty<>(this, "chartStyle", ChartStyle.RAW);
   private final ObservableList<NumberSeriesLayer> seriesLayers = FXCollections.observableArrayList();
   private final ObservableList<PerVariableYAxis> perSeriesYAxes = FXCollections.observableArrayList();
   // Our own plot clip: the parent's clip is private and only updated inside its own layoutChartChildren,
   // which we override without calling super, so we install and manage this one instead.
   private final Rectangle plotClip = new Rectangle();
   private final ChangeListener<Object> chartUpdaterListener = (o, oldValue, newValue) -> requestChartLayout();
   private final ObservableList<ChartMarker> markers = FXCollections.observableArrayList();
   private final DynamicChartLegend legend = new DynamicChartLegend();

   private final BooleanProperty updateIndexMarkersVisible = new SimpleBooleanProperty(this, "updateIndexMarkersVisible", false);

   private final Executor backgroundExecutor;
   private final ChartRenderManager chartRenderManager;
   private final ChangeListener<? super Boolean> autoRangingListener = (ov, t, t1) -> updateAxisRange();

   public DynamicLineChart(FastAxisBase xAxis, FastAxisBase yAxis, Executor backgroundExecutor, ChartRenderManager chartRenderManager)
   {
      super(xAxis, yAxis);

      this.chartRenderManager = chartRenderManager;

      this.backgroundExecutor = backgroundExecutor;

      ChangeListener<? super FastAxisBase> xAxisChangeListener = (o, oldAxis, newAxis) ->
      {
         if (oldAxis != null)
            oldAxis.autoRangingProperty().removeListener(autoRangingListener);

         if (newAxis.getSide() == null)
            newAxis.setSide(Side.BOTTOM);
         newAxis.setEffectiveOrientation(Orientation.HORIZONTAL);
         newAxis.autoRangingProperty().addListener(autoRangingListener);
      };
      xAxisProperty().addListener(xAxisChangeListener);
      xAxisChangeListener.changed(null, null, xAxis);

      ChangeListener<? super FastAxisBase> yAxisChangeListener = (o, oldAxis, newAxis) ->
      {
         if (oldAxis != null)
            oldAxis.autoRangingProperty().removeListener(autoRangingListener);
         if (newAxis.getSide() == null)
            newAxis.setSide(Side.LEFT);
         newAxis.setEffectiveOrientation(Orientation.VERTICAL);
         newAxis.autoRangingProperty().addListener(autoRangingListener);
      };
      yAxisProperty().addListener(yAxisChangeListener);
      yAxisChangeListener.changed(null, null, yAxis);

      chartStyleProperty.addListener(chartUpdaterListener);
      showYAxisProperty.addListener(chartUpdaterListener);

      plotClip.setSmooth(false);
      plotContent.setClip(plotClip);

      plotContent.getChildren().addAll(seriesGroup, markerGroup);

      // We don't want seriesGroup/markerGroup to autoSize or do layout
      seriesGroup.setAutoSizeChildren(false);
      markerGroup.setAutoSizeChildren(false);
      // setup css style classes
      seriesGroup.getStyleClass().setAll("series-group");
      markerGroup.getStyleClass().setAll("marker-group");
      // mark seriesGroup/markerGroup as unmanaged as its preferred size changes do not effect our layout
      seriesGroup.setManaged(false);
      markerGroup.setManaged(false);

      ChangeListener<Object> updateMarkerListener = (o, oldValue, newValue) -> updateMarkers();
      markers.addListener((ListChangeListener<ChartMarker>) change ->
      {
         while (change.next())
         {
            if (change.wasAdded())
            {
               for (ChartMarker newMarker : change.getAddedSubList())
               {
                  markerGroup.getChildren().add(newMarker);
                  if (markerAutoUpdateProperty.get())
                     newMarker.addListener(updateMarkerListener);
               }
               updateMarkers();
            }

            if (change.wasRemoved())
            {
               for (ChartMarker oldMarker : change.getRemoved())
               {
                  markerGroup.getChildren().remove(oldMarker);
                  oldMarker.destroy();
               }
            }
         }
      });
      markerAutoUpdateProperty.addListener((o, oldValue, newValue) ->
      {
         if (newValue)
            markers.forEach(marker -> marker.addListener(updateMarkerListener));
         else
            markers.forEach(marker -> marker.removeListener(updateMarkerListener));
      });

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
      layer.updateIndexMarkerVisibleProperty().bind(updateIndexMarkersVisible);
      setSeriesDefaultStyleClass(layer, seriesIndex);
      seriesLayers.add(layer);
      legend.getItems().add(layer.getLegendNode());
      // Add the plot under the markers
      seriesGroup.getChildren().add(layer);

      PerVariableYAxis perVariableYAxis = new PerVariableYAxis();
      perVariableYAxis.applyColorStyle(seriesIndex);
      perSeriesYAxes.add(perVariableYAxis);
      getChartChildren().add(perVariableYAxis.getNode());

      chartUpdaterListener.changed(null, null, null);
   }

   public void removeSeries(NumberSeries series)
   {
      Optional<NumberSeriesLayer> containingLayer = seriesLayers.stream().filter(layer -> layer.getNumberSeries() == series).findFirst();

      if (containingLayer.isPresent())
      {
         int indexOf = seriesLayers.indexOf(containingLayer.get());
         NumberSeriesLayer removedLayer = seriesLayers.remove(indexOf);
         removedLayer.chartStyleProperty().unbind();
         removedLayer.updateIndexMarkerVisibleProperty().unbind();
         series.negatedProperty().removeListener(chartUpdaterListener);
         series.customYBoundsProperty().removeListener(chartUpdaterListener);
         series.dirtyProperty().removeListener(chartUpdaterListener);
         containingLayer.get().chartStyleProperty().unbind();
         seriesGroup.getChildren().remove(containingLayer.get());
         legend.getItems().remove(containingLayer.get().getLegendNode());

         PerVariableYAxis removedAxis = perSeriesYAxes.remove(indexOf);
         getChartChildren().remove(removedAxis.getNode());

         for (int i = indexOf; i < seriesLayers.size(); i++)
         {
            setSeriesDefaultStyleClass(seriesLayers.get(i), i);
            perSeriesYAxes.get(i).applyColorStyle(i);
         }

         chartUpdaterListener.changed(null, null, null);
      }
   }

   public void addMarker(ChartMarker marker)
   {
      markers.add(marker);
   }

   public void removeMarker(ChartMarker marker)
   {
      markers.remove(marker);
   }

   private void updateSeriesList(double top, double left, double width, double height)
   {
      for (NumberSeriesLayer seriesLayer : seriesLayers)
      {
         seriesLayer.scheduleRender();
      }
   }

   @Override
   protected void layoutChartChildren(double top, double left, double width, double height)
   {
      // This replicates DynamicXYChart.layoutChartChildren (from ihmc-javafx-extensions, whose clip is
      // private and hence not usable from here) and extends it to reserve stacked space on the left for
      // the per-variable Y-axes. When no per-variable axis is visible the geometry reduces exactly to the
      // parent's (perVariableTotalWidth == 0), so RAW/Auto/Manual behaves identically to the parent.
      updateAxisRange();

      // snap top and left to pixels
      top = snapPositionY(top);
      left = snapPositionX(left);

      FastAxisBase xAxis = xAxisProperty().get();
      FastAxisBase yAxis = yAxisProperty().get();

      // Measure the per-variable axes to stack on the left; only shown when individual-scaling and toggled on.
      boolean showPerVariableAxes = showYAxisProperty.get() && chartStyleProperty.get() == ChartStyle.NORMALIZED;
      double perVariableTotalWidth = 0.0;
      double[] perVariableAxisWidths = new double[perSeriesYAxes.size()];
      for (int i = 0; i < perSeriesYAxes.size(); i++)
      {
         PerVariableYAxis perVariableYAxis = perSeriesYAxes.get(i);
         boolean visible = showPerVariableAxes && perVariableYAxis.hasRealBounds();
         perVariableYAxis.setVisible(visible);
         if (visible)
         {
            perVariableAxisWidths[i] = Math.ceil(perVariableYAxis.prefWidth(height));
            perVariableTotalWidth += perVariableAxisWidths[i];
         }
      }

      // try and work out width and height of axises
      double xAxisWidth = 0;
      double xAxisHeight = 0; // guess x axis height to start with
      double yAxisWidth = 0;
      double yAxisHeight = 0;
      for (int count = 0; count < 5; count++)
      {
         yAxisHeight = Math.max(0, snapSizeY(height - xAxisHeight));
         yAxisWidth = yAxis.prefWidth(yAxisHeight);
         xAxisWidth = Math.max(0, snapSizeX(width - yAxisWidth - perVariableTotalWidth));
         double newXAxisHeight = xAxis.prefHeight(xAxisWidth);
         if (newXAxisHeight == xAxisHeight)
            break;
         xAxisHeight = newXAxisHeight;
      }
      // round axis sizes up to whole integers to snap to pixel
      xAxisWidth = Math.ceil(xAxisWidth);
      xAxisHeight = Math.ceil(xAxisHeight);
      yAxisWidth = Math.ceil(yAxisWidth);
      yAxisHeight = Math.ceil(yAxisHeight);

      // resize axises
      xAxis.resizeRelocate(left + perVariableTotalWidth + yAxisWidth, top + yAxisHeight, xAxisWidth, xAxisHeight);
      yAxis.resizeRelocate(left + perVariableTotalWidth + 1, top, yAxisWidth, yAxisHeight);
      // When the chart is resized, need to specifically call out the axises
      // to lay out as they are unmanaged.
      xAxis.requestAxisLayout();
      xAxis.layout();
      yAxis.requestAxisLayout();
      yAxis.layout();

      // Stack the per-variable axes to the left of the primary Y-axis: series 0 innermost, series N outermost.
      double axisCursor = left + perVariableTotalWidth;
      for (int i = 0; i < perSeriesYAxes.size(); i++)
      {
         PerVariableYAxis perVariableYAxis = perSeriesYAxes.get(i);
         if (!perVariableYAxis.isVisible())
            continue;
         double axisWidth = perVariableAxisWidths[i];
         axisCursor -= axisWidth;
         perVariableYAxis.resizeRelocate(axisCursor, top, axisWidth, yAxisHeight);
         perVariableYAxis.requestAxisLayout();
         perVariableYAxis.layout();
      }

      // layout plot content
      layoutPlotChildren(top, left, xAxisWidth, yAxisHeight);
      // update clip
      plotClip.setX(left);
      plotClip.setY(top);
      plotClip.setWidth(xAxisWidth + 1);
      plotClip.setHeight(yAxisHeight + 1);
      // position plot group, its origin is the bottom left corner of the plot area
      plotContent.setLayoutX(left + perVariableTotalWidth + yAxisWidth);
      plotContent.setLayoutY(top);
      plotContent.requestLayout(); // Note: not sure this is right, maybe plotContent should be resizeable
   }

   @Override
   protected void layoutPlotChildren(double top, double left, double width, double height)
   {
      // position plot group, its origin is the bottom left corner of the plot area
      seriesGroup.setLayoutX(left);
      seriesGroup.setLayoutY(top);
      seriesGroup.requestLayout(); // Note: not sure this is right, maybe plotContent should be resizeable
      markerGroup.setLayoutX(left);
      markerGroup.setLayoutY(top);
      markerGroup.requestLayout(); // Note: not sure this is right, maybe plotContent should be resizeable

      updateSeriesList(top, left, width, height);
      if (markerAutoUpdateProperty.get())
         updateMarkers();
   }

   public void updateMarkers()
   {
      markers.forEach(marker -> marker.updateMarker(getXAxis(), getYAxis()));
   }

   @Override
   protected void updateAxisRange()
   {
      updateXAxisRange();
      updateYAxisRange();
      updatePerVariableYAxisRanges();
   }

   private void updatePerVariableYAxisRanges()
   {
      for (int i = 0; i < seriesLayers.size(); i++)
         perSeriesYAxes.get(i).setRealBounds(computeRealYBounds(seriesLayers.get(i).getNumberSeries()));
   }

   protected void updateXAxisRange()
   {
      ChartIntegerBounds xBounds = null;

      FastAxisBase xAxis = getXAxis();

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
            xAxis.invalidateRange(xBounds.getLower(), xBounds.getUpper());
      }
   }

   protected void updateYAxisRange()
   {
      ChartDoubleBounds yBounds = null;

      FastAxisBase yAxis = getYAxis();

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
               ChartDoubleBounds dataYBounds = computeRealYBounds(layer.getNumberSeries());

               if (dataYBounds == null)
                  continue;

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

   /**
    * Property controlling visibility of markers used to indicate up to what index the charts have been
    * updated.
    */
   public BooleanProperty updateIndexMarkersVisible()
   {
      return updateIndexMarkersVisible;
   }

   public BooleanProperty markerAutoUpdateProperty()
   {
      return markerAutoUpdateProperty;
   }

   public BooleanProperty showYAxisProperty()
   {
      return showYAxisProperty;
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

   static ChartDoubleBounds computeRealYBounds(NumberSeries series)
   {
      ChartDoubleBounds dataYBounds = series.yBoundsProperty().getValue();

      if (dataYBounds == null)
         return null;

      if (series.getCustomYBounds() != null)
         dataYBounds = series.getCustomYBounds();
      if (series.isNegated())
         dataYBounds = dataYBounds.negate();

      return dataYBounds;
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