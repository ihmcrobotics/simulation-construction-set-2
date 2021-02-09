package us.ihmc.scs2.simulation.robot;

import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.transform.RigidBodyTransform;
import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.mecano.frames.MovingReferenceFrame;
import us.ihmc.mecano.spatial.Twist;
import us.ihmc.mecano.yoVariables.spatial.YoFixedFrameTwist;
import us.ihmc.scs2.definition.robot.KinematicPointDefinition;
import us.ihmc.scs2.simulation.robot.multiBodySystem.SimJointBasics;
import us.ihmc.yoVariables.euclid.referenceFrame.YoFramePoseUsingYawPitchRoll;
import us.ihmc.yoVariables.euclid.referenceFrame.YoFrameVector3D;
import us.ihmc.yoVariables.registry.YoRegistry;

public class KinematicPoint
{
   private final String name;
   private final SimJointBasics parentJoint;
   private final MovingReferenceFrame frame;

   private final YoFramePoseUsingYawPitchRoll offset;
   private final YoFramePoseUsingYawPitchRoll pose;
   private final YoFixedFrameTwist twist;

   public KinematicPoint(KinematicPointDefinition definition, SimJointBasics parentJoint)
   {
      this(definition.getName(), parentJoint, definition.getTransformToParent());
   }

   public KinematicPoint(String name, SimJointBasics parentJoint, RigidBodyTransformReadOnly transformToParent)
   {
      this.name = name;
      this.parentJoint = parentJoint;

      ReferenceFrame rootFrame = parentJoint.getFrameBeforeJoint().getRootFrame();
      YoRegistry registry = parentJoint.getRegistry();
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

      pose = new YoFramePoseUsingYawPitchRoll(name, rootFrame, registry);
      twist = new YoFixedFrameTwist(parentJoint.getSuccessor().getBodyFixedFrame(),
                                    rootFrame,
                                    new YoFrameVector3D(name + "AngularVelocity", frame, registry),
                                    new YoFrameVector3D(name + "LinearVelocity", frame, registry));
   }

   public void update()
   {
      frame.update();
      pose.setFromReferenceFrame(frame);
      twist.set(frame.getTwistOfFrame());
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

   public YoFramePoseUsingYawPitchRoll getPose()
   {
      return pose;
   }

   public YoFixedFrameTwist getTwist()
   {
      return twist;
   }
}
