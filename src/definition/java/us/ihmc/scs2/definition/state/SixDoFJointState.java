package us.ihmc.scs2.definition.state;

import java.util.EnumSet;
import java.util.Set;

import us.ihmc.euclid.geometry.Pose3D;
import us.ihmc.euclid.geometry.interfaces.Pose3DReadOnly;
import us.ihmc.euclid.tuple3D.Vector3D;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;
import us.ihmc.mecano.multiBodySystem.interfaces.JointReadOnly;
import us.ihmc.mecano.tools.JointStateType;
import us.ihmc.scs2.definition.state.interfaces.SixDoFJointStateBasics;

public class SixDoFJointState implements SixDoFJointStateBasics
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

   @Override
   public void clear()
   {
      availableStates.clear();
   }

   @Override
   public void setConfiguration(Pose3DReadOnly configuration)
   {
      availableStates.add(JointStateType.CONFIGURATION);
      this.configuration.set(configuration);
   }

   @Override
   public void setVelocity(Vector3DReadOnly angularVelocity, Vector3DReadOnly linearVelocity)
   {
      availableStates.add(JointStateType.VELOCITY);
      this.angularVelocity.set(angularVelocity);
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
   public void addVelocity(JointReadOnly joint)
   {
      if (hasOutputFor(JointStateType.VELOCITY))
      {
         angularVelocity.add(joint.getJointTwist().getAngularPart());
         linearVelocity.add(joint.getJointTwist().getLinearPart());
      }
      else
      {
         setVelocity(joint);
      }
   }

   @Override
   public void addAcceleration(JointReadOnly joint)
   {
      if (hasOutputFor(JointStateType.ACCELERATION))
      {
         angularAcceleration.add(joint.getJointAcceleration().getAngularPart());
         linearAcceleration.add(joint.getJointAcceleration().getLinearPart());
      }
      else
      {
         setAcceleration(joint);
      }
   }

   @Override
   public void addEffort(JointReadOnly joint)
   {
      if (hasOutputFor(JointStateType.EFFORT))
      {
         torque.add(joint.getJointWrench().getAngularPart());
         force.add(joint.getJointWrench().getLinearPart());
      }
      else
      {
         setEffort(joint);
      }
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
}
