package us.ihmc.scs2.definition.state;

import java.util.EnumSet;
import java.util.Set;

import org.ejml.data.DMatrix;

import us.ihmc.euclid.tuple3D.Vector3D;
import us.ihmc.euclid.tuple4D.Quaternion;
import us.ihmc.mecano.multiBodySystem.interfaces.JointBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.JointReadOnly;
import us.ihmc.mecano.multiBodySystem.interfaces.SphericalJointBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.SphericalJointReadOnly;
import us.ihmc.mecano.tools.JointStateType;
import us.ihmc.scs2.definition.state.interfaces.JointStateBasics;

public class SphericalJointState extends JointStateBase implements JointStateBasics
{
   private final Set<JointStateType> availableStates = EnumSet.noneOf(JointStateType.class);
   private final Quaternion configuration = new Quaternion();
   private final Vector3D angularVelocity = new Vector3D();
   private final Vector3D angularAcceleration = new Vector3D();
   private final Vector3D torque = new Vector3D();

   public SphericalJointState()
   {
   }

   public SphericalJointState(SphericalJointState other)
   {
      configuration.set(other.configuration);
      angularVelocity.set(other.angularVelocity);
      angularAcceleration.set(other.angularAcceleration);
      torque.set(other.torque);
      availableStates.addAll(other.availableStates);
   }

   @Override
   public void clear()
   {
      availableStates.clear();
   }

   @Override
   public int getConfigurationSize()
   {
      return 4;
   }

   @Override
   public int getDegreesOfFreedom()
   {
      return 3;
   }

   @Override
   public void setConfiguration(JointReadOnly joint)
   {
      SphericalJointReadOnly sphericalJoint = (SphericalJointReadOnly) joint;
      configuration.set(sphericalJoint.getJointOrientation());
   }

   @Override
   public void setVelocity(JointReadOnly joint)
   {
      SphericalJointReadOnly sphericalJoint = (SphericalJointReadOnly) joint;
      angularVelocity.set(sphericalJoint.getJointAngularVelocity());
   }

   @Override
   public void setAcceleration(JointReadOnly joint)
   {
      SphericalJointReadOnly sphericalJoint = (SphericalJointReadOnly) joint;
      angularAcceleration.set(sphericalJoint.getJointAngularAcceleration());
   }

   @Override
   public void setEffort(JointReadOnly joint)
   {
      SphericalJointReadOnly sphericalJoint = (SphericalJointReadOnly) joint;
      torque.set(sphericalJoint.getJointTorque());
   }

   @Override
   public boolean hasOutputFor(JointStateType query)
   {
      return availableStates.contains(query);
   }

   public Quaternion getConfiguration()
   {
      return configuration;
   }

   @Override
   public void getConfiguration(JointBasics jointToUpdate)
   {
      SphericalJointBasics sphericalJoint = (SphericalJointBasics) jointToUpdate;
      sphericalJoint.getJointOrientation().set(configuration);
   }

   @Override
   public int getConfiguration(int startRow, DMatrix configurationToPack)
   {
      configuration.get(startRow, configurationToPack);
      return startRow + getConfigurationSize();
   }

   public Vector3D getAngularVelocity()
   {
      return angularVelocity;
   }

   @Override
   public void getVelocity(JointBasics jointToUpdate)
   {
      SphericalJointBasics sphericalJoint = (SphericalJointBasics) jointToUpdate;
      sphericalJoint.getJointAngularVelocity().set(angularVelocity);
   }

   @Override
   public int getVelocity(int startRow, DMatrix velocityToPack)
   {
      angularVelocity.get(startRow, velocityToPack);
      return startRow + getDegreesOfFreedom();
   }

   public Vector3D getAngularAcceleration()
   {
      return angularAcceleration;
   }

   @Override
   public void getAcceleration(JointBasics jointToUpdate)
   {
      SphericalJointBasics sphericalJoint = (SphericalJointBasics) jointToUpdate;
      sphericalJoint.getJointAngularAcceleration().set(angularAcceleration);
   }

   @Override
   public int getAcceleration(int startRow, DMatrix accelerationToPack)
   {
      angularAcceleration.get(startRow, accelerationToPack);
      return startRow + getDegreesOfFreedom();
   }

   public Vector3D getTorque()
   {
      return torque;
   }

   @Override
   public void getEffort(JointBasics jointToUpdate)
   {
      SphericalJointBasics sphericalJoint = (SphericalJointBasics) jointToUpdate;
      sphericalJoint.getJointTorque().set(torque);
   }

   @Override
   public int getEffort(int startRow, DMatrix effortToPack)
   {
      torque.get(startRow, effortToPack);
      return startRow + getDegreesOfFreedom();
   }

   @Override
   public SphericalJointState copy()
   {
      return new SphericalJointState(this);
   }
}
