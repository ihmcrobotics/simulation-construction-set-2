package us.ihmc.scs2.examples.ellipsoid;

import us.ihmc.scs2.definition.collision.CollisionShapeDefinition;
import us.ihmc.scs2.definition.geometry.EllipsoidGeometryDefinition;
import us.ihmc.scs2.definition.geometry.GeometryDefinition;
import us.ihmc.scs2.definition.robot.RigidBodyDefinition;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.robot.SixDoFJointDefinition;
import us.ihmc.scs2.definition.visual.ColorDefinitions;
import us.ihmc.scs2.definition.visual.VisualDefinition;
import us.ihmc.scs2.definition.visual.VisualDefinition.MaterialDefinition;

public class EllipsoidRobotSimulation extends RobotDefinition
{
   public EllipsoidRobotSimulation()
   {
      super("Ellipsoid");

      RigidBodyDefinition elevator = new RigidBodyDefinition("elevator");
      SixDoFJointDefinition floatingJoint = new SixDoFJointDefinition(getRootJointName());
      RigidBodyDefinition cylinder = createEllipsoidRigidBody();
      cylinder.setMass(10.0);
      cylinder.getMomentOfInertia().setToDiagonal(0.1, 0.1, 0.1);

      setRootBodyDefinition(elevator);
      elevator.addChildJoint(floatingJoint);
      floatingJoint.setSuccessor(cylinder);
   }

   public String getRootJointName()
   {
      return "rootJoint";
   }

   private final RigidBodyDefinition createEllipsoidRigidBody()
   {
      RigidBodyDefinition ball = new RigidBodyDefinition("Ellipsoid");
      GeometryDefinition geometryDefinition = new EllipsoidGeometryDefinition(0.3, 0.15, 0.6);
      MaterialDefinition materialDefinition = new MaterialDefinition(ColorDefinitions.Red());
      ball.addVisualDefinition(new VisualDefinition(geometryDefinition, materialDefinition));
      ball.addCollisionShapeDefinition(new CollisionShapeDefinition(geometryDefinition));
      return ball;
   }
}
