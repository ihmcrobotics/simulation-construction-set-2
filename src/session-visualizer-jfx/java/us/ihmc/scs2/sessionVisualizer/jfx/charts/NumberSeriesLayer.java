package us.ihmc.scs2.sessionVisualizer.jfx.charts;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.RenderingHints.Key;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.DoubleUnaryOperator;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.scene.chart.NumberAxis;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import us.ihmc.euclid.tuple2D.Point2D;
import us.ihmc.scs2.sessionVisualizer.jfx.charts.DynamicLineChart.ChartStyle;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.ChartRenderManager;

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

   private final StyleableDoubleProperty strokeWidth = new StyleableDoubleProperty(1.0)
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
   private WritableImage writableImage = null;
   private AtomicBoolean isRenderingImage = new AtomicBoolean(false);
   private AtomicBoolean isUpdatingImage = new AtomicBoolean(false);
   private BufferedImage imageToRender = null;

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

      //      renderingHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      //      renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
   }

   public void requestUpdate()
   {
      updateRequested.set(true);
   }

   public void prepareToRender()
   {
      if (updateRequested.get())
      {
         backgroundExecutor.execute(() ->
         {
            if (updateImage())
               renderManager.submitRenderRequest(this::render);
            updateRequested.set(false);
         });
      }
   }

   public void render()
   {
      if (imageToRender == null)
         return;

      if (isUpdatingImage.get())
      {
         renderManager.submitRenderRequest(this::render);
         return;
      }

      isRenderingImage.set(true);

      int width = imageToRender.getWidth();
      int height = imageToRender.getHeight();

      if (writableImage == null || (int) Math.round(writableImage.getWidth()) != width || (int) Math.round(writableImage.getHeight()) != height)
      {
         writableImage = new WritableImage(width, height);
         setImage(writableImage);
      }

      int[] data = ((DataBufferInt) imageToRender.getRaster().getDataBuffer()).getData();
      writableImage.getPixelWriter().setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), data, 0, width);

      isRenderingImage.set(false);
   }

   private int[] xData, yData;

   private boolean updateImage()
   {
      if (isRenderingImage.get())
         return false;

      if (isUpdatingImage.get())
         return false;

      isUpdatingImage.set(true);

      double width = xAxis.getWidth();
      double height = yAxis.getHeight();
      int widthInt = (int) Math.round(width);
      int heightInt = (int) Math.round(height);

      Graphics2D graphics;

      if (imageToRender == null || imageToRender.getWidth() != widthInt || imageToRender.getHeight() != heightInt)
      {
         imageToRender = new BufferedImage(widthInt, heightInt, BufferedImage.TYPE_INT_ARGB);
         graphics = imageToRender.createGraphics();
      }
      else
      {
         graphics = imageToRender.createGraphics();
         graphics.setBackground(new java.awt.Color(255,255,255,0));
         graphics.clearRect(0, 0, widthInt, heightInt);
      }

      if (numberSeries.getDataEntry() == null)
      {
         isUpdatingImage.set(false);
         return false;
      }

      List<Point2D> data = numberSeries.getDataEntry().getData();

      if (data == null || data.isEmpty())
      {
         isUpdatingImage.set(false);
         return false;
      }

      xData = resize(xData, data.size());
      yData = resize(yData, data.size());

      graphics.addRenderingHints(renderingHints);

      graphics.setColor(toAWTColor(stroke.get()));
      graphics.setStroke(new BasicStroke((float) (strokeWidth.get()), BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));

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

      //      try
      //      {
      DoubleUnaryOperator yTransformFinal = yTransform;
      //         SwingUtilities.invokeAndWait(() ->
      //         {
      drawMultiLine(graphics, data, xTransform, yTransformFinal, xData, yData);

      graphics.setColor(toAWTColor(Color.GREY));
      graphics.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
      List<Point2D> markerData = Arrays.asList(new Point2D(numberSeries.getDataEntry().getBufferCurrentIndex(), yAxis.getLowerBound()),
                                               new Point2D(numberSeries.getDataEntry().getBufferCurrentIndex(), yAxis.getUpperBound()));
      drawMultiLine(graphics, markerData, xTransform, yTransformFinal, xData, yData);
      graphics.dispose();
      //         });
      //      }
      //      catch (InvocationTargetException | InterruptedException e)
      //      {
      //         e.printStackTrace();
      //         bufferedImage = null;
      //      }

      isUpdatingImage.set(false);
      return true;
   }

   private static int[] resize(int[] in, int length)
   {
      if (in == null || in.length < length)
         return new int[length];
      else
         return in;
   }

   private static void drawMultiLine(Graphics2D graphics, List<Point2D> points, DoubleUnaryOperator xTransform, DoubleUnaryOperator yTransform, int[] xData,
                                     int[] yData)
   {
      for (int i = 0; i < points.size(); i++)
      {
         Point2D point = points.get(i);
         xData[i] = (int) Math.round(xTransform.applyAsDouble(point.getX()));
         yData[i] = (int) Math.round(yTransform.applyAsDouble(point.getY()));
      }
      graphics.drawPolyline(xData, yData, points.size());
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