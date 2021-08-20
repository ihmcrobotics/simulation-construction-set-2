package us.ihmc.scs2.sessionVisualizer.jfx;

public interface SessionVisualizerControls
{
   void setCameraOrientation(double latitude, double longitude, double roll);

   void setCameraPosition(double x, double y, double z);

   void setCameraFocusPosition(double x, double y, double z);
   
   void requestCameraRigidBodyTracking(String robotName, String rigidBodyName);

   void shutdown();

   void shutdownNow();

   void addVisualizerShutdownListener(Runnable listener);

   void waitUntilFullyUp();
}
