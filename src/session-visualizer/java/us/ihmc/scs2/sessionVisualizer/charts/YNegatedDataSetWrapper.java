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

public class YNegatedDataSetWrapper implements DataSet2D
{
   private static final long serialVersionUID = 7639501422824365887L;

   private final DataSet2D input;

   private final AxisDescription negatedYAxisDescription;

   private final List<AxisDescription> axesDescriptions = new ArrayList<>();

   public YNegatedDataSetWrapper(DataSet2D input)
   {
      this.input = input;
      negatedYAxisDescription = new DefaultAxisDescription(this, "y-Axis", "a.u.");
      axesDescriptions.add(input.getAxisDescription(DIM_X));
      axesDescriptions.add(negatedYAxisDescription);

   }

   @Override
   public double get(int dimIndex, int index)
   {
      if (dimIndex == DIM_Y)
         return -input.get(dimIndex, index);
      else
         return input.get(dimIndex, index);
   }

   @Override
   public int getIndex(int dimIndex, double value)
   {
      if (dimIndex == DIM_Y)
         return input.getIndex(dimIndex, -value);
      else
         return input.getIndex(dimIndex, value);
   }

   @Override
   public List<AxisDescription> getAxisDescriptions()
   {
      return axesDescriptions;
   }

   @Override
   public YNegatedDataSetWrapper recomputeLimits(int dimension)
   {
      input.recomputeLimits(dimension);
      if (dimension == 1)
      {
         negatedYAxisDescription.clear();
         negatedYAxisDescription.set(-input.getAxisDescription(DIM_Y).getMax(), -input.getAxisDescription(DIM_Y).getMin());
      }
      return this;
   }

   // Unaffected methods

   @Override
   public AtomicBoolean autoNotification()
   {
      return input.autoNotification();
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
   public YNegatedDataSetWrapper setStyle(String style)
   {
      input.setStyle(style);
      return this;
   }
}
