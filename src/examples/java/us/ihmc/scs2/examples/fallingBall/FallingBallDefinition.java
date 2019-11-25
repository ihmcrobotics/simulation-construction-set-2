package us.ihmc.scs2.examples.fallingBall;

import us.ihmc.euclid.geometry.Pose3D;
import us.ihmc.euclid.tuple3D.Vector3D;
import us.ihmc.scs2.definition.collision.CollisionShapeDefinition;
import us.ihmc.scs2.definition.geometry.GeometryDefinition;
import us.ihmc.scs2.definition.geometry.SphereGeometryDefinition;
import us.ihmc.scs2.definition.robot.RigidBodyDefinition;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.robot.SixDoFJointDefinition;
import us.ihmc.scs2.definition.robot.interfaces.RobotInitialStateProvider;
import us.ihmc.scs2.definition.state.SixDoFJointState;
import us.ihmc.scs2.definition.state.interfaces.JointStateReadOnly;
import us.ihmc.scs2.definition.visual.ColorDefinitions;
import us.ihmc.scs2.definition.visual.VisualDefinition;
import us.ihmc.scs2.definition.visual.VisualDefinition.MaterialDefinition;

public class FallingBallDefinition extends RobotDefinition implements RobotInitialStateProvider
{
   private static final String ROOT_JOINT_NAME = "rootJoint";

   public FallingBallDefinition()
   {
      super("FallingBall");

      RigidBodyDefinition elevator = new RigidBodyDefinition("elevator");
      SixDoFJointDefinition floatingJoint = new SixDoFJointDefinition(ROOT_JOINT_NAME);
      RigidBodyDefinition ball = createBallRigidBody();
      ball.setMass(10.0);
      ball.getMomentOfInertia().setToDiagonal(0.1, 0.1, 0.1);

      setRootBodyDefinition(elevator);
      elevator.addChildJoint(floatingJoint);
      floatingJoint.setSuccessor(ball);
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

   @Override
   public JointStateReadOnly getInitialJointState(String jointName)
   {
      if (jointName.equals(ROOT_JOINT_NAME))
      {
         SixDoFJointState jointState = new SixDoFJointState();
         jointState.setConfiguration(new Pose3D(0.0, 0.0, 1.0, 0.0, 0.0, 0.0));
         jointState.setVelocity(new Vector3D(), new Vector3D(-1.0, 0.0, 0.0));
         return jointState;
      }
      else
      {
         return null;
      }
   }
}
