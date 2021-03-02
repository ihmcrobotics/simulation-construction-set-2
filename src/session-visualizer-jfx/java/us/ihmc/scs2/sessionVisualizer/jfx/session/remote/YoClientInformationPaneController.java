package us.ihmc.scs2.sessionVisualizer.jfx.session.remote;

import java.util.function.Supplier;

import javafx.animation.AnimationTimer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import us.ihmc.commons.Conversions;
import us.ihmc.scs2.sessionVisualizer.jfx.session.SessionInfoController;

public class YoClientInformationPaneController extends AnimationTimer implements SessionInfoController
{
   @FXML 
   private AnchorPane mainPane;
   @FXML
   private Label delayLabel;
   @FXML
   private Label logDurationLabel;
   @FXML
   private Label cameraLabel;

   private Supplier<String> delayValueSupplier;
   private Supplier<String> logDurationValueSupplier;
   private Supplier<String> cameraValueSupplier;

   private final ObjectProperty<RemoteSession> activeSessionProperty = new SimpleObjectProperty<>(this, "activeSession", null);

   public void initialize()
   {
      delayValueSupplier = () ->
      {
         RemoteSession activeSession = activeSessionProperty.get();
         if (activeSession == null)
            return null;
         else
            return Conversions.nanosecondsToMilliseconds(activeSession.getDelay()) + "ms";
      };

      logDurationValueSupplier = () ->
      {
         RemoteSession activeSession = activeSessionProperty.get();
         if (activeSession == null)
            return null;

         LoggerStatusUpdater loggerStatusUpdater = activeSession.getLoggerStatusUpdater();

         if (loggerStatusUpdater.isLogging())
            return loggerStatusUpdater.getCurrentLogDuration() + "sec";
         else
            return "Logger offline";
      };

      cameraValueSupplier = () ->
      {
         RemoteSession activeSession = activeSessionProperty.get();
         if (activeSession == null)
            return null;

         LoggerStatusUpdater loggerStatusUpdater = activeSession.getLoggerStatusUpdater();

         return loggerStatusUpdater.isCameraRecording() ? "Recording" : "Off";
      };
   }

   public ObjectProperty<RemoteSession> activeSessionProperty()
   {
      return activeSessionProperty;
   }

   @Override
   public void handle(long now)
   {
      updateLabel(delayLabel, delayValueSupplier, "N/D");
      updateLabel(logDurationLabel, logDurationValueSupplier, "N/D");
      updateLabel(cameraLabel, cameraValueSupplier, "N/D");
   }

   @Override
   public Pane getMainPane()
   {
      return mainPane;
   }

   private void updateLabel(Label label, Supplier<String> textSupplier, String defaultText)
   {
      if (textSupplier == null)
      {
         label.setText(defaultText);
         return;
      }

      String text = textSupplier.get();

      if (text == null)
      {
         label.setText(defaultText);
         return;
      }

      label.setText(text);
   }
}
