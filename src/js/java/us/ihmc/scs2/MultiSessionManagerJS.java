package us.ihmc.scs2;

import us.ihmc.scs2.session.Session;

public class MultiSessionManagerJS
{
   private final SessionVisualizerToolkitJS toolkit;

   private Session activeSession;

   public MultiSessionManagerJS(SessionVisualizerToolkitJS toolkit)
   {
      this.toolkit = toolkit;
   }

   public void startSession(Session session, Runnable sessionLoadedCallback)
   {
      toolkit.startSession(session, sessionLoadedCallback);
   }

   public void stopSession()
   {
      if (!toolkit.hasActiveSession())
         return;

      toolkit.stopSession();
   }
}
