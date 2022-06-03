package us.ihmc.scs2;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.stage.Window;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.log.LogTools;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.terrain.TerrainObjectDefinition;
import us.ihmc.scs2.definition.visual.VisualDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicDefinition;
import us.ihmc.scs2.session.Session;
import us.ihmc.scs2.session.SessionDataExportRequest;
import us.ihmc.scs2.session.SessionPropertiesHelper;
import us.ihmc.scs2.sessionVisualizer.jfx.SceneVideoRecordingRequest;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizer;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerControls;
import us.ihmc.scs2.sharedMemory.CropBufferRequest;
import us.ihmc.scs2.sharedMemory.YoSharedBuffer;
import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;
import us.ihmc.scs2.simulation.SimulationSession;
import us.ihmc.scs2.simulation.SimulationSessionControls;
import us.ihmc.scs2.simulation.TimeConsumer;
import us.ihmc.scs2.simulation.parameters.ContactParametersReadOnly;
import us.ihmc.scs2.simulation.parameters.ContactPointBasedContactParameters;
import us.ihmc.scs2.simulation.physicsEngine.PhysicsEngine;
import us.ihmc.scs2.simulation.physicsEngine.PhysicsEngineFactory;
import us.ihmc.scs2.simulation.robot.Robot;
import us.ihmc.yoVariables.buffer.interfaces.YoBufferProcessor;
import us.ihmc.yoVariables.registry.YoNamespace;
import us.ihmc.yoVariables.registry.YoRegistry;
import us.ihmc.yoVariables.registry.YoVariableHolder;
import us.ihmc.yoVariables.variable.YoDouble;
import us.ihmc.yoVariables.variable.YoVariable;

/**
 * Convenience class for creating a simulation environment with a JavaFX GUI.
 * 
 * @author Sylvain Bertrand
 */
public class SimulationConstructionSet2 implements YoVariableHolder, SimulationSessionControls, SessionVisualizerControls
{
   public static final ReferenceFrame inertialFrame = SimulationSession.DEFAULT_INERTIAL_FRAME;

   /**
    * Default value for {@link #visualizerEnabled}. If the system property is not set, it is
    * {@code true} by default.
    */
   public static final boolean DEFAULT_VISUALIZER_ENABLED = SessionPropertiesHelper.loadBooleanProperty("create.scs.gui", true)
         && SessionPropertiesHelper.loadBooleanProperty("scs2.disablegui", true, false);

   private SimulationSession simulationSession;

   private SimulationSessionControls simulationSessionControls;
   private SessionVisualizerControls visualizerControls;

   private boolean visualizerEnabled = DEFAULT_VISUALIZER_ENABLED;
   /**
    * This is initialized to {@code null} such that the JavaFX flag is not set by default, allowing the
    * user to set it from outside.
    */
   private Boolean javaFXThreadImplicitExit = null;

   /**
    * Factory for setting up a contact point based physics engine. It is the default physics engine and
    * is an adaptation of SCS1's physics engine.
    * 
    * @return the physics engine factory.
    */
   public static PhysicsEngineFactory contactPointBasedPhysicsEngineFactory()
   {
      return PhysicsEngineFactory.newContactPointBasedPhysicsEngineFactory();
   }

   /**
    * Factory for setting up a contact point based physics engine. It is the default physics engine and
    * is an adaptation of SCS1's physics engine.
    * 
    * @param contactParameters the parameters to use for resolving contacts.
    * @return the physics engine factory.
    */
   public static PhysicsEngineFactory contactPointBasedPhysicsEngineFactory(ContactPointBasedContactParameters contactParameters)
   {
      return PhysicsEngineFactory.newContactPointBasedPhysicsEngineFactory(contactParameters);
   }

   /**
    * Factory for setting up an impulse based physics engine. It is still at the experimental phase but
    * can handle complex contact interactions.
    * 
    * @return the physics engine factory.
    */
   public static PhysicsEngineFactory impulseBasedPhysicsEngineFactory()
   {
      return impulseBasedPhysicsEngineFactory(null);
   }

