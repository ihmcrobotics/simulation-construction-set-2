package us.ihmc.scs2.sessionVisualizer.jfx.controllers.menu;

import java.io.File;
import java.io.IOException;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerIOTools;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerTopics;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoComposite.pattern.YoCompositePatternPropertyWindowController;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;

public class YoCompositeMenuController
{
   private final ObjectProperty<YoCompositePatternPropertyWindowController> activeControllerProperty = new SimpleObjectProperty<>(this,
                                                                                                                                  "activeController",
                                                                                                                                  null);

   private SessionVisualizerToolkit toolkit;
   private JavaFXMessager messager;
   private SessionVisualizerTopics topics;
   private Stage mainWindow;

   public void initialize(SessionVisualizerToolkit toolkit)
   {
      this.toolkit = toolkit;

      messager = toolkit.getMessager();
      topics = toolkit.getTopics();
      mainWindow = toolkit.getMainWindow();
   }

   @FXML
   public void openYoCompositePatternEditor()
   {
      if (activeControllerProperty.get() != null)
      {
         activeControllerProperty.get().showWindow();
         return;
      }

      try
      {
         FXMLLoader fxmlLoader = new FXMLLoader(SessionVisualizerIOTools.YO_COMPOSITE_PATTERN_PROPERTY_WINDOW_URL);
         fxmlLoader.load();
         YoCompositePatternPropertyWindowController controller = fxmlLoader.getController();
         controller.initialize(toolkit, mainWindow);
         activeControllerProperty.set(controller);
         controller.showWindow();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   @FXML
   public void loadYoCompositePattern()
   {
      File result = SessionVisualizerIOTools.yoCompositeConfigurationOpenFileDialog(mainWindow);
      if (result != null)
         messager.submitMessage(topics.getYoCompositePatternLoadRequest(), result);
   }

   @FXML
   public void saveYoCompositePattern()
   {
      File result = SessionVisualizerIOTools.yoCompositeConfigurationSaveFileDialog(mainWindow);
      if (result != null)
         messager.submitMessage(topics.getYoCompositePatternSaveRequest(), result);
   }

   @FXML
   public void refreshAllYoComposite()
   {
      toolkit.getYoCompositeSearchManager().refreshYoCompositesInBackground();
   }
}