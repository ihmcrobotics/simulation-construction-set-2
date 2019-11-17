package us.ihmc.scs2.session;

public class SessionProperties
{
   private final SessionMode activeMode;
   private final boolean runAtRealTimeRate;
   private final double playbackRealTimeRate;
   private final long sessionTickToTimeIncrement;

   public SessionProperties(SessionMode activeMode, boolean runAtRealTimeRate, double playbackRealTimeRate, long sessionTickToTimeIncrement)
   {
      this.activeMode = activeMode;
      this.runAtRealTimeRate = runAtRealTimeRate;
      this.playbackRealTimeRate = playbackRealTimeRate;
      this.sessionTickToTimeIncrement = sessionTickToTimeIncrement;
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
}
