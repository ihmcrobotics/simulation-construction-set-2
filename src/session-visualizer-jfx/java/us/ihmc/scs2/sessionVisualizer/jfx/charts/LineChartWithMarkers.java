package us.ihmc.scs2.sessionVisualizer.jfx.charts;

import java.util.Objects;

import javafx.beans.InvalidationListener;
import javafx.beans.NamedArg;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.shape.Line;

public class LineChartWithMarkers<X extends Number, Y extends Number> extends LineChart<X, Y>
{
   private final ObservableList<Data<X, Y>> horizontalMarkers;
   private final ObservableList<Data<X, Y>> verticalMarkers;

   public LineChartWithMarkers(@NamedArg("xAxis") Axis<X> xAxis, @NamedArg("yAxis") Axis<Y> yAxis)
   {
      super(xAxis, yAxis);
      horizontalMarkers = FXCollections.observableArrayList(data -> new Observable[] {data.YValueProperty()});
      horizontalMarkers.addListener((InvalidationListener) observable -> layoutPlotChildren());
      verticalMarkers = FXCollections.observableArrayList(data -> new Observable[] {data.XValueProperty()});
      verticalMarkers.addListener((InvalidationListener) observable -> layoutPlotChildren());
   }

   @Override
   protected void dataItemAdded(Series<X, Y> series, int itemIndex, Data<X, Y> item)
   {
   }

   public void addHorizontalValueMarker(Data<X, Y> marker)
   {
      Objects.requireNonNull(marker, "the marker must not be null");
      if (horizontalMarkers.contains(marker))
         return;
      Line line = new Line();
      marker.setNode(line);
      getPlotChildren().add(line);
      horizontalMarkers.add(marker);
   }

   public void removeHorizontalValueMarker(Data<X, Y> marker)
   {
      Objects.requireNonNull(marker, "the marker must not be null");
      if (marker.getNode() != null)
      {
         getPlotChildren().remove(marker.getNode());
         marker.setNode(null);
      }
      horizontalMarkers.remove(marker);
   }

   public void addVerticalValueMarker(Data<X, Y> marker)
   {
      addVerticalValueMarker(marker, null);
   }

   public void addVerticalValueMarker(Data<X, Y> marker, String markerStyle)
   {
      Objects.requireNonNull(marker, "the marker must not be null");
      if (verticalMarkers.contains(marker))
         return;
      Line line = new Line();
      if (markerStyle != null)
         line.setStyle(markerStyle);
      marker.setNode(line);
      getPlotChildren().add(line);
      verticalMarkers.add(marker);
   }

   public void removeVerticalValueMarker(Data<X, Y> marker)
   {
      Objects.requireNonNull(marker, "the marker must not be null");
      if (marker.getNode() != null)
      {
         getPlotChildren().remove(marker.getNode());
         marker.setNode(null);
      }
      verticalMarkers.remove(marker);
   }

   @Override
   protected void layoutPlotChildren()
   {
      super.layoutPlotChildren();

      for (Data<X, Y> horizontalMarker : horizontalMarkers)
      {
         Line line = (Line) horizontalMarker.getNode();
         line.setStartX(0);
         line.setEndX(getBoundsInLocal().getWidth());
         line.setStartY(getYAxis().getDisplayPosition(horizontalMarker.getYValue()) + 0.5); // 0.5 for crispness
         line.setEndY(line.getStartY());
         line.toFront();
      }
      for (Data<X, Y> verticalMarker : verticalMarkers)
      {
         Line line = (Line) verticalMarker.getNode();
         line.setStartX(getXAxis().getDisplayPosition(verticalMarker.getXValue()) + 0.5); // 0.5 for crispness
         line.setEndX(line.getStartX());
         line.setStartY(0d);
         line.setEndY(getBoundsInLocal().getHeight());
         line.toFront();
      }
   }
}
