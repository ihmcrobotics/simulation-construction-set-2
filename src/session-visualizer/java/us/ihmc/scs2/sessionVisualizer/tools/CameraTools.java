package us.ihmc.scs2.sessionVisualizer.tools;

import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.control.MenuItem;
import javafx.scene.input.PickResult;
import us.ihmc.javaFXToolkit.cameraControllers.FocusBasedCameraMouseEventHandler;

public class CameraTools
{
   public static void setupNodeTrackingContextMenu(FocusBasedCameraMouseEventHandler cameraController, SubScene ownerSubScene)
   {
      ObjectProperty<Node> nodeTracked = cameraController.getNodeTracker().nodeToTrackProperty();

      ContextMenuTools.setupContextMenu(ownerSubScene, (owner, event) ->
      {
         PickResult pickResult = event.getPickResult();
         Node intersectedNode = pickResult.getIntersectedNode();
         if (intersectedNode == null || intersectedNode instanceof SubScene || intersectedNode == nodeTracked.get())
            return null;
         MenuItem menuItem = new MenuItem("Start tracking node: " + intersectedNode.getId());
         menuItem.setOnAction(e -> nodeTracked.set(intersectedNode));
         return menuItem;
      }, (owner, event) ->
      {
         if (nodeTracked.get() == null)
            return null;
         MenuItem menuItem = new MenuItem("Stop tracking node: " + nodeTracked.get().getId());
         menuItem.setOnAction(e -> nodeTracked.set(null));
         return menuItem;
      });
   }
}
