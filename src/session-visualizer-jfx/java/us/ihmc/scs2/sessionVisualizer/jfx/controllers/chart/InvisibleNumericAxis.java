package us.ihmc.scs2.sessionVisualizer.jfx.controllers.chart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.gsi.chart.axes.AxisTransform;
import de.gsi.chart.axes.LogAxisType;
import de.gsi.chart.axes.spi.AbstractAxis;
import de.gsi.chart.axes.spi.AxisRange;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.scene.layout.Region;

/**
 * A axis class that plots a range of numbers with major tick marks every "tickUnit". You can use
 * any Number type with this axis, Long, Double, BigDecimal etc.
 * <p>
 * Compared to the {@code NumberAxis}, this one has a few additional features:
 * <ul>
 * <li>Re-calculates tick unit also when the {@link #autoRangingProperty() auto-ranging} is off</li>
 * <li>Supports configuration of {@link #autoRangePaddingProperty() auto-range padding}</li>
 * <li>Supports configuration of {@link #autoRangeRoundingProperty() auto-range rounding}</li>
 * </ul>
 */
public final class InvisibleNumericAxis extends AbstractAxis
{
   private static final int DEFAULT_RANGE_LENGTH = 2;
   private static final double DEFAULT_RANGE_PADDING = 0.1;
   private double localScale;
   private double localCurrentLowerBound;
   private double localOffset;

   private final BooleanProperty forceZeroInRange = new SimpleBooleanProperty(this, "forceZeroInRange", true)
   {
      @Override
      protected void invalidated()
      {
         if (isAutoRanging() || isAutoGrowRanging())
         {
            invalidate();
            requestAxisLayout();
         }
      }
   };

   private final DoubleProperty autoRangePadding = new SimpleDoubleProperty(0);

   /**
    * Creates an {@link #autoRangingProperty() auto-ranging} Axis.
    */
   public InvisibleNumericAxis()
   {
      super();
      super.currentLowerBound.addListener((evt, o, n) ->
      {
         localCurrentLowerBound = currentLowerBound.get();
         final double zero = super.getDisplayPosition(0);
         localOffset = zero + localCurrentLowerBound * scaleProperty().get();
      });

      super.scaleProperty().addListener((evt, o, n) ->
      {
         localScale = scaleProperty().get();
         final double zero = super.getDisplayPosition(0);
         localOffset = zero + currentLowerBound.get() * localScale;
      });
   }

   /**
    * Creates a {@link #autoRangingProperty() non-auto-ranging} Axis with the given upper bound, lower
    * bound and tick unit.
    *
    * @param lowerBound the {@link #minProperty() lower bound} of the axis
    * @param upperBound the {@link #maxProperty() upper bound} of the axis
    */
   public InvisibleNumericAxis(final double lowerBound, final double upperBound)
   {
      this(null, lowerBound, upperBound);
   }

   /**
    * Create a {@link #autoRangingProperty() non-auto-ranging} Axis with the given upper bound, lower
    * bound and tick unit.
    *
    * @param axisLabel  the axis {@link #nameProperty() label}
    * @param lowerBound the {@link #minProperty() lower bound} of the axis
    * @param upperBound the {@link #maxProperty() upper bound} of the axis
    */
   public InvisibleNumericAxis(final String axisLabel, final double lowerBound, final double upperBound)
   {
      super(lowerBound, upperBound);
      this.setName(axisLabel);

      super.currentLowerBound.addListener((evt, o, n) -> localCurrentLowerBound = currentLowerBound.get());
      super.scaleProperty().addListener((evt, o, n) -> localScale = scaleProperty().get());
   }

   @Override
   protected AxisRange autoRange(final double minValue, final double maxValue, final double length, final double labelSize)
   {
      final double min = minValue > 0 && isForceZeroInRange() ? 0 : minValue;
      final double max = maxValue < 0 && isForceZeroInRange() ? 0 : maxValue;
      final double padding = InvisibleNumericAxis.getEffectiveRange(min, max) * getAutoRangePadding();
      final double paddedMin = InvisibleNumericAxis.clampBoundToZero(min - padding, min);
      final double paddedMax = InvisibleNumericAxis.clampBoundToZero(max + padding, max);

      return computeRange(paddedMin, paddedMax, length, labelSize);
   }

   /**
    * Fraction of the range to be applied as padding on both sides of the axis range. E.g. if set to
    * 0.1 (10%) on axis with data range [10, 20], the new automatically calculated range will be [9,
    * 21].
    *
    * @return autoRangePadding property
    */
   @Override
   public DoubleProperty autoRangePaddingProperty()
   {
      return autoRangePadding;
   }

   @Override
   protected List<Double> calculateMajorTickValues(final double axisLength, final AxisRange range)
   {
      return Collections.emptyList();
   }

   @Override
   protected List<Double> calculateMinorTickValues()
   {
      return Collections.emptyList();
   }

   /**
    * Computes the preferred tick unit based on the upper/lower bounds and the length of the axis in
    * screen coordinates.
    * 
    * @param axisLength the length in screen coordinates
    * @return the tick unit
    */
   @Override
   public double computePreferredTickUnit(final double axisLength)
   {
      return 1.0;
   }

