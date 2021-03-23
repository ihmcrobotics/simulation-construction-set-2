package us.ihmc.scs2.sessionVisualizer.jfx.controllers.menu;

import java.io.File;
import java.io.IOException;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckMenuItem;
import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerIOTools;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerTopics;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.YoGraphicPropertyWindowController;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerWindowToolkit;

public class YoGraphicMenuController
{
   @FXML
   private CheckMenuItem overheadPlotterMenuItem;

   private final ObjectProperty<YoGraphicPropertyWindowController> activeControllerProperty = new SimpleObjectProperty<>(this, "activeController", null);

   private SessionVisualizerWindowToolkit toolkit;
   private JavaFXMessager messager;
   private SessionVisualizerTopics topics;

   public void initialize(SessionVisualizerWindowToolkit toolkit)
   {
      this.toolkit = toolkit;

      messager = toolkit.getMessager();
      topics = toolkit.getTopics();

      messager.bindBidirectional(topics.getShowOverheadPlotter(), overheadPlotterMenuItem.selectedProperty(), false);
   }

   @FXML
   private void loadYoGraphic()
   {
      File result = SessionVisualizerIOTools.yoGraphicConfigurationOpenFileDialog(toolkit.getWindow());
      if (result != null)
         messager.submitMessage(topics.getYoGraphicLoadRequest(), result);
   }

   @FXML
   private void saveYoGraphic()
   {
      File result = SessionVisualizerIOTools.yoGraphicConfigurationSaveFileDialog(toolkit.getWindow());
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
