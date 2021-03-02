package us.ihmc.scs2.sessionVisualizer.jfx.session.log;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.AnimationTimer;
import javafx.scene.layout.Pane;
import javafx.stage.Window;

public class MultiVideoViewer extends AnimationTimer
{
   private final Pane thumbnailsContainer;
   private final List<VideoViewer> videoViewers = new ArrayList<>();

   public MultiVideoViewer(Window owner, Pane thumbnailsContainer, MultiVideoDataReader multiReader, double defaultThumbnailWidth)
   {
      this.thumbnailsContainer = thumbnailsContainer;

      for (VideoDataReader reader : multiReader.getReaders())
      {
         videoViewers.add(new VideoViewer(owner, reader, defaultThumbnailWidth));
      }
   }

   @Override
   public void start()
   {
      super.start();

      for (VideoViewer videoViewer : videoViewers)
      {
         thumbnailsContainer.getChildren().add(videoViewer.getThumbnail());
      }
   }

   @Override
   public void handle(long now)
   {
      videoViewers.forEach(VideoViewer::update);
   }

   @Override
   public void stop()
   {
      super.stop();

      for (VideoViewer videoViewer : videoViewers)
      {
         thumbnailsContainer.getChildren().remove(videoViewer.getThumbnail());
         videoViewer.stop();
      }
   }
}
