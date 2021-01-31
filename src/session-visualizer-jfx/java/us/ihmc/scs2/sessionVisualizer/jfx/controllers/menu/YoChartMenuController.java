package us.ihmc.scs2.sessionVisualizer.jfx.controllers.menu;

import java.io.File;
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Pair;
import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.scs2.session.SessionState;
import us.ihmc.scs2.sessionVisualizer.jfx.SecondaryWindowController;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerIOTools;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerTopics;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.chart.YoChartGroupPanelController;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;

public class YoChartMenuController
{
   private SessionVisualizerToolkit toolkit;
   private Window owner;
   private SessionVisualizerTopics topics;
   private JavaFXMessager messager;

   public void initialize(SessionVisualizerToolkit toolkit, Window owner)
   {
      this.toolkit = toolkit;
      this.owner = owner;
      topics = toolkit.getTopics();
      messager = toolkit.getMessager();
   }

   @FXML
   public void loadChartGroup()
   {
      File result = SessionVisualizerIOTools.yoChartConfigurationOpenFileDialog(owner);
      if (result != null)
         messager.submitMessage(topics.getYoChartGroupLoadConfiguration(), new Pair<>(owner, result));
   }

   @FXML
   public void saveChartGroup()
   {
      File result = SessionVisualizerIOTools.yoChartConfigurationSaveFileDialog(owner);
      if (result != null)
         messager.submitMessage(topics.getYoChartGroupSaveConfiguration(), new Pair<>(owner, result));
   }

   @FXML
   public void newChartWindow()
   {
      Stage newWindow = newSecondaryChartWindow(toolkit);
      newWindow.show();
   }

   public static Stage newSecondaryChartWindow(SessionVisualizerToolkit toolkit)
   {
      Stage stage = new Stage();
      FXMLLoader loader;

      try
      {
         // Loading template for secondary window
         loader = new FXMLLoader(SessionVisualizerIOTools.SECONDARY_WINDOW_URL);
         VBox mainNode = loader.load();
         SecondaryWindowController controller = loader.getController();
         controller.initialize(toolkit, stage);
         controller.start();

         // Loading the chart pane & controller
         loader = new FXMLLoader(SessionVisualizerIOTools.CHART_GROUP_PANEL_URL);
         AnchorPane chartGroupPane = loader.load();
         YoChartGroupPanelController chartGroupController = loader.getController();

         chartGroupController.initialize(toolkit, stage);
         chartGroupController.start();
         chartGroupController.maximumRowProperty().set(6);
         chartGroupController.maximumColProperty().set(4);

         stage.getIcons().add(SessionVisualizerIOTools.SCS_ICON_IMAGE);

         mainNode.getChildren().set(1, chartGroupPane);
         VBox.setVgrow(chartGroupPane, Priority.ALWAYS);
         Scene scene = new Scene(mainNode, 1024, 768);
         stage.setScene(scene);
         stage.setTitle("Chart window");
         stage.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, e ->
         {
            controller.stop();
            stage.close();
            toolkit.removeSecondaryWindow(stage);
            toolkit.removeYoChartGroupController(chartGroupController);
            chartGroupController.close();
         });
         toolkit.getMessager().registerJavaFXSyncedTopicListener(toolkit.getTopics().getSessionCurrentState(), state ->
         {
            if (state == SessionState.INACTIVE)
            {
               controller.stop();
               stage.close();
               toolkit.removeSecondaryWindow(stage);
               toolkit.removeYoChartGroupController(chartGroupController);
               chartGroupController.close();
            }
         });
         toolkit.addSecondaryWindow(stage);
         toolkit.addYoChartGroupController(chartGroupController);

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
