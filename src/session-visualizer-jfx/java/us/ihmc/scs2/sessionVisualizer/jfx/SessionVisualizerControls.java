package us.ihmc.scs2.sessionVisualizer.jfx;

import java.io.File;

public interface SessionVisualizerControls
{
   void setCameraOrientation(double latitude, double longitude, double roll);

   void setCameraPosition(double x, double y, double z);

   void setCameraFocusPosition(double x, double y, double z);

   void requestCameraRigidBodyTracking(String robotName, String rigidBodyName);
   
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
}
