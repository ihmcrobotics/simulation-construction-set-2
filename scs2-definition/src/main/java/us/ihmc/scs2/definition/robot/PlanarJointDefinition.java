package us.ihmc.scs2.definition.robot;

import java.util.Objects;

import us.ihmc.euclid.tools.EuclidHashCodeTools;
import us.ihmc.euclid.tuple3D.interfaces.Tuple3DReadOnly;
import us.ihmc.mecano.multiBodySystem.PlanarJoint;
import us.ihmc.mecano.multiBodySystem.interfaces.PlanarJointBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.scs2.definition.state.PlanarJointState;
import us.ihmc.scs2.definition.state.interfaces.JointStateReadOnly;

public class PlanarJointDefinition extends JointDefinition
{
   private PlanarJointState initialJointState;

   public PlanarJointDefinition()
   {
   }

   public PlanarJointDefinition(String name)
   {
      super(name);
   }

   public PlanarJointDefinition(String name, Tuple3DReadOnly offsetFromParent)
   {
      super(name, offsetFromParent);
   }

   public PlanarJointDefinition(PlanarJointDefinition other)
   {
      super(other);
      initialJointState = other.initialJointState == null ? null : other.initialJointState.copy();
   }

   @Override
   public void setInitialJointState(JointStateReadOnly initialJointState)
   {
      if (initialJointState instanceof PlanarJointState)
         setInitialJointState((PlanarJointState) initialJointState);
      else if (this.initialJointState == null)
         this.initialJointState = new PlanarJointState(initialJointState);
      else
         this.initialJointState.set(initialJointState);
   }

   public void setInitialJointState(PlanarJointState initialJointState)
   {
      this.initialJointState = initialJointState;
   }

   @Override
   public PlanarJointState getInitialJointState()
   {
      return initialJointState;
   }

   @Override
   public PlanarJointBasics toJoint(RigidBodyBasics predecessor)
   {
      return new PlanarJoint(getName(), predecessor, getTransformToParent());
   }

   @Override
   public PlanarJointDefinition copy()
   {
      return new PlanarJointDefinition(this);
   }

   @Override
   public int hashCode()
   {
      long bits = super.hashCode();
      bits = EuclidHashCodeTools.addToHashCode(bits, initialJointState);
      return EuclidHashCodeTools.toIntHashCode(bits);
   }

   @Override
   public boolean equals(Object object)
   {
      if (object == this)
         return true;
      if (!super.equals(object))
         return false;

      PlanarJointDefinition other = (PlanarJointDefinition) object;

      if (!Objects.equals(initialJointState, other.initialJointState))
         return false;

      return true;
   }
}
