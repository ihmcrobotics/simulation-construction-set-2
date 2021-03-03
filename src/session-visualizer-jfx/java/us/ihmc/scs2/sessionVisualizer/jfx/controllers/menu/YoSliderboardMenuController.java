package us.ihmc.scs2.sessionVisualizer.jfx.controllers.menu;

import java.io.IOException;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerIOTools;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.sliderboard.YoSliderboardWindowController;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;

public class YoSliderboardMenuController
{
   private final ObjectProperty<YoSliderboardWindowController> activeControllerProperty = new SimpleObjectProperty<>(this, "activeController", null);

   private SessionVisualizerToolkit toolkit;
   private Stage mainWindow;

   public void initialize(SessionVisualizerToolkit toolkit)
   {
      this.toolkit = toolkit;
      mainWindow = toolkit.getMainWindow();
   }

   @FXML
   public void openSliderboardWindow()
   {
      if (activeControllerProperty.get() != null)
      {
         activeControllerProperty.get().showWindow();
         return;
      }

      try
      {
         FXMLLoader fxmlLoader = new FXMLLoader(SessionVisualizerIOTools.YO_SLIDERBOARD_WINDOW_URL);
         fxmlLoader.load();
         YoSliderboardWindowController controller = fxmlLoader.getController();
         controller.initialize(toolkit, mainWindow);
         activeControllerProperty.set(controller);
         controller.showWindow();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }
}
