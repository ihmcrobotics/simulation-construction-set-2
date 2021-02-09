package us.ihmc.scs2.simulation.robot;

import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.mecano.spatial.interfaces.SpatialImpulseReadOnly;
import us.ihmc.mecano.spatial.interfaces.WrenchReadOnly;
import us.ihmc.mecano.yoVariables.spatial.YoFixedFrameWrench;
import us.ihmc.scs2.definition.robot.WrenchSensorDefinition;
import us.ihmc.scs2.simulation.robot.multiBodySystem.SimJointBasics;
import us.ihmc.scs2.simulation.robot.multiBodySystem.SimRigidBodyBasics;
import us.ihmc.yoVariables.euclid.referenceFrame.YoFrameVector3D;
import us.ihmc.yoVariables.registry.YoRegistry;

public class SimWrenchSensor extends SimSensor
{
   private final YoFixedFrameWrench wrench;

   public SimWrenchSensor(WrenchSensorDefinition definition, SimJointBasics parentJoint)
   {
      this(definition.getName(), parentJoint, definition.getTransformToJoint());
   }

   public SimWrenchSensor(String name, SimJointBasics parentJoint, RigidBodyTransformReadOnly transformToParent)
   {
      super(name, parentJoint, transformToParent);

      YoRegistry registry = parentJoint.getRegistry();
      wrench = new YoFixedFrameWrench(parentJoint.getSuccessor().getBodyFixedFrame(),
                                      new YoFrameVector3D(name + "Moment", getFrame(), registry),
                                      new YoFrameVector3D(name + "Force", getFrame(), registry));
   }

   @Override
   public void update(RobotPhysicsOutput robotPhysicsOutput)
   {
      super.update(robotPhysicsOutput);

      SimRigidBodyBasics body = getParentJoint().getSuccessor();
      double dt = robotPhysicsOutput.getDT();
      WrenchReadOnly externalWrench = robotPhysicsOutput.getExternalWrenchProvider().apply(body);
      SpatialImpulseReadOnly externalImpulse = robotPhysicsOutput.getExternalImpulseProvider().apply(body);

      if (externalImpulse != null)
      {
         wrench.set(externalImpulse);
         wrench.scale(1.0 / dt);

         if (externalWrench != null)
            wrench.add(externalWrench);
      }
      else if (externalWrench != null)
      {
         wrench.set(externalWrench);
      }
      else
      {
         wrench.setToZero();
      }
   }

   public YoFixedFrameWrench getWrench()
   {
      return wrench;
   }
}