   /**
    * Factory for setting up an impulse based physics engine. It is still at the experimental phase but
    * can handle complex contact interactions.
    * 
    * @param contactParameters the parameters to use for resolving contacts.
    * @return the physics engine factory.
    */
   public static PhysicsEngineFactory impulseBasedPhysicsEngineFactory(ContactParametersReadOnly contactParameters)
   {
      return PhysicsEngineFactory.newImpulseBasedPhysicsEngineFactory(contactParameters);
   }

   /**
    * Factory for setting up a physics engine that does close to nothing.
    * <p>
    * This is convenient for using the simulation environment for visualization. The physics engine
    * will still run the controllers attached to the robot and apply their output, but that's it. The
    * robot sensors are not updated.
    * </p>
    * 
    * @return the physics engine factory.
    */
   public static PhysicsEngineFactory doNothingPhysicsEngine()
   {
      return PhysicsEngineFactory.newDoNothingPhysicsEngineFactory();
   }

   /**
    * Creates a new simulation environment.
    * <ul>
    * <li>See {@link #addRobot(RobotDefinition)} for adding robots to the simulation.
    * <li>See {@link #addTerrainObject(TerrainObjectDefinition)} for adding objects to the environment.
    * <li>Call {@link #startSimulationThread()} to fire up the environment before simulating.
    * </ul>
    */
   public SimulationConstructionSet2()
   {
      this(Session.retrieveCallerName(), contactPointBasedPhysicsEngineFactory());
   }

   /**
    * Creates a new simulation environment.
    * <ul>
    * <li>See {@link #addRobot(RobotDefinition)} for adding robots to the simulation.
    * <li>See {@link #addTerrainObject(TerrainObjectDefinition)} for adding objects to the environment.
    * <li>Call {@link #startSimulationThread()} to fire up the environment before simulating.
    * </ul>
    * 
    * @param simulationName the name of the simulation.
    */
   public SimulationConstructionSet2(String simulationName)
   {
      this(simulationName, contactPointBasedPhysicsEngineFactory());
   }

   /**
    * Creates a new simulation environment.
    * <ul>
    * <li>See {@link #addRobot(RobotDefinition)} for adding robots to the simulation.
    * <li>See {@link #addTerrainObject(TerrainObjectDefinition)} for adding objects to the environment.
    * <li>Call {@link #startSimulationThread()} to fire up the environment before simulating.
    * </ul>
    * 
    * @param physicsEngineFactory the factory to use for setting the physics engine.
    */
   public SimulationConstructionSet2(PhysicsEngineFactory physicsEngineFactory)
   {
      this(Session.retrieveCallerName(), physicsEngineFactory);
   }

   /**
    * Creates a new simulation environment.
    * <ul>
    * <li>See {@link #addRobot(RobotDefinition)} for adding robots to the simulation.
    * <li>See {@link #addTerrainObject(TerrainObjectDefinition)} for adding objects to the environment.
    * <li>Call {@link #startSimulationThread()} to fire up the environment before simulating.
    * </ul>
    * 
    * @param simulationName       the name of the simulation.
    * @param physicsEngineFactory the factory to use for setting the physics engine.
    */
   public SimulationConstructionSet2(String simulationName, PhysicsEngineFactory physicsEngineFactory)
   {
      simulationSession = new SimulationSession(inertialFrame, simulationName, physicsEngineFactory);
      simulationSessionControls = simulationSession.getSimulationSessionControls();
   }

   /**
    * Gets the internal session.
    * 
    * @return the simulation session.
    */
   public SimulationSession getSimulationSession()
   {
      return simulationSession;
   }

   /**
    * Gets the instance of the physics engine used in this simulation.
    * 
    * @return the physics engine.
    */
   public PhysicsEngine getPhysicsEngine()
   {
      return simulationSession.getPhysicsEngine();
   }

   /**
    * Sets whether the visualizer should be created.
    *
    * @param visualizerEnabled whether to create the visualizer or not.
    */
   public void setVisualizerEnabled(boolean visualizerEnabled)
   {
      this.visualizerEnabled = visualizerEnabled;
   }

   /**
    * Gets whether the visualizer is to be created or not.
    *
    * @return {@code true} if the visualizer is to be created.
    */
   public boolean isVisualizerEnabled()
   {
      return visualizerEnabled;
   }

