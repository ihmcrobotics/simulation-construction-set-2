package us.ihmc.scs2.sessionVisualizer.controllers.menu;

import java.util.concurrent.atomic.AtomicReference;

import com.jfoenix.controls.JFXTextField;

import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.DoubleStringConverter;
import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.scs2.session.SessionMode;
import us.ihmc.scs2.sessionVisualizer.SCSGUITopics;
import us.ihmc.scs2.sessionVisualizer.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;

public class RunMenuController
{
   @FXML
   private CheckMenuItem simulateAtRealTimeCheckMenuItem;
   @FXML
   private JFXTextField playbackRealTimeRateTextField;

   private JavaFXMessager messager;
   private SCSGUITopics topics;

   private AtomicReference<YoBufferPropertiesReadOnly> bufferProperties;

   public void initialize(SessionVisualizerToolkit toolkit)
   {
      this.messager = toolkit.getMessager();
      topics = toolkit.getTopics();
      bufferProperties = messager.createInput(topics.getYoBufferCurrentProperties(), null);

      messager.bindBidirectional(topics.getRunAtRealTimeRate(), simulateAtRealTimeCheckMenuItem.selectedProperty(), false);

      TextFormatter<Double> formatter = new TextFormatter<>(new DoubleStringConverter());
      formatter.setValue(1.0);
      playbackRealTimeRateTextField.setTextFormatter(formatter);
      toolkit.getMessager().bindBidirectional(toolkit.getTopics().getPlaybackRealTimeRate(), formatter.valueProperty(), false);

      /*
       * TODO: Workaround for a bug in JFX that's causing the previous MenuItem to be triggered and
       * pressing enter while editing the TextField. Registering an EventHandler (even empty) using
       * TextField.setOnAction(...) changes the internal logic and prevents the bug from occurring, see:
       * @formatter:off
       * https://stackoverflow.com/questions/51307577/javafx-custommenuitem-strange-behaviour-with-textfield
       * @formatter:on
       */
      playbackRealTimeRateTextField.setOnAction(e ->
      {
      });
   }

   @FXML
   private void startSimulating()
   {
      messager.submitMessage(topics.getSessionCurrentMode(), SessionMode.RUNNING);
   }

   @FXML
   private void startPlayback()
   {
      messager.submitMessage(topics.getSessionCurrentMode(), SessionMode.PLAYBACK);
   }

   @FXML
   private void pause()
   {
      messager.submitMessage(topics.getSessionCurrentMode(), SessionMode.PAUSE);
   }

   @FXML
   private void setInPoint()
   {
      if (bufferProperties.get() != null)
         messager.submitMessage(topics.getYoBufferInPointIndexRequest(), bufferProperties.get().getCurrentIndex());
   }

   @FXML
   private void gotoInPoint()
   {
      if (bufferProperties.get() != null)
         messager.submitMessage(topics.getYoBufferCurrentIndexRequest(), bufferProperties.get().getInPoint());
   }

   @FXML
   private void stepBack()
   {
      messager.submitMessage(topics.getYoBufferDecrementCurrentIndexRequest(), 1);
   }

   @FXML
   private void stepForward()
   {
      messager.submitMessage(topics.getYoBufferIncrementCurrentIndexRequest(), 1);
   }

   @FXML
   private void gotoOutPoint()
   {
      if (bufferProperties.get() != null)
         messager.submitMessage(topics.getYoBufferCurrentIndexRequest(), bufferProperties.get().getOutPoint());
   }

   @FXML
   private void setOutPoint()
   {
      if (bufferProperties.get() != null)
         messager.submitMessage(topics.getYoBufferOutPointIndexRequest(), bufferProperties.get().getCurrentIndex());
   }
}
