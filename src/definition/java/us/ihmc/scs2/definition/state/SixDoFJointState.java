package us.ihmc.scs2.definition.state;

import java.util.EnumSet;
import java.util.Set;

import us.ihmc.euclid.geometry.Pose3D;
import us.ihmc.euclid.geometry.interfaces.Pose3DReadOnly;
import us.ihmc.euclid.orientation.interfaces.Orientation3DReadOnly;
import us.ihmc.euclid.tuple3D.Vector3D;
import us.ihmc.euclid.tuple3D.interfaces.Tuple3DReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;
import us.ihmc.mecano.tools.JointStateType;
import us.ihmc.scs2.definition.state.interfaces.SixDoFJointStateBasics;

public class SixDoFJointState extends JointStateBase implements SixDoFJointStateBasics
{
   private final Set<JointStateType> availableStates = EnumSet.noneOf(JointStateType.class);
   private final Pose3D configuration = new Pose3D();
   private final Vector3D angularVelocity = new Vector3D();
   private final Vector3D linearVelocity = new Vector3D();
   private final Vector3D angularAcceleration = new Vector3D();
   private final Vector3D linearAcceleration = new Vector3D();
   private final Vector3D torque = new Vector3D();
   private final Vector3D force = new Vector3D();

   public SixDoFJointState()
   {
   }

   public SixDoFJointState(Orientation3DReadOnly orientation, Tuple3DReadOnly position)
   {
      setConfiguration(orientation, position);
   }

   public SixDoFJointState(SixDoFJointState other)
   {
      configuration.set(other.configuration);
      angularVelocity.set(other.angularVelocity);
      linearVelocity.set(other.linearVelocity);
      angularAcceleration.set(other.angularAcceleration);
      linearAcceleration.set(other.linearAcceleration);
      torque.set(other.torque);
      force.set(other.force);
      availableStates.addAll(other.availableStates);
   }

   @Override
   public void clear()
   {
      availableStates.clear();
   }

   @Override
   public void setConfiguration(Orientation3DReadOnly orientation, Tuple3DReadOnly position)
   {
      availableStates.add(JointStateType.CONFIGURATION);

      if (orientation != null)
         configuration.getOrientation().set(orientation);
      else
         configuration.getOrientation().setToZero();

      if (position != null)
         configuration.getPosition().set(position);
      else
         configuration.getPosition().setToZero();
   }

   @Override
   public void setVelocity(Vector3DReadOnly angularVelocity, Vector3DReadOnly linearVelocity)
   {
      availableStates.add(JointStateType.VELOCITY);
      if (angularVelocity == null)
         this.angularVelocity.setToZero();
      else
         this.angularVelocity.set(angularVelocity);
      if (linearVelocity == null)
         this.linearVelocity.setToZero();
      else
         this.linearVelocity.set(linearVelocity);
   }

   @Override
   public void setAcceleration(Vector3DReadOnly angularAcceleration, Vector3DReadOnly linearAcceleration)
   {
      availableStates.add(JointStateType.ACCELERATION);
      this.angularAcceleration.set(angularAcceleration);
      this.linearAcceleration.set(linearAcceleration);
   }

   @Override
   public void setEffort(Vector3DReadOnly torque, Vector3DReadOnly force)
   {
      availableStates.add(JointStateType.EFFORT);
      this.torque.set(torque);
      this.force.set(force);
   }

   @Override
   public boolean hasOutputFor(JointStateType query)
   {
      return availableStates.contains(query);
   }

   @Override
   public Pose3DReadOnly getConfiguration()
   {
      return configuration;
   }

   @Override
   public Vector3DReadOnly getAngularVelocity()
   {
      return angularVelocity;
   }

   @Override
   public Vector3DReadOnly getLinearVelocity()
   {
      return linearVelocity;
   }

   @Override
   public Vector3DReadOnly getAngularAcceleration()
   {
      return angularAcceleration;
   }

   @Override
   public Vector3DReadOnly getLinearAcceleration()
   {
      return linearAcceleration;
   }

   @Override
   public Vector3DReadOnly getTorque()
   {
      return torque;
   }

   @Override
   public Vector3DReadOnly getForce()
   {
      return force;
   }

   @Override
   public SixDoFJointState copy()
   {
      return new SixDoFJointState(this);
   }
}
