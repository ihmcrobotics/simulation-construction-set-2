package us.ihmc.scs2.sessionVisualizer.charts;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import de.gsi.dataset.AxisDescription;
import de.gsi.dataset.DataSet;
import de.gsi.dataset.DataSet2D;
import de.gsi.dataset.event.EventListener;
import de.gsi.dataset.locks.DataSetLock;
import de.gsi.dataset.spi.DefaultAxisDescription;

public class YNormalizedDataSetWrapper implements DataSet2D
{
   private static final long serialVersionUID = -1627795247131961107L;

   private final DataSet2D input;

   private final AxisDescription normalizedYAxisDescription;

   private final List<AxisDescription> axesDescriptions = new ArrayList<>();

   public YNormalizedDataSetWrapper(DataSet2D input)
   {
      this.input = input;

      normalizedYAxisDescription = new DefaultAxisDescription(this, "y-Axis", "a.u.");
      axesDescriptions.add(input.getAxisDescription(DIM_X));
      axesDescriptions.add(normalizedYAxisDescription);

   }

   private double normalizeYValue(double rawValue)
   {
      double min = input.getAxisDescription(DIM_Y).getMin();
      double max = input.getAxisDescription(DIM_Y).getMax();
      double value = rawValue - min;

      if (min == max)
         return value;
      else
         return value / (max - min);
   }

   private double denormalizeYValue(double normalizedValue)
   {
      double min = input.getAxisDescription(DIM_Y).getMin();
      double max = input.getAxisDescription(DIM_Y).getMax();

      if (min == max)
         return normalizedValue + min;
      else
         return normalizedValue * (max - min) + min;
   }

   @Override
   public double get(int dimIndex, int index)
   {
      if (dimIndex == DIM_Y)
         return normalizeYValue(input.get(dimIndex, index));
      else
         return input.get(dimIndex, index);
   }

   @Override
   public int getIndex(int dimIndex, double value)
   {
      if (dimIndex == DIM_Y)
         return input.getIndex(dimIndex, denormalizeYValue(value));
      else
         return input.getIndex(dimIndex, value);
   }

   @Override
   public List<AxisDescription> getAxisDescriptions()
   {
      return axesDescriptions;
   }

   // Unaffected methods

   @Override
   public AtomicBoolean autoNotification()
   {
      return input.autoNotification();
   }

   @Override
   public YNormalizedDataSetWrapper recomputeLimits(int dimension)
   {
      input.recomputeLimits(dimension);
      if (dimension == 1)
      {
         normalizedYAxisDescription.clear();
         normalizedYAxisDescription.setMin(0.0);
         normalizedYAxisDescription.setMax(1.0);
      }
      return this;
   }

   @Override
   public List<EventListener> updateEventListener()
   {
      return input.updateEventListener();
   }

   @Override
   public int getDataCount()
   {
      return input.getDataCount();
   }

   @Override
   public String getDataLabel(int index)
   {
      return input.getDataLabel(index);
   }

   @Override
   public String getName()
   {
      return input.getName();
   }

   @Override
   public String getStyle()
   {
      return input.getStyle();
   }

   @Override
   public String getStyle(int index)
   {
      return input.getStyle(index);
   }

   @Override
   public <D extends DataSet> DataSetLock<D> lock()
   {
      return input.lock();
   }

   @Override
   public YNormalizedDataSetWrapper setStyle(String style)
   {
      input.setStyle(style);
      return this;
   }
}
