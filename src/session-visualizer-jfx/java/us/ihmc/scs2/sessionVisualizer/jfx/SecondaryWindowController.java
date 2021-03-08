package us.ihmc.scs2.sessionVisualizer.jfx;

import javafx.fxml.FXML;
import javafx.stage.Window;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.SecondaryWindowControlsController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.menu.MainWindowMenuBarController;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;

public class SecondaryWindowController
{
   @FXML
   private MainWindowMenuBarController menuController;
   @FXML
   private SecondaryWindowControlsController controlsController;

   public void initialize(SessionVisualizerToolkit toolkit, Window owner)
   {
      menuController.initialize(toolkit, owner);
      controlsController.initialize(toolkit);
   }

   public void start()
   {
   }

   public void stop()
   {
   }
}