   /**
    * Sets the JavaFX implicit exit flag.
    * <p>
    * If {@code true}, the main JavaFX thread will terminate when closing the last window. Once the
    * JavaFX thread is terminated, it is not possible to restart it. Sets this to false to make
    * possible to start a new session with the same JVM.
    * </p>
    * <p>
    * If {@code false}, the main JavaFX thread will only terminate when explicitly requested via
    * {@link Platform#exit()} for instance. This will cause the JVM to not terminate even after
    * shutting down this simulation.
    * </p>
    * <p>
    * If {@code null}, the JavaFX implicit exit flag is not modified.
    * </p>
    *
    * @param javaFXThreadImplicitExit a flag indicating whether or not to implicitly exit when the last
    *                                 window is closed.
    * @see Platform#setImplicitExit(boolean)
    */
   public void setJavaFXThreadImplicitExit(Boolean javaFXThreadImplicitExit)
   {
      this.javaFXThreadImplicitExit = javaFXThreadImplicitExit;
   }

   /**
    * Gets the JavaFX implicit exit flag value.
    * <p>
    * If {@code true}, the main JavaFX thread will terminate when closing the last window. Once the
    * JavaFX thread is terminated, it is not possible to restart it. Sets this to false to make
    * possible to start a new session with the same JVM.
    * </p>
    * <p>
    * If {@code false}, the main JavaFX thread will only terminate when explicitly requested via
    * {@link Platform#exit()} for instance. This will cause the JVM to not terminate even after
    * shutting down this simulation.
    * </p>
    * <p>
    * If {@code null}, the JavaFX implicit exit flag is not modified.
    * </p>
    *
    * @return the flag current value.
    * @see Platform#setImplicitExit(boolean)
    */
   public Boolean isJavaFXThreadImplicitExit()
   {
      return javaFXThreadImplicitExit;
   }

   /**
    * Gets the list of robots being simulated.
    * 
    * @return the simulated robots.
    */
   public List<? extends Robot> getRobots()
   {
      return getPhysicsEngine().getRobots();
   }

   /**
    * Adds a robot to this simulation.
    * 
    * @param robotDefinition the definition to use for creating a new robot to add to this simulation.
    * @return the instantiated robot.
    */
   public Robot addRobot(RobotDefinition robotDefinition)
   {
      return simulationSession.addRobot(robotDefinition);
   }

   /**
    * Adds a robot to this simulation.
    * 
    * @param robot the robot to add.
    */
   public void addRobot(Robot robot)
   {
      simulationSession.addRobot(robot);
   }

   /**
    * Adds a robots to this simulation.
    * 
    * @param robots the robots to add.
    */
   public void addRobots(Collection<? extends Robot> robots)
   {
      simulationSession.addRobots(robots);
   }

   /**
    * Adds a terrain (static) object to the environment.
    * 
    * @param terrainObjectDefinition the definition used to create the new terrain object.
    */
   public void addTerrainObject(TerrainObjectDefinition terrainObjectDefinition)
   {
      simulationSession.addTerrainObject(terrainObjectDefinition);
   }

   /**
    * Adds terrain (static) objects to the environment.
    * 
    * @param terrainObjectDefinitions the definitions used to create the new terrain objects.
    */
   public void addTerrainObjects(Collection<? extends TerrainObjectDefinition> terrainObjectDefinitions)
   {
      simulationSession.addTerrainObjects(terrainObjectDefinitions);
   }

   /**
    * Gets the variable holding the current time (in seconds) in this simulation.
    * 
    * @return the current time (in seconds) variable.
    */
   public YoDouble getTime()
   {
      return simulationSession.getTime();
   }

   /**
    * Gets the inertial frame used for this simulation.
    * <p>
    * It is typically <b>not</b> equal to {@link ReferenceFrame#getWorldFrame()}.
    * </p>
    * 
    * @return the inertial frame.
    */
   public ReferenceFrame getInertialFrame()
   {
      return simulationSession.getInertialFrame();
   }

