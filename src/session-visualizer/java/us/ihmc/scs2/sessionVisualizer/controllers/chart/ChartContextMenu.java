package us.ihmc.scs2.sessionVisualizer.controllers.chart;

import java.util.function.Predicate;
import java.util.function.Supplier;

import de.gsi.chart.plugins.ChartPlugin;
import de.gsi.chart.plugins.MouseEventsHelper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class ChartContextMenu extends ChartPlugin
{
   public static final Predicate<MouseEvent> DEFAULT_MOUSE_FILTER = event -> MouseEventsHelper.isOnlySecondaryButtonDown(event);

   private Predicate<MouseEvent> mouseFilter = ChartHorizontalPanner.DEFAULT_MOUSE_FILTER;

   private Supplier<ContextMenu> contextMenuFactory = null;
   private ObjectProperty<ContextMenu> activeContextMenu = new SimpleObjectProperty<ContextMenu>(this, "activeContextMenuProperty", null);

   private final EventHandler<MouseEvent> openContextMenuHandler = event ->
   {
      if (mouseFilter != null && !mouseFilter.test(event))
         return;
      if (contextMenuFactory == null)
         return;

      ContextMenu newContextMenu = contextMenuFactory.get();

      activeContextMenu.set(newContextMenu);
      newContextMenu.show(event.getPickResult().getIntersectedNode(), event.getScreenX(), event.getScreenY());
      event.consume();
   };

   private final EventHandler<MouseEvent> closeContextMenuHandler = event ->
   {
      if (mouseFilter == null || mouseFilter.test(event))
         return;

      activeContextMenu.set(null);
   };

   public ChartContextMenu()
   {
      this(null);
   }

   public ChartContextMenu(Supplier<ContextMenu> contextMenuFactory)
   {
      activeContextMenu.addListener((o, oldValue, newValue) ->
      {
         if (oldValue != null)
            oldValue.hide();
      });
      setContextMenuFactory(contextMenuFactory);
      registerInputEventHandler(MouseEvent.MOUSE_RELEASED, openContextMenuHandler);
      registerInputEventHandler(MouseEvent.MOUSE_PRESSED, closeContextMenuHandler);
      registerInputEventHandler(ScrollEvent.ANY, event -> activeContextMenu.set(null));
   }

   public void setMouseFilter(Predicate<MouseEvent> mouseFilter)
   {
      this.mouseFilter = mouseFilter;
   }

   public void setContextMenuFactory(Supplier<ContextMenu> contextMenuFactory)
   {
      this.contextMenuFactory = contextMenuFactory;
   }
}
