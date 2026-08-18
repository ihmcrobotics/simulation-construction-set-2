package us.ihmc.scs2.sessionVisualizer.jfx.charts;

import javafx.geometry.Side;
import javafx.scene.chart.NumberAxis;

/**
 * A per-variable Y-axis shown on the left of a {@link DynamicLineChart} in individual-scaling
 * (NORMALIZED) mode. Each visible variable gets one, colored to match its series' line and showing
 * that variable's real min/max range. It wraps a {@link NumberAxis} (which is final and cannot be
 * subclassed) and is positioned manually by the chart, hence its node is unmanaged.
 */
public class PerVariableYAxis
{
   private static final int TICK_COUNT = 5;

   private final NumberAxis axis = new NumberAxis();
   private boolean hasRealBounds = false;

   public PerVariableYAxis()
   {
      axis.setSide(Side.LEFT);
      axis.setAutoRanging(false);
      axis.setAnimated(false);
      axis.setMinorTickVisible(false);
      axis.setManaged(false);
      axis.getStyleClass().setAll("axis", "per-variable-axis");
   }

   public NumberAxis getNode()
   {
      return axis;
   }

   public void applyColorStyle(int seriesIndex)
   {
      axis.getStyleClass().setAll("axis", "per-variable-axis", DynamicLineChart.getDefaultColorStyle(seriesIndex));
   }

   public void setRealBounds(ChartDoubleBounds bounds)
   {
      if (bounds == null)
      {
         hasRealBounds = false;
         return;
      }

      hasRealBounds = true;
      double lower = bounds.getLower();
      double upper = bounds.getUpper();
      axis.setLowerBound(lower);
      axis.setUpperBound(upper);
      double range = upper - lower;
      axis.setTickUnit(range > 0.0 ? range / TICK_COUNT : 1.0);
   }

   public boolean hasRealBounds()
   {
      return hasRealBounds;
   }

   public void setVisible(boolean visible)
   {
      axis.setVisible(visible);
   }

   public boolean isVisible()
   {
      return axis.isVisible();
   }

   public double prefWidth(double height)
   {
      return axis.prefWidth(height);
   }

   public void resizeRelocate(double x, double y, double width, double height)
   {
      axis.resizeRelocate(x, y, width, height);
   }

   public void requestAxisLayout()
   {
      axis.requestAxisLayout();
   }

   public void layout()
   {
      axis.layout();
   }
}
