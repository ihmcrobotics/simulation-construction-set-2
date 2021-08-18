package us.ihmc.scs2.simulation.physicsEngine;

import java.util.List;

import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.terrain.TerrainObjectDefinition;
import us.ihmc.scs2.simulation.robot.Robot;
import us.ihmc.yoVariables.registry.YoRegistry;

public interface PhysicsEngine
{
   boolean initialize(Vector3DReadOnly gravity);

   void simulate(double currentTime, double dt, Vector3DReadOnly gravity);

   Robot addRobot(RobotDefinition robotDefinition);

   void addTerrainObject(TerrainObjectDefinition terrainObjectDefinition);

   ReferenceFrame getInertialFrame();

   List<RobotDefinition> getRobotDefinitions();

   List<TerrainObjectDefinition> getTerrainObjectDefinitions();

   YoRegistry getPhysicsEngineRegistry();
}
