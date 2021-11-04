package us.ihmc.scs2.definition.robot;

import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.euclid.tuple3D.Vector3D;
import us.ihmc.scs2.definition.YawPitchRollTransformDefinition;

public class LoopClosureDefinition
{
   private YawPitchRollTransformDefinition transformToSecondParent = new YawPitchRollTransformDefinition();
   private RigidBodyDefinition secondPredecessor;

   private Vector3D kpSoftConstraint;
   private Vector3D kdSoftConstraint;

   public LoopClosureDefinition()
   {
   }

   public LoopClosureDefinition(LoopClosureDefinition other)
   {
      transformToSecondParent.set(other.transformToSecondParent);
      kpSoftConstraint = other.kpSoftConstraint == null ? null : new Vector3D(other.kpSoftConstraint);
      kdSoftConstraint = other.kdSoftConstraint == null ? null : new Vector3D(other.kdSoftConstraint);
   }

   public void setTransformToSecondParent(YawPitchRollTransformDefinition transformToSecondParent)
   {
      this.transformToSecondParent = transformToSecondParent;
   }

   public void setTransformToSecondParent(RigidBodyTransformReadOnly transformToSecondParent)
   {
      this.transformToSecondParent.set(transformToSecondParent);
   }

   public YawPitchRollTransformDefinition getTransformToSecondParent()
   {
      return transformToSecondParent;
   }

   public void setSecondPredecessor(RigidBodyDefinition secondPredecessor)
   {
      this.secondPredecessor = secondPredecessor;
   }

   public RigidBodyDefinition getSecondPredecessor()
   {
      return secondPredecessor;
   }

   public void setKpSoftConstraint(Vector3D kpSoftConstraint)
   {
      this.kpSoftConstraint = kpSoftConstraint;
   }

   public Vector3D getKpSoftConstraint()
   {
      return kpSoftConstraint;
   }

   public void setKdSoftConstraint(Vector3D kdSoftConstraint)
   {
      this.kdSoftConstraint = kdSoftConstraint;
   }

   public Vector3D getKdSoftConstraint()
   {
      return kdSoftConstraint;
   }

   public LoopClosureDefinition copy()
   {
      return new LoopClosureDefinition(this);
   }

   @Override
   public String toString()
   {
      return "transformToSecondParent: " + transformToSecondParent + ", secondPredecessor: " + secondPredecessor;
   }
}
