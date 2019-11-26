package us.ihmc.scs2.examples.ball;

import us.ihmc.scs2.definition.collision.CollisionShapeDefinition;
import us.ihmc.scs2.definition.geometry.GeometryDefinition;
import us.ihmc.scs2.definition.geometry.SphereGeometryDefinition;
import us.ihmc.scs2.definition.robot.RigidBodyDefinition;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.robot.SixDoFJointDefinition;
import us.ihmc.scs2.definition.visual.ColorDefinitions;
import us.ihmc.scs2.definition.visual.VisualDefinition;
import us.ihmc.scs2.definition.visual.VisualDefinition.MaterialDefinition;

public class BallRobotDefinition extends RobotDefinition
{
   public BallRobotDefinition()
   {
      super("Ball");

      RigidBodyDefinition elevator = new RigidBodyDefinition("elevator");
      SixDoFJointDefinition floatingJoint = new SixDoFJointDefinition(getRootJointName());
      RigidBodyDefinition ball = createBallRigidBody();
      ball.setMass(10.0);
      ball.getMomentOfInertia().setToDiagonal(0.1, 0.1, 0.1);

      setRootBodyDefinition(elevator);
      elevator.addChildJoint(floatingJoint);
      floatingJoint.setSuccessor(ball);
   }

   public String getRootJointName()
   {
      return "rootJoint";
   }

   private final RigidBodyDefinition createBallRigidBody()
   {
      RigidBodyDefinition ball = new RigidBodyDefinition("Ball");
      GeometryDefinition geometryDefinition = new SphereGeometryDefinition(0.3);
      MaterialDefinition materialDefinition = new MaterialDefinition(ColorDefinitions.Red());
      ball.addVisualDefinition(new VisualDefinition(geometryDefinition, materialDefinition));
      ball.addCollisionShapeDefinition(new CollisionShapeDefinition(geometryDefinition));
      return ball;
   }
}
