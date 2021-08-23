package us.ihmc.scs2.sessionVisualizer.jfx.managers;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import javafx.animation.AnimationTimer;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.SubScene;
import javafx.scene.image.WritableImage;
import javafx.scene.transform.Scale;
import us.ihmc.codecs.builder.H264Settings;
import us.ihmc.codecs.builder.MP4H264MovieBuilder;
import us.ihmc.codecs.generated.EProfileIdc;
import us.ihmc.codecs.generated.EUsageType;
import us.ihmc.commons.Conversions;
import us.ihmc.euclid.tools.EuclidCoreTools;
import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.log.LogTools;
import us.ihmc.scs2.session.SessionMode;
import us.ihmc.scs2.sessionVisualizer.jfx.SceneVideoRecordingRequest;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerTopics;
import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;
import us.ihmc.scs2.sharedMemory.tools.SharedMemoryTools;

public class VideoRecordingManager
{
   private final JavaFXMessager messager;
   private final SessionVisualizerTopics topics;

   private SubScene scene;

   private final AtomicReference<YoBufferPropertiesReadOnly> currentBufferProperties;
   private final AtomicReference<SessionMode> currentSessionMode;
   private final AtomicReference<Long> sessionDT;
   private final AtomicReference<Integer> bufferRecordTickPeriod;

   private final AtomicReference<Recorder> activeRecorder = new AtomicReference<>(null);

   public VideoRecordingManager(JavaFXMessager messager, SessionVisualizerTopics topics)
   {
      this.messager = messager;
      this.topics = topics;

      currentBufferProperties = messager.createInput(topics.getYoBufferCurrentProperties());
      currentSessionMode = messager.createInput(topics.getSessionCurrentMode());
      sessionDT = messager.createInput(topics.getSessionTickToTimeIncrement());
      bufferRecordTickPeriod = messager.createInput(topics.getBufferRecordTickPeriod());

      messager.registerTopicListener(topics.getSceneVideoRecordingRequest(), request -> submitRequest(request));
   }

   public void setScene(SubScene scene)
   {
      this.scene = scene;
   }

   private void submitRequest(SceneVideoRecordingRequest request)
   {
      LogTools.info("Received video export request: " + request);

      if (activeRecorder.get() != null)
         activeRecorder.get().stop();

      SnapshotParameters params = new SnapshotParameters();

      double sceneWidth = scene.getWidth();
      double sceneHeight = scene.getHeight();
      double sceneRatio = sceneWidth / sceneHeight;
      double desiredRatio = (double) request.getWidth() / (double) request.getHeight();

      if (!EuclidCoreTools.epsilonEquals(sceneRatio, desiredRatio, 1.0e-6))
      {
         if (sceneRatio > desiredRatio)
         { // Need to reduce the width
            double adjustedWidth = sceneHeight * desiredRatio;
            double minX = 0.5 * (sceneWidth - adjustedWidth);
            double minY = 0.0;
            double width = adjustedWidth;
            double height = sceneHeight;
            params.setViewport(new Rectangle2D(minX, minY, width, height));
         }
         else
         { // Need to reduce the height
            double adjustedHeight = sceneWidth / desiredRatio;
            double minX = 0.0;
            double minY = 0.5 * (sceneHeight - adjustedHeight);
            double width = sceneWidth;
            double height = adjustedHeight;
            params.setViewport(new Rectangle2D(minX, minY, width, height));
         }
      }

      double widthScale = request.getWidth() / sceneWidth;
      double heightScale = request.getHeight() / sceneHeight;
      double scale = Math.max(widthScale, heightScale);
      params.setTransform(new Scale(scale, scale));

      Recorder recorder = new Recorder(request, params, () -> activeRecorder.set(null));
      activeRecorder.set(recorder);
      recorder.start();
   }

   private class Recorder extends AnimationTimer
   {
      private final SceneVideoRecordingRequest request;

      private int bufferStart;
      private int bufferEnd;

      private int currentRecordingBufferIndex;
      private int bufferIndexIncrement = -1;
      private int numberOfBufferTicks = -1;

