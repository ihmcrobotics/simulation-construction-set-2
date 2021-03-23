package us.ihmc.scs2.sessionVisualizer.jfx.controllers.menu;

import java.io.File;
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.util.Pair;
import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.scs2.sessionVisualizer.jfx.SecondaryWindowController;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerIOTools;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerTopics;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerWindowToolkit;

public class YoChartMenuController
{
   private SessionVisualizerWindowToolkit toolkit;
   private SessionVisualizerTopics topics;
   private JavaFXMessager messager;

   public void initialize(SessionVisualizerWindowToolkit toolkit)
   {
      this.toolkit = toolkit;
      topics = toolkit.getTopics();
      messager = toolkit.getMessager();
   }

   @FXML
   public void loadChartGroup()
   {
      File result = SessionVisualizerIOTools.yoChartConfigurationOpenFileDialog(toolkit.getWindow());
      if (result != null)
         messager.submitMessage(topics.getYoChartGroupLoadConfiguration(), new Pair<>(toolkit.getWindow(), result));
   }

   @FXML
   public void saveChartGroup()
   {
      File result = SessionVisualizerIOTools.yoChartConfigurationSaveFileDialog(toolkit.getWindow());
      if (result != null)
         messager.submitMessage(topics.getYoChartGroupSaveConfiguration(), new Pair<>(toolkit.getWindow(), result));
   }

   @FXML
   public void newChartWindow()
   {
      Stage newWindow = newSecondaryChartWindow(toolkit.getGlobalToolkit());
      newWindow.show();
   }

   public static Stage newSecondaryChartWindow(SessionVisualizerToolkit toolkit)
   {
      Stage stage = new Stage();

      try
      {
         // Loading template for secondary window
         FXMLLoader loader = new FXMLLoader(SessionVisualizerIOTools.SECONDARY_WINDOW_URL);
         loader.load();
         SecondaryWindowController controller = loader.getController();
         controller.initialize(toolkit, stage);
         controller.setupChartGroup();
         controller.start();
         return stage;
      }
      catch (IOException e)
      {
         e.printStackTrace();
         stage.close();
         return null;
      }
   }
}
