package us.ihmc.scs2.sessionVisualizer.jfx.session.log;

import java.io.File;
import java.io.IOException;

import org.bytedeco.javacv.Frame;
import org.jcodec.containers.mp4.MP4Packet;

import us.ihmc.codecs.builder.MP4MJPEGMovieBuilder;
import us.ihmc.codecs.demuxer.MP4VideoDemuxer;
import us.ihmc.robotDataLogger.logger.MagewellMuxer;
import us.ihmc.scs2.session.log.ProgressConsumer;

public class VideoConverter
{
   public static int cropMagewellVideo(MagewellDemuxer magewellDemuxer,
                                       File target,
                                       long startCameraTimestamp,
                                       long endCameraTimestamp,
                                       ProgressConsumer progressConsumer) throws IOException
   {
      MagewellMuxer magewellMuxer = null;

      int frameRate = (int) magewellDemuxer.getFrameRate();
      long cameraTimeStamp = 0;

      long startFrame = getFrame(startCameraTimestamp, magewellDemuxer); // This also moves the stream to the startFrame
      long endFrame = getFrame(endCameraTimestamp, magewellDemuxer);
      long numberOfFrames = endFrame - startFrame;

      magewellDemuxer.seekToPTS(startCameraTimestamp);

      Frame frame;
      while ((frame = magewellDemuxer.getNextFrame()) != null && magewellDemuxer.getFrameNumber() <= endFrame)
      {
         if (magewellMuxer == null)
         {
            magewellMuxer = new MagewellMuxer(target, magewellDemuxer.getImageWidth(), magewellDemuxer.getImageHeight());
            magewellMuxer.start();
         }

         magewellMuxer.recordFrame(frame, cameraTimeStamp);

         if (progressConsumer != null)
         {
            progressConsumer.info("frame %d/%d".formatted(magewellDemuxer.getFrameNumber() - startFrame, numberOfFrames));
            progressConsumer.progress((double) (magewellDemuxer.getFrameNumber() - startFrame) / (double) numberOfFrames);
         }
      }

      if (magewellMuxer != null)
      {
         magewellMuxer.close();
      }

      return frameRate;
   }

   /**
    * @param source
    * @param target
    * @param startPTS
    * @param endPTS
    * @return frame rate of the new video file
    * @throws IOException
    */
   public static int cropBlackMagicVideo(File source, File target, long startPTS, long endPTS, ProgressConsumer progressConsumer) throws IOException
   {
      MP4MJPEGMovieBuilder builder = null;
      MP4VideoDemuxer demuxer = new MP4VideoDemuxer(source);

      int frameRate = getFrameRate(demuxer);

      long endFrame = getFrame(endPTS, demuxer);
      long startFrame = getFrame(startPTS, demuxer); // This also moves the stream to the startFrame
      long numberOfFrames = endFrame - startFrame;

      MP4Packet frame;
      while ((frame = demuxer.getNextPacket()) != null && demuxer.getCurrentFrame() <= endFrame)
      {
         if (builder == null)
         {
            builder = new MP4MJPEGMovieBuilder(target, demuxer.getWidth(), demuxer.getHeight(), frameRate, 1);
         }
         // frame.toYUV420();
         builder.encodeFrame(frame.getData());

         if (progressConsumer != null)
         {
            progressConsumer.info("frame %d/%d".formatted(demuxer.getCurrentFrame() - startFrame, numberOfFrames));
            progressConsumer.progress((double) (demuxer.getCurrentFrame() - startFrame) / (double) numberOfFrames);
         }
      }

      if (builder != null)
         builder.close();
      demuxer.delete();

      return frameRate;
   }

   private static int getFrameRate(MP4VideoDemuxer demuxer) throws IOException
   {
      demuxer.seekToFrame(0);
      long startPts = demuxer.getCurrentPTS();
      demuxer.seekToFrame(1);
      long endPts = demuxer.getCurrentPTS();

      double step = endPts - startPts;
      int rate = (int) Math.round(demuxer.getTimescale() / step);

      System.out.println("Framerate is " + rate);
      return rate;
   }

   private static long getFrame(long endCameraTimestamp, MagewellDemuxer demuxer) throws IOException
   {
      demuxer.seekToPTS(endCameraTimestamp);
      return demuxer.getFrameNumber();
   }

   private static long getFrame(long endPTS, MP4VideoDemuxer demuxer) throws IOException
   {
      demuxer.seekToPTS(endPTS);
      long endFrame = demuxer.getCurrentFrame();
      return endFrame;
   }
}
