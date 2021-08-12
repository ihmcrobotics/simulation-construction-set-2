package us.ihmc.scs2.definition.collision;

import java.util.Objects;

import us.ihmc.euclid.transform.RigidBodyTransform;
import us.ihmc.scs2.definition.geometry.GeometryDefinition;

public class CollisionShapeDefinition
{
   private String name;
   private RigidBodyTransform originPose;
   private GeometryDefinition geometryDefinition;

   /**
    * Collision identifier for this collidable. Use {@link CollidableHelper} to compute collision masks
    * and groups.
    */
   private long collisionMask = -1L;
   /**
    * Collision identifiers of other collidables that this collidable is allowed to collide with. Use
    * {@link CollidableHelper} to compute collision masks and groups.
    */
   private long collisionGroup = -1L;

   public CollisionShapeDefinition()
   {
   }

   public CollisionShapeDefinition(GeometryDefinition geometryDefinition)
   {
      this.geometryDefinition = geometryDefinition;
   }

   public CollisionShapeDefinition(RigidBodyTransform originPose, GeometryDefinition geometryDefinition)
   {
      this.originPose = originPose;
      this.geometryDefinition = geometryDefinition;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public void setOriginPose(RigidBodyTransform originPose)
   {
      this.originPose = originPose;
   }

   public void setGeometryDefinition(GeometryDefinition geometryDefinition)
   {
      this.geometryDefinition = geometryDefinition;
   }

   public void setCollisionMask(long collisionMask)
   {
      this.collisionMask = collisionMask;
   }

   public void setCollisionGroup(long collisionGroup)
   {
      this.collisionGroup = collisionGroup;
   }

   public String getName()
   {
      return name;
   }

   public RigidBodyTransform getOriginPose()
   {
      return originPose;
   }

   public GeometryDefinition getGeometryDefinition()
   {
      return geometryDefinition;
   }

   public long getCollisionMask()
   {
      return collisionMask;
   }

   public long getCollisionGroup()
   {
      return collisionGroup;
   }

   @Override
   public boolean equals(Object object)
   {
      if (object == this)
      {
         return true;
      }
      else if (object instanceof CollisionShapeDefinition)
      {
         CollisionShapeDefinition other = (CollisionShapeDefinition) object;

         if (!Objects.equals(name, other.name))
            return false;
         if (!Objects.equals(originPose, other.originPose))
            return false;
         if (!Objects.equals(geometryDefinition, other.geometryDefinition))
            return false;
         if (collisionMask != other.collisionMask)
            return false;
         if (collisionGroup != other.collisionGroup)
            return false;
         return true;
      }
      else
      {
         return false;
      }
   }

   @Override
   public String toString()
   {
      return "CollisionShapeDefinition [name=" + name + ", originPose=" + originPose + ", geometryDefinition=" + geometryDefinition + ", collisionMask="
            + collisionMask + ", collisionGroup=" + collisionGroup + "]";
   }
}
