package us.ihmc.scs2.simulation.robot;

import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.mecano.yoVariables.spatial.YoFixedFrameWrench;
import us.ihmc.scs2.definition.robot.WrenchSensorDefinition;
import us.ihmc.yoVariables.euclid.referenceFrame.YoFrameVector3D;
import us.ihmc.yoVariables.registry.YoRegistry;

public class SimWrenchSensor extends SimSensor
{
   private final YoFixedFrameWrench wrench;

   public SimWrenchSensor(WrenchSensorDefinition definition, SimJointBasics parentJoint, YoRegistry registry)
   {
      this(definition.getName(), parentJoint, definition.getTransformToJoint(), registry);
   }

   public SimWrenchSensor(String name, SimJointBasics parentJoint, RigidBodyTransformReadOnly transformToParent, YoRegistry registry)
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
