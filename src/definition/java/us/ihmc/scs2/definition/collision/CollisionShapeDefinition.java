package us.ihmc.scs2.definition.collision;

import us.ihmc.euclid.transform.RigidBodyTransform;
import us.ihmc.scs2.definition.geometry.GeometryDefinition;

public class CollisionShapeDefinition
{
   private String name;
   private RigidBodyTransform originPose;
   private GeometryDefinition geometryDefinition;

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
}
