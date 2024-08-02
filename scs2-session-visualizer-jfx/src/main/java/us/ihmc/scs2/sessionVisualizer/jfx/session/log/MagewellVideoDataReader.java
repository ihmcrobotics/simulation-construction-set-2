package us.ihmc.scs2.sessionVisualizer.jfx.session.log;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.JavaFXFrameConverter;
import us.ihmc.robotDataLogger.Camera;
import us.ihmc.scs2.session.log.ProgressConsumer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class MagewellVideoDataReader implements VideoDataReader
{
   private final TimestampScrubber timestampScrubber;
   private final String name;

   private final MagewellDemuxer magewellDemuxer;

   private final File videoFile;
   private final Camera camera;
   private final FrameData frameData = new FrameData();

   public MagewellVideoDataReader(Camera camera, File dataDirectory, boolean hasTimeBase) throws IOException
   {
      this.camera = camera;
      name = camera.getNameAsString();
      boolean interlaced = camera.getInterlaced();

      if (!hasTimeBase)
      {
         System.err.println("Video data is using timestamps instead of frame numbers. Falling back to seeking based on timestamp.");
      }

      videoFile = new File(dataDirectory, camera.getVideoFileAsString());

      if (!videoFile.exists())
      {
         throw new IOException("Cannot find video: " + videoFile);
      }

      magewellDemuxer = new MagewellDemuxer(dataDirectory, camera);

      File timestampFile = new File(dataDirectory, camera.getTimestampFileAsString());
      this.timestampScrubber = new TimestampScrubber(timestampFile, hasTimeBase, interlaced);
   }

   public int getImageHeight()
   {
      return magewellDemuxer.getImageHeight();
   }

   public int getImageWidth()
   {
      return magewellDemuxer.getImageWidth();
   }

   public void readVideoFrame(long queryRobotTimestamp)
   {
      long currentVideoTimestamps = timestampScrubber.getVideoTimestamp(queryRobotTimestamp);
      long currentRobotTimestamp = timestampScrubber.getCurrentRobotTimestamp();

      magewellDemuxer.seekToPTS(currentVideoTimestamps);

      // This is a copy that can be shown in the video view to debug timestamp issues
      {
         FrameData copyForWriting = frameData;
         copyForWriting.queryRobotTimestamp = queryRobotTimestamp;
         copyForWriting.currentRobotTimestamp = currentRobotTimestamp;
         copyForWriting.currentVideoTimestamp = currentVideoTimestamps;
         copyForWriting.currentDemuxerTimestamp = magewellDemuxer.getCurrentPTS();
      }

      Frame nextFrame = magewellDemuxer.getNextFrame();
      frameData.frame = convertFrameToWritableImage(nextFrame);
   }

   /**
    * This class converts a {@link Frame} to a {@link WritableImage} in order to be displayed correctly in JavaFX.
    *
    * @param frameToConvert is the next frame we want to visualize so we convert it to be compatible with JavaFX
    * @return {@link WritableImage}
    */
   public WritableImage convertFrameToWritableImage(Frame frameToConvert)
   {
      Image currentImage;

      if (frameToConvert == null)
      {
         return null;
      }

      try (JavaFXFrameConverter frameConverter = new JavaFXFrameConverter())
      {
         currentImage = frameConverter.convert(frameToConvert);
      }
      WritableImage writableImage = new WritableImage((int) currentImage.getWidth(), (int) currentImage.getHeight());
      PixelReader pixelReader = currentImage.getPixelReader();
      PixelWriter pixelWriter = writableImage.getPixelWriter();

      for (int y = 0; y < currentImage.getHeight(); y++)
      {
         for (int x = 0; x < currentImage.getWidth(); x++)
         {
            pixelWriter.setArgb(x, y, pixelReader.getArgb(x, y));
         }
      }

      return writableImage;
   }

   public Frame getFrame()
   {
      return magewellDemuxer.getNextFrame();
   }

   public void cropVideo(File outputFile, File timestampFile, long startTimestamp, long endTimestamp, ProgressConsumer monitor) throws IOException
   {
      long startVideoTimestamp = timestampScrubber.getVideoTimestamp(startTimestamp);
      long endVideoTimestamp = timestampScrubber.getVideoTimestamp(endTimestamp);

      int framerate = VideoConverter.crop(videoFile, outputFile, startVideoTimestamp, endVideoTimestamp, monitor);

      PrintWriter timestampWriter = new PrintWriter(timestampFile);
      timestampWriter.println(1);
      timestampWriter.println(framerate);

      long pts = 0;
      /*
       * PTS gets reordered to be monotonically increasing starting from 0
       */
      for (int i = 0; i < timestampScrubber.getRobotTimestampsLength(); i++)
      {
         long robotTimestamp = timestampScrubber.getRobotTimestampAtIndex(i);

         if (robotTimestamp >= startTimestamp && robotTimestamp <= endTimestamp)
         {
            timestampWriter.print(robotTimestamp);
            timestampWriter.print(" ");
            timestampWriter.println(pts);
            pts++;
         }
         else if (robotTimestamp > endTimestamp)
         {
            break;
         }
      }

      timestampWriter.close();
   }

   public void exportVideo(File selectedFile, long startTimestamp, long endTimestamp, ProgressConsumer progressConsumer)
   {
      long startVideoTimestamp = timestampScrubber.getVideoTimestamp(startTimestamp);
      long endVideoTimestamp = timestampScrubber.getVideoTimestamp(endTimestamp);

      try
      {
         VideoConverter.convert(videoFile, selectedFile, startVideoTimestamp, endVideoTimestamp, progressConsumer);
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   public String getName()
   {
      return name;
   }

   public Camera getCamera()
   {
      return camera;
   }

   public FrameData pollCurrentFrame()
   {
      return frameData;
   }

   public int getCurrentIndex()
   {
      return timestampScrubber.getCurrentIndex();
   }

   public boolean replacedRobotTimestampsContainsIndex(int index)
   {
      return timestampScrubber.getReplacedRobotTimestampIndex(index);
   }
}
