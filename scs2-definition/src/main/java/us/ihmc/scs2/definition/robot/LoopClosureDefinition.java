package us.ihmc.scs2.definition.robot;

import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.euclid.tuple3D.Vector3D;
import us.ihmc.euclid.tuple3D.interfaces.Tuple3DReadOnly;
import us.ihmc.scs2.definition.YawPitchRollTransformDefinition;

public class LoopClosureDefinition
{
   private YawPitchRollTransformDefinition transformToSuccessorParent = new YawPitchRollTransformDefinition();
   private Vector3D kpSoftConstraint;
   private Vector3D kdSoftConstraint;

   public LoopClosureDefinition()
   {
   }

   public LoopClosureDefinition(LoopClosureDefinition other)
   {
      transformToSuccessorParent.set(other.transformToSuccessorParent);
      kpSoftConstraint = other.kpSoftConstraint == null ? null : new Vector3D(other.kpSoftConstraint);
      kdSoftConstraint = other.kdSoftConstraint == null ? null : new Vector3D(other.kdSoftConstraint);
   }

   public void setTransformToSuccessorParent(YawPitchRollTransformDefinition transformToSuccessorParent)
   {
      this.transformToSuccessorParent = transformToSuccessorParent;
   }

   public void setTransformToSuccessorParent(RigidBodyTransformReadOnly transformToSuccessorParent)
   {
      this.transformToSuccessorParent.set(transformToSuccessorParent);
   }

   public void setOffsetFromSuccessorParent(Tuple3DReadOnly offsetFromSuccessorParent)
   {
      this.transformToSuccessorParent.setTranslationAndIdentityRotation(offsetFromSuccessorParent);
   }

   public YawPitchRollTransformDefinition getTransformToSuccessorParent()
   {
      return transformToSuccessorParent;
   }

   public void setKpSoftConstraint(double kpSoftConstraint)
   {
      setKpSoftConstraint(new Vector3D(kpSoftConstraint, kpSoftConstraint, kpSoftConstraint));
   }

   public void setKpSoftConstraint(Vector3D kpSoftConstraint)
   {
      this.kpSoftConstraint = kpSoftConstraint;
   }

   public Vector3D getKpSoftConstraint()
   {
      return kpSoftConstraint;
   }

   public void setKdSoftConstraint(double kdSoftConstraint)
   {
      setKdSoftConstraint(new Vector3D(kdSoftConstraint, kdSoftConstraint, kdSoftConstraint));
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
      return "transformToSuccessorParent: " + transformToSuccessorParent;
   }
}
