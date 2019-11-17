package us.ihmc.scs2.sessionVisualizer.charts;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.DoubleUnaryOperator;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.chart.NumberAxis;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.transform.Scale;
import us.ihmc.euclid.tuple2D.Point2D;
import us.ihmc.scs2.sessionVisualizer.charts.DynamicLineChart.ChartStyle;
import us.ihmc.scs2.sessionVisualizer.managers.ChartRenderManager;

public class NumberSeriesLayer extends ImageView
{
   private static final StyleablePropertyFactory<NumberSeriesLayer> FACTORY = new StyleablePropertyFactory<>(NumberSeriesLayer.getClassCssMetaData());
   private static final CssMetaData<NumberSeriesLayer, Color> STROKE = FACTORY.createColorCssMetaData("-fx-stroke", s -> s.stroke, Color.BLUE, false);
   private static final CssMetaData<NumberSeriesLayer, Number> STROKE_WIDTH = FACTORY.createSizeCssMetaData("-fx-stroke-width", s -> s.strokeWidth, 1.0, false);

   private final NumberSeries numberSeries;
   private final DynamicChartLegendItem legendNode = new DynamicChartLegendItem();

   private final NumberAxis xAxis, yAxis;

   private final AtomicBoolean updateRequested = new AtomicBoolean(true);
   private final Executor backgroundExecutor;

   private final StyleableObjectProperty<Color> stroke = new StyleableObjectProperty<Color>(Color.BLACK)
   {
      @Override
      protected void invalidated()
      {
         Paint color = get();
         requestUpdate();
         legendNode.setTextFill(color);
      };

      @Override
      public String getName()
      {
         return "stroke";
      }

      @Override
      public Object getBean()
      {
         return NumberSeriesLayer.this;
      }

      @Override
      public CssMetaData<? extends Styleable, Color> getCssMetaData()
      {
         return STROKE;
      }
   };

   private final StyleableDoubleProperty strokeWidth = new StyleableDoubleProperty(1.5)
   {
      @Override
      protected void invalidated()
      {
         requestUpdate();
      };

      @Override
      public String getName()
      {
         return "strokeWidth";
      }

      @Override
      public Object getBean()
      {
         return NumberSeriesLayer.this;
      }

      @Override
      public CssMetaData<? extends Styleable, Number> getCssMetaData()
      {
         return STROKE_WIDTH;
      }
   };

   private final ChartRenderManager renderManager;
   private final Map<Key, Object> renderingHints = new HashMap<>();

   private final ObjectProperty<ChartStyle> chartStyleProperty = new SimpleObjectProperty<>(this, "chartStyle", ChartStyle.RAW);
   private final DoubleProperty imageScaleProperty = new SimpleDoubleProperty(this, "imageScale", 0.0);
   private final Scale imageScale = new Scale();

   public NumberSeriesLayer(NumberAxis xAxis, NumberAxis yAxis, NumberSeries numberSeries, Executor backgroundExecutor, ChartRenderManager renderManager)
   {
      this.renderManager = renderManager;
      getStyleClass().add("dynamic-chart-series-line");
      this.xAxis = xAxis;
      this.yAxis = yAxis;
      this.numberSeries = numberSeries;
      this.backgroundExecutor = backgroundExecutor;
      legendNode.seriesNameProperty().bind(numberSeries.seriesNameProperty());
      legendNode.currentValueProperty().bind(numberSeries.currentValueProperty());

      renderingHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      imageScaleProperty.addListener((o, oldValue, newValue) ->
      {
         double newScale = 1.0 / newValue.doubleValue();
         imageScale.setX(newScale);
         imageScale.setY(newScale);
      });
      imageScaleProperty.set(1.25);

      getTransforms().add(imageScale);
   }

   public void requestUpdate()
   {
      updateRequested.set(true);
   }

   private Image imageWaitingToRender = null;

   public void prepareToRender()
   {
      if (updateRequested.get())
      {
         backgroundExecutor.execute(() ->
         {
            imageWaitingToRender = newImage();
            renderManager.submitRenderRequest(this);
            updateRequested.set(false);
         });
      }
   }

   public void render()
   {
      if (imageWaitingToRender == null)
         return;

      setImage(imageWaitingToRender);
      imageWaitingToRender = null;
   }

