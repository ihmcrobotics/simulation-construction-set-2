package us.ihmc.scs2.definition.state.interfaces;

import us.ihmc.euclid.geometry.interfaces.Pose3DReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;
import us.ihmc.mecano.multiBodySystem.interfaces.JointReadOnly;
import us.ihmc.mecano.multiBodySystem.interfaces.SixDoFJointReadOnly;
import us.ihmc.mecano.spatial.interfaces.SpatialVectorReadOnly;

public interface SixDoFJointStateBasics extends JointStateBasics, SixDoFJointStateReadOnly
{
   void setConfiguration(Pose3DReadOnly configuration);

   void setVelocity(Vector3DReadOnly angularVelocity, Vector3DReadOnly linearVelocity);

   void setAcceleration(Vector3DReadOnly angularAcceleration, Vector3DReadOnly linearAcceleration);

   void setEffort(Vector3DReadOnly torque, Vector3DReadOnly force);

   default void setVelocity(SpatialVectorReadOnly velocity)
   {
      setVelocity(velocity.getAngularPart(), velocity.getLinearPart());
   }

   default void setAcceleration(SpatialVectorReadOnly acceleration)
   {
      setAcceleration(acceleration.getAngularPart(), acceleration.getLinearPart());
   }

   default void setEffort(SpatialVectorReadOnly effort)
   {
      setEffort(effort.getAngularPart(), effort.getLinearPart());
   }

   @Override
   default void setConfiguration(JointReadOnly joint)
   {
      setConfiguration(((SixDoFJointReadOnly) joint).getJointPose());
   }

   @Override
   default void setVelocity(JointReadOnly joint)
   {
      setVelocity(((SixDoFJointReadOnly) joint).getJointTwist());
   }

   @Override
   default void setAcceleration(JointReadOnly joint)
   {
      setAcceleration(((SixDoFJointReadOnly) joint).getJointAcceleration());
   }

   @Override
   default void setEffort(JointReadOnly joint)
   {
      setEffort(((SixDoFJointReadOnly) joint).getJointWrench());
   }
}
