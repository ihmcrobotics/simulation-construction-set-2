package us.ihmc.scs2.simulation.robot;

import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.mecano.algorithms.interfaces.RigidBodyAccelerationProvider;
import us.ihmc.mecano.algorithms.interfaces.RigidBodyTwistProvider;
import us.ihmc.scs2.definition.robot.IMUSensorDefinition;
import us.ihmc.yoVariables.euclid.referenceFrame.YoFramePoint3D;
import us.ihmc.yoVariables.euclid.referenceFrame.YoFrameQuaternion;
import us.ihmc.yoVariables.euclid.referenceFrame.YoFrameVector3D;
import us.ihmc.yoVariables.registry.YoRegistry;

public class SimIMUSensor extends SimSensor
{
   private final YoFrameQuaternion orientation;
   private final YoFrameVector3D angularVelocity;
   private final YoFrameVector3D linearAcceleration;

   public SimIMUSensor(IMUSensorDefinition definition, SimJointBasics parentJoint)
   {
      this(definition.getName(), parentJoint, definition.getTransformToJoint());
   }

   public SimIMUSensor(String name, SimJointBasics parentJoint, RigidBodyTransformReadOnly transformToParent)
   {
      super(name, parentJoint, transformToParent);
      ReferenceFrame rootFrame = parentJoint.getFrameAfterJoint().getRootFrame();
      YoRegistry registry = parentJoint.getRegistry();
      orientation = new YoFrameQuaternion(name + "Orientation", rootFrame, registry);
      angularVelocity = new YoFrameVector3D(name + "AngularVelocity", getFrame(), registry);
      linearAcceleration = new YoFrameVector3D(name + "LinearAcceleration", getFrame(), registry);
   }

   @Override
   public void update(RobotPhysicsOutput robotPhysicsOutput)
   {
      super.update(robotPhysicsOutput);
      orientation.setFromReferenceFrame(getFrame());
      angularVelocity.set(getFrame().getTwistOfFrame().getAngularPart());

      double dt = robotPhysicsOutput.getDT();
      RigidBodyTwistProvider deltaTwistProvider = robotPhysicsOutput.getDeltaTwistProvider();
      RigidBodyAccelerationProvider accelerationProvider = robotPhysicsOutput.getAccelerationProvider();
      SimRigidBodyBasics body = getParentJoint().getSuccessor();
      YoFramePoint3D bodyFixedPoint = getOffset().getPosition();
      linearAcceleration.scaleAdd(1.0 / dt,
                                  deltaTwistProvider.getLinearVelocityOfBodyFixedPoint(body, bodyFixedPoint),
                                  accelerationProvider.getLinearAccelerationOfBodyFixedPoint(body, bodyFixedPoint));
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