   /**
    * Reinitializes the physics engine.
    * <p>
    * Can be useful for resetting the simulation.
    * </p>
    * <p>
    * This is a non-blocking operation and schedules the change to be performed as soon as possible.
    * </p>
    */
   public void reinitializeSimulation()
   {
      simulationSession.reinitializeSession();
   }

   // ------------------------------------------------------------------------------- //
   // ----------------------------- YoVariables ------------------------------------- //
   // ------------------------------------------------------------------------------- //

   /**
    * Gets the simulation's root registry.
    * 
    * @return the root registry.
    */
   public YoRegistry getRootRegistry()
   {
      return simulationSession.getRootRegistry();
   }

   /**
    * Adds a registry to the simulation's root registry.
    * 
    * @param registry the registry to add.
    */
   public void addRegistry(YoRegistry registry)
   {
      getRootRegistry().addChild(registry);
   }

   /** {@inheritDoc} */
   @Override
   public List<YoVariable> getVariables()
   {
      return getRootRegistry().getVariables();
   }

   /** {@inheritDoc} */
   @Override
   public YoVariable findVariable(String namespaceEnding, String name)
   {
      return getRootRegistry().findVariable(namespaceEnding, name);
   }

   /** {@inheritDoc} */
   @Override
   public List<YoVariable> findVariables(String namespaceEnding, String name)
   {
      return getRootRegistry().findVariables(namespaceEnding, name);
   }

   /** {@inheritDoc} */
   @Override
   public List<YoVariable> findVariables(YoNamespace namespace)
   {
      return getRootRegistry().findVariables(namespace);
   }

   /** {@inheritDoc} */
   @Override
   public boolean hasUniqueVariable(String namespaceEnding, String name)
   {
      return getRootRegistry().hasUniqueVariable(namespaceEnding, name);
   }

   // ------------------------------------------------------------------------------- //
   // ------------------------ Simulation Properties -------------------------------- //
   // ------------------------------------------------------------------------------- //

   /** {@inheritDoc} */
   @Override
   public double getDT()
   {
      return simulationSessionControls.getDT();
   }

   /** {@inheritDoc} */
   @Override
   public void setDT(double dt)
   {
      simulationSessionControls.setDT(dt);
   }

   /** {@inheritDoc} */
   @Override
   public boolean isSimulationThreadRunning()
   {
      return simulationSessionControls.isSimulationThreadRunning();
   }

   /** {@inheritDoc} */
   @Override
   public boolean isRealTimeRateSimulation()
   {
      return simulationSessionControls.isRealTimeRateSimulation();
   }

   /** {@inheritDoc} */
   @Override
   public double getPlaybackRealTimeRate()
   {
      return simulationSession.getPlaybackRealTimeRate();
   }

   /** {@inheritDoc} */
   @Override
   public boolean isSimulating()
   {
      return simulationSessionControls.isSimulating();
   }

   /** {@inheritDoc} */
   @Override
   public boolean isPlaying()
   {
      return simulationSessionControls.isPlaying();
   }

   /** {@inheritDoc} */
   @Override
   public boolean isPaused()
   {
      return simulationSessionControls.isPaused();
   }

   // ------------------------------------------------------------------------------- //
   // ------------------------- Simulation Controls --------------------------------- //
   // ------------------------------------------------------------------------------- //

   /**
    * {@inheritDoc}
    * <p>
    * Starts the visualizer thread as well when called for the first and if the visualizer is enabled.
    * </p>
    * <p>
    * It is recommended to call {@link #waitUntilVisualizerFullyUp()} right after starting the
    * simulation thread.
    * </p>
    *
    * @see #setVisualizerEnabled(boolean)
    */
   @Override
   public boolean startSimulationThread()
   {
      boolean started = simulationSession.startSessionThread();

      if (visualizerEnabled && visualizerControls == null)
      {
         visualizerControls = SessionVisualizer.startSessionVisualizer(simulationSession, javaFXThreadImplicitExit);
         visualizerControls.addVisualizerShutdownListener(this::shutdownSession);
      }
      return started;
   }

   /** {@inheritDoc} */
   @Override
   public boolean stopSimulationThread()
   {
      return simulationSessionControls.stopSimulationThread();
   }

