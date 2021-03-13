package us.ihmc.scs2.sessionVisualizer.jfx.managers;

import java.util.ArrayDeque;
import java.util.Deque;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import us.ihmc.scs2.session.Session;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.ObservedAnimationTimer;

public class ChartRenderManager extends ObservedAnimationTimer implements Manager
{
   private final IntegerProperty numberOfLayersToRenderPerUpdate = new SimpleIntegerProperty(this, "numberOfLayersToRenderPerUpdate", -1);
   private final Deque<Runnable> chartUpdaterToCall = new ArrayDeque<>();

   public void submitRenderRequest(Runnable chartUpdater)
   {
      if (!chartUpdaterToCall.contains(chartUpdater))
         chartUpdaterToCall.add(chartUpdater);
   }

   public void clearRequests()
   {
      chartUpdaterToCall.clear();
   }

   @Override
   public void handleImpl(long now)
   {
      int numberOfLayers = numberOfLayersToRenderPerUpdate.get();
      if (numberOfLayers <= 0)
         numberOfLayers = chartUpdaterToCall.size();

      for (int i = 0; i < numberOfLayers; i++)
      {
         Runnable layer = chartUpdaterToCall.poll();
         if (layer == null)
            return;
         layer.run();
      }
   }

   @Override
   public void startSession(Session session)
   {
      start();
   }

   @Override
   public void stopSession()
   {
      stop();
      clearRequests();
   }

   @Override
   public boolean isSessionLoaded()
   {
      return true;
   }
}
