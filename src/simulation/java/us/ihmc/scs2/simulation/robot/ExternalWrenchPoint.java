package us.ihmc.scs2.simulation.robot;

import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.mecano.yoVariables.spatial.YoFixedFrameWrench;
import us.ihmc.scs2.definition.robot.KinematicPointDefinition;
import us.ihmc.yoVariables.euclid.referenceFrame.YoFrameVector3D;
import us.ihmc.yoVariables.registry.YoRegistry;

public class ExternalWrenchPoint extends KinematicPoint
{
   protected final YoFixedFrameWrench wrench;

   public ExternalWrenchPoint(KinematicPointDefinition definition, SimJointBasics parentJoint, YoRegistry registry)
   {
      this(definition.getName(), parentJoint, definition.getTransformToParent(), registry);
   }

   public ExternalWrenchPoint(String name, SimJointBasics parentJoint, RigidBodyTransformReadOnly transformToParent, YoRegistry registry)
   {
      super(name, parentJoint, transformToParent, registry);

      wrench = new YoFixedFrameWrench(parentJoint.getSuccessor().getBodyFixedFrame(),
                                      new YoFrameVector3D(name + "Moment", getFrame(), registry),
                                      new YoFrameVector3D(name + "Force", getFrame(), registry));
   }

   public YoFixedFrameWrench getWrench()
   {
      return wrench;
   }
}