   private Image newImage()
   {
      if (numberSeries.getDataEntry() == null)
         return null;

      List<Point2D> data = numberSeries.getDataEntry().getData();

      if (data == null || data.isEmpty())
         return null;

      if (data.isEmpty())
         return null;

      double width = imageScaleProperty.get() * xAxis.getWidth();
      double height = imageScaleProperty.get() * yAxis.getHeight();
      BufferedImage bufferedImage = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);
      Graphics2D graphics = bufferedImage.createGraphics();
      graphics.addRenderingHints(renderingHints);

      graphics.setColor(toAWTColor(stroke.get()));
      graphics.setStroke(new BasicStroke((float) (imageScaleProperty.get() * strokeWidth.get()), BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));

      DoubleUnaryOperator xTransform = xToHorizontalDisplayTransform(width, xAxis.getLowerBound(), xAxis.getUpperBound());
      DoubleUnaryOperator yTransform = yToVerticalDisplayTransform(height, yAxis.getLowerBound(), yAxis.getUpperBound());

      if (chartStyleProperty.get() == ChartStyle.NORMALIZED)
      {
         ChartDoubleBounds yBounds = numberSeries.getCustomYBounds();
         if (yBounds == null)
            yBounds = numberSeries.getDataYBounds();
         if (numberSeries.isNegated())
            yBounds = yBounds.negate();

         yTransform = yTransform.compose(normalizeTransform(yBounds));
      }
      if (numberSeries.isNegated())
      {
         yTransform = yTransform.compose(negateTransform());
      }

      drawMultiLine(graphics, data, xTransform, yTransform);

      return SwingFXUtils.toFXImage(bufferedImage, null);
   }

   private static void drawMultiLine(Graphics2D graphics, List<Point2D> points, DoubleUnaryOperator xTransform, DoubleUnaryOperator yTransform)
   {
      Point2D previousPoint = points.get(0);
      int xPrev = (int) Math.round(xTransform.applyAsDouble(previousPoint.getX()));
      int yPrev = (int) Math.round(yTransform.applyAsDouble(previousPoint.getY()));

      for (Point2D point : points.subList(1, points.size()))
      {
         int x = (int) Math.round(xTransform.applyAsDouble(point.getX()));
         int y = (int) Math.round(yTransform.applyAsDouble(point.getY()));
         graphics.drawLine(xPrev, yPrev, x, y);
         xPrev = x;
         yPrev = y;
      }
   }

   private static java.awt.Color toAWTColor(Color color)
   {
      float red = (float) color.getRed();
      float green = (float) color.getGreen();
      float blue = (float) color.getBlue();
      float alpha = (float) color.getOpacity();
      return new java.awt.Color(red, green, blue, alpha);
   }

   private static DoubleUnaryOperator negateTransform()
   {
      return coordinate -> -coordinate;
   }

   private static DoubleUnaryOperator normalizeTransform(ChartDoubleBounds bounds)
   {
      return normalizeTransform(bounds.getLower(), bounds.getUpper());
   }

   private static DoubleUnaryOperator normalizeTransform(double min, double max)
   {
      return affineTransform(1.0 / (max - min), -min / (max - min));
   }

   private static DoubleUnaryOperator xToHorizontalDisplayTransform(double displayWidth, double xMin, double xMax)
   {
      if (xMax == xMin)
         return affineTransform(displayWidth, -xMin * displayWidth);
      else
         return affineTransform(displayWidth / (xMax - xMin), -xMin * displayWidth / (xMax - xMin));
   }

   private static DoubleUnaryOperator yToVerticalDisplayTransform(double displayHeight, double yMin, double yMax)
   {
      if (yMax == yMin)
         return affineTransform(-displayHeight, displayHeight * (1.0 + yMin));
      else
         return affineTransform(-displayHeight / (yMax - yMin), displayHeight * (1.0 + yMin / (yMax - yMin)));
   }

   private static DoubleUnaryOperator affineTransform(double scale, double offset)
   {
      return coordinate -> coordinate * scale + offset;
   }

   public ObjectProperty<ChartStyle> chartStyleProperty()
   {
      return chartStyleProperty;
   }

   public NumberSeries getNumberSeries()
   {
      return numberSeries;
   }

   public DynamicChartLegendItem getLegendNode()
   {
      return legendNode;
   }

   @Override
   public List<CssMetaData<? extends Styleable, ?>> getCssMetaData()
   {
      return FACTORY.getCssMetaData();
   }
}
