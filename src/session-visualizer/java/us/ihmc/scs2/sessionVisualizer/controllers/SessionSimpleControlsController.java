package us.ihmc.scs2.sessionVisualizer.controllers;

import static us.ihmc.scs2.sessionVisualizer.controllers.SessionAdvancedControlsController.setupMainControlsActiveMode;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.scs2.session.SessionMode;
import us.ihmc.scs2.sessionVisualizer.SCSGUITopics;
import us.ihmc.scs2.sessionVisualizer.managers.SessionVisualizerToolkit;

public class SessionSimpleControlsController
{
   private JavaFXMessager messager;
   private SCSGUITopics topics;

   @FXML
   private HBox controlsHBox;
   @FXML
   private FontAwesomeIconView runningIconView, playbackIconView, pauseIconView;

   public SessionSimpleControlsController()
   {
   }

   public void initialize(SessionVisualizerToolkit toolkit)
   {
      this.messager = toolkit.getMessager();
      topics = toolkit.getTopics();
      messager.registerTopicListener(topics.getShowAdvancedControls(), showAdvancedControls -> show(!showAdvancedControls));

      setupMainControlsActiveMode(this, messager, topics, runningIconView, playbackIconView, pauseIconView);
   }

   @FXML
   private void startRunning()
   {
      messager.submitMessage(topics.getSessionCurrentMode(), SessionMode.RUNNING);
   }

   @FXML
   private void startPlayback()
   {
      messager.submitMessage(topics.getSessionCurrentMode(), SessionMode.PLAYBACK);
   }

   @FXML
   private void openAdvancedControls()
   {
      messager.submitMessage(topics.getShowAdvancedControls(), true);
   }

   @FXML
   private void pause()
   {
      messager.submitMessage(topics.getSessionCurrentMode(), SessionMode.PAUSE);
   }

   public void show(boolean show)
   {
      controlsHBox.setMouseTransparent(!show);
      controlsHBox.setVisible(show);
   }
}
