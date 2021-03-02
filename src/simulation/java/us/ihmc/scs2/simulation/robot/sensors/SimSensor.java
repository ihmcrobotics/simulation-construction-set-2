package us.ihmc.scs2.simulation.robot.sensors;

import us.ihmc.euclid.transform.RigidBodyTransform;
import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.mecano.frames.MovingReferenceFrame;
import us.ihmc.mecano.spatial.Twist;
import us.ihmc.scs2.definition.robot.SensorDefinition;
import us.ihmc.scs2.simulation.robot.RobotPhysicsOutput;
import us.ihmc.scs2.simulation.robot.multiBodySystem.interfaces.SimJointBasics;
import us.ihmc.yoVariables.euclid.referenceFrame.YoFramePoseUsingYawPitchRoll;

public abstract class SimSensor
{
   private final String name;
   private final SimJointBasics parentJoint;
   private final MovingReferenceFrame frame;

   private final YoFramePoseUsingYawPitchRoll offset;

   public SimSensor(SensorDefinition definition, SimJointBasics parentJoint)
   {
      this(definition.getName(), parentJoint, definition.getTransformToJoint());
   }

   public SimSensor(String name, SimJointBasics parentJoint, RigidBodyTransformReadOnly transformToParent)
   {
      this.name = name;
      this.parentJoint = parentJoint;

      offset = new YoFramePoseUsingYawPitchRoll(name + "Offset", parentJoint.getFrameAfterJoint(), parentJoint.getRegistry());
      offset.set(transformToParent);

      frame = new MovingReferenceFrame(name + "Frame", parentJoint.getFrameAfterJoint())
      {
         @Override
         protected void updateTransformToParent(RigidBodyTransform transformToParent)
         {
            offset.get(transformToParent);
         }

         @Override
         protected void updateTwistRelativeToParent(Twist twistRelativeToParentToPack)
         {
         }
      };
   }

   public void update(RobotPhysicsOutput robotPhysicsOutput)
   {
      frame.update();
   }

   public String getName()
   {
      return name;
   }

   public SimJointBasics getParentJoint()
   {
      return parentJoint;
   }

   public MovingReferenceFrame getFrame()
   {
      return frame;
   }

   public YoFramePoseUsingYawPitchRoll getOffset()
   {
      return offset;
   }

   @Override
   public String toString()
   {
      return getClass().getSimpleName() + " - " + getName();
   }
}
