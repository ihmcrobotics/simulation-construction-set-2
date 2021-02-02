package us.ihmc.scs2.sessionVisualizer.jfx.session.log;

import java.io.File;
import java.io.IOException;

import org.jcodec.containers.mp4.MP4Packet;

import us.ihmc.codecs.builder.H264Settings;
import us.ihmc.codecs.builder.MP4H264MovieBuilder;
import us.ihmc.codecs.builder.MP4MJPEGMovieBuilder;
import us.ihmc.codecs.demuxer.MP4VideoDemuxer;
import us.ihmc.codecs.generated.EProfileIdc;
import us.ihmc.codecs.generated.YUVPicture;

public class VideoConverter
{

   /**
    * @param source
    * @param target
    * @param startPTS
    * @param endPTS
    * @param monitor
    * @return frame rate of the new video file
    * @throws IOException
    */
   public static int crop(File source, File target, long startPTS, long endPTS, ProgressConsumer progressConsumer) throws IOException
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
            progressConsumer.progress((double) (demuxer.getCurrentFrame() - startFrame) / (double) numberOfFrames);
         }
      }

      if (builder != null)
         builder.close();
      demuxer.delete();

      return frameRate;

   }

   /**
    * @param source
    * @param target
    * @param startPTS
    * @param endPTS
    * @param bitrate
    * @param progressConsumer
    * @return frame rate of the new video file
    * @throws IOException
    */
   public static void convert(File source, File target, long startPTS, long endPTS, ProgressConsumer progressConsumer) throws IOException
   {
      MP4H264MovieBuilder builder = null;
      MP4VideoDemuxer demuxer = new MP4VideoDemuxer(source);

      H264Settings settings = new H264Settings();
      settings.setBitrate(10000);
      settings.setProfileIdc(EProfileIdc.PRO_HIGH);

      int frameRate = getFrameRate(demuxer);

      long endFrame = getFrame(endPTS, demuxer);
      long startFrame = getFrame(startPTS, demuxer); // This also moves the stream to the startFrame
      long numberOfFrames = endFrame - startFrame;

      YUVPicture frame;
      while ((frame = demuxer.getNextFrame()) != null && demuxer.getCurrentFrame() <= endFrame)
      {
         if (builder == null)
         {
            builder = new MP4H264MovieBuilder(target, frame.getWidth(), frame.getHeight(), frameRate, settings);
         }
         // frame.toYUV420();
         builder.encodeFrame(frame);
         frame.delete();

         if (progressConsumer != null)
         {
            progressConsumer.progress((int) ((double) (demuxer.getCurrentFrame() - startFrame) / (double) numberOfFrames));
         }
      }

      builder.close();
      demuxer.delete();

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

   private static long getFrame(long endPTS, MP4VideoDemuxer demuxer) throws IOException
   {
      demuxer.seekToPTS(endPTS);
      long endFrame = demuxer.getCurrentFrame();
      return endFrame;
   }
}
