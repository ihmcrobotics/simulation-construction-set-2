package us.ihmc.scs2.session;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import javax.xml.bind.JAXBException;

import us.ihmc.commons.Conversions;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.referenceFrame.tools.ReferenceFrameTools;
import us.ihmc.log.LogTools;
import us.ihmc.messager.Messager;
import us.ihmc.messager.TopicListener;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.terrain.TerrainObjectDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicDefinition;
import us.ihmc.scs2.sharedMemory.CropBufferRequest;
import us.ihmc.scs2.sharedMemory.FillBufferRequest;
import us.ihmc.scs2.sharedMemory.YoSharedBuffer;
import us.ihmc.scs2.sharedMemory.interfaces.LinkedYoVariableFactory;
import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;
import us.ihmc.yoVariables.registry.YoRegistry;
import us.ihmc.yoVariables.variable.YoDouble;

public abstract class Session
{
   public static final String ROOT_REGISTRY_NAME = "root";
   public static final ReferenceFrame DEFAULT_INERTIAL_FRAME = ReferenceFrameTools.constructARootFrame("worldFrame");

   protected final YoRegistry rootRegistry = new YoRegistry(ROOT_REGISTRY_NAME);
   protected final YoRegistry sessionRegistry = new YoRegistry(getClass().getSimpleName());
   protected final YoDouble time = new YoDouble("time", rootRegistry);
   private final JVMStatisticsGenerator jvmStatisticsGenerator = new JVMStatisticsGenerator(sessionRegistry);

   protected final YoRegistry runRegistry = new YoRegistry("runStatistics");
   private final YoTimer runActualDT = new YoTimer("runActualDT", TimeUnit.MILLISECONDS, runRegistry);
   private final YoTimer runTimer = new YoTimer("runTimer", TimeUnit.MILLISECONDS, runRegistry);
   private final YoTimer runInitializeTimer = new YoTimer("runInitializeTimer", TimeUnit.MILLISECONDS, runRegistry);
   private final YoTimer runSpecificTimer = new YoTimer("runSpecificTimer", TimeUnit.MILLISECONDS, runRegistry);
   private final YoTimer runFinalizeTimer = new YoTimer("runFinalizeTimer", TimeUnit.MILLISECONDS, runRegistry);

   protected final YoRegistry playbackRegistry = new YoRegistry("playbackStatistics");
   private final YoTimer playbackActualDT = new YoTimer("playbackActualDT", TimeUnit.MILLISECONDS, playbackRegistry);
   private final YoTimer playbackTimer = new YoTimer("playbackTimer", TimeUnit.MILLISECONDS, playbackRegistry);

   protected final YoRegistry pauseRegistry = new YoRegistry("pauseStatistics");
   private final YoTimer pauseActualDT = new YoTimer("pauseActualDT", TimeUnit.MILLISECONDS, pauseRegistry);
   private final YoTimer pauseTimer = new YoTimer("pauseTimer", TimeUnit.MILLISECONDS, pauseRegistry);

   protected final YoSharedBuffer sharedBuffer = new YoSharedBuffer(rootRegistry, 8192);

   private final AtomicReference<SessionMode> activeMode = new AtomicReference<>(SessionMode.PAUSE);
   private final AtomicBoolean runAtRealTimeRate = new AtomicBoolean(false);
   private final AtomicReference<Double> playbackRealTimeRate = new AtomicReference<>(2.0);
   private int stepSizePerPlaybackTick = 1;
   private final AtomicInteger bufferRecordTickPeriod = new AtomicInteger(1);
   /**
    * Map from one session tick to the time increment in the data.
    * <p>
    * When simulating, this corresponds to the simulation DT for instance.
    * </p>
    */
   private final AtomicLong sessionDTNanoseconds = new AtomicLong(Conversions.secondsToNanoseconds(1.0e-4));
   private final AtomicLong desiredBufferPublishPeriod = new AtomicLong(-1L);

