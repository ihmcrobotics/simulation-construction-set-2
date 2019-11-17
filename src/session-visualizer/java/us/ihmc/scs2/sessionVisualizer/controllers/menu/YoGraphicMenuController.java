package us.ihmc.scs2.sessionVisualizer.controllers.menu;

import java.io.File;
import java.io.IOException;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckMenuItem;
import javafx.stage.Stage;
import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.scs2.sessionVisualizer.SCSGUITopics;
import us.ihmc.scs2.sessionVisualizer.SessionVisualizerIOTools;
import us.ihmc.scs2.sessionVisualizer.controllers.yoGraphic.YoGraphicPropertyWindowController;
import us.ihmc.scs2.sessionVisualizer.managers.SessionVisualizerToolkit;

public class YoGraphicMenuController
{
   @FXML
   private CheckMenuItem overheadPlotterMenuItem;

   private final ObjectProperty<YoGraphicPropertyWindowController> activeControllerProperty = new SimpleObjectProperty<>(this, "activeController", null);

   private SessionVisualizerToolkit toolkit;
   private Stage mainWindow;
   private JavaFXMessager messager;
   private SCSGUITopics topics;

   public void initialize(SessionVisualizerToolkit toolkit)
   {
      this.toolkit = toolkit;

      messager = toolkit.getMessager();
      topics = toolkit.getTopics();

      mainWindow = toolkit.getMainWindow();
      messager.bindBidirectional(topics.getShowOverheadPlotter(), overheadPlotterMenuItem.selectedProperty(), false);
   }

   @FXML
   private void loadYoGraphic()
   {
      File result = SessionVisualizerIOTools.yoGraphicConfigurationOpenFileDialog(mainWindow);
      if (result != null)
         messager.submitMessage(topics.getYoGraphicLoadRequest(), result);
   }

   @FXML
   private void saveYoGraphic()
   {
      File result = SessionVisualizerIOTools.yoGraphicConfigurationSaveFileDialog(mainWindow);
      if (result != null)
         messager.submitMessage(topics.getYoGraphicSaveRequest(), result);
   }

   @FXML
   private void openYoGraphicEditor()
   {
      if (activeControllerProperty.get() != null)
      {
         activeControllerProperty.get().showWindow();
         return;
      }

      try
      {
         FXMLLoader fxmlLoader = new FXMLLoader(SessionVisualizerIOTools.YO_GRAPHIC_PROPERTY_URL);
         fxmlLoader.load();
         YoGraphicPropertyWindowController controller = fxmlLoader.getController();
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
