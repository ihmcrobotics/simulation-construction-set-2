package us.ihmc.scs2.sessionVisualizer.jfx;

import java.io.File;
import java.util.Collection;

import us.ihmc.scs2.definition.visual.VisualDefinition;

public interface SessionVisualizerControls
{
   void setCameraOrientation(double latitude, double longitude, double roll);

   void setCameraPosition(double x, double y, double z);

   void setCameraFocusPosition(double x, double y, double z);

   void setCameraZoom(double distanceFromFocus);

   void requestCameraRigidBodyTracking(String robotName, String rigidBodyName);

   default void addStaticVisuals(Collection<? extends VisualDefinition> visualDefinitions)
   {
      for (VisualDefinition visualDefinition : visualDefinitions)
      {
         addStaticVisual(visualDefinition);
      }
   }

   void addStaticVisual(VisualDefinition visualDefinition);

   default void exportVideo(File file)
   {
      SceneVideoRecordingRequest request = new SceneVideoRecordingRequest();
      request.setFile(file);
      exportVideo(request);
   }

   void exportVideo(SceneVideoRecordingRequest request);

   void shutdown();

   void shutdownNow();

   void addVisualizerShutdownListener(Runnable listener);

   void waitUntilFullyUp();

   void waitUntilDown();
}
