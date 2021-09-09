package us.ihmc.scs2.sessionVisualizer.jfx.controllers.menu;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.SubScene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerIOTools;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerTopics;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.SessionDataExportStageController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.VideoRecordingPreviewPaneController;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerWindowToolkit;

public class FileMenuController
{
   @FXML
   private MenuItem takeSnapshotMenuItem;
   @FXML
   private MenuItem exportVideoMenuItem;
   @FXML
   private MenuItem loadConfigurationMenuItem;
   @FXML
   private MenuItem loadDefaultConfigurationMenuItem;
   @FXML
   private MenuItem saveConfigurationMenuItem;
   @FXML
   private MenuItem saveDefaultConfigurationMenuItem;
   @FXML
   private MenuItem closeMenuItem;

   private Stage owner;
   private JavaFXMessager messager;
   private SessionVisualizerTopics topics;

   private Property<VideoRecordingPreviewPaneController> videoExportController = new SimpleObjectProperty<>(this, "videoExportController", null);
   private Property<SessionDataExportStageController> dataExportController = new SimpleObjectProperty<>(this, "dataExportController", null);
   private SubScene mainScene3D;
   private SessionVisualizerWindowToolkit toolkit;

   public void initialize(SessionVisualizerWindowToolkit toolkit)
   {
      this.toolkit = toolkit;
      owner = toolkit.getWindow();
      messager = toolkit.getMessager();
      topics = toolkit.getTopics();
      mainScene3D = toolkit.getGlobalToolkit().getMainScene3D();

      messager.registerJavaFXSyncedTopicListener(topics.getDisableUserControls(), disable ->
      {
         takeSnapshotMenuItem.setDisable(disable);
         exportVideoMenuItem.setDisable(disable);
      });
   }

   @FXML
   private void exportData()
   {
      if (dataExportController.getValue() != null)
      {
         dataExportController.getValue().close();
         dataExportController.setValue(null);
      }

      try
      {
         FXMLLoader loader = new FXMLLoader(SessionVisualizerIOTools.SESSION_DATA_EXPORT_STAGE_URL);
         loader.load();
         SessionDataExportStageController controller = loader.getController();
         controller.initialize(toolkit);
         controller.getStage().show();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   @FXML
   private void importData()
   {
      // TODO implement me
   }

   @FXML
   private void requestVideo()
   {
      if (videoExportController.getValue() != null)
      {
         videoExportController.getValue().close();
         videoExportController.setValue(null);
      }

      try
      {
         FXMLLoader loader = new FXMLLoader(SessionVisualizerIOTools.VIDEO_PREVIEW_PANE_URL);
         loader.load();
         VideoRecordingPreviewPaneController controller = loader.getController();
         controller.initialize(owner, mainScene3D, messager, topics);
         controller.getStage().show();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   @FXML
   private void loadConfiguration()
   {
      File result = SessionVisualizerIOTools.scs2ConfigurationOpenFileDialog(owner);

      if (result != null)
         messager.submitMessage(topics.getSessionVisualizerConfigurationLoadRequest(), result);
   }

   @FXML
   private void loadDefaultConfiguration()
   {
      Alert alert = new Alert(AlertType.CONFIRMATION, "Load default configuration?", ButtonType.OK, ButtonType.CANCEL);
      SessionVisualizerIOTools.addSCSIconToDialog(alert);
      Optional<ButtonType> result = alert.showAndWait();
      if (result.isPresent() && result.get() == ButtonType.OK)
         messager.submitMessage(topics.getSessionVisualizerDefaultConfigurationLoadRequest(), true);
   }

   @FXML
   private void saveConfiguration()
   {
      File result = SessionVisualizerIOTools.scs2ConfigurationSaveFileDialog(owner);

      if (result != null)
         messager.submitMessage(topics.getSessionVisualizerConfigurationSaveRequest(), result);
   }

   @FXML
   private void saveDefaultConfiguration()
   {
      Alert alert = new Alert(AlertType.CONFIRMATION, "Save current configuration as default?", ButtonType.OK, ButtonType.CANCEL);
      SessionVisualizerIOTools.addSCSIconToDialog(alert);
      Optional<ButtonType> result = alert.showAndWait();
      if (result.isPresent() && result.get() == ButtonType.OK)
         messager.submitMessage(topics.getSessionVisualizerDefaultConfigurationSaveRequest(), true);
   }

   @FXML
   private void requestSnapshot()
   {
      messager.submitMessage(topics.getTakeSnapshot(), new Object());
   }

   @FXML
   private void close()
   {
      messager.submitMessage(topics.getSessionVisualizerCloseRequest(), true);
   }
}
