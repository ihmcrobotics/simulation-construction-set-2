package us.ihmc.scs2.sessionVisualizer.jfx.session.log;

import javafx.scene.image.WritableImage;

public class FrameData
{
   public WritableImage frame;
   public long queryRobotTimestamp;
   public long robotTimestamp;
   public long cameraCurrentPTS;
   public long demuxerCurrentPTS;
}
