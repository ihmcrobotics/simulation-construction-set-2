package us.ihmc.scs2.definition.robot;

import us.ihmc.euclid.tuple3D.interfaces.Tuple3DReadOnly;
import us.ihmc.mecano.multiBodySystem.SixDoFJoint;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.SixDoFJointBasics;

public class SixDoFJointDefinition extends JointDefinition
{
   // For compatibility with SCS1
   @Deprecated
   private String variableName;

   public SixDoFJointDefinition()
   {
   }

   public SixDoFJointDefinition(String name)
   {
      super(name);
   }

   public SixDoFJointDefinition(String name, Tuple3DReadOnly offsetFromParent)
   {
      super(name, offsetFromParent);
   }

   public SixDoFJointDefinition(SixDoFJointDefinition other)
   {
      super(other);
   }

   @Override
   public SixDoFJointBasics toJoint(RigidBodyBasics predecessor)
   {
      return new SixDoFJoint(getName(), predecessor, getTransformToParent());
   }

   // For compatibility with SCS1
   @Deprecated
   public void setVariableName(String variableName)
   {
      this.variableName = variableName;
   }

   // For compatibility with SCS1
   @Deprecated
   public String getVariableName()
   {
      return variableName;
   }

   @Override
   public SixDoFJointDefinition copy()
   {
      return new SixDoFJointDefinition(this);
   }
}
