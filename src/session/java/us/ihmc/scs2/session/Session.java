package us.ihmc.scs2.session;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import us.ihmc.commons.Conversions;
import us.ihmc.log.LogTools;
import us.ihmc.messager.Messager;
import us.ihmc.messager.TopicListener;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.terrain.TerrainObjectDefinition;
import us.ihmc.scs2.sharedMemory.CropBufferRequest;
import us.ihmc.scs2.sharedMemory.YoSharedBuffer;
import us.ihmc.scs2.sharedMemory.interfaces.LinkedYoVariableFactory;
import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;
import us.ihmc.yoVariables.registry.YoRegistry;

public abstract class Session
{
   public static final String ROOT_REGISTRY_NAME = "root";

   protected final YoRegistry rootRegistry = new YoRegistry(ROOT_REGISTRY_NAME);
   protected final YoSharedBuffer sharedBuffer = new YoSharedBuffer(rootRegistry, 8192);

   private final AtomicReference<SessionMode> activeMode = new AtomicReference<>(SessionMode.PAUSE);
   private final AtomicBoolean runAtRealTimeRate = new AtomicBoolean(false);
   private final AtomicReference<Double> playbackRealTimeRate = new AtomicReference<>(2.0);
   private int stepSizePerPlaybackTick = 1;
   /**
    * Map from one session tick to the time increment in the data.
    * <p>
    * When simulating, this corresponds to the simulation DT for instance.
    * </p>
    */
   private final AtomicLong sessionTickToTimeIncrement = new AtomicLong(Conversions.secondsToNanoseconds(1.0e-4));
   private final AtomicLong desiredBufferPublishPeriod = new AtomicLong(-1L);

   // State listener to publish internal to outside world
   private final List<Consumer<SessionProperties>> sessionPropertiesListeners = new ArrayList<>();
   private final List<Consumer<YoBufferPropertiesReadOnly>> currentBufferPropertiesListeners = new ArrayList<>();

   // Fields for external requests on buffer.
   private final AtomicReference<CropBufferRequest> pendingCropBufferRequest = new AtomicReference<>(null);
   private final AtomicReference<Integer> pendingBufferIndexRequest = new AtomicReference<>(null);
   private final AtomicReference<Integer> pendingBufferInPointIndexRequest = new AtomicReference<>(null);
   private final AtomicReference<Integer> pendingBufferOutPointIndexRequest = new AtomicReference<>(null);
   private final AtomicReference<Integer> pendingIncrementBufferIndexRequest = new AtomicReference<>(null);
   private final AtomicReference<Integer> pendingDecrementBufferIndexRequest = new AtomicReference<>(null);
   private final AtomicReference<Integer> pendingBufferSizeRequest = new AtomicReference<>(null);

   // Strictly internal fields
   private SessionTopicListeners sessionTopicListeners = null;
   private boolean sessionStarted = false;
   private boolean sessionInitialized = false;
   private boolean isSessionShutdown = false;
   private long lastPublishedBufferTimestamp = -1L;
   protected boolean firstRunTick = true;
   protected boolean firstPauseTick = true;

