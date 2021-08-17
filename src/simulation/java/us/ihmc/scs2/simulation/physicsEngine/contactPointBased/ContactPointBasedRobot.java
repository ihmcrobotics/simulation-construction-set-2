package us.ihmc.scs2.simulation.physicsEngine.contactPointBased;

import java.util.List;

import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;
import us.ihmc.mecano.algorithms.ForwardDynamicsCalculator;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.simulation.collision.Collidable;
import us.ihmc.scs2.simulation.collision.CollidableHolder;
import us.ihmc.scs2.simulation.robot.Robot;
import us.ihmc.scs2.simulation.robot.multiBodySystem.interfaces.SimJointBasics;

public class ContactPointBasedRobot extends Robot implements CollidableHolder
{
   private ContactPointBasedRobotPhysics robotPhysics;

   public ContactPointBasedRobot(Robot other)
   {
      super(other);
   }

   public ContactPointBasedRobot(RobotDefinition robotDefinition, ReferenceFrame inertialFrame)
   {
      super(robotDefinition, inertialFrame);
   }

   public void setupPhysicsAndControllers()
   {
      if (robotPhysics != null)
         return;

      setupControllers();
      robotPhysics = new ContactPointBasedRobotPhysics(this);
   }

   public void resetCalculators()
   {
      robotPhysics.resetCalculators();
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

   public void writeJointAccelerations()
   {
      robotPhysics.writeJointAccelerations();
   }

   public void integrateState(double dt)
   {
      robotPhysics.integrateState(dt);
   }

   public void updateSensors()
   {
      for (SimJointBasics joint : rootBody.childrenSubtreeIterable())
      {
         joint.getAuxialiryData().update(robotPhysics.getPhysicsOutput());
      }
   }

   @Override
   public List<Collidable> getCollidables()
   {
      return robotPhysics.getCollidables();
   }
}
