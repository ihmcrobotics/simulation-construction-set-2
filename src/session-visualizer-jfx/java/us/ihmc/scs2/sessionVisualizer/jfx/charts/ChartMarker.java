package us.ihmc.scs2.sessionVisualizer.jfx.charts;

import java.util.List;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.shape.Line;

public final class ChartMarker extends Line
{
   private static final StyleablePropertyFactory<ChartMarker> FACTORY = new StyleablePropertyFactory<>(ChartMarker.getClassCssMetaData());
   private static final CssMetaData<ChartMarker, ChartMarkerType> MARKER_TYPE = FACTORY.createEnumCssMetaData(ChartMarkerType.class,
                                                                                                              "-marker-type",
                                                                                                              s -> s.typeProperty,
                                                                                                              ChartMarkerType.HORIZONTAL);

   private final StyleableObjectProperty<ChartMarkerType> typeProperty = new StyleableObjectProperty<ChartMarkerType>(ChartMarkerType.HORIZONTAL)
   {
      @Override
      public String getName()
      {
         return "markerType";
      }

      @Override
      public Object getBean()
      {
         return ChartMarker.this;
      }

      @Override
      public CssMetaData<? extends Styleable, ChartMarkerType> getCssMetaData()
      {
         return MARKER_TYPE;
      }
   };

   private final Data<Number, Number> coordinates;
   private final DoubleProperty positionProperty = new SimpleDoubleProperty(this, "position", 0.0);

   public ChartMarker(Data<Number, Number> coordinates)
   {
      this.coordinates = coordinates;
      typeProperty.addListener((o, oldValue, newValue) -> updatePositionBinding(newValue));
      updatePositionBinding(typeProperty.get());
      coordinates.setNode(this);
   }

   public ChartMarker(ChartMarkerType type, Data<Number, Number> coordinates)
   {
      this(coordinates);
      this.typeProperty.set(type);
   }

   private void updatePositionBinding(ChartMarkerType type)
   {
      positionProperty.bind(type == ChartMarkerType.HORIZONTAL ? coordinates.YValueProperty() : coordinates.XValueProperty());
   }

   public void addListener(ChangeListener<Object> listener)
   {
      positionProperty.addListener(listener);
   }

   public Node getNode()
   {
      return coordinates.getNode();
   }

   public void updateMarker(NumberAxis xAxis, NumberAxis yAxis)
   {
      if (typeProperty.get() == ChartMarkerType.HORIZONTAL)
      {
         setStartX(0);
         setEndX(xAxis.getWidth());
         setStartY(Math.ceil(yAxis.getDisplayPosition(coordinates.getYValue())));
         setEndY(getStartY());
         toFront();
      }
      else if (typeProperty.get() == ChartMarkerType.VERTICAL)
      {
         setStartY(0d);
         setEndY(yAxis.getHeight());
         setStartX(Math.ceil(xAxis.getDisplayPosition(coordinates.getXValue())));
         setEndX(getStartX());
         toFront();
      }
   }

   public void destroy()
   {
      coordinates.setNode(null);
   }

   @Override
   public List<CssMetaData<? extends Styleable, ?>> getCssMetaData()
   {
      return FACTORY.getCssMetaData();
   }
}