   private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory("SCS2-Session-Thread"));
   private ScheduledFuture<?> activeScheduledFuture;
   private final EnumMap<SessionMode, Runnable> sessionModeToTaskMap = new EnumMap<>(SessionMode.class);

   public Session()
   {
      sessionModeToTaskMap.put(SessionMode.RUNNING, this::runTick);
      sessionModeToTaskMap.put(SessionMode.PLAYBACK, this::playbackTick);
      sessionModeToTaskMap.put(SessionMode.PAUSE, this::pauseTick);
   }

   public static String retrieveCallerName()
   {
      StackTraceElement[] stackTrace = new Throwable().getStackTrace();
      String className = stackTrace[stackTrace.length - 1].getClassName();
      return className.substring(className.lastIndexOf(".") + 1);
   }

   public void setupWithMessager(Messager messager)
   {
      if (sessionTopicListeners != null)
         throw new IllegalArgumentException("This session has already been setup with a messager.");

      sessionTopicListeners = new SessionTopicListeners(messager);
   }

   public void setSessionMode(SessionMode sessionMode)
   {
      SessionMode currentMode = activeMode.get();
      if (sessionMode != currentMode)
      {
         activeMode.set(sessionMode);
         firstRunTick = true;
         firstPauseTick = true;
         restartSessionTask();
      }
   }

   public void addSessionPropertiesListener(Consumer<SessionProperties> listener)
   {
      sessionPropertiesListeners.add(listener);
   }

   public void removeSessionPropertiesListener(Consumer<SessionProperties> listener)
   {
      sessionPropertiesListeners.remove(listener);
   }

   public void addCurrentBufferPropertiesListener(Consumer<YoBufferPropertiesReadOnly> listener)
   {
      currentBufferPropertiesListeners.add(listener);
   }

   public void removeCurrentBufferPropertiesListener(Consumer<YoBufferPropertiesReadOnly> listener)
   {
      currentBufferPropertiesListeners.remove(listener);
   }

   public void setSessionTickToTimeIncrement(long sessionTickToTimeIncrement)
   {
      if (this.sessionTickToTimeIncrement.get() == sessionTickToTimeIncrement)
         return;

      this.sessionTickToTimeIncrement.set(sessionTickToTimeIncrement);
      restartSessionTask();
   }

   public void submitRunAtRealTimeRate(boolean runAtRealTimeRate)
   {
      if (this.runAtRealTimeRate.get() == runAtRealTimeRate)
         return;

      this.runAtRealTimeRate.set(runAtRealTimeRate);
      restartSessionTask();
   }

   public void submitPlaybackRealTimeRate(double realTimeRate)
   {
      if (playbackRealTimeRate.get().doubleValue() == realTimeRate)
         return;

      playbackRealTimeRate.set(new Double(realTimeRate));
      restartSessionTask();
   }

   public void submitDesiredBufferPublishPeriod(long publishPeriod)
   {
      desiredBufferPublishPeriod.set(publishPeriod);
   }

   public void submitCropBufferRequest(CropBufferRequest cropBufferRequest)
   {
      pendingCropBufferRequest.set(cropBufferRequest);
   }

   public void submitBufferSizeRequest(Integer bufferSizeRequest)
   {
      pendingBufferSizeRequest.set(bufferSizeRequest);
   }

   public void submitBufferIndexRequest(Integer bufferIndexRequest)
   {
      pendingBufferIndexRequest.set(bufferIndexRequest);
   }

   public void submitIncrementBufferIndexRequest(Integer incrementBufferIndexRequest)
   {
      pendingIncrementBufferIndexRequest.set(incrementBufferIndexRequest);
   }

   public void submitDecrementBufferIndexRequest(Integer incrementBufferIndexRequest)
   {
      pendingDecrementBufferIndexRequest.set(incrementBufferIndexRequest);
   }

   public void submitBufferInPointIndexRequest(Integer bufferInPointIndexRequest)
   {
      pendingBufferInPointIndexRequest.set(bufferInPointIndexRequest);
   }

   public void submitBufferOutPointIndexRequest(Integer bufferOutPointIndexRequest)
   {
      pendingBufferOutPointIndexRequest.set(bufferOutPointIndexRequest);
   }

   public void setSessionState(SessionState state)
   {
      if (state == SessionState.ACTIVE)
      {
         if (!sessionStarted)
         {
            LogTools.info("Starting session");
            startSessionThread();
         }
      }
      else
      {
         if (!isSessionShutdown)
            scheduleShutdown();
      }
   }

   public void startSessionThread()
   {
      LogTools.info("Started session's thread");
      sessionStarted = true;
      restartSessionTask();
      executorService.scheduleAtFixedRate(this::reportActiveMode, 500, 200, TimeUnit.MILLISECONDS);
   }

   private void scheduleShutdown()
   {
      executorService.execute(() -> shutdownSession());
   }

   public void shutdownSession()
   {
      if (isSessionShutdown)
         return;

      isSessionShutdown = true;

      LogTools.info("Stopped session's thread");
      sessionStarted = false;
      if (sessionTopicListeners != null)
      {
         sessionTopicListeners.detachFromMessager();
         sessionTopicListeners = null;
      }
      executorService.shutdown();
   }

   private void restartSessionTask()
   {
      if (!sessionStarted)
         return;

      if (activeScheduledFuture != null)
         activeScheduledFuture.cancel(false);

      activeScheduledFuture = executorService.scheduleAtFixedRate(sessionModeToTaskMap.get(activeMode.get()),
                                                                  0,
                                                                  computeThreadPeriod(activeMode.get()),
                                                                  TimeUnit.NANOSECONDS);
      reportActiveMode();
   }

   private long computeThreadPeriod(SessionMode mode)
   {
      switch (mode)
      {
         case RUNNING:
            return computeRunTaskPeriod();
         case PAUSE:
            return computePauseTaskPeriod();
         case PLAYBACK:
            return computePlaybackTaskPeriod();
         default:
            throw new UnsupportedOperationException("Unhandled session mode: " + mode);
      }
   }

   protected void setSessionModeTask(SessionMode sessionMode, Runnable runnable)
   {
      sessionModeToTaskMap.put(sessionMode, runnable);
   }

   private void reportActiveMode()
   {
      for (Consumer<SessionProperties> listener : sessionPropertiesListeners)
         listener.accept(new SessionProperties(activeMode.get(),
                                               runAtRealTimeRate.get(),
                                               playbackRealTimeRate.get().doubleValue(),
                                               sessionTickToTimeIncrement.get()));
   }

   /**
    * Called when starting this session regardless of the initial mode.
    */
   public void initializeSession()
   {
      sharedBuffer.registerMissingBuffers();
   }

   protected long computeRunTaskPeriod()
   {
      return runAtRealTimeRate.get() ? sessionTickToTimeIncrement.get() : 1L;
   }

   public void runTick()
   {
      if (!sessionInitialized)
      {
         initializeSession();
         sessionInitialized = true;
      }

      initializeRunTick();

      boolean caughtException;
      try
      {
         doSpecificRunTick();
         caughtException = false;
      }
      catch (Exception e)
      {
         e.printStackTrace();
         caughtException = true;
      }

      finalizeRunTick();

      if (caughtException)
         setSessionMode(SessionMode.PAUSE);
   }

   protected void initializeRunTick()
   {
      sharedBuffer.incrementBufferIndex(true);

      if (firstRunTick)
      {
         sharedBuffer.setInPoint(sharedBuffer.getProperties().getCurrentIndex());
         firstRunTick = false;
      }

      sharedBuffer.processLinkedPushRequests(false);
   }

   protected abstract void doSpecificRunTick();

   protected void finalizeRunTick()
   {
      sharedBuffer.writeBuffer();

      long currentTimestamp = System.nanoTime();

      if (currentTimestamp - lastPublishedBufferTimestamp > desiredBufferPublishPeriod.get())
      {
         sharedBuffer.prepareLinkedBuffersForPull();
         lastPublishedBufferTimestamp = currentTimestamp;
      }

      processBufferRequests(false);
      publishBufferProperties(sharedBuffer.getProperties());
   }

   protected long computePlaybackTaskPeriod()
   {
      if (playbackRealTimeRate.get().doubleValue() <= 0.5)
      {
         stepSizePerPlaybackTick = 1;
         return (long) (sessionTickToTimeIncrement.get() / playbackRealTimeRate.get().doubleValue());
      }

      stepSizePerPlaybackTick = 2 * Math.max(1, (int) Math.floor(playbackRealTimeRate.get().doubleValue()));
      long timeIncrement = sessionTickToTimeIncrement.longValue() * stepSizePerPlaybackTick;
      return (long) (timeIncrement / playbackRealTimeRate.get().doubleValue());
   }

   public void playbackTick()
   {
      if (!sessionInitialized)
      {
         initializeSession();
         sharedBuffer.writeBuffer();
         sessionInitialized = true;
      }

      initializePlaybackTick();

      boolean caughtException;
      try
      {
         doSpecificPlaybackTick();
         caughtException = false;
      }
      catch (Exception e)
      {
         e.printStackTrace();
         caughtException = true;
      }

      finalizePlaybackTick();

      if (caughtException)
         setSessionMode(SessionMode.PAUSE);
   }

   protected void initializePlaybackTick()
   {
      sharedBuffer.flushLinkedPushRequests();
      sharedBuffer.readBuffer();
   }

   protected void doSpecificPlaybackTick()
   {

   }

   protected void finalizePlaybackTick()
   {
      long currentTimestamp = System.nanoTime();

      if (currentTimestamp - lastPublishedBufferTimestamp > desiredBufferPublishPeriod.get())
      {
         sharedBuffer.prepareLinkedBuffersForPull();
         lastPublishedBufferTimestamp = currentTimestamp;
      }

      sharedBuffer.incrementBufferIndex(false, stepSizePerPlaybackTick);
      processBufferRequests(false);
      publishBufferProperties(sharedBuffer.getProperties());
   }

   protected long computePauseTaskPeriod()
   {
      return Conversions.secondsToNanoseconds(0.01);
   }

   public void pauseTick()
   {
      if (!sessionInitialized)
      {
         initializeSession();
         sharedBuffer.writeBuffer();
         sessionInitialized = true;
      }

      boolean shouldPublishBuffer = initializePauseTick();
      shouldPublishBuffer |= doSpecificPauseTick();
      finalizePauseTick(shouldPublishBuffer);
   }

   protected boolean initializePauseTick()
   {
      boolean shouldPublish = firstPauseTick;
      firstPauseTick = false;
      shouldPublish |= sharedBuffer.processLinkedPushRequests(true);
      shouldPublish |= processBufferRequests(true);
      if (!shouldPublish)
         shouldPublish = sharedBuffer.hasRequestPending();

      return shouldPublish;
   }

   protected boolean doSpecificPauseTick()
   {
      return false;
   }

   protected void finalizePauseTick(boolean shouldPublishBuffer)
   {
      if (shouldPublishBuffer)
      {
         sharedBuffer.readBuffer();
         sharedBuffer.prepareLinkedBuffersForPull();
      }
      publishBufferProperties(sharedBuffer.getProperties());
   }

   protected void publishBufferProperties(YoBufferPropertiesReadOnly bufferProperties)
   {
      for (Consumer<YoBufferPropertiesReadOnly> listener : currentBufferPropertiesListeners)
      {
         listener.accept(bufferProperties.copy());
      }
   }

   protected boolean processBufferRequests(boolean bufferChangesPermitted)
   {
      boolean hasBufferBeenUpdated = false;

      if (bufferChangesPermitted)
      {
         CropBufferRequest newCropRequest = pendingCropBufferRequest.getAndSet(null);
         if (newCropRequest != null)
         {
            sharedBuffer.cropBuffer(newCropRequest);
            hasBufferBeenUpdated = true;
            bufferChangesPermitted = false;
         }
      }

      if (bufferChangesPermitted)
      {
         Integer newIndex = pendingBufferIndexRequest.getAndSet(null);
         if (newIndex != null)
            hasBufferBeenUpdated |= sharedBuffer.setCurrentIndex(newIndex.intValue());

         Integer newInPoint = pendingBufferInPointIndexRequest.getAndSet(null);
         if (newInPoint != null)
            hasBufferBeenUpdated |= sharedBuffer.setInPoint(newInPoint.intValue());

         Integer newOutPoint = pendingBufferOutPointIndexRequest.getAndSet(null);
         if (newOutPoint != null)
            hasBufferBeenUpdated |= sharedBuffer.setOutPoint(newOutPoint.intValue());

         Integer stepSize = pendingIncrementBufferIndexRequest.getAndSet(null);
         if (stepSize != null)
         {
            sharedBuffer.incrementBufferIndex(false, stepSize.intValue());
            hasBufferBeenUpdated = true;
         }

         stepSize = pendingDecrementBufferIndexRequest.getAndSet(null);
         if (stepSize != null)
         {
            sharedBuffer.decrementBufferIndex(stepSize.intValue());
            hasBufferBeenUpdated = true;
         }

         Integer newSize = pendingBufferSizeRequest.getAndSet(null);
         if (newSize != null)
         {
            hasBufferBeenUpdated |= sharedBuffer.resizeBuffer(newSize.intValue());
         }

         return hasBufferBeenUpdated;
      }
      else
      {
         Integer newSize = pendingBufferSizeRequest.getAndSet(null);
         if (newSize != null)
         {
            sharedBuffer.resizeBuffer(newSize.intValue());
         }

         pendingBufferIndexRequest.set(null);
         pendingIncrementBufferIndexRequest.set(null);
         pendingDecrementBufferIndexRequest.set(null);
         pendingBufferInPointIndexRequest.set(null);
         pendingBufferOutPointIndexRequest.set(null);
         pendingCropBufferRequest.set(null);
         return hasBufferBeenUpdated;
      }
   }

   public boolean hasSessionStarted()
   {
      return sessionStarted;
   }

   public SessionMode getActiveMode()
   {
      return activeMode.get();
   }

   public boolean getRunAtRealTimeRate()
   {
      return runAtRealTimeRate.get();
   }

   public double getPlaybackRealTimeRate()
   {
      return playbackRealTimeRate.get().doubleValue();
   }

   public long getSessionTickToTimeIncrement()
   {
      return sessionTickToTimeIncrement.get();
   }

   public long getDesiredBufferPublishPeriod()
   {
      return desiredBufferPublishPeriod.get();
   }

   public abstract String getSessionName();

   public abstract List<RobotDefinition> getRobotDefinitions();

   public abstract List<TerrainObjectDefinition> getTerrainObjectDefinitions();

   public LinkedYoVariableFactory getLinkedYoVariableFactory()
   {
      return sharedBuffer;
   }

   private class SessionTopicListeners
   {
      private final Messager messager;

      private final TopicListener<CropBufferRequest> cropRequestListener = Session.this::submitCropBufferRequest;
      private final TopicListener<Integer> currentIndexListener = Session.this::submitBufferIndexRequest;
      private final TopicListener<Integer> inPointIndexListener = Session.this::submitBufferInPointIndexRequest;
      private final TopicListener<Integer> outPointIndexListener = Session.this::submitBufferOutPointIndexRequest;
      private final TopicListener<Integer> incrementCurrentIndexListener = Session.this::submitIncrementBufferIndexRequest;
      private final TopicListener<Integer> decrementCurrentIndexListener = Session.this::submitDecrementBufferIndexRequest;
      private final TopicListener<Integer> currentBufferSizeListener = Session.this::submitBufferSizeRequest;

      private final TopicListener<SessionState> sessionCurrentStateListener = Session.this::setSessionState;
      private final TopicListener<SessionMode> sessionCurrentModeListener = Session.this::setSessionMode;
      private final TopicListener<Long> sessionTicktoTimeIncrementListener = Session.this::setSessionTickToTimeIncrement;
      private final TopicListener<Boolean> runAtRealTimeRateListener = Session.this::submitRunAtRealTimeRate;
      private final TopicListener<Double> playbackRealTimeRate = Session.this::submitPlaybackRealTimeRate;

      private final Consumer<YoBufferPropertiesReadOnly> bufferPropertiesListener = createBufferPropertiesListener();
      private final Consumer<SessionProperties> sessionPropertiesListener = createSessionPropertiesListener();

      private SessionTopicListeners(Messager messager)
      {
         this.messager = messager;

         addCurrentBufferPropertiesListener(bufferPropertiesListener);

         messager.registerTopicListener(YoSharedBufferMessagerAPI.CropRequest, cropRequestListener);
         messager.registerTopicListener(YoSharedBufferMessagerAPI.CurrentIndexRequest, currentIndexListener);
         messager.registerTopicListener(YoSharedBufferMessagerAPI.InPointIndexRequest, inPointIndexListener);
         messager.registerTopicListener(YoSharedBufferMessagerAPI.OutPointIndexRequest, outPointIndexListener);
         messager.registerTopicListener(YoSharedBufferMessagerAPI.IncrementCurrentIndexRequest, incrementCurrentIndexListener);
         messager.registerTopicListener(YoSharedBufferMessagerAPI.DecrementCurrentIndexRequest, decrementCurrentIndexListener);
         messager.registerTopicListener(YoSharedBufferMessagerAPI.CurrentBufferSizeRequest, currentBufferSizeListener);

         addSessionPropertiesListener(sessionPropertiesListener);

         messager.registerTopicListener(SessionMessagerAPI.SessionCurrentState, sessionCurrentStateListener);
         messager.registerTopicListener(SessionMessagerAPI.SessionCurrentMode, sessionCurrentModeListener);
         messager.registerTopicListener(SessionMessagerAPI.SessionTickToTimeIncrement, sessionTicktoTimeIncrementListener);
         messager.registerTopicListener(SessionMessagerAPI.RunAtRealTimeRate, runAtRealTimeRateListener);
         messager.registerTopicListener(SessionMessagerAPI.PlaybackRealTimeRate, playbackRealTimeRate);
      }

      private void detachFromMessager()
      {
         if (messager == null)
            return;

         messager.removeTopicListener(YoSharedBufferMessagerAPI.CropRequest, cropRequestListener);
         messager.removeTopicListener(YoSharedBufferMessagerAPI.CurrentIndexRequest, currentIndexListener);
         messager.removeTopicListener(YoSharedBufferMessagerAPI.InPointIndexRequest, inPointIndexListener);
         messager.removeTopicListener(YoSharedBufferMessagerAPI.OutPointIndexRequest, outPointIndexListener);
         messager.removeTopicListener(YoSharedBufferMessagerAPI.IncrementCurrentIndexRequest, incrementCurrentIndexListener);
         messager.removeTopicListener(YoSharedBufferMessagerAPI.DecrementCurrentIndexRequest, decrementCurrentIndexListener);
         messager.removeTopicListener(YoSharedBufferMessagerAPI.CurrentBufferSizeRequest, currentBufferSizeListener);

         messager.removeTopicListener(SessionMessagerAPI.SessionCurrentState, sessionCurrentStateListener);
         messager.removeTopicListener(SessionMessagerAPI.SessionCurrentMode, sessionCurrentModeListener);
         messager.removeTopicListener(SessionMessagerAPI.SessionTickToTimeIncrement, sessionTicktoTimeIncrementListener);
         messager.removeTopicListener(SessionMessagerAPI.RunAtRealTimeRate, runAtRealTimeRateListener);
         messager.removeTopicListener(SessionMessagerAPI.PlaybackRealTimeRate, playbackRealTimeRate);
      }

      private Consumer<YoBufferPropertiesReadOnly> createBufferPropertiesListener()
      {
         return bufferProperties ->
         {
            if (!messager.isMessagerOpen())
               return;

            messager.submitMessage(YoSharedBufferMessagerAPI.CurrentBufferProperties, bufferProperties);
         };
      }

      private Consumer<SessionProperties> createSessionPropertiesListener()
      {
         return sessionProperties ->
         {
            if (!messager.isMessagerOpen())
               return;

            messager.submitMessage(SessionMessagerAPI.SessionCurrentMode, sessionProperties.getActiveMode());
            messager.submitMessage(SessionMessagerAPI.SessionTickToTimeIncrement, sessionProperties.getSessionTickToTimeIncrement());
            messager.submitMessage(SessionMessagerAPI.PlaybackRealTimeRate, sessionProperties.getPlaybackRealTimeRate());
            messager.submitMessage(SessionMessagerAPI.RunAtRealTimeRate, sessionProperties.isRunAtRealTimeRate());
         };
      }
   }
}
