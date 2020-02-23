package us.ihmc.scs2.sessionVisualizer.controllers.chart;

import java.util.function.Predicate;

import de.gsi.chart.Chart;
import de.gsi.chart.XYChart;
import de.gsi.chart.axes.Axis;
import de.gsi.chart.plugins.ChartPlugin;
import de.gsi.chart.plugins.MouseEventsHelper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.scs2.sessionVisualizer.SessionVisualizerTopics;
import us.ihmc.scs2.sessionVisualizer.managers.SessionVisualizerToolkit;

public class ChartHorizontalPanner extends ChartPlugin
{
   /**
    * Default pan mouse filter passing on left mouse button with {@link MouseEvent#isControlDown()
    * control key down}.
    */
   public static final Predicate<MouseEvent> DEFAULT_MOUSE_FILTER = event -> MouseEventsHelper.isOnlySecondaryButtonDown(event);

   private Predicate<MouseEvent> mouseFilter = ChartHorizontalPanner.DEFAULT_MOUSE_FILTER;
   private Point2D previousMouseLocation = null;

   private Cursor originalCursor;

   private final ObjectProperty<Cursor> dragCursor = new SimpleObjectProperty<>(this, "dragCursor");

   private final EventHandler<MouseEvent> panStartHandler = event ->
   {
      if (mouseFilter == null || mouseFilter.test(event))
      {
         panStarted(event);
         event.consume();
      }
   };

   private final EventHandler<MouseEvent> panDragHandler = event ->
   {
      if (panOngoing())
      {
         panDragged(event);
         event.consume();
      }
   };

   private final EventHandler<MouseEvent> panEndHandler = event ->
   {
      if (panOngoing())
      {
         panEnded();
         event.consume();
      }
   };

   private SessionVisualizerTopics topics;
   private JavaFXMessager messager;

   public ChartHorizontalPanner(SessionVisualizerToolkit toolkit)
   {
      topics = toolkit.getTopics();
      messager = toolkit.getMessager();

      setDragCursor(Cursor.CLOSED_HAND);
      registerMouseHandlers();
   }

   /**
    * Mouse cursor to be used during drag operation.
    *
    * @return the mouse cursor property
    */
   public final ObjectProperty<Cursor> dragCursorProperty()
   {
      return dragCursor;
   }

   /**
    * Returns the value of the {@link #dragCursorProperty()}
    *
    * @return the current cursor
    */
   public final Cursor getDragCursor()
   {
      return dragCursorProperty().get();
   }

   /**
    * Returns MouseEvent filter triggering pan operation.
    *
    * @return filter used to test whether given MouseEvent should start panning operation
    * @see #setMouseFilter(Predicate)
    */
   public Predicate<MouseEvent> getMouseFilter()
   {
      return mouseFilter;
   }

   private void installCursor()
   {
      originalCursor = getChart().getCursor();
      if (getDragCursor() != null)
      {
         getChart().setCursor(getDragCursor());
      }
   }

   private void panChart(final Chart chart, final Point2D mouseLocation)
   {
      if (!(chart instanceof XYChart))
         return;

      XYChart xyChart = (XYChart) chart;
      Axis xAxis = xyChart.getXAxis();

      final double prevData = xAxis.getValueForDisplay(previousMouseLocation.getX());
      final double newData = xAxis.getValueForDisplay(mouseLocation.getX());
      final double offset = prevData - newData;

      messager.submitMessage(topics.getYoChartRequestShift(), (int) offset);
   }

   private void panDragged(final MouseEvent event)
   {
      final Point2D mouseLocation = getLocationInPlotArea(event);
      panChart(getChart(), mouseLocation);
      previousMouseLocation = mouseLocation;
   }

   private void panEnded()
   {
      previousMouseLocation = null;
      uninstallCursor();
   }

   private boolean panOngoing()
   {
      return previousMouseLocation != null;
   }

   private void panStarted(final MouseEvent event)
   {
      previousMouseLocation = getLocationInPlotArea(event);
      installCursor();
   }

   private void registerMouseHandlers()
   {
      registerInputEventHandler(MouseEvent.MOUSE_PRESSED, panStartHandler);
      registerInputEventHandler(MouseEvent.MOUSE_DRAGGED, panDragHandler);
      registerInputEventHandler(MouseEvent.MOUSE_RELEASED, panEndHandler);
   }

   /**
    * Sets value of the {@link #dragCursorProperty()}.
    *
    * @param cursor the cursor to be used by the plugin
    */
   public final void setDragCursor(final Cursor cursor)
   {
      dragCursorProperty().set(cursor);
   }

   /**
    * Sets the filter determining whether given MouseEvent triggered on {@link MouseEvent#DRAG_DETECTED
    * event type} should start the panning operation.
    * <p>
    * By default it is initialized to {@link #DEFAULT_MOUSE_FILTER}.
    *
    * @param mouseFilter the mouse filter to be used. Can be set to {@code null} to start panning on
    *                    any {@link MouseEvent#DRAG_DETECTED DRAG_DETECTED} event.
    */
   public void setMouseFilter(final Predicate<MouseEvent> mouseFilter)
   {
      this.mouseFilter = mouseFilter;
   }

   private void uninstallCursor()
   {
      getChart().setCursor(originalCursor);
   }
}
