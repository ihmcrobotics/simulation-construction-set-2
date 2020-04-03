package us.ihmc.scs2.examples.chaoticBall;

import us.ihmc.euclid.geometry.Pose3D;
import us.ihmc.euclid.transform.RigidBodyTransform;
import us.ihmc.scs2.definition.geometry.SphereGeometryDefinition;
import us.ihmc.scs2.definition.robot.RigidBodyDefinition;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.robot.SixDoFJointDefinition;
import us.ihmc.scs2.definition.robot.interfaces.RobotInitialStateProvider;
import us.ihmc.scs2.definition.state.OneDoFJointState;
import us.ihmc.scs2.definition.state.SixDoFJointState;
import us.ihmc.scs2.definition.state.interfaces.JointStateReadOnly;
import us.ihmc.scs2.definition.visual.ColorDefinition;
import us.ihmc.scs2.definition.visual.VisualDefinition;

import java.util.Random;

public class ChaoticBallDefinition extends RobotDefinition implements RobotInitialStateProvider
{
   public ChaoticBallDefinition()
   {
      super("chaoticBall");

      RigidBodyDefinition block = new RigidBodyDefinition("mass");
      block.setMass(1.0);

      SphereGeometryDefinition geometryDefinition = new SphereGeometryDefinition(0.1);
      RigidBodyTransform visualPose = new RigidBodyTransform();
      visualPose.getTranslation().setZ(0.0);
      VisualDefinition.MaterialDefinition materialDefinition = new VisualDefinition.MaterialDefinition(new ColorDefinition(new Random().nextInt()));
      block.addVisualDefinition(new VisualDefinition(visualPose, geometryDefinition, materialDefinition));

      SixDoFJointDefinition sixDoF = new SixDoFJointDefinition("sixDof");
      sixDoF.setSuccessor(block);

      RigidBodyDefinition elevator = new RigidBodyDefinition("elevator");
      elevator.getChildrenJoints().add(sixDoF);

      setRootBodyDefinition(elevator);
   }

   @Override
   public JointStateReadOnly getInitialJointState(String jointName)
   {
      SixDoFJointState jointState = new SixDoFJointState();
      Pose3D pose = new Pose3D();
      pose.setPosition(1.0, 1.0, 1.0);
      jointState.setConfiguration(pose);
      return jointState;
   }
}