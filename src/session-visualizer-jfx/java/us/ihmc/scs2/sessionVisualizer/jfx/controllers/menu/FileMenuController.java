package us.ihmc.scs2.sessionVisualizer.jfx.controllers.menu;

import java.io.File;
import java.util.Optional;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.scs2.sessionVisualizer.jfx.SceneVideoRecordingRequest;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerIOTools;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerTopics;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerWindowToolkit;

public class FileMenuController
{
   private Stage owner;
   private JavaFXMessager messager;
   private SessionVisualizerTopics topics;

   public void initialize(SessionVisualizerWindowToolkit toolkit)
   {
      owner = toolkit.getWindow();
      messager = toolkit.getMessager();
      topics = toolkit.getTopics();
   }

   @FXML
   private void requestExportData()
   {
      // TODO implement me
   }

   @FXML
   private void requestImportData()
   {
      // TODO implement me
   }

   @FXML
   private void requestVideo()
   {
      FileChooser fileChooser = new FileChooser();
      fileChooser.setInitialDirectory(SessionVisualizerIOTools.getDefaultFilePath("video"));
      fileChooser.getExtensionFilters().add(new ExtensionFilter("MP4", "*.mp4"));
      File result = fileChooser.showSaveDialog(owner);

      if (result == null)
         return;

      SessionVisualizerIOTools.setDefaultFilePath("video", result);

      SceneVideoRecordingRequest request = new SceneVideoRecordingRequest();
      request.setFile(result);
      
      messager.submitMessage(topics.getSceneVideoRecordingRequest(), request);
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
