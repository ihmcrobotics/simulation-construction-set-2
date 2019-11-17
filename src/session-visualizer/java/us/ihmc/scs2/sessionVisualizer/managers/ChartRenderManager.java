package us.ihmc.scs2.sessionVisualizer.managers;

import java.util.ArrayDeque;
import java.util.Deque;

import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import us.ihmc.scs2.session.Session;
import us.ihmc.scs2.sessionVisualizer.charts.NumberSeriesLayer;

public class ChartRenderManager extends AnimationTimer implements Manager
{
   private final IntegerProperty numberOfLayersToRenderPerUpdate = new SimpleIntegerProperty(this, "numberOfLayersToRenderPerUpdate", 15);
   private final Deque<NumberSeriesLayer> layersToRender = new ArrayDeque<>();

   public void submitRenderRequest(NumberSeriesLayer layer)
   {
      if (!layersToRender.contains(layer))
         layersToRender.add(layer);
   }

   public void clearRequests()
   {
      layersToRender.clear();
   }

   @Override
   public void handle(long now)
   {
      int numberOfLayers = numberOfLayersToRenderPerUpdate.get();
      if (numberOfLayers <= 0)
         numberOfLayers = layersToRender.size();

      for (int i = 0; i < numberOfLayers; i++)
      {
         NumberSeriesLayer layer = layersToRender.poll();
         if (layer == null)
            return;
         layer.render();
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
