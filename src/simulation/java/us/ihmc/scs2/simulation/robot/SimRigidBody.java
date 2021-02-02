package us.ihmc.scs2.simulation.robot;

import us.ihmc.euclid.matrix.interfaces.Matrix3DReadOnly;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Tuple3DReadOnly;
import us.ihmc.mecano.multiBodySystem.RigidBody;
import us.ihmc.mecano.multiBodySystem.interfaces.JointBasics;

public class SimRigidBody extends RigidBody
{
   public SimRigidBody(String bodyName, ReferenceFrame parentStationaryFrame)
   {
      super(bodyName, parentStationaryFrame);
   }

   public SimRigidBody(String bodyName, RigidBodyTransformReadOnly transformToParent, ReferenceFrame parentStationaryFrame)
   {
      super(bodyName, transformToParent, parentStationaryFrame);
   }

   public SimRigidBody(String bodyName, JointBasics parentJoint, double Ixx, double Iyy, double Izz, double mass, Tuple3DReadOnly centerOfMassOffset)
   {
      super(bodyName, parentJoint, Ixx, Iyy, Izz, mass, centerOfMassOffset);
   }

   public SimRigidBody(String bodyName, JointBasics parentJoint, Matrix3DReadOnly momentOfInertia, double mass, Tuple3DReadOnly centerOfMassOffset)
   {
      super(bodyName, parentJoint, momentOfInertia, mass, centerOfMassOffset);
   }

   public SimRigidBody(String bodyName, JointBasics parentJoint, Matrix3DReadOnly momentOfInertia, double mass, RigidBodyTransformReadOnly inertiaPose)
   {
      super(bodyName, parentJoint, momentOfInertia, mass, inertiaPose);
   }

}
