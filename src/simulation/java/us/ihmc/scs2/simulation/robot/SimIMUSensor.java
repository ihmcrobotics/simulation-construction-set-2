package us.ihmc.scs2.simulation.robot;

import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.scs2.definition.robot.IMUSensorDefinition;
import us.ihmc.yoVariables.euclid.referenceFrame.YoFrameQuaternion;
import us.ihmc.yoVariables.euclid.referenceFrame.YoFrameVector3D;
import us.ihmc.yoVariables.registry.YoRegistry;

public class SimIMUSensor extends SimSensor
{
   private final YoFrameQuaternion orientation;
   private final YoFrameVector3D angularVelocity;
   private final YoFrameVector3D linearAcceleration;

   public SimIMUSensor(IMUSensorDefinition definition, SimJointBasics parentJoint, YoRegistry registry)
   {
      this(definition.getName(), parentJoint, definition.getTransformToJoint(), registry);
   }

   public SimIMUSensor(String name, SimJointBasics parentJoint, RigidBodyTransformReadOnly transformToParent, YoRegistry registry)
   {
      super(name, parentJoint, transformToParent, registry);
      ReferenceFrame rootFrame = parentJoint.getFrameAfterJoint().getRootFrame();
      orientation = new YoFrameQuaternion(name + "Orientation", rootFrame, registry);
      angularVelocity = new YoFrameVector3D(name + "AngularVelocity", getFrame(), registry);
      linearAcceleration = new YoFrameVector3D(name + "LinearAcceleration", getFrame(), registry);
   }

   @Override
   public void update()
   {
      super.update();
      orientation.setFromReferenceFrame(getFrame());
      angularVelocity.set(getFrame().getTwistOfFrame().getAngularPart());
      // TODO Linear acceleration
   }

   public YoFrameQuaternion getOrientation()
   {
      return orientation;
   }

   public YoFrameVector3D getAngularVelocity()
   {
      return angularVelocity;
   }

   public YoFrameVector3D getLinearAcceleration()
   {
      return linearAcceleration;
   }
}