   /** {@inheritDoc} */
   @Override
   public void setRealTimeRateSimulation(boolean enableRealTimeRate)
   {
      simulationSessionControls.setRealTimeRateSimulation(enableRealTimeRate);
   }

   /** {@inheritDoc} */
   @Override
   public void setPlaybackRealTimeRate(double realTimeRate)
   {
      simulationSession.submitPlaybackRealTimeRate(realTimeRate);
   }

   /** {@inheritDoc} */
   @Override
   public void simulate(double duration)
   {
      simulationSessionControls.simulate(duration);
   }

   /** {@inheritDoc} */
   @Override
   public void simulate(int numberOfTicks)
   {
      simulationSessionControls.simulate(numberOfTicks);
   }

   /** {@inheritDoc} */
   @Override
   public boolean simulateNow(long numberOfTicks)
   {
      return simulationSessionControls.simulateNow(numberOfTicks);
   }

   /** {@inheritDoc} */
   @Override
   public void addSimulationThrowableListener(Consumer<Throwable> listener)
   {
      simulationSessionControls.addSimulationThrowableListener(listener);
   }

   /** {@inheritDoc} */
   @Override
   public void addExternalTerminalCondition(BooleanSupplier... externalTerminalConditions)
   {
      simulationSessionControls.addExternalTerminalCondition(externalTerminalConditions);
   }

   /** {@inheritDoc} */
   @Override
   public boolean removeExternalTerminalCondition(BooleanSupplier externalTerminalCondition)
   {
      return simulationSessionControls.removeExternalTerminalCondition(externalTerminalCondition);
   }

   /** {@inheritDoc} */
   @Override
   public void clearExternalTerminalConditions()
   {
      simulationSessionControls.clearExternalTerminalConditions();
   }

   /** {@inheritDoc} */
   @Override
   public void play()
   {
      simulationSessionControls.play();
   }

   /** {@inheritDoc} */
   @Override
   public void pause()
   {
      simulationSessionControls.pause();
   }

   // ------------------------------------------------------------------------------- //
   // -------------------------- Buffer Properties ---------------------------------- //
   // ------------------------------------------------------------------------------- //

   /** {@inheritDoc} */
   @Override
   public YoBufferPropertiesReadOnly getBufferProperties()
   {
      return simulationSessionControls.getBufferProperties();
   }

   /** {@inheritDoc} */
   @Override
   public int getBufferRecordTickPeriod()
   {
      return simulationSessionControls.getBufferRecordTickPeriod();
   }

   /** {@inheritDoc} */
   @Override
   public YoSharedBuffer getBuffer()
   {
      return simulationSessionControls.getBuffer();
   }

   // ------------------------------------------------------------------------------- //
   // --------------------------- Buffer Controls ----------------------------------- //
   // ------------------------------------------------------------------------------- //

   /** {@inheritDoc} */
   @Override
   public boolean initializeBufferRecordTickPeriod(int bufferRecordTickPeriod)
   {
      return simulationSessionControls.initializeBufferRecordTickPeriod(bufferRecordTickPeriod);
   }

   /** {@inheritDoc} */
   @Override
   public void setBufferRecordTickPeriod(int bufferRecordTickPeriod)
   {
      simulationSessionControls.setBufferRecordTickPeriod(bufferRecordTickPeriod);
   }

   /** {@inheritDoc} */
   @Override
   public void gotoBufferIndex(int bufferIndexRequest)
   {
      simulationSessionControls.gotoBufferIndex(bufferIndexRequest);
   }

   /** {@inheritDoc} */
   @Override
   public void setBufferInPoint(int index)
   {
      simulationSessionControls.setBufferInPoint(index);
   }

   /** {@inheritDoc} */
   @Override
   public void setBufferOutPoint(int index)
   {
      simulationSessionControls.setBufferOutPoint(index);
   }

   /** {@inheritDoc} */
   @Override
   public void stepBufferIndexBackward(int stepSize)
   {
      stepBufferIndexBackward(stepSize);
   }

