package us.ihmc.scs2.sessionVisualizer.controllers.chart;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import de.gsi.chart.XYChart;
import de.gsi.chart.plugins.ChartPlugin;
import de.gsi.chart.plugins.MouseEventsHelper;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import us.ihmc.commons.MathTools;
import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.scs2.sessionVisualizer.SessionVisualizerTopics;
import us.ihmc.scs2.sessionVisualizer.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;

public class ChartScrubber extends ChartPlugin
{
   public static final Predicate<MouseEvent> DEFAULT_MOUSE_FILTER = event -> MouseEventsHelper.isOnlyPrimaryButtonDown(event);

   private Predicate<MouseEvent> mouseFilter = DEFAULT_MOUSE_FILTER;
   private final EventHandler<MouseEvent> setBufferIndexHandler = event -> handleMouseEvent(event);

   private final AtomicReference<YoBufferPropertiesReadOnly> bufferProperties;
   private final SessionVisualizerTopics topics;
   private final JavaFXMessager messager;

   public ChartScrubber(SessionVisualizerToolkit toolkit)
   {
      topics = toolkit.getTopics();
      messager = toolkit.getMessager();
      bufferProperties = messager.createInput(topics.getYoBufferCurrentProperties());

      registerInputEventHandler(MouseEvent.MOUSE_PRESSED, setBufferIndexHandler);
      registerInputEventHandler(MouseEvent.MOUSE_DRAGGED, setBufferIndexHandler);
   }

   /**
    * Sets the filter determining whether given {@link MouseEvent} should start scrubbing through the
    * data.
    * <p>
    * By default it is initialized to {@link #DEFAULT_MOUSE_FILTER}.
    * </p>
    *
    * @param mouseFilter the mouse filter to be used.
    */
   public void setMouseFilter(final Predicate<MouseEvent> mouseFilter)
   {
      Objects.requireNonNull(mouseFilter, "The filter cannot be null");
      this.mouseFilter = mouseFilter;
   }

   /**
    * Returns {@link MouseEvent} filter for scrubbing in the buffer.
    *
    * @return filter used to test whether given MouseEvent should start panning operation
    * @see #setMouseFilter(Predicate)
    */
   public Predicate<MouseEvent> getMouseFilter()
   {
      return mouseFilter;
   }

   private void handleMouseEvent(MouseEvent event)
   {
      if (!(getChart() instanceof XYChart))
         return;

      if (bufferProperties.get() == null)
         return;

      if (mouseFilter.test(event))
      {
         Node intersectedNode = event.getPickResult().getIntersectedNode();

         if (intersectedNode == null)
            return;

         double xLocal = getLocationInPlotArea(event).getX();
         int index = (int) Math.round(((XYChart) getChart()).getXAxis().getValueForDisplay(xLocal));
         index = MathTools.clamp(index, 0, bufferProperties.get().getSize());

         messager.submitMessage(topics.getYoBufferCurrentIndexRequest(), index);
         // TODO Not sure if the event should be consumed. Check if interfering with closing the context menu.
         event.consume();
      }
   }

   @Override
   protected void finalize() throws Throwable
   {
      messager.removeInput(topics.getYoBufferCurrentProperties(), bufferProperties);
   }
}
