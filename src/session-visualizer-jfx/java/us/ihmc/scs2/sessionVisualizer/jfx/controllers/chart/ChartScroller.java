package us.ihmc.scs2.sessionVisualizer.jfx.controllers.chart;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import de.gsi.chart.plugins.ChartPlugin;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.EventHandler;
import javafx.scene.input.ScrollEvent;
import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerTopics;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;

public class ChartScroller extends ChartPlugin
{
   private final IntegerProperty smallIncrement = new SimpleIntegerProperty(this, "smallIncrementProperty", 1);
   private final IntegerProperty largeIncrement = new SimpleIntegerProperty(this, "largeIncrementProperty", 10);

   private Predicate<ScrollEvent> scrollFilter = null;
   private Predicate<ScrollEvent> largeIncrementFilter = event -> event.isControlDown() && !event.isAltDown() && !event.isMetaDown() && !event.isShiftDown();
   private final EventHandler<ScrollEvent> incrementBufferIndexHandler = event -> handleScrollEvent(event);

   private final AtomicReference<YoBufferPropertiesReadOnly> bufferProperties;
   private final SessionVisualizerTopics topics;
   private final JavaFXMessager messager;

   public ChartScroller(SessionVisualizerToolkit toolkit)
   {
      topics = toolkit.getTopics();
      messager = toolkit.getMessager();
      bufferProperties = messager.createInput(topics.getYoBufferCurrentProperties());

      registerInputEventHandler(ScrollEvent.ANY, incrementBufferIndexHandler);
   }

   public IntegerProperty smallIncrementProperty()
   {
      return smallIncrement;
   }

   public int getSmallIncrement()
   {
      return smallIncrement.get();
   }

   public void setSmallIncrement(int smallIncrement)
   {
      this.smallIncrement.set(smallIncrement);
   }

   public IntegerProperty largeIncrementProperty()
   {
      return largeIncrement;
   }

   public int getLargeIncrement()
   {
      return largeIncrement.get();
   }

   public void setLargeIncrement(int largeIncrement)
   {
      this.largeIncrement.set(largeIncrement);
   }

   public void setScrollFilter(final Predicate<ScrollEvent> scrollFilter)
   {
      Objects.requireNonNull(scrollFilter, "The filter cannot be null");
      this.scrollFilter = scrollFilter;
   }

   public void setLargeIncrementFilter(Predicate<ScrollEvent> largeIncrementFilter)
   {
      this.largeIncrementFilter = largeIncrementFilter;
   }

   public Predicate<ScrollEvent> getScrollFilter()
   {
      return scrollFilter;
   }

   public Predicate<ScrollEvent> getLargeIncrementFilter()
   {
      return largeIncrementFilter;
   }

   private void handleScrollEvent(ScrollEvent event)
   {
      if (bufferProperties.get() == null)
         return;

      if (scrollFilter != null && !scrollFilter.test(event))
         return;

      if (event.getDeltaY() == 0.0)
         return;

      int scrollDelta;

      if (largeIncrementFilter == null || largeIncrementFilter.test(event))
         scrollDelta = largeIncrement.get();
      else
         scrollDelta = smallIncrement.get();

      if (event.getDeltaY() < 0.0)
         messager.submitMessage(topics.getYoBufferDecrementCurrentIndexRequest(), scrollDelta);
      else
         messager.submitMessage(topics.getYoBufferIncrementCurrentIndexRequest(), scrollDelta);
   }

   @Override
   protected void finalize() throws Throwable
   {
      messager.removeInput(topics.getYoBufferCurrentProperties(), bufferProperties);
   }
}
