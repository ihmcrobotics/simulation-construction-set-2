package us.ihmc.scs2.sessionVisualizer.jfx.session.log;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerIOTools;
import us.ihmc.scs2.sessionVisualizer.jfx.session.log.VideoDataReader.FrameData;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.JavaFXMissingTools;

public class VideoViewer
{
   private static final double THUMBNAIL_HIGHLIGHT_SCALE = 1.05;

   private final ImageView thumbnail = new ImageView();
   private final StackPane thumbnailContainer = new StackPane(thumbnail);
   private final ImageView videoView = new ImageView();
   private final Label cameraTargetPTSLabel = new Label();
   private final Label cameraCurrentPTSLabel = new Label();
   private final Label robotTimestampLabel = new Label();

   private final BooleanProperty updateVideoView = new SimpleBooleanProperty(this, "updateVideoView", false);
   private final ObjectProperty<Stage> videoWindowProperty = new SimpleObjectProperty<>(this, "videoWindow", null);
   private final VideoDataReader reader;
   private final double defaultThumbnailSize;

   public VideoViewer(Window owner, VideoDataReader reader, double defaultThumbnailSize)
   {
      this.reader = reader;
      this.defaultThumbnailSize = defaultThumbnailSize;
      thumbnail.setPreserveRatio(true);
      videoView.setPreserveRatio(true);
      thumbnail.setFitWidth(defaultThumbnailSize);
      thumbnail.setOnMouseEntered(e ->
      {
         Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.1),
                                                       new KeyValue(thumbnail.fitWidthProperty(),
                                                                    THUMBNAIL_HIGHLIGHT_SCALE * defaultThumbnailSize,
                                                                    Interpolator.EASE_BOTH)));
         timeline.playFromStart();
      });
      thumbnail.setOnMouseExited(e ->
      {
         Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.1),
                                                       new KeyValue(thumbnail.fitWidthProperty(), defaultThumbnailSize, Interpolator.EASE_BOTH)));
         timeline.playFromStart();
      });

      thumbnail.addEventHandler(MouseEvent.MOUSE_CLICKED, e ->
      {
         if (e.getClickCount() != 2)
            return;

         videoView.setImage(thumbnail.getImage());

         Stage stage;

         if (videoWindowProperty.get() != null)
         {
            stage = videoWindowProperty.get();
         }
         else
         {
            stage = new Stage();
            AnchorPane anchorPane = new AnchorPane();
            Pane root = createImageViewPane(videoView);
            anchorPane.getChildren().add(root);
            JavaFXMissingTools.setAnchorConstraints(root, 0);
            VBox labelNames = new VBox(new Label("cameraTargetPTSLabel"), new Label("cameraCurrentPTSLabel"), new Label("robotTimestampLabel"));
            VBox labels = new VBox(cameraTargetPTSLabel, cameraCurrentPTSLabel, robotTimestampLabel);
            HBox labelsContainer = new HBox(5, labelNames, labels);
            anchorPane.getChildren().add(labelsContainer);
            AnchorPane.setLeftAnchor(labelsContainer, 0.0);
            AnchorPane.setBottomAnchor(labelsContainer, 0.0);

            videoWindowProperty.set(stage);
            stage.getIcons().add(SessionVisualizerIOTools.LOG_SESSION_IMAGE);
            stage.setTitle(reader.getName());
            owner.setOnHiding(e2 -> stage.close());
            Scene scene = new Scene(anchorPane);
            stage.setScene(scene);
            updateVideoView.bind(stage.showingProperty());
         }

         Screen screen = Screen.getScreensForRectangle(e.getScreenX(), e.getScreenY(), 1, 1).get(0);
         Rectangle2D visualBounds = screen.getVisualBounds();
         double width = 0.5 * visualBounds.getWidth();
         double height = 0.5 * visualBounds.getHeight();
         double x = visualBounds.getMinX() + 0.5 * (visualBounds.getWidth() - width);
         double y = visualBounds.getMinY() + 0.5 * (visualBounds.getHeight() - height);
         stage.setX(x);
         stage.setY(y);
         stage.setWidth(width);
         stage.setHeight(height);

         stage.toFront();
         stage.show();
      });
   }

   private static Pane createImageViewPane(ImageView imageView)
   {
      return new Pane(imageView)
      {
         @Override
         protected void layoutChildren()
         {
            Image image = imageView.getImage();
            if (image == null)
               return;
            double imageRatio = image.getWidth() / image.getHeight();
            double width = Math.min(getWidth(), getHeight() * imageRatio);
            double height = width / imageRatio;

            double x = 0.5 * (getWidth() - width);
            double y = 0.5 * (getHeight() - height);
            imageView.setFitWidth(width);
            imageView.setX(x);
            imageView.setY(y);
         }
      };
   }

   public void update()
   {
      FrameData currentFrameData = reader.pollCurrentFrame();

      if (currentFrameData == null)
         return;

      WritableImage currentFrame = currentFrameData.frame;

      thumbnailContainer.setPrefWidth(THUMBNAIL_HIGHLIGHT_SCALE * defaultThumbnailSize);
      thumbnailContainer.setPrefHeight(THUMBNAIL_HIGHLIGHT_SCALE * defaultThumbnailSize * currentFrame.getHeight() / currentFrame.getWidth());

      thumbnail.setImage(currentFrame);

      if (updateVideoView.get())
      {
         videoView.setImage(currentFrame);
         cameraTargetPTSLabel.setText(Long.toString(currentFrameData.cameraTargetPTS));
         cameraCurrentPTSLabel.setText(Long.toString(currentFrameData.cameraCurrentPTS));
         robotTimestampLabel.setText(Long.toString(currentFrameData.robotTimestamp));
      }
   }

   public void stop()
   {
      if (videoWindowProperty.get() != null)
      {
         videoWindowProperty.get().close();
         videoWindowProperty.set(null);
      }
   }

   public Node getThumbnail()
   {
      return thumbnailContainer;
   }
}