   // State listener to publish internal to outside world
   private final long sessionPropertiesPublishPeriod = 500L;
   private long lastSessionPropertiesPublishTimestamp = -1L;
   private final List<SessionModeChangeListener> sessionModeChangeListeners = new ArrayList<>();
   private final List<Consumer<SessionState>> sessionStateChangedListeners = new ArrayList<>();
   private final List<Consumer<SessionProperties>> sessionPropertiesListeners = new ArrayList<>();
   private final List<Consumer<YoBufferPropertiesReadOnly>> currentBufferPropertiesListeners = new ArrayList<>();
   private final List<Runnable> shutdownListeners = new ArrayList<>();

   // For exception handling
   private final List<Consumer<Throwable>> runThrowableListeners = new ArrayList<>();
   private final List<Consumer<Throwable>> playbackThrowableListeners = new ArrayList<>();

   // Fields for external requests on buffer.
   private final AtomicReference<CropBufferRequest> pendingCropBufferRequest = new AtomicReference<>(null);
   private final AtomicReference<FillBufferRequest> pendingFillBufferRequest = new AtomicReference<>(null);
   private final AtomicReference<Integer> pendingBufferIndexRequest = new AtomicReference<>(null);
   private final AtomicReference<Integer> pendingBufferInPointIndexRequest = new AtomicReference<>(null);
   private final AtomicReference<Integer> pendingBufferOutPointIndexRequest = new AtomicReference<>(null);
   private final AtomicReference<Integer> pendingIncrementBufferIndexRequest = new AtomicReference<>(null);
   private final AtomicReference<Integer> pendingDecrementBufferIndexRequest = new AtomicReference<>(null);
   private final AtomicReference<Integer> pendingBufferSizeRequest = new AtomicReference<>(null);
   private final AtomicReference<SessionDataExportRequest> pendingDataExportRequest = new AtomicReference<>(null);

   // Strictly internal fields
   private final List<SessionTopicListenerManager> sessionTopicListenerManagers = new ArrayList<>();
   private boolean sessionStarted = false;
   private boolean sessionInitialized = false;
   private boolean isSessionShutdown = false;
   private long lastPublishedBufferTimestamp = -1L;
   protected boolean firstRunTick = true;
   protected boolean firstPauseTick = true;

