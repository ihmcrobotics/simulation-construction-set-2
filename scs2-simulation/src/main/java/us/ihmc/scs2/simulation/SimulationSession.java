package us.ihmc.scs2.simulation;

import us.ihmc.commons.Conversions;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.referenceFrame.tools.ReferenceFrameTools;
import us.ihmc.messager.Messager;
import us.ihmc.messager.TopicListener;
import us.ihmc.scs2.definition.robot.CameraSensorDefinition;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.robot.RobotStateDefinition;
import us.ihmc.scs2.definition.terrain.TerrainObjectDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicDefinition;
import us.ihmc.scs2.session.DaemonThreadFactory;
import us.ihmc.scs2.session.Session;
import us.ihmc.scs2.session.SessionDataExportRequest;
import us.ihmc.scs2.session.SessionMessagerAPI;
import us.ihmc.scs2.session.SessionMessagerAPI.Sensors.SensorMessage;
import us.ihmc.scs2.session.SessionMode;
import us.ihmc.scs2.sharedMemory.CropBufferRequest;
import us.ihmc.scs2.sharedMemory.YoSharedBuffer;
import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;
import us.ihmc.scs2.simulation.SimulationTerminalCondition.TerminalState;
import us.ihmc.scs2.simulation.physicsEngine.PhysicsEngine;
import us.ihmc.scs2.simulation.physicsEngine.PhysicsEngineFactory;
import us.ihmc.scs2.simulation.robot.Robot;
import us.ihmc.scs2.simulation.robot.multiBodySystem.interfaces.SimJointBasics;
import us.ihmc.scs2.simulation.robot.sensors.SimCameraSensor;
import us.ihmc.scs2.simulation.robot.sensors.SimCameraSensor.CameraDefinitionConsumer;
import us.ihmc.scs2.simulation.robot.sensors.SimCameraSensor.CameraFrameConsumer;
import us.ihmc.yoVariables.buffer.interfaces.YoBufferProcessor;
import us.ihmc.yoVariables.euclid.referenceFrame.YoFrameVector3D;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class SimulationSession extends Session
{
   public static final ReferenceFrame DEFAULT_INERTIAL_FRAME = ReferenceFrameTools.constructARootFrame("worldFrame");

   private final PhysicsEngine physicsEngine;
   private final YoFrameVector3D gravity;
   private final String simulationName;
   private final List<YoGraphicDefinition> yoGraphicDefinitions = new ArrayList<>();
   private final List<Consumer<SensorMessage<CameraSensorDefinition>>> cameraDefinitionListeners = new ArrayList<>();
   private final List<CameraDefinitionConsumer> cameraDefinitionNotifiers = new ArrayList<>();
   private final Map<String, Map<String, SimCameraSensor>> robotNameToSensorNameToCameraMap = new HashMap<>();
   private final ExecutorService cameraBroadcastExecutor = Executors.newSingleThreadExecutor(new DaemonThreadFactory("SCS2-Camera-Server"));
   private final List<SimulationTerminalCondition> terminalConditions = new ArrayList<>();

   private final List<TimeConsumer> beforePhysicsCallbacks = new ArrayList<>();
   private final List<TimeConsumer> afterPhysicsCallbacks = new ArrayList<>();

   private final List<Runnable> cleanupActions = new ArrayList<>();

   private SimulationSessionControlsImpl controls = null;

   public SimulationSession()
   {
      this(retrieveCallerName());
   }

   public SimulationSession(PhysicsEngineFactory physicsEngineFactory)
   {
      this(retrieveCallerName(), physicsEngineFactory);
   }

   public SimulationSession(String simulationName)
   {
      this(DEFAULT_INERTIAL_FRAME, simulationName);
   }

   public SimulationSession(ReferenceFrame inertialFrame, String simulationName)
   {
      this(inertialFrame, simulationName, PhysicsEngineFactory.newImpulseBasedPhysicsEngineFactory());
   }

   public SimulationSession(String simulationName, PhysicsEngineFactory physicsEngineFactory)
   {
      this(DEFAULT_INERTIAL_FRAME, simulationName, physicsEngineFactory);
   }

   public SimulationSession(ReferenceFrame inertialFrame, String simulationName, PhysicsEngineFactory physicsEngineFactory)
   {
      super(inertialFrame);

      if (!inertialFrame.isRootFrame())
         throw new IllegalArgumentException("The given inertialFrame is not a root frame: " + inertialFrame);

      this.simulationName = simulationName;

      physicsEngine = physicsEngineFactory.build(inertialFrame, rootRegistry);
      sessionRegistry.addChild(physicsEngine.getPhysicsEngineRegistry());

      setSessionDTSeconds(0.0001);
      setSessionMode(SessionMode.PAUSE);
      gravity = new YoFrameVector3D("gravity", inertialFrame, rootRegistry);
      gravity.set(0.0, 0.0, -9.81);
   }

   public SimulationSessionControls getSimulationSessionControls()
   {
      if (controls == null)
         controls = new SimulationSessionControlsImpl();
      return controls;
   }

   @Override
   protected void initializeSession()
   {
      super.initializeSession();
      physicsEngine.initialize(gravity);
      time.set(0.0);
   }

   @Override
   public void shutdownSession()
   {
      super.shutdownSession();
      cleanupActions.forEach(Runnable::run);
      cleanupActions.clear();
      cameraBroadcastExecutor.shutdown();
      physicsEngine.dispose();
   }

   @Override
   protected void doGeneric(SessionMode currentMode)
   {
      super.doGeneric(currentMode);

      cameraDefinitionNotifiers.forEach(notifier -> notifier.nextDefinition(null));
   }

   @Override
   protected double doSpecificRunTick()
   {
      double dt = Conversions.nanosecondsToSeconds(getSessionDTNanoseconds());
      for (int i = 0; i < beforePhysicsCallbacks.size(); i++)
         beforePhysicsCallbacks.get(i).accept(time.getValue());
      physicsEngine.simulate(time.getValue(), dt, gravity);
      double newTime = time.getValue() + dt;
      for (int i = 0; i < afterPhysicsCallbacks.size(); i++)
         afterPhysicsCallbacks.get(i).accept(newTime);
      return newTime;
   }

   @Override
   protected void schedulingSessionMode(SessionMode previousMode, SessionMode newMode)
   {
      if (previousMode == newMode)
         return;

      if (previousMode == SessionMode.RUNNING)
      {
         physicsEngine.pause();
         finalizeRunTick(true);
      }
   }

   public void addRobot(Robot robot)
   {
      physicsEngine.addRobot(robot);
   }

   public void addRobots(Collection<? extends Robot> robots)
   {
      physicsEngine.addRobots(robots);
   }

   public Robot addRobot(RobotDefinition robotDefinition)
   {
      Robot robot = new Robot(robotDefinition, inertialFrame);
      configureCameraSensors(robot);
      addRobot(robot);
      return robot;
   }

   private void configureCameraSensors(Robot robot)
   {
      for (SimJointBasics joint : robot.getAllJoints())
      {
         for (SimCameraSensor cameraSensor : joint.getAuxiliaryData().getCameraSensors())
            configureCameraSensor(robot.getName(), cameraSensor);
      }
   }

   private void configureCameraSensor(String robotName, SimCameraSensor cameraSensor)
   {
      robotNameToSensorNameToCameraMap.computeIfAbsent(robotName, s -> new HashMap<>()).put(cameraSensor.getName(), cameraSensor);

      CameraDefinitionConsumer cameraDefinitionNotifier = newDefinition ->
      {
         String sensorName = cameraSensor.getName();
         CameraSensorDefinition definitionData = newDefinition != null ? newDefinition : cameraSensor.toCameraSensorDefinition();
         SensorMessage<CameraSensorDefinition> newMessage = new SensorMessage<>(robotName, sensorName, definitionData);
         cameraDefinitionListeners.forEach(listener -> listener.accept(newMessage));
      };
      cameraDefinitionNotifiers.add(cameraDefinitionNotifier);
      cameraSensor.addCameraDefinitionConsumer(cameraDefinitionNotifier);
   }

   @Override
   public void setupWithMessager(Messager messager)
   {
      super.setupWithMessager(messager);

      cameraDefinitionListeners.add(message -> messager.submitMessage(SessionMessagerAPI.Sensors.CameraSensorDefinitionData, message));
      TopicListener<SensorMessage<BufferedImage>> listener = message ->
      {
         long timestamp = Conversions.secondsToNanoseconds(time.getValue());

         SimCameraSensor cameraSensor = robotNameToSensorNameToCameraMap.getOrDefault(message.getRobotName(), Collections.emptyMap())
                                                                        .get(message.getSensorName());

         cameraBroadcastExecutor.execute(() ->
                                         {
                                            for (CameraFrameConsumer consumer : cameraSensor.getCameraFrameConsumers())
                                            {
                                               consumer.nextFrame(timestamp, message.getMessageContent());
                                            }
                                         });
      };
      messager.addTopicListener(SessionMessagerAPI.Sensors.CameraSensorFrame, listener);
      cleanupActions.add(() -> messager.removeTopicListener(SessionMessagerAPI.Sensors.CameraSensorFrame, listener));
   }

   public void addTerrainObject(TerrainObjectDefinition terrainObjectDefinition)
   {
      physicsEngine.addTerrainObject(terrainObjectDefinition);
   }

   public void addTerrainObjects(Collection<? extends TerrainObjectDefinition> terrainObjectDefinitions)
   {
      for (TerrainObjectDefinition terrainObjectDefinition : terrainObjectDefinitions)
      {
         physicsEngine.addTerrainObject(terrainObjectDefinition);
      }
   }

   public void addYoGraphicDefinition(YoGraphicDefinition yoGraphicDefinition)
   {
      yoGraphicDefinitions.add(yoGraphicDefinition);
   }

   public void addYoGraphicDefinitions(YoGraphicDefinition... yoGraphicDefinitions)
   {
      for (YoGraphicDefinition yoGraphicDefinition : yoGraphicDefinitions)
      {
         addYoGraphicDefinition(yoGraphicDefinition);
      }
   }

   public void addYoGraphicDefinitions(Iterable<? extends YoGraphicDefinition> yoGraphicDefinitions)
   {
      for (YoGraphicDefinition yoGraphicDefinition : yoGraphicDefinitions)
      {
         addYoGraphicDefinition(yoGraphicDefinition);
      }
   }

   @Override
   public String getSessionName()
   {
      return simulationName;
   }

   public PhysicsEngine getPhysicsEngine()
   {
      return physicsEngine;
   }

   @Override
   public List<RobotDefinition> getRobotDefinitions()
   {
      return physicsEngine.getRobotDefinitions();
   }

   @Override
   public List<TerrainObjectDefinition> getTerrainObjectDefinitions()
   {
      return physicsEngine.getTerrainObjectDefinitions();
   }

   @Override
   public List<YoGraphicDefinition> getYoGraphicDefinitions()
   {
      return yoGraphicDefinitions;
   }

   @Override
   public List<RobotStateDefinition> getCurrentRobotStateDefinitions(boolean initialState)
   {
      if (initialState)
         return physicsEngine.getBeforePhysicsRobotStateDefinitions();
      else
         return physicsEngine.getCurrentRobotStateDefinitions();
   }

   public void setGravity(double x, double y, double z)
   {
      this.gravity.set(x, y, z);
   }

   public YoFrameVector3D getGravity()
   {
      return gravity;
   }

   public void addBeforePhysicsCallback(TimeConsumer beforePhysicsCallback)
   {
      beforePhysicsCallbacks.add(beforePhysicsCallback);
   }

   public boolean removeBeforePhysicsCallback(TimeConsumer beforePhysicsCallback)
   {
      return beforePhysicsCallbacks.remove(beforePhysicsCallback);
   }

   public void addAfterPhysicsCallback(TimeConsumer afterPhysicsCallback)
   {
      afterPhysicsCallbacks.add(afterPhysicsCallback);
   }

   public boolean removeAfterPhysicsCallback(TimeConsumer afterPhysicsCallback)
   {
      return afterPhysicsCallbacks.remove(afterPhysicsCallback);
   }

   private class SimulationSessionControlsImpl implements SimulationSessionControls
   {
      // ------------------------------------------------------------------------------- //
      // ------------------------ Simulation Properties -------------------------------- //
      // ------------------------------------------------------------------------------- //

      /** {@inheritDoc} */
      @Override
      public double getDT()
      {
         return getSessionDTSeconds();
      }

      /** {@inheritDoc} */
      @Override
      public void setDT(double dt)
      {
         setSessionDTSeconds(dt);
      }

      /** {@inheritDoc} */
      @Override
      public boolean isSimulationThreadRunning()
      {
         return hasSessionStarted();
      }

      /** {@inheritDoc} */
      @Override
      public boolean isRealTimeRateSimulation()
      {
         return getRunAtRealTimeRate();
      }

      /** {@inheritDoc} */
      @Override
      public double getPlaybackRealTimeRate()
      {
         return SimulationSession.this.getPlaybackRealTimeRate();
      }

      /** {@inheritDoc} */
      @Override
      public boolean isSimulating()
      {
         return getActiveMode() == SessionMode.RUNNING;
      }

      /** {@inheritDoc} */
      @Override
      public boolean isPlaying()
      {
         return getActiveMode() == SessionMode.PLAYBACK;
      }

      /** {@inheritDoc} */
      @Override
      public boolean isPaused()
      {
         return getActiveMode() == SessionMode.PAUSE;
      }

      /** {@inheritDoc} */
      @Override
      public boolean isSessionShutdown()
      {
         return SimulationSession.this.isSessionShutdown();
      }

      // ------------------------------------------------------------------------------- //
      // ------------------------- Simulation Controls --------------------------------- //
      // ------------------------------------------------------------------------------- //

      /** {@inheritDoc} */
      @Override
      public boolean startSimulationThread()
      {
         return startSessionThread();
      }

      /** {@inheritDoc} */
      @Override
      public boolean stopSimulationThread()
      {
         return stopSessionThread();
      }

      /** {@inheritDoc} */
      @Override
      public void shutdownSession()
      {
         SimulationSession.this.shutdownSession();
      }

      /** {@inheritDoc} */
      @Override
      public void addSessionShutdownListener(Runnable listener)
      {
         addShutdownListener(listener);
      }

      /** {@inheritDoc} */
      @Override
      public void setRealTimeRateSimulation(boolean enableRealTimeRate)
      {
         submitRunAtRealTimeRate(enableRealTimeRate);
      }

      /** {@inheritDoc} */
      @Override
      public void setPlaybackRealTimeRate(double realTimeRate)
      {
         SimulationSession.this.submitPlaybackRealTimeRate(realTimeRate);
      }

      /** {@inheritDoc} */
      @Override
      public void simulate(double duration)
      {
         if (getActiveMode() == SessionMode.RUNNING)
            setSessionMode(SessionMode.PAUSE);

         submitRunMaxDuration(duration == Double.POSITIVE_INFINITY ? -1L : (long) (duration * 1.0e9));

         SessionModeTransition transition;

         if (terminalConditions.isEmpty())
         {
            transition = null;
         }
         else
         {
            BooleanSupplier terminalCondition = () -> testTerminalConditions() != null;
            transition = SessionModeTransition.newTransition(terminalCondition, SessionMode.PAUSE);
         }

         setSessionMode(SessionMode.RUNNING, transition);
      }

      /** {@inheritDoc} */
      @Override
      public void simulate(int numberOfTicks)
      {
         if (getActiveMode() == SessionMode.RUNNING)
            setSessionMode(SessionMode.PAUSE);

         submitRunMaxDuration(numberOfTicks * getSessionDTNanoseconds());

         SessionModeTransition transition;

         if (terminalConditions.isEmpty())
         {
            transition = null;
         }
         else
         {
            BooleanSupplier terminalCondition = () -> testTerminalConditions() != null;
            transition = SessionModeTransition.newTransition(terminalCondition, SessionMode.PAUSE);
         }

         setSessionMode(SessionMode.RUNNING, transition);
      }

      /** {@inheritDoc} */
      @Override
      public boolean simulateNow(long numberOfTicks)
      {
         if (isSessionShutdown())
            return false;

         boolean sessionStartedInitialValue = isSimulationThreadRunning();

         if (sessionStartedInitialValue)
         {
            if (!stopSimulationThread())
               return false; // Could not stop the thread, abort.
         }

         SessionMode activeModeInitialValue = getActiveMode();
         long maxDurationInitialValue = getRunMaxDuration();
         submitRunMaxDuration(-1L); // Make sure the max duration does not interfere with the number of ticks.

         try
         {
            setSessionMode(SessionMode.RUNNING);

            boolean success = true;

            if (numberOfTicks == -1L || numberOfTicks == Long.MAX_VALUE)
            {
               while (true)
               {
                  if (isSessionShutdown())
                     return false;

                  if (!handleVisualizerSessionModeRequests())
                     break;

                  success = runTick();

                  if (!success)
                     break;

                  TerminalState state = testTerminalConditions();

                  if (state != null)
                  {
                     if (state == TerminalState.FAILURE)
                        success = false;
                     break;
                  }
               }
            }
            else
            {
               for (long tick = 0; tick < numberOfTicks; tick++)
               {
                  if (isSessionShutdown())
                     return false;

                  if (!handleVisualizerSessionModeRequests())
                     break;

                  success = runTick();

                  if (!success)
                     break;

                  TerminalState state = testTerminalConditions();

                  if (state != null)
                  {
                     if (state == TerminalState.FAILURE)
                        success = false;
                     break;
                  }
               }
            }

            return success;
         }
         finally
         {
            // This ensures that the controller is being pause.
            physicsEngine.pause();
            submitRunMaxDuration(maxDurationInitialValue); // Restore the max duration.
            requestBufferListenerForceUpdate();

            if (sessionStartedInitialValue)
               startSessionThread();
            setSessionMode(activeModeInitialValue);
         }
      }

      private boolean handleVisualizerSessionModeRequests()
      {
         if (isSimulating() || !hasWrittenBufferInLastRunTick())
            return true; // Make sure we stop running when the buffer was just updated.

         // The GUI requested a mode change, we pause the simulation until the GUI request RUNNING again.
         CountDownLatch latch = new CountDownLatch(1);

         SessionModeChangeListener listener = (prevMode, newMode) ->
         {
            if (newMode != prevMode && newMode == SessionMode.RUNNING)
            {
               stopSimulationThread();
               if (getBufferCurrentIndex() != getBufferOutPoint())
               { // We make sure to go back to the out-point
                  gotoBufferOutPoint();
                  finalizePauseTick(true);
               }
               latch.countDown();
            }
         };
         addPreSessionModeChangeListener(listener);

         startSessionThread();

         try
         {
            latch.await();
         }
         catch (InterruptedException e)
         {
            return false;
         }
         finally
         {
            removePreSessionModeChangeListener(listener);
         }

         return true;
      }

      private TerminalState testTerminalConditions()
      {
         for (int i = 0; i < terminalConditions.size(); i++)
         {
            TerminalState result = terminalConditions.get(i).testCondition();
            if (result != null)
               return result;
         }

         return null;
      }

      /** {@inheritDoc} */
      @Override
      public void addSimulationThrowableListener(Consumer<Throwable> listener)
      {
         addRunThrowableListener(listener);
      }

      /** {@inheritDoc} */
      @Override
      public void addExternalTerminalCondition(SimulationTerminalCondition... externalTerminalConditions)
      {
         for (int i = 0; i < externalTerminalConditions.length; i++)
         {
            terminalConditions.add(externalTerminalConditions[i]);
         }
      }

      /** {@inheritDoc} */
      @Override
      public boolean removeExternalTerminalCondition(SimulationTerminalCondition externalTerminalCondition)
      {
         return terminalConditions.remove(externalTerminalCondition);
      }

      /** {@inheritDoc} */
      @Override
      public void clearExternalTerminalConditions()
      {
         terminalConditions.clear();
      }

      /** {@inheritDoc} */
      @Override
      public void play()
      {
         setSessionMode(SessionMode.PLAYBACK);
      }

      /** {@inheritDoc} */
      @Override
      public void pause()
      {
         setSessionMode(SessionMode.PAUSE);
      }

      // ------------------------------------------------------------------------------- //
      // -------------------------- Buffer Properties ---------------------------------- //
      // ------------------------------------------------------------------------------- //

      /** {@inheritDoc} */
      @Override
      public YoBufferPropertiesReadOnly getBufferProperties()
      {
         return SimulationSession.this.getBufferProperties();
      }

      /** {@inheritDoc} */
      @Override
      public int getBufferRecordTickPeriod()
      {
         return SimulationSession.this.getBufferRecordTickPeriod();
      }

      /** {@inheritDoc} */
      @Override
      public YoSharedBuffer getBuffer()
      {
         return SimulationSession.this.getBuffer();
      }

      // ------------------------------------------------------------------------------- //
      // --------------------------- Buffer Controls ----------------------------------- //
      // ------------------------------------------------------------------------------- //

      /** {@inheritDoc} */
      @Override
      public boolean initializeBufferRecordTickPeriod(int bufferRecordTickPeriod)
      {
         return SimulationSession.this.initializeBufferRecordTickPeriod(bufferRecordTickPeriod);
      }

      /** {@inheritDoc} */
      @Override
      public void setBufferRecordTickPeriod(int bufferRecordTickPeriod)
      {
         SimulationSession.this.setBufferRecordTickPeriod(bufferRecordTickPeriod);
      }

      @Override
      public void tick()
      {
         if (!isPaused())
            return;

         if (isSimulationThreadRunning())
         {
            stopSimulationThread();
            getBuffer().incrementBufferIndex(true);
            startSessionThread();
         }
         else
         {
            getBuffer().incrementBufferIndex(true);
         }
      }

      /** {@inheritDoc} */
      @Override
      public void gotoBufferIndex(int bufferIndexRequest)
      {
         submitBufferIndexRequestAndWait(bufferIndexRequest);
      }

      /** {@inheritDoc} */
      @Override
      public void setBufferInPoint(int index)
      {
         submitBufferInPointIndexRequestAndWait(index);
      }

      /** {@inheritDoc} */
      @Override
      public void setBufferOutPoint(int index)
      {
         submitBufferOutPointIndexRequestAndWait(index);
      }

      /** {@inheritDoc} */
      @Override
      public void stepBufferIndexBackward(int stepSize)
      {
         submitDecrementBufferIndexRequestAndWait(stepSize);
      }

      /** {@inheritDoc} */
      @Override
      public void stepBufferIndexForward(int stepSize)
      {
         submitIncrementBufferIndexRequestAndWait(stepSize);
      }

      /** {@inheritDoc} */
      @Override
      public void cropBuffer(CropBufferRequest request)
      {
         submitCropBufferRequestAndWait(request);
      }

      /** {@inheritDoc} */
      @Override
      public boolean initializeBufferSize(int bufferSize)
      {
         return SimulationSession.this.initializeBufferSize(bufferSize);
      }

      /** {@inheritDoc} */
      @Override
      public void changeBufferSize(int bufferSize)
      {
         submitBufferSizeRequestAndWait(bufferSize);
      }

      /** {@inheritDoc} */
      @Override
      public void applyBufferProcessor(YoBufferProcessor processor)
      {
         if (!isPaused())
            return;

         if (isSimulationThreadRunning())
         {
            stopSimulationThread();
            getBuffer().applyProcessor(processor);
            startSimulationThread();
         }
         else
         {
            getBuffer().applyProcessor(processor);
         }
      }

      // ------------------------------------------------------------------------------- //
      // ---------------------------- Misc Controls ------------------------------------ //
      // ------------------------------------------------------------------------------- //

      /** {@inheritDoc} */
      @Override
      public String getSimulationName()
      {
         return SimulationSession.this.getSessionName();
      }

      /** {@inheritDoc} */
      @Override
      public void exportData(SessionDataExportRequest request)
      {
         submitSessionDataExportRequestAndWait(request);
      }

      /** {@inheritDoc} */
      @Override
      public void addBeforePhysicsCallback(TimeConsumer beforePhysicsCallback)
      {
         SimulationSession.this.addBeforePhysicsCallback(beforePhysicsCallback);
      }

      /** {@inheritDoc} */
      @Override
      public boolean removeBeforePhysicsCallback(TimeConsumer beforePhysicsCallback)
      {
         return SimulationSession.this.removeBeforePhysicsCallback(beforePhysicsCallback);
      }

      /** {@inheritDoc} */
      @Override
      public void addAfterPhysicsCallback(TimeConsumer afterPhysicsCallback)
      {
         SimulationSession.this.addAfterPhysicsCallback(afterPhysicsCallback);
      }

      /** {@inheritDoc} */
      @Override
      public boolean removeAfterPhysicsCallback(TimeConsumer afterPhysicsCallback)
      {
         return SimulationSession.this.removeAfterPhysicsCallback(afterPhysicsCallback);
      }
   }
}
