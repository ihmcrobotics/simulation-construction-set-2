package us.ihmc.scs2.sessionVisualizer.controllers.menu;

import javafx.fxml.FXML;
import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.scs2.sessionVisualizer.SessionVisualizerTopics;
import us.ihmc.scs2.sessionVisualizer.managers.SessionVisualizerToolkit;

public class SessionMenuController
{
   private SessionVisualizerTopics topics;
   private JavaFXMessager messager;

   public void initialize(SessionVisualizerToolkit toolkit)
   {
      topics = toolkit.getTopics();
      messager = toolkit.getMessager();
   }

   @FXML
   void startLogSession()
   {
      messager.submitMessage(topics.getLogSessionControlsRequest(), true);
   }

   @FXML
   void startRemoteSession()
   {
      messager.submitMessage(topics.getRemoteSessionControlsRequest(), true);
   }
}
