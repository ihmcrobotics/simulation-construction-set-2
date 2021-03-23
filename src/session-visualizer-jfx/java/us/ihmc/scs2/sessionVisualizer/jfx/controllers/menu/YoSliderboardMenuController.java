package us.ihmc.scs2.sessionVisualizer.jfx.controllers.menu;

import java.io.IOException;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import us.ihmc.scs2.session.SessionState;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerIOTools;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.sliderboard.bcf2000.YoBCF2000SliderboardWindowController;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerWindowToolkit;

public class YoSliderboardMenuController
{
   private final ObjectProperty<YoBCF2000SliderboardWindowController> activeControllerProperty = new SimpleObjectProperty<>(this, "activeController", null);

   private SessionVisualizerWindowToolkit toolkit;

   public void initialize(SessionVisualizerWindowToolkit toolkit)
   {
      this.toolkit = toolkit;
      toolkit.getMessager().registerTopicListener(toolkit.getTopics().getSessionCurrentState(), m ->
      {
         if (m == SessionState.INACTIVE)
            activeControllerProperty.set(null);
      });
   }

   @FXML
   public void openBCF2000SliderboardWindow()
   {
      if (activeControllerProperty.get() != null)
      {
         activeControllerProperty.get().showWindow();
         return;
      }

      try
      {
         FXMLLoader fxmlLoader = new FXMLLoader(SessionVisualizerIOTools.YO_SLIDERBOARD_BCF2000_WINDOW_URL);
         fxmlLoader.load();
         YoBCF2000SliderboardWindowController controller = fxmlLoader.getController();
         controller.initialize(toolkit);
         activeControllerProperty.set(controller);
         controller.showWindow();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }
}