   protected final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2, new DaemonThreadFactory("SCS2-Session-Thread"));
   protected PeriodicTaskWrapper activePeriodicTask;

   private final EnumMap<SessionMode, Runnable> sessionModeToTaskMap = new EnumMap<>(SessionMode.class);

   protected boolean hasBufferSizeBeenInitialized = false;
   protected boolean hasBufferRecordPeriodBeenInitialized = false;

   public Session()
   {
      rootRegistry.addChild(sessionRegistry);
      sessionRegistry.addChild(runRegistry);
      sessionRegistry.addChild(playbackRegistry);
      sessionRegistry.addChild(pauseRegistry);

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
      for (SessionTopicListenerManager manager : sessionTopicListenerManagers)
      {
         if (messager == manager.messager)
            throw new IllegalArgumentException("Messager already registered.");
      }

      sessionTopicListenerManagers.add(new SessionTopicListenerManager(messager));
   }

   public void setSessionMode(SessionMode sessionMode)
   {
      setSessionMode(sessionMode, null);
   }

   protected void setSessionMode(SessionMode sessionMode, SessionModeTransition transition)
   {
      SessionMode currentMode = activeMode.get();

      if (sessionMode != currentMode)
         scheduleSessionTask(sessionMode, transition);
   }

   public void addSessionModeChangeListener(SessionModeChangeListener listener)
   {
      sessionModeChangeListeners.add(listener);
   }

   public void removeSessionModeChangeListener(SessionModeChangeListener listener)
   {
      sessionModeChangeListeners.remove(listener);
   }

   public void addSessionStateChangedListener(Consumer<SessionState> listener)
   {
      sessionStateChangedListeners.add(listener);
   }

   public void removeSessionStateChangedListener(Consumer<SessionState> listener)
   {
      sessionStateChangedListeners.remove(listener);
   }

   public void addShutdownListener(Runnable listener)
   {
      shutdownListeners.add(listener);
   }

   public void removeShutdownListener(Runnable listener)
   {
      shutdownListeners.remove(listener);
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

   public void addRunThrowableListener(Consumer<Throwable> listener)
   {
      runThrowableListeners.add(listener);
   }

   public void removeRunThrowableListener(Consumer<Throwable> listener)
   {
      runThrowableListeners.remove(listener);
   }

   public void addPlaybackThrowableListener(Consumer<Throwable> listener)
   {
      playbackThrowableListeners.add(listener);
   }

   public void removePlaybackThrowableListener(Consumer<Throwable> listener)
   {
      playbackThrowableListeners.remove(listener);
   }

   public void setSessionDTSeconds(double sessionDTSeconds)
   {
      setSessionDTNanoseconds(Conversions.secondsToNanoseconds(sessionDTSeconds));
   }

   public void setSessionDTNanoseconds(long sessionDTNanoseconds)
   {
      if (this.sessionDTNanoseconds.get() == sessionDTNanoseconds)
         return;

      this.sessionDTNanoseconds.set(sessionDTNanoseconds);
      scheduleSessionTask(getActiveMode());
   }

   public boolean initializeBufferSize(int bufferSize)
   {
      if (hasBufferSizeBeenInitialized)
         return false;
      submitBufferSizeRequest(bufferSize);
      return true;
   }

   public boolean initializeBufferRecordTickPeriod(int bufferRecordTickPeriod)
   {
      if (hasBufferRecordPeriodBeenInitialized)
         return false;
      submitBufferRecordTickPeriod(bufferRecordTickPeriod);
      return true;
   }

   public void submitRunAtRealTimeRate(boolean runAtRealTimeRate)
   {
      if (this.runAtRealTimeRate.get() == runAtRealTimeRate)
         return;

      this.runAtRealTimeRate.set(runAtRealTimeRate);
      scheduleSessionTask(getActiveMode());
   }

   public void submitPlaybackRealTimeRate(double realTimeRate)
   {
      if (playbackRealTimeRate.get().doubleValue() == realTimeRate)
         return;

      playbackRealTimeRate.set(Double.valueOf(realTimeRate));
      scheduleSessionTask(getActiveMode());
   }

   public void submitBufferRecordTickPeriod(int bufferRecordTickPeriod)
   {
      hasBufferRecordPeriodBeenInitialized = true;
      if (bufferRecordTickPeriod == this.bufferRecordTickPeriod.get())
         return;
      this.bufferRecordTickPeriod.set(Math.max(1, bufferRecordTickPeriod));
   }

   public void submitDesiredBufferPublishPeriod(long publishPeriod)
   {
      desiredBufferPublishPeriod.set(publishPeriod);
   }

   public void submitCropBufferRequest(CropBufferRequest cropBufferRequest)
   {
      pendingCropBufferRequest.set(cropBufferRequest);
   }

   public void submitFillBufferRequest(FillBufferRequest fillBufferRequest)
   {
      pendingFillBufferRequest.set(fillBufferRequest);
   }

   public void submitBufferSizeRequest(Integer bufferSizeRequest)
   {
      pendingBufferSizeRequest.set(bufferSizeRequest);
      hasBufferSizeBeenInitialized = true;
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

   public void submitSessionDataExportRequest(SessionDataExportRequest sessionDataExportRequest)
   {
      pendingDataExportRequest.set(sessionDataExportRequest);
   }

   public void setSessionState(SessionState state)
   {
      if (state == SessionState.ACTIVE)
      {
         if (!sessionStarted)
         {
            LogTools.info("Starting session");
            startSessionThread();
            sessionStateChangedListeners.forEach(listener -> listener.accept(state));
         }
      }
      else
      {
         if (!isSessionShutdown)
         {
            shutdownSession();
            sessionStateChangedListeners.forEach(listener -> listener.accept(state));
         }
      }
   }

   public void startSessionThread()
   {
      LogTools.info("Started session's thread");
      sessionStarted = true;
      scheduleSessionTask(getActiveMode());
   }

   public void shutdownSession()
   {
      if (isSessionShutdown)
         return;

      isSessionShutdown = true;

      shutdownListeners.forEach(Runnable::run);

      LogTools.info("Stopped session's thread");
      sessionStarted = false;

      if (activePeriodicTask != null)
      {
         activePeriodicTask.stopAndWait();
         activePeriodicTask = null;
      }

      sessionTopicListenerManagers.forEach(SessionTopicListenerManager::detachFromMessager);
      sessionTopicListenerManagers.clear();
      sharedBuffer.dispose();
      rootRegistry.clear();

      executorService.shutdown();
   }

   private void scheduleSessionTask(SessionMode sessionMode)
   {
      scheduleSessionTask(sessionMode, null);
   }

   private void scheduleSessionTask(SessionMode newMode, SessionModeTransition transition)
   {
      if (!sessionStarted)
         return;

      SessionMode previousMode = activeMode.get();

      if (activePeriodicTask != null)
      {
         activePeriodicTask.stopAndWait();
         activePeriodicTask = null;
      }

      if (isSessionShutdown)
         return;

      if (newMode != previousMode)
      {
         firstRunTick = true;
         firstPauseTick = true;
      }

      runActualDT.reset();
      playbackActualDT.reset();
      pauseActualDT.reset();

      Runnable command;
      Runnable sessionModeTask = sessionModeToTaskMap.get(newMode);

      if (transition == null)
      {
         command = sessionModeTask;
      }
      else
      {
         Objects.requireNonNull(transition.getNextMode(), "nextMode argument is required when providing a terminalCondition");

         command = new Runnable()
         {
            private boolean terminated = false;

            @Override
            public void run()
            {
               sessionModeTask.run();

               if (terminated)
                  return;

               terminated = transition.isDone();

               if (terminated)
               {
                  executorService.execute(() ->
                  {
                     setSessionMode(transition.getNextMode());
                     transition.notifyTransitionComplete();
                  });
               }
            }
         };
      }

      if (isSessionShutdown)
         return;

      activePeriodicTask = new PeriodicTaskWrapper(command, computeThreadPeriod(newMode), TimeUnit.NANOSECONDS);
      activeMode.set(newMode);
      schedulingSessionMode(previousMode, newMode);
      executorService.execute(activePeriodicTask);
      sessionModeChangeListeners.forEach(listener -> listener.onChange(previousMode, newMode));
      reportActiveMode();
   }

   protected void schedulingSessionMode(SessionMode previousMode, SessionMode newMode)
   {

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
         listener.accept(getSessionProperties());
   }

   public SessionProperties getSessionProperties()
   {
      return new SessionProperties(activeMode.get(),
                                   runAtRealTimeRate.get(),
                                   playbackRealTimeRate.get().doubleValue(),
                                   sessionDTNanoseconds.get(),
                                   bufferRecordTickPeriod.get());
   }

   /**
    * Called when starting this session regardless of the initial mode.
    */
   public void initializeSession()
   {
   }

   protected long computeRunTaskPeriod()
   {
      return runAtRealTimeRate.get() ? sessionDTNanoseconds.get() : 1L;
   }

   /**
    * Stuff that needs to be done no matter the active session mode.
    */
   protected void doGeneric(SessionMode currentMode)
   {
      if (!sessionInitialized)
      {
         initializeSession();
         // Not sure why we wouldn't want that when starting in RUNNING.
         // When running simulation, the session starts in PAUSE, writing in the buffer allows to write the robot initial state.
         if (currentMode == SessionMode.PAUSE || currentMode == SessionMode.PLAYBACK)
            sharedBuffer.writeBuffer();
         sessionInitialized = true;
      }

      long currentTimestamp = System.nanoTime();

      if (currentTimestamp - lastSessionPropertiesPublishTimestamp > sessionPropertiesPublishPeriod)
      {
         lastSessionPropertiesPublishTimestamp = currentTimestamp;
         reportActiveMode();
      }
   }

   private int nextBufferRecordTickCounter = 0;

   public void runTick()
   {
      runTimer.start();
      runActualDT.update();

      doGeneric(SessionMode.RUNNING);

      runInitializeTimer.start();
      initializeRunTick();
      runInitializeTimer.stop();

      boolean caughtException;
      try
      {
         runSpecificTimer.start();
         time.set(doSpecificRunTick());
         runSpecificTimer.stop();
         caughtException = false;
      }
      catch (Throwable e)
      {
         e.printStackTrace();
         runThrowableListeners.forEach(listener -> listener.accept(e));
         caughtException = true;
      }

      jvmStatisticsGenerator.update();

      runFinalizeTimer.start();
      finalizeRunTick(false);
      runFinalizeTimer.stop();

      runTimer.stop();

      if (caughtException)
         setSessionMode(SessionMode.PAUSE);
   }

   protected void initializeRunTick()
   {
      if (firstRunTick)
      {
         sharedBuffer.incrementBufferIndex(true);
         sharedBuffer.processLinkedPushRequests(false);
         nextBufferRecordTickCounter = 0;
         firstRunTick = false;
      }
      else if (nextBufferRecordTickCounter <= 0)
      {
         sharedBuffer.incrementBufferIndex(true);
         sharedBuffer.processLinkedPushRequests(false);
      }
   }

   /**
    * Performs action specific to the implementation of this session.
    *
    * @return the current time in seconds.
    */
   protected abstract double doSpecificRunTick();

   protected void finalizeRunTick(boolean forceWriteBuffer)
   {
      boolean writeBuffer = nextBufferRecordTickCounter <= 0;

      if (!writeBuffer && forceWriteBuffer)
      {
         sharedBuffer.incrementBufferIndex(true);
         writeBuffer = true;
      }

      if (writeBuffer)
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

         nextBufferRecordTickCounter = Math.max(1, bufferRecordTickPeriod.get());
      }

      nextBufferRecordTickCounter--;
   }

   protected long computePlaybackTaskPeriod()
   {
      long timeIncrement = sessionDTNanoseconds.get() * bufferRecordTickPeriod.get();

      if (playbackRealTimeRate.get().doubleValue() <= 0.5)
      {
         stepSizePerPlaybackTick = 1;
         return (long) (timeIncrement / playbackRealTimeRate.get().doubleValue());
      }
      else
      {
         stepSizePerPlaybackTick = 2 * Math.max(1, (int) Math.floor(playbackRealTimeRate.get().doubleValue()));
         return (long) (timeIncrement * stepSizePerPlaybackTick / playbackRealTimeRate.get().doubleValue());
      }
   }

   public void playbackTick()
   {
      playbackTimer.start();
      playbackActualDT.update();

      doGeneric(SessionMode.PLAYBACK);

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
         playbackThrowableListeners.forEach(listener -> listener.accept(e));
         caughtException = true;
      }

      finalizePlaybackTick();

      playbackTimer.stop();

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
      pauseTimer.start();
      pauseActualDT.update();

      doGeneric(SessionMode.PAUSE);

      boolean shouldReadBuffer = initializePauseTick();
      shouldReadBuffer |= doSpecificPauseTick();
      finalizePauseTick(shouldReadBuffer);

      pauseTimer.stop();
   }

   protected boolean initializePauseTick()
   {
      boolean shouldReadBuffer = firstPauseTick;
      firstPauseTick = false;
      shouldReadBuffer |= sharedBuffer.processLinkedPushRequests(true);
      shouldReadBuffer |= processBufferRequests(true);
      if (!shouldReadBuffer)
         shouldReadBuffer = sharedBuffer.hasRequestPending();

      return shouldReadBuffer;
   }

   protected boolean doSpecificPauseTick()
   {
      return false;
   }

   protected void finalizePauseTick(boolean shouldReadBuffer)
   {
      if (shouldReadBuffer)
         sharedBuffer.readBuffer();
      sharedBuffer.prepareLinkedBuffersForPull();
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

         FillBufferRequest fillBufferRequest = pendingFillBufferRequest.getAndSet(null);
         if (fillBufferRequest != null)
         {
            sharedBuffer.fillBuffer(fillBufferRequest);
            hasBufferBeenUpdated = true;
         }

         SessionDataExportRequest dataExportRequest = pendingDataExportRequest.getAndSet(null);
         if (dataExportRequest != null)
         {
            try
            {
               SessionIOTools.exportSessionData(this, dataExportRequest);
            }
            catch (JAXBException | IOException | URISyntaxException e)
            {
               e.printStackTrace();
            }
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

   public YoDouble getTime()
   {
      return time;
   }

   public YoRegistry getRootRegistry()
   {
      return rootRegistry;
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

   public int getBufferRecordTickPeriod()
   {
      return bufferRecordTickPeriod.get();
   }

   public double getSessionDTSeconds()
   {
      return sessionDTNanoseconds.get() * 1.0e-9;
   }

   public long getSessionDTNanoseconds()
   {
      return sessionDTNanoseconds.get();
   }

   public long getDesiredBufferPublishPeriod()
   {
      return desiredBufferPublishPeriod.get();
   }

   public abstract String getSessionName();

   public ReferenceFrame getInertialFrame()
   {
      return DEFAULT_INERTIAL_FRAME;
   }

   public abstract List<RobotDefinition> getRobotDefinitions();

   public abstract List<TerrainObjectDefinition> getTerrainObjectDefinitions();

   public List<YoGraphicDefinition> getYoGraphicDefinitions()
   {
      return Collections.emptyList();
   }

   YoSharedBuffer getBuffer()
   {
      return sharedBuffer;
   }

   public LinkedYoVariableFactory getLinkedYoVariableFactory()
   {
      return sharedBuffer;
   }

   private class SessionTopicListenerManager
   {
      private final Messager messager;

      private final TopicListener<CropBufferRequest> cropRequestListener = Session.this::submitCropBufferRequest;
      private final TopicListener<FillBufferRequest> fillRequestListener = Session.this::submitFillBufferRequest;
      private final TopicListener<Integer> currentIndexListener = Session.this::submitBufferIndexRequest;
      private final TopicListener<Integer> inPointIndexListener = Session.this::submitBufferInPointIndexRequest;
      private final TopicListener<Integer> outPointIndexListener = Session.this::submitBufferOutPointIndexRequest;
      private final TopicListener<Integer> incrementCurrentIndexListener = Session.this::submitIncrementBufferIndexRequest;
      private final TopicListener<Integer> decrementCurrentIndexListener = Session.this::submitDecrementBufferIndexRequest;
      private final TopicListener<Integer> currentBufferSizeListener = Session.this::submitBufferSizeRequest;
      private final TopicListener<Integer> initializeBufferSizeListener = Session.this::initializeBufferSize;

      private final TopicListener<SessionState> sessionCurrentStateListener = Session.this::setSessionState;
      private final TopicListener<SessionMode> sessionCurrentModeListener = Session.this::setSessionMode;
      private final TopicListener<Long> sessionDTNanosecondsListener = Session.this::setSessionDTNanoseconds;
      private final TopicListener<Boolean> runAtRealTimeRateListener = Session.this::submitRunAtRealTimeRate;
      private final TopicListener<Double> playbackRealTimeRateListener = Session.this::submitPlaybackRealTimeRate;
      private final TopicListener<Integer> bufferRecordTickPeriodListener = Session.this::submitBufferRecordTickPeriod;
      private final TopicListener<Integer> initializeBufferRecordTickPeriodListener = Session.this::initializeBufferRecordTickPeriod;
      private final TopicListener<SessionDataExportRequest> sessionDataExportRequestListener = Session.this::submitSessionDataExportRequest;

      private final Consumer<YoBufferPropertiesReadOnly> bufferPropertiesListener = createBufferPropertiesListener();
      private final Consumer<SessionProperties> sessionPropertiesListener = createSessionPropertiesListener();

      private SessionTopicListenerManager(Messager messager)
      {
         this.messager = messager;

         addCurrentBufferPropertiesListener(bufferPropertiesListener);

         messager.registerTopicListener(YoSharedBufferMessagerAPI.CropRequest, cropRequestListener);
         messager.registerTopicListener(YoSharedBufferMessagerAPI.FillRequest, fillRequestListener);
         messager.registerTopicListener(YoSharedBufferMessagerAPI.CurrentIndexRequest, currentIndexListener);
         messager.registerTopicListener(YoSharedBufferMessagerAPI.InPointIndexRequest, inPointIndexListener);
         messager.registerTopicListener(YoSharedBufferMessagerAPI.OutPointIndexRequest, outPointIndexListener);
         messager.registerTopicListener(YoSharedBufferMessagerAPI.IncrementCurrentIndexRequest, incrementCurrentIndexListener);
         messager.registerTopicListener(YoSharedBufferMessagerAPI.DecrementCurrentIndexRequest, decrementCurrentIndexListener);
         messager.registerTopicListener(YoSharedBufferMessagerAPI.CurrentBufferSizeRequest, currentBufferSizeListener);
         messager.registerTopicListener(YoSharedBufferMessagerAPI.InitializeBufferSize, initializeBufferSizeListener);

         addSessionPropertiesListener(sessionPropertiesListener);

         messager.registerTopicListener(SessionMessagerAPI.SessionCurrentState, sessionCurrentStateListener);
         messager.registerTopicListener(SessionMessagerAPI.SessionCurrentMode, sessionCurrentModeListener);
         messager.registerTopicListener(SessionMessagerAPI.SessionDTNanoseconds, sessionDTNanosecondsListener);
         messager.registerTopicListener(SessionMessagerAPI.RunAtRealTimeRate, runAtRealTimeRateListener);
         messager.registerTopicListener(SessionMessagerAPI.PlaybackRealTimeRate, playbackRealTimeRateListener);
         messager.registerTopicListener(SessionMessagerAPI.BufferRecordTickPeriod, bufferRecordTickPeriodListener);
         messager.registerTopicListener(SessionMessagerAPI.InitializeBufferRecordTickPeriod, initializeBufferRecordTickPeriodListener);
         messager.registerTopicListener(SessionMessagerAPI.SessionDataExportRequest, sessionDataExportRequestListener);
      }

      private void detachFromMessager()
      {
         if (messager == null)
            return;

         messager.removeTopicListener(YoSharedBufferMessagerAPI.CropRequest, cropRequestListener);
         messager.removeTopicListener(YoSharedBufferMessagerAPI.FillRequest, fillRequestListener);
         messager.removeTopicListener(YoSharedBufferMessagerAPI.CurrentIndexRequest, currentIndexListener);
         messager.removeTopicListener(YoSharedBufferMessagerAPI.InPointIndexRequest, inPointIndexListener);
         messager.removeTopicListener(YoSharedBufferMessagerAPI.OutPointIndexRequest, outPointIndexListener);
         messager.removeTopicListener(YoSharedBufferMessagerAPI.IncrementCurrentIndexRequest, incrementCurrentIndexListener);
         messager.removeTopicListener(YoSharedBufferMessagerAPI.DecrementCurrentIndexRequest, decrementCurrentIndexListener);
         messager.removeTopicListener(YoSharedBufferMessagerAPI.CurrentBufferSizeRequest, currentBufferSizeListener);
         messager.removeTopicListener(YoSharedBufferMessagerAPI.InitializeBufferSize, initializeBufferSizeListener);

         messager.removeTopicListener(SessionMessagerAPI.SessionCurrentState, sessionCurrentStateListener);
         messager.removeTopicListener(SessionMessagerAPI.SessionCurrentMode, sessionCurrentModeListener);
         messager.removeTopicListener(SessionMessagerAPI.SessionDTNanoseconds, sessionDTNanosecondsListener);
         messager.removeTopicListener(SessionMessagerAPI.RunAtRealTimeRate, runAtRealTimeRateListener);
         messager.removeTopicListener(SessionMessagerAPI.PlaybackRealTimeRate, playbackRealTimeRateListener);
         messager.removeTopicListener(SessionMessagerAPI.BufferRecordTickPeriod, bufferRecordTickPeriodListener);
         messager.removeTopicListener(SessionMessagerAPI.InitializeBufferRecordTickPeriod, initializeBufferRecordTickPeriodListener);
         messager.removeTopicListener(SessionMessagerAPI.SessionDataExportRequest, sessionDataExportRequestListener);
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
            messager.submitMessage(SessionMessagerAPI.SessionDTNanoseconds, sessionProperties.getSessionDTNanoseconds());
            messager.submitMessage(SessionMessagerAPI.PlaybackRealTimeRate, sessionProperties.getPlaybackRealTimeRate());
            messager.submitMessage(SessionMessagerAPI.RunAtRealTimeRate, sessionProperties.isRunAtRealTimeRate());
            messager.submitMessage(SessionMessagerAPI.BufferRecordTickPeriod, sessionProperties.getBufferRecordTickPeriod());
         };
      }
   }

   public interface SessionModeTransition
   {
      SessionMode getNextMode();

      boolean isDone();

      default void notifyTransitionComplete()
      {
      }

      static SessionModeTransition newTransition(BooleanSupplier doneCondition, SessionMode nextMode)
      {
         return new SessionModeTransition()
         {
            @Override
            public boolean isDone()
            {
               return doneCondition.getAsBoolean();
            }

            @Override
            public SessionMode getNextMode()
            {
               return nextMode;
            }
         };
      }
   }

   public interface SessionModeChangeListener
   {
      void onChange(SessionMode previousMode, SessionMode newMode);
   }

   public static class PeriodicTaskWrapper implements Runnable
   {
      private static final int ONE_MILLION = 1000000;

      private final Runnable task;
      private final long periodInNanos;
      private final AtomicBoolean running = new AtomicBoolean(true);
      private final AtomicBoolean isDone = new AtomicBoolean(false);
      private final CountDownLatch startedLatch = new CountDownLatch(1);
      private final CountDownLatch doneLatch = new CountDownLatch(1);

      private Thread owner;

      public PeriodicTaskWrapper(Runnable task, long period, TimeUnit timeUnit)
      {
         this.task = task;
         periodInNanos = timeUnit.toNanos(period);
      }

      public void stop()
      {
         running.set(false);
      }

      public boolean isDone()
      {
         return isDone.get();
      }

      public void stopAndWait()
      {
         stop();

         if (Thread.currentThread() == owner)
            return;

         try
         {
            doneLatch.await();
         }
         catch (InterruptedException e)
         {
            e.printStackTrace();
         }
      }

      public void waitUntilFirstTickDone()
      {
         if (Thread.currentThread() == owner)
            return;

         try
         {
            startedLatch.await();
         }
         catch (InterruptedException e)
         {
            e.printStackTrace();
         }
      }

      @Override
      public void run()
      {
         if (owner == null)
            owner = Thread.currentThread();

         try
         {
            while (running.get())
            {
               long timeElapsed = System.nanoTime();
               task.run();
               timeElapsed = System.nanoTime() - timeElapsed;

               startedLatch.countDown();

               if (timeElapsed < periodInNanos)
               {
                  long nanos = periodInNanos - timeElapsed;
                  long millis = nanos / ONE_MILLION;
                  nanos -= millis * ONE_MILLION;

                  try
                  {
                     Thread.sleep(millis, (int) nanos);
                  }
                  catch (InterruptedException e)
                  {
                     e.printStackTrace();
                     running.set(false);
                  }
               }
            }
         }
         catch (Throwable e)
         {
            e.printStackTrace();
         }

         isDone.set(true);
         doneLatch.countDown();
      }
   }
}