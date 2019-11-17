package us.ihmc.scs2.examples.springDamperEngine;

import java.util.Random;

import us.ihmc.euclid.Axis;
import us.ihmc.euclid.transform.RigidBodyTransform;
import us.ihmc.scs2.definition.geometry.BoxGeometryDefinition;
import us.ihmc.scs2.definition.robot.PrismaticJointDefinition;
import us.ihmc.scs2.definition.robot.RigidBodyDefinition;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.robot.interfaces.RobotInitialStateProvider;
import us.ihmc.scs2.definition.state.OneDoFJointState;
import us.ihmc.scs2.definition.state.interfaces.JointStateReadOnly;
import us.ihmc.scs2.definition.visual.ColorDefinition;
import us.ihmc.scs2.definition.visual.VisualDefinition;
import us.ihmc.scs2.definition.visual.VisualDefinition.MaterialDefinition;

public class SpringDamperDefinition extends RobotDefinition implements RobotInitialStateProvider
{
   public SpringDamperDefinition()
   {
      super("springDamper");

      RigidBodyDefinition block = new RigidBodyDefinition("mass");
      block.setMass(1.0);

      BoxGeometryDefinition geometryDefinition = new BoxGeometryDefinition(0.5, 0.5, 0.5);
      RigidBodyTransform visualPose = new RigidBodyTransform();
      visualPose.setTranslationZ(0.0);
      MaterialDefinition materialDefinition = new MaterialDefinition(new ColorDefinition(new Random().nextInt()));
      block.addVisualDefinition(new VisualDefinition(visualPose, geometryDefinition, materialDefinition));

      PrismaticJointDefinition slider = new PrismaticJointDefinition("slider");
      slider.setSuccessor(block);
      slider.getAxis().set(Axis.Z);

      RigidBodyDefinition elevator = new RigidBodyDefinition("elevator");
      elevator.getChildrenJoints().add(slider);

      setRootBodyDefinition(elevator);
   }

   @Override
   public JointStateReadOnly getInitialJointState(String jointName)
   {
      OneDoFJointState prismaticJointState = new OneDoFJointState();
      prismaticJointState.setConfiguration(1.5);
      return prismaticJointState;
   }
}
