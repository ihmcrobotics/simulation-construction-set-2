package us.ihmc.scs2.sessionVisualizer.jfx.charts;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import us.ihmc.euclid.tuple2D.Point2D;

public class DataEntry
{
   private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

   private final Property<ChartIntegerBounds> xBoundsProperty = new SimpleObjectProperty<ChartIntegerBounds>(this, "xBounds", null);
   private final Property<ChartDoubleBounds> yBoundsProperty = new SimpleObjectProperty<ChartDoubleBounds>(this, "yBounds", null);
   private final List<Point2D> data;
   private final IntegerProperty bufferCurrentIndexProperty = new SimpleIntegerProperty(this, "bufferCurrentIndex", -1);

   private final BooleanProperty dirtyProperty = new SimpleBooleanProperty(this, "dirtyFlag", false);

   public DataEntry()
   {
      data = new ArrayList<>();
   }

   public DataEntry(ChartIntegerBounds xBounds, ChartDoubleBounds yBounds, List<Point2D> data, int currentIndex)
   {
      xBoundsProperty.setValue(xBounds);
      yBoundsProperty.setValue(yBounds);
      this.data = data;
      bufferCurrentIndexProperty.set(currentIndex);
   }

   public ReentrantReadWriteLock getLock()
   {
      return lock;
   }

   public Property<ChartIntegerBounds> xBoundsProperty()
   {
      return xBoundsProperty;
   }

   public Property<ChartDoubleBounds> yBoundsProperty()
   {
      return yBoundsProperty;
   }

   public List<Point2D> getData()
   {
      return data;
   }

   public IntegerProperty bufferCurrentIndexProperty()
   {
      return bufferCurrentIndexProperty;
   }

   public void markDirty()
   {
      dirtyProperty.set(true);
   }

   public boolean peekDirty()
   {
      return dirtyProperty.get();
   }

   public boolean pollDirty()
   {
      boolean result = dirtyProperty.get();
      dirtyProperty.set(false);
      return result;
   }

   public BooleanProperty dirtyProperty()
   {
      return dirtyProperty;
   }
}
