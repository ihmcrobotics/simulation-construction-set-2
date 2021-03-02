package us.ihmc.scs2.definition.robot;

import us.ihmc.euclid.tuple3D.interfaces.Tuple3DReadOnly;

public class GroundContactPointDefinition extends ExternalForcePointDefinition
{
   private int groupIdentifier = 0;

   public GroundContactPointDefinition()
   {
      super();
   }

   public GroundContactPointDefinition(String name, Tuple3DReadOnly offsetFromJoint)
   {
      super(name, offsetFromJoint);
   }

   public GroundContactPointDefinition(String name, Tuple3DReadOnly offsetFromJoint, int groupIdentifier)
   {
      super(name, offsetFromJoint);
      this.groupIdentifier = groupIdentifier;
   }

   public GroundContactPointDefinition(GroundContactPointDefinition other)
   {
      super(other);
      groupIdentifier = other.groupIdentifier;
   }

   public int getGroupIdentifier()
   {
      return groupIdentifier;
   }

   @Override
   public GroundContactPointDefinition copy()
   {
      return new GroundContactPointDefinition(this);
   }
}
