package us.ihmc.scs2.simulation;

import java.util.ArrayList;
import java.util.List;

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
import us.ihmc.scs2.simulation.robot.Robot;
import us.ihmc.yoVariables.euclid.referenceFrame.YoFrameVector3D;
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

   public SimulationSession()
   {
      this(retrieveCallerName());
   }

   public SimulationSession(String simulationName)
   {
      this(DEFAULT_INERTIAL_FRAME, simulationName);
   }

   public SimulationSession(ReferenceFrame inertialFrame)
   {
      this(inertialFrame, retrieveCallerName());
   }

   public SimulationSession(ReferenceFrame inertialFrame, String simulationName)
   {
      if (!inertialFrame.isRootFrame())
         throw new IllegalArgumentException("The given inertialFrame is not a root frame: " + inertialFrame);

      this.inertialFrame = inertialFrame;
      this.simulationName = simulationName;

      physicsEngine = new PhysicsEngine(inertialFrame, rootRegistry);

      submitBufferSizeRequest(200000);
      setSessionTickToTimeIncrement(Conversions.secondsToNanoseconds(0.0001));
      setSessionMode(SessionMode.PAUSE);
      gravity.set(0.0, 0.0, -9.81);
   }

   @Override
   public void initializeSession()
   {
      super.initializeSession();
      physicsEngine.initialize(gravity);
   }

   @Override
   protected void doSpecificRunTick()
   {
      double dt = Conversions.nanosecondsToSeconds(getSessionTickToTimeIncrement());
      physicsEngine.simulate(dt, gravity);
      simulationTime.add(dt);
   }

   public void addRobot(RobotDefinition robotDefinition)
   {
      physicsEngine.addRobot(robotDefinition);
   }

   public void addRobot(Robot robot)
   {
      physicsEngine.addRobot(robot);
   }

   public void addTerrainObject(TerrainObjectDefinition terrainObjectDefinition)
   {
      physicsEngine.addTerrainObject(terrainObjectDefinition);
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
}
