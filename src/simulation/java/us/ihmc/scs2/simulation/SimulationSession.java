package us.ihmc.scs2.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import org.apache.commons.lang3.mutable.MutableBoolean;

import us.ihmc.commons.Conversions;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.referenceFrame.tools.ReferenceFrameTools;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.terrain.TerrainObjectDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicDefinition;
import us.ihmc.scs2.session.Session;
import us.ihmc.scs2.session.SessionMode;
import us.ihmc.scs2.sharedMemory.interfaces.LinkedYoVariableFactory;
import us.ihmc.scs2.simulation.physicsEngine.PhysicsEngine;
import us.ihmc.scs2.simulation.physicsEngine.PhysicsEngineFactory;
import us.ihmc.scs2.simulation.robot.Robot;
import us.ihmc.yoVariables.euclid.referenceFrame.YoFrameVector3D;
import us.ihmc.yoVariables.exceptions.IllegalOperationException;
import us.ihmc.yoVariables.variable.YoDouble;

public class SimulationSession extends Session
{
   public static final ReferenceFrame DEFAULT_INERTIAL_FRAME = ReferenceFrameTools.constructARootFrame("worldFrame");

   private final ReferenceFrame inertialFrame;
   private final PhysicsEngine physicsEngine;
   private final YoDouble simulationTime = new YoDouble("simulationTime", rootRegistry);
   private final YoFrameVector3D gravity = new YoFrameVector3D("gravity", ReferenceFrame.getWorldFrame(), rootRegistry);
   private final String simulationName;
   private final List<YoGraphicDefinition> yoGraphicDefinitions = new ArrayList<>();

   private SimulationSessionControlsImpl controls = null;

   private boolean hasSessionStarted = false;

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
      if (!inertialFrame.isRootFrame())
         throw new IllegalArgumentException("The given inertialFrame is not a root frame: " + inertialFrame);

      this.inertialFrame = inertialFrame;
      this.simulationName = simulationName;

      physicsEngine = physicsEngineFactory.build(inertialFrame, rootRegistry);

      setSessionTickToTimeIncrement(Conversions.secondsToNanoseconds(0.0001));
      setSessionMode(SessionMode.PAUSE);
      gravity.set(0.0, 0.0, -9.81);
   }

   public SimulationSessionControls getSimulationSessionControls()
   {
      if (controls == null)
         controls = new SimulationSessionControlsImpl();
      return controls;
   }

   @Override
   public void initializeSession()
   {
      hasSessionStarted = true;
      super.initializeSession();
      physicsEngine.initialize(gravity);
   }

   @Override
   protected void doSpecificRunTick()
   {
      double dt = Conversions.nanosecondsToSeconds(getSessionTickToTimeIncrement());
      physicsEngine.simulate(simulationTime.getValue(), dt, gravity);
      simulationTime.add(dt);
   }

   public Robot addRobot(RobotDefinition robotDefinition)
   {
      checkSessionHasNotStarted();

      return physicsEngine.addRobot(robotDefinition);
   }

   public void addTerrainObject(TerrainObjectDefinition terrainObjectDefinition)
   {
      checkSessionHasNotStarted();

      physicsEngine.addTerrainObject(terrainObjectDefinition);
   }

   public void addYoGraphicDefinition(YoGraphicDefinition yoGraphicDefinition)
   {
      checkSessionHasNotStarted();

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

   public ReferenceFrame getInertialFrame()
   {
      return inertialFrame;
   }

   @Override
   public LinkedYoVariableFactory getLinkedYoVariableFactory()
   {
      return sharedBuffer;
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

   private void checkSessionHasNotStarted()
   {
      if (hasSessionStarted)
         throw new IllegalOperationException("Illegal operation after session has started.");
   }

   private class SimulationSessionControlsImpl implements SimulationSessionControls
   {
      @Override
      public void pause()
      {
         setSessionMode(SessionMode.PAUSE);
      }

      @Override
      public void simulate()
      {
         setSessionMode(SessionMode.RUNNING);
      }

      @Override
      public void simulate(double duration)
      {
         if (getActiveMode() == SessionMode.RUNNING)
            setSessionMode(SessionMode.PAUSE);

         double startTime = simulationTime.getValue();
         BooleanSupplier terminalCondition = () -> simulationTime.getValue() - startTime >= duration;
         setSessionMode(SessionMode.RUNNING, terminalCondition, SessionMode.PAUSE);
      }

      @Override
      public void simulate(int numberOfTicks)
      {
         if (getActiveMode() == SessionMode.RUNNING)
            setSessionMode(SessionMode.PAUSE);

         BooleanSupplier terminalCondition = new BooleanSupplier()
         {
            private int tickCounter = 0;

            @Override
            public boolean getAsBoolean()
            {
               tickCounter++;
               return tickCounter >= numberOfTicks;
            }
         };
         setSessionMode(SessionMode.RUNNING, terminalCondition, SessionMode.PAUSE);
      }

      @Override
      public boolean simulateAndWait(double duration)
      {
         long numberOfTicks = Conversions.secondsToNanoseconds(duration) / getSessionTickToTimeIncrement();
         return simulateAndWait(numberOfTicks);
      }

      @Override
      public boolean simulateAndWait(long numberOfTicks)
      {
         if (getActiveMode() == SessionMode.RUNNING)
            setSessionMode(SessionMode.PAUSE);

         MutableBoolean success = new MutableBoolean(false);
         CountDownLatch doneLatch = new CountDownLatch(1);

         Consumer<SessionMode> modeListener = mode ->
         {
            if (mode != SessionMode.RUNNING)
               doneLatch.countDown();
         };

         Runnable shutdownListener = doneLatch::countDown;

         addSessionModeChangedListener(modeListener);
         addShutdownListener(shutdownListener);

         BooleanSupplier terminalCondition = new BooleanSupplier()
         {
            private int tickCounter = 0;

            @Override
            public boolean getAsBoolean()
            {
               tickCounter++;
               boolean done = tickCounter >= numberOfTicks;

               if (done)
               {
                  success.setTrue();
                  doneLatch.countDown();
               }
               return done;
            }
         };

         setSessionMode(SessionMode.RUNNING, terminalCondition, SessionMode.PAUSE);

         try
         {
            doneLatch.await();
         }
         catch (InterruptedException e)
         {
            e.printStackTrace();
         }

         removeSessionModeChangedListener(modeListener);
         removeShutdownListener(shutdownListener);

         return success.isTrue();
      }

      @Override
      public void addSimulationThrowableListener(Consumer<Throwable> listener)
      {
         addRunThrowableListener(listener);
      }

      // Buffer controls
      @Override
      public void setBufferInPointToCurrent()
      {
         submitBufferInPointIndexRequest(sharedBuffer.getProperties().getCurrentIndex());
      }

      @Override
      public void setBufferOutPointToCurrent()
      {
         submitBufferOutPointIndexRequest(sharedBuffer.getProperties().getCurrentIndex());
      }
   }
}