   /** {@inheritDoc} */
   @Override
   public void stepBufferIndexForward(int stepSize)
   {
      simulationSessionControls.stepBufferIndexForward(stepSize);
   }

   /** {@inheritDoc} */
   @Override
   public void cropBuffer(CropBufferRequest request)
   {
      simulationSessionControls.cropBuffer(request);
   }

   /** {@inheritDoc} */
   @Override
   public boolean initializeBufferSize(int bufferSize)
   {
      return simulationSessionControls.initializeBufferSize(bufferSize);
   }

   /** {@inheritDoc} */
   @Override
   public void changeBufferSize(int bufferSize)
   {
      simulationSessionControls.changeBufferSize(bufferSize);
   }

   /** {@inheritDoc} */
   @Override
   public void applyBufferProcessor(YoBufferProcessor processor)
   {
      simulationSessionControls.applyBufferProcessor(processor);
   }

   // ------------------------------------------------------------------------------- //
   // ---------------------------- Misc Controls ------------------------------------ //
   // ------------------------------------------------------------------------------- //

   /** {@inheritDoc} */
   @Override
   public String getSimulationName()
   {
      return simulationSessionControls.getSimulationName();
   }

   /** {@inheritDoc} */
   @Override
   public void exportData(SessionDataExportRequest request)
   {
      simulationSessionControls.exportData(request);
   }

   /** {@inheritDoc} */
   @Override
   public void addBeforePhysicsCallback(TimeConsumer beforePhysicsCallback)
   {
      simulationSessionControls.addBeforePhysicsCallback(beforePhysicsCallback);
   }

   /** {@inheritDoc} */
   @Override
   public boolean removeBeforePhysicsCallback(TimeConsumer beforePhysicsCallback)
   {
      return simulationSessionControls.removeBeforePhysicsCallback(beforePhysicsCallback);
   }

   /** {@inheritDoc} */
   @Override
   public void addAfterPhysicsCallback(TimeConsumer afterPhysicsCallback)
   {
      simulationSessionControls.addAfterPhysicsCallback(afterPhysicsCallback);
   }

   /** {@inheritDoc} */
   @Override
   public boolean removeAfterPhysicsCallback(TimeConsumer afterPhysicsCallback)
   {
      return simulationSessionControls.removeAfterPhysicsCallback(afterPhysicsCallback);
   }

   // ------------------------------------------------------------------------------- //
   // ------------------------- Visualizer Controls --------------------------------- //
   // ------------------------------------------------------------------------------- //

   /** {@inheritDoc} */
   @Override
   public void setCameraOrientation(double latitude, double longitude)
   {
      if (visualizerControls != null)
         visualizerControls.setCameraOrientation(latitude, longitude);
   }

   /** {@inheritDoc} */
   @Override
   public void setCameraPosition(double x, double y, double z)
   {
      if (visualizerControls != null)
         visualizerControls.setCameraPosition(x, y, z);
   }

   /** {@inheritDoc} */
   @Override
   public void setCameraFocusPosition(double x, double y, double z)
   {
      if (visualizerControls != null)
         visualizerControls.setCameraFocusPosition(x, y, z);
   }

   /** {@inheritDoc} */
   @Override
   public void setCameraZoom(double distanceFromFocus)
   {
      if (visualizerControls != null)
         visualizerControls.setCameraZoom(distanceFromFocus);
   }

   /** {@inheritDoc} */
   @Override
   public void requestCameraRigidBodyTracking(String robotName, String rigidBodyName)
   {
      if (visualizerControls != null)
         visualizerControls.requestCameraRigidBodyTracking(robotName, rigidBodyName);
   }

   /** {@inheritDoc} */
   @Override
   public void addStaticVisual(VisualDefinition visualDefinition)
   {
      if (visualizerControls != null)
         visualizerControls.addStaticVisual(visualDefinition);
   }

   /** {@inheritDoc} */
   @Override
   public void removeStaticVisual(VisualDefinition visualDefinition)
   {
      if (visualizerControls != null)
         visualizerControls.removeStaticVisual(visualDefinition);
   }

