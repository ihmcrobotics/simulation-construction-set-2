package us.ihmc.scs2.definition.robot;

import us.ihmc.euclid.tuple3D.interfaces.Tuple3DReadOnly;

public class ExternalForcePointDefinition extends KinematicPointDefinition
{
   public ExternalForcePointDefinition()
   {
      super();
   }

   public ExternalForcePointDefinition(String name, Tuple3DReadOnly offsetFromJoint)
   {
      super(name, offsetFromJoint);
   }

   public ExternalForcePointDefinition(ExternalForcePointDefinition other)
   {
      super(other);
   }

   @Override
   public ExternalForcePointDefinition copy()
   {
      return new ExternalForcePointDefinition(this);
   }
}
