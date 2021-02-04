package us.ihmc.scs2.simulation.robot;

import us.ihmc.euclid.transform.RigidBodyTransform;
import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.mecano.frames.MovingReferenceFrame;
import us.ihmc.mecano.spatial.Twist;
import us.ihmc.scs2.definition.robot.SensorDefinition;
import us.ihmc.yoVariables.euclid.referenceFrame.YoFramePoseUsingYawPitchRoll;
import us.ihmc.yoVariables.registry.YoRegistry;

public abstract class SimSensor
{
   private final String name;
   private final SimJointBasics parentJoint;
   private final MovingReferenceFrame frame;

   private final YoFramePoseUsingYawPitchRoll offset;

   public SimSensor(SensorDefinition definition, SimJointBasics parentJoint, YoRegistry registry)
   {
      this(definition.getName(), parentJoint, definition.getTransformToJoint(), registry);
   }

   public SimSensor(String name, SimJointBasics parentJoint, RigidBodyTransformReadOnly transformToParent, YoRegistry registry)
   {
      this.name = name;
      this.parentJoint = parentJoint;

      offset = new YoFramePoseUsingYawPitchRoll(name + "Offset", parentJoint.getFrameAfterJoint(), registry);
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

   public void update()
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
}
