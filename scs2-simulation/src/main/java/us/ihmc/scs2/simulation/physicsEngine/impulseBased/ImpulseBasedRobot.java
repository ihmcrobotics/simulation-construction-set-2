package us.ihmc.scs2.simulation.physicsEngine.impulseBased;

import org.ejml.data.DMatrixRMaj;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;
import us.ihmc.mecano.algorithms.ForwardDynamicsCalculator;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyReadOnly;
import us.ihmc.mecano.spatial.interfaces.SpatialImpulseReadOnly;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.simulation.collision.Collidable;
import us.ihmc.scs2.simulation.collision.CollidableHolder;
import us.ihmc.scs2.simulation.robot.Robot;
import us.ihmc.scs2.simulation.robot.RobotExtension;
import us.ihmc.scs2.simulation.robot.multiBodySystem.interfaces.SimJointBasics;
import us.ihmc.yoVariables.registry.YoRegistry;

import java.util.List;

public class ImpulseBasedRobot extends RobotExtension implements CollidableHolder
{
   private final ImpulseBasedRobotPhysics robotPhysics;

   public ImpulseBasedRobot(Robot robot, YoRegistry physicsRegistry)
   {
      super(robot, physicsRegistry);
      robotPhysics = new ImpulseBasedRobotPhysics(this, getRobotPhysicsRegistry());
   }

   public ImpulseBasedRobot(RobotDefinition robotDefinition, ReferenceFrame inertialFrame, YoRegistry physicsRegistry)
   {
      super(robotDefinition, inertialFrame, physicsRegistry);
      robotPhysics = new ImpulseBasedRobotPhysics(this, getRobotPhysicsRegistry());
   }

   public void enableJointWrenchCalculator()
   {
      robotPhysics.enableJointWrenchCalculator();
   }

   public void resetCalculators()
   {
      robotPhysics.resetCalculators();
   }

   public void computeJointLowLevelControl()
   {
      robotPhysics.computeJointLowLevelControl();
   }

   public void doForwardDynamics(Vector3DReadOnly gravity)
   {
      robotPhysics.doForwardDynamics(gravity);
   }

   public void updateCollidableBoundingBoxes()
   {
      robotPhysics.updateCollidableBoundingBoxes();
   }

   public ForwardDynamicsCalculator getForwardDynamicsCalculator()
   {
      return robotPhysics.getForwardDynamicsCalculator();
   }

   public RobotJointLimitImpulseBasedCalculator getJointLimitConstraintCalculator()
   {
      return robotPhysics.getJointLimitConstraintCalculator();
   }

   public SingleContactImpulseCalculator getOrCreateEnvironmentContactConstraintCalculator()
   {
      return robotPhysics.getOrCreateEnvironmentContactConstraintCalculator();
   }

   public SingleContactImpulseCalculator getOrCreateInterRobotContactConstraintCalculator(ImpulseBasedRobot otherRobot)
   {
      return robotPhysics.getOrCreateInterRobotContactConstraintCalculator(otherRobot);
   }

   public void addRigidBodyExternalImpulse(RigidBodyReadOnly target, SpatialImpulseReadOnly wrenchToAdd)
   {
      robotPhysics.addRigidBodyExternalImpulse(target, wrenchToAdd);
   }

   public void addJointVelocityChange(DMatrixRMaj velocityChange)
   {
      robotPhysics.addJointVelocityChange(velocityChange);
   }

   public void writeJointAccelerations()
   {
      robotPhysics.writeJointAccelerations();
   }

   public void writeJointDeltaVelocities()
   {
      robotPhysics.writeJointDeltaVelocities();
   }

   public void computeJointWrenches(double dt)
   {
      robotPhysics.computeJointWrenches(dt);
   }

   public void resetDT()
   {
      robotPhysics.getPhysicsOutput().setDT(0);
   }

   public void integrateState(double dt)
   {
      robotPhysics.integrateState(dt);
   }

   public void updateSensors()
   {
      for (SimJointBasics joint : getJointsToConsider())
      {
         joint.getAuxiliaryData().update(robotPhysics.getPhysicsOutput());
      }
   }

   @Override
   public List<Collidable> getCollidables()
   {
      return robotPhysics.getCollidables();
   }
}