      private int currentPhase = 0;

      private MP4H264MovieBuilder movieBuilder;
      private BufferedImage bufferedImage;
      private WritableImage jfxImage;

      private final SnapshotParameters params;

      private final Runnable stopListener;

      public Recorder(SceneVideoRecordingRequest request, SnapshotParameters params, Runnable stopListener)
      {
         this.request = request;
         this.params = params;
         this.stopListener = stopListener;
         bufferStart = request.getBufferStart();
         bufferEnd = request.getBufferEnd();
         bufferedImage = new BufferedImage(request.getWidth(), request.getHeight(), BufferedImage.TYPE_INT_ARGB);
         jfxImage = new WritableImage(request.getWidth(), request.getHeight());

         H264Settings settings = new H264Settings();
         settings.setBitrate(request.getWidth() * request.getHeight() / 100);
         settings.setUsageType(EUsageType.CAMERA_VIDEO_REAL_TIME);
         settings.setProfileIdc(EProfileIdc.PRO_HIGH);

         try
         {
            movieBuilder = new MP4H264MovieBuilder(request.getFile(), request.getWidth(), request.getHeight(), (int) request.getFrameRate(), settings);
         }
         catch (IOException e)
         {
            e.printStackTrace();
            return;
         }

      }

      @Override
      public void handle(long now)
      {
         if (currentSessionMode.get() != SessionMode.PAUSE)
         {
            messager.submitMessage(topics.getSessionCurrentMode(), SessionMode.PAUSE);
            return;
         }

         if (sessionDT.get() == null || bufferRecordTickPeriod.get() == null)
            return;

         YoBufferPropertiesReadOnly bufferProperties = currentBufferProperties.getAndSet(null);

         if (bufferProperties == null)
            return;

         switch (currentPhase)
         {
            case 0:
               if (initialize(bufferProperties))
                  currentPhase++;
               return;
            case 1:
               if (recordNextFrame(bufferProperties))
                  currentPhase++;
               return;
            default:
               try
               {
                  movieBuilder.close();
                  stop();
               }
               catch (IOException e)
               {
                  e.printStackTrace();
               }
         }
      }

      private boolean initialize(YoBufferPropertiesReadOnly bufferProperties)
      {
         if (bufferStart < 0)
            bufferStart = bufferProperties.getInPoint();
         if (bufferEnd < 0)
            bufferEnd = bufferProperties.getOutPoint();

         double dt = Conversions.nanosecondsToSeconds(sessionDT.get()) * bufferRecordTickPeriod.get();
         double recordDT = request.getRealTimeRate() / request.getFrameRate();
         bufferIndexIncrement = (int) Math.ceil(recordDT / dt);

         currentRecordingBufferIndex = bufferStart;
         numberOfBufferTicks = SharedMemoryTools.computeSubLength(bufferStart, bufferEnd, bufferProperties.getSize());
         messager.submitMessage(topics.getYoBufferCurrentIndexRequest(), currentRecordingBufferIndex);

         return true;
      }

      private boolean recordNextFrame(YoBufferPropertiesReadOnly bufferProperties)
      {
         if (currentRecordingBufferIndex != bufferProperties.getCurrentIndex())
            return false;

         scene.snapshot(params, jfxImage);
         bufferedImage = SwingFXUtils.fromFXImage(jfxImage, bufferedImage);

         try
         {
            movieBuilder.encodeFrame(bufferedImage);
         }
         catch (IOException e)
         {
            e.printStackTrace();
            stop();
         }

         numberOfBufferTicks -= bufferIndexIncrement;

         if (numberOfBufferTicks < 0)
            return true;

         currentRecordingBufferIndex = SharedMemoryTools.increment(currentRecordingBufferIndex, bufferIndexIncrement, bufferProperties.getSize());
         messager.submitMessage(topics.getYoBufferCurrentIndexRequest(), currentRecordingBufferIndex);

         return false;
      }

      @Override
      public void stop()
      {
         super.stop();
         stopListener.run();
      }
   }
}