   /** {@inheritDoc} */
   @Override
   public void addYoGraphic(YoGraphicDefinition yoGraphicDefinition)
   {
      if (visualizerControls != null)
         visualizerControls.addYoGraphic(yoGraphicDefinition);
      else // It is possible that the simulation hasn't been started yet, add the graphic to the session instead.
         simulationSession.addYoGraphicDefinition(yoGraphicDefinition);
   }

   /** {@inheritDoc} */
   @Override
   public void addYoEntry(String groupName, Collection<String> variableNames)
   {
      if (visualizerControls != null)
         visualizerControls.addYoEntry(groupName, variableNames);
   }

   /** {@inheritDoc} */
   @Override
   public void exportVideo(SceneVideoRecordingRequest request)
   {
      if (visualizerControls != null)
         visualizerControls.exportVideo(request);
   }

   /** {@inheritDoc} */
   @Override
   public void disableGUIControls()
   {
      if (visualizerControls != null)
         visualizerControls.disableGUIControls();
   }

   /** {@inheritDoc} */
   @Override
   public void enableGUIControls()
   {
      if (visualizerControls != null)
         visualizerControls.enableGUIControls();
   }

   /** {@inheritDoc} */
   @Override
   public Window getPrimaryGUIWindow()
   {
      return visualizerControls == null ? null : visualizerControls.getPrimaryGUIWindow();
   }

   /** {@inheritDoc} */
   @Override
   public void addCustomGUIControl(Node control)
   {
      if (visualizerControls != null)
         visualizerControls.addCustomGUIControl(control);
   }

   /** {@inheritDoc} */
   @Override
   public boolean removeCustomGUIControl(Node control)
   {
      if (visualizerControls != null)
         return visualizerControls.removeCustomGUIControl(control);
      else
         return false;
   }

   /** {@inheritDoc} */
   @Override
   public void loadCustomGUIPane(String name, URL fxmlResource)
   {
      if (visualizerControls != null)
         visualizerControls.loadCustomGUIPane(name, fxmlResource);
   }

   /** {@inheritDoc} */
   @Override
   public void addCustomGUIPane(String name, Pane pane)
   {
      if (visualizerControls != null)
         visualizerControls.addCustomGUIPane(name, pane);
   }

   /** {@inheritDoc} */
   @Override
   public boolean removeCustomGUIPane(String name)
   {
      if (visualizerControls != null)
         return visualizerControls.removeCustomGUIPane(name);
      else
         return false;
   }

   /** {@inheritDoc} */
   @Override
   public void waitUntilVisualizerFullyUp()
   {
      if (visualizerControls != null)
         visualizerControls.waitUntilVisualizerFullyUp();
   }

   /** {@inheritDoc} */
   @Override
   public void waitUntilVisualizerDown()
   {
      if (visualizerControls != null)
         visualizerControls.waitUntilVisualizerDown();
   }

   // TODO Missing setupGraph, setupGraphGroup

   // ------------------------------------------------------------------------------- //
   // -------------------------- Shutdown Controls ---------------------------------- //
   // ------------------------------------------------------------------------------- //

   /** {@inheritDoc} */
   @Override
   public boolean isSessionShutdown()
   {
      return simulationSessionControls.isSessionShutdown();
   }

   private boolean hasBeenDestroyed;

   /** {@inheritDoc} */
   @Override
   public void shutdownSession()
   {
      if (hasBeenDestroyed)
         return;

      LogTools.info("Destroying simulation");
      hasBeenDestroyed = true;

      if (visualizerControls != null)
         visualizerControls.shutdownSession();

      // The visualizer will shutdown the session, just being extra cautious here.
      simulationSessionControls.shutdownSession();
   }

   /** {@inheritDoc} */
   @Override
   public void requestVisualizerShutdown()
   {
      if (visualizerControls != null)
         visualizerControls.requestVisualizerShutdown();
   }

   @Override
   public void addSessionShutdownListener(Runnable listener)
   {
      simulationSessionControls.addSessionShutdownListener(listener);
   }

   /** {@inheritDoc} */
   @Override
   public void addVisualizerShutdownListener(Runnable listener)
   {
      if (visualizerControls != null)
         visualizerControls.addVisualizerShutdownListener(listener);
   }
}
