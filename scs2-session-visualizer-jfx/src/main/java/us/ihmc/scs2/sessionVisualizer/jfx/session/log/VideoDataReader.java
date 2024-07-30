package us.ihmc.scs2.sessionVisualizer.jfx.session.log;

import us.ihmc.robotDataLogger.Camera;
import us.ihmc.scs2.session.log.ProgressConsumer;

import java.io.File;
import java.io.IOException;

public interface VideoDataReader
{

   int getImageHeight();

   int getImageWidth();

   void readVideoFrame(long timestamp);

   void cropVideo(File file, File file2, long test, long what, ProgressConsumer heck) throws IOException;

   void exportVideo(File file, long test, long what, ProgressConsumer heck);

   String getName();

   Camera getCamera();

   default MagewellVideoDataReader.FrameData pollCurrentFrameMagewell()
   {
      return null;
   };

   default BlackMagicVideoDataReader.FrameData pollCurrentFrameBlackMagic()
   {
      return null;
   };

   int getCurrentIndex();

   boolean replacedRobotTimestampsContainsIndex(int index);
}
