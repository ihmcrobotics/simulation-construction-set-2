package us.ihmc.scs2.sessionVisualizer.jfx.controllers.menu;

import javafx.fxml.FXML;
import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerTopics;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerWindowToolkit;

public class FileMenuController
{
   private JavaFXMessager messager;
   private SessionVisualizerTopics topics;

   public void initialize(SessionVisualizerWindowToolkit toolkit)
   {
      this.messager = toolkit.getMessager();
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
   private void requestSnapshot()
   {
      messager.submitMessage(topics.getTakeSnapshot(), new Object());
   }
   
   @FXML
   private void close()
   {
      // TODO implement me
   }
}