   @Override
   protected AxisRange computeRange(final double min, final double max, final double axisLength, final double labelSize)
   {
      double minValue = min;
      double maxValue = max;
      if (max - min == 0)
      {
         final double padding = getAutoRangePadding() == 0 ? InvisibleNumericAxis.DEFAULT_RANGE_PADDING : getAutoRangePadding();
         final double paddedRange = InvisibleNumericAxis.getEffectiveRange(min, max) * padding;
         minValue = min - paddedRange / 2;
         maxValue = max + paddedRange / 2;
      }
      return new AxisRange(minValue, maxValue, axisLength, calculateNewScale(axisLength, minValue, maxValue), 1.0);
   }

   /**
    * When {@code true} zero is always included in the visible range. This only has effect if
    * {@link #autoRangingProperty() auto-ranging} is on.
    *
    * @return forceZeroInRange property
    */
   public BooleanProperty forceZeroInRangeProperty()
   {
      return forceZeroInRange;
   }

   /**
    * Returns the value of the {@link #autoRangePaddingProperty()}.
    *
    * @return the auto range padding
    */
   @Override
   public double getAutoRangePadding()
   {
      return autoRangePaddingProperty().get();
   }

   @Override
   protected AxisRange getAxisRange()
   {
      final AxisRange localRange = super.getAxisRange();
      final double lower = localRange.getLowerBound();
      final double upper = localRange.getUpperBound();
      final double axisLength = localRange.getAxisLength();
      final double scale = localRange.getScale();
      return new AxisRange(lower, upper, axisLength, scale, 1.0);
   }

   @Override
   public AxisTransform getAxisTransform()
   {
      return null;
   }

   @Override
   public List<CssMetaData<? extends Styleable, ?>> getCssMetaData()
   {
      return InvisibleNumericAxis.getClassCssMetaData();
   }

   /**
    * Get the display position along this axis for a given value. If the value is not in the current
    * range, the returned value will be an extrapolation of the display position. -- cached double
    * optimised version (shaves of 50% on delays)
    *
    * @param value The data value to work out display position for
    * @return display position
    */
   @Override
   public double getDisplayPosition(final double value)
   {
      return localOffset + (value - localCurrentLowerBound) * localScale;
   }

   @Override
   public LogAxisType getLogAxisType()
   {
      return LogAxisType.LINEAR_SCALE;
   }

   /**
    * Get the data value for the given display position on this axis. If the axis is a CategoryAxis
    * this will be the nearest value. -- cached double optimised version (shaves of 50% on delays)
    *
    * @param displayPosition A pixel position on this axis
    * @return the nearest data value to the given pixel position or null if not on axis;
    */
   @Override
   public double getValueForDisplay(final double displayPosition)
   {
      return (displayPosition - localOffset) / localScale + localCurrentLowerBound;
   }

   /**
    * Get the display position of the zero line along this axis.
    *
    * @return display position or Double.NaN if zero is not in current range;
    */
   @Override
   public double getZeroPosition()
   {
      if (0 < getMin() || 0 > getMax())
      {
         return Double.NaN;
      }
      // noinspection unchecked
      return getDisplayPosition(0);
   }

   /**
    * Returns the value of the {@link #forceZeroInRangeProperty()}.
    *
    * @return value of the forceZeroInRange property
    */
   public boolean isForceZeroInRange()
   {
      return forceZeroInRange.getValue();
   }

   @Override
   public boolean isLogAxis()
   {
      return false;
   }

   /**
    * Checks if the given value is plottable on this axis
    *
    * @param value The value to check if its on axis
    * @return true if the given value is plottable on this axis
    */
   @Override
   public boolean isValueOnAxis(final double value)
   {
      return value >= getMin() && value <= getMax();
   }

   /**
    * Sets the value of the {@link #autoRangePaddingProperty()}
    *
    * @param padding padding factor
    */
   @Override
   public void setAutoRangePadding(final double padding)
   {
      autoRangePaddingProperty().set(padding);
   }

   /**
    * Sets the value of the {@link #forceZeroInRangeProperty()}.
    *
    * @param value if {@code true}, zero is always included in the visible range
    */
   public void setForceZeroInRange(final boolean value)
   {
      forceZeroInRange.setValue(value);
   }

   @Override
   protected void setRange(final AxisRange range, final boolean animate)
   {
      super.setRange(range, animate);
   }

   // -------------- STYLESHEET HANDLING
   // ------------------------------------------------------------------------------

   /**
    * If padding pushed the bound above or below zero - stick it to zero.
    * 
    * @param paddedBound padded bounds
    * @param bound       computed raw bounds
    * @return clamped bounds
    */
   private static double clampBoundToZero(final double paddedBound, final double bound)
   {
      if (paddedBound < 0 && bound >= 0 || paddedBound > 0 && bound <= 0)
      {
         return 0;
      }
      return paddedBound;
   }

   private static double getEffectiveRange(final double min, final double max)
   {
      double effectiveRange = max - min;
      if (effectiveRange == 0)
      {
         effectiveRange = min == 0 ? InvisibleNumericAxis.DEFAULT_RANGE_LENGTH : Math.abs(min);
      }
      return effectiveRange;
   }

   public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData()
   {
      return StyleableProperties.STYLEABLES;
   }

   private static class StyleableProperties
   {
      private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

      static
      {
         final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Region.getClassCssMetaData());
         STYLEABLES = Collections.unmodifiableList(styleables);
      }
   }
}