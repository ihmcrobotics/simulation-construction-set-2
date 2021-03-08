package us.ihmc.scs2.sessionVisualizer.jfx.session.log;

import com.jfoenix.controls.JFXProgressBar;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.JavaFXMissingTools;

public class LogCropProgressController implements ProgressConsumer
{
   @FXML
   private VBox mainPane;
   @FXML
   private Label taskInProgressLabel, progressInfoLabel, progressErrorLabel;
   @FXML
   private JFXProgressBar progressBar;

   private Pane parent;
   private Timeline openTimeline, closeTimeline;

   public void initialize(Pane parent)
   {
      this.parent = parent;
      taskInProgressLabel.setText("N/D");
      progressInfoLabel.setText(null);
      progressErrorLabel.setText(null);

      DoubleProperty scaleYProperty = mainPane.scaleYProperty();
      openTimeline = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(scaleYProperty, 0.0, Interpolator.EASE_BOTH)),
                                  new KeyFrame(Duration.millis(200), new KeyValue(scaleYProperty, 1.0, Interpolator.EASE_BOTH)));
      closeTimeline = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(scaleYProperty, 1.0, Interpolator.EASE_BOTH)),
                                   new KeyFrame(Duration.seconds(5.0), new KeyValue(scaleYProperty, 1.0, Interpolator.EASE_BOTH)),
                                   new KeyFrame(Duration.seconds(5.2), new KeyValue(scaleYProperty, 0.0, Interpolator.EASE_BOTH)));
      closeTimeline.setOnFinished(e -> parent.getChildren().remove(mainPane));
   }

   @Override
   public void started(String task)
   {
      taskInProgressLabel.setText(task);

      JavaFXMissingTools.runLaterIfNeeded(() ->
      {
         if (!parent.getChildren().contains(mainPane))
         {
            mainPane.setScaleY(0.0);
            parent.getChildren().add(mainPane);
            openTimeline.playFromStart();
         }
      });
   }

   @Override
   public void info(String info)
   {
      JavaFXMissingTools.runLaterIfNeeded(() -> progressInfoLabel.setText(info));
   }

   @Override
   public void error(String error)
   {
      JavaFXMissingTools.runLaterIfNeeded(() -> progressErrorLabel.setText(error));
   }

   @Override
   public void progress(double progressPercentage)
   {
      JavaFXMissingTools.runLaterIfNeeded(() -> progressBar.setProgress(progressPercentage));
   }

   @Override
   public void done()
   {
      info("Done!");
      JavaFXMissingTools.runLaterIfNeeded(() -> closeTimeline.playFromStart());
   }
}