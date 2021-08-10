package us.ihmc.scs2.sessionVisualizer.jfx.controllers.menu;

import java.io.File;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
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
   }

   @FXML
   private void loadConfiguration()
   {
      File result = SessionVisualizerIOTools.scs2ConfigurationOpenFileDialog(owner);

      if (result != null)
         messager.submitMessage(topics.getSessionVisualizerConfigurationLoadRequest(), result);
   }

   @FXML
   private void saveConfiguration()
   {
      File result = SessionVisualizerIOTools.scs2ConfigurationSaveFileDialog(owner);

      if (result != null)
         messager.submitMessage(topics.getSessionVisualizerConfigurationSaveRequest(), result);
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
