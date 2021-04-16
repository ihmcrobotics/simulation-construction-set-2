package us.ihmc.scs2.session;

public class SessionProperties
{
   private final SessionMode activeMode;
   private final boolean runAtRealTimeRate;
   private final double playbackRealTimeRate;
   private final long sessionTickToTimeIncrement;
   private final int bufferRecordTickPeriod;

   public SessionProperties(SessionMode activeMode, boolean runAtRealTimeRate, double playbackRealTimeRate, long sessionTickToTimeIncrement,
                            int bufferRecordTickPeriod)
   {
      this.activeMode = activeMode;
      this.runAtRealTimeRate = runAtRealTimeRate;
      this.playbackRealTimeRate = playbackRealTimeRate;
      this.sessionTickToTimeIncrement = sessionTickToTimeIncrement;
      this.bufferRecordTickPeriod = bufferRecordTickPeriod;
   }

   public SessionMode getActiveMode()
   {
      return activeMode;
   }

   public boolean isRunAtRealTimeRate()
   {
      return runAtRealTimeRate;
   }

   public double getPlaybackRealTimeRate()
   {
      return playbackRealTimeRate;
   }

   public long getSessionTickToTimeIncrement()
   {
      return sessionTickToTimeIncrement;
   }

   public int getBufferRecordTickPeriod()
   {
      return bufferRecordTickPeriod;
   }
}
