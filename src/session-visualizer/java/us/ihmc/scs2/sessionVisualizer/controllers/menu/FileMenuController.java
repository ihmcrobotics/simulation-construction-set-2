package us.ihmc.scs2.sessionVisualizer.controllers.menu;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.scs2.sessionVisualizer.SessionVisualizerTopics;
import us.ihmc.scs2.sessionVisualizer.managers.SessionVisualizerToolkit;

public class FileMenuController
{
   private Stage mainWindow;
   private JavaFXMessager messager;
   private SessionVisualizerTopics topics;

   public void initialize(SessionVisualizerToolkit toolkit)
   {
      this.messager = toolkit.getMessager();
      this.mainWindow = toolkit.getMainWindow();
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
      // TODO implement me
   }

   @FXML
   private void requestSnapshot()
   {
      messager.submitMessage(topics.getTakeSnapshot(), new Object());
   }
   
   @FXML
   private void close()
   {
      mainWindow.close();
   }
}
