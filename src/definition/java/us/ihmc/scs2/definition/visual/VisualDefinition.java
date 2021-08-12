package us.ihmc.scs2.definition.visual;

import us.ihmc.euclid.transform.AffineTransform;
import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.scs2.definition.geometry.GeometryDefinition;

public class VisualDefinition
{
   private String name;
   private AffineTransform originPose;
   private GeometryDefinition geometryDefinition;
   private MaterialDefinition materialDefinition;

   public VisualDefinition()
   {
   }

   public VisualDefinition(GeometryDefinition geometryDefinition, MaterialDefinition materialDefinition)
   {
      this.geometryDefinition = geometryDefinition;
      this.materialDefinition = materialDefinition;
   }

   public VisualDefinition(RigidBodyTransformReadOnly originPose, GeometryDefinition geometryDefinition, MaterialDefinition materialDefinition)
   {
      this(new AffineTransform(originPose), geometryDefinition, materialDefinition);
   }

   public VisualDefinition(AffineTransform originPose, GeometryDefinition geometryDefinition, MaterialDefinition materialDefinition)
   {
      this.originPose = originPose;
      this.geometryDefinition = geometryDefinition;
      this.materialDefinition = materialDefinition;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public void setOriginPose(RigidBodyTransformReadOnly originPose)
   {
      this.originPose = new AffineTransform(originPose);
   }

   public void setOriginPose(AffineTransform originPose)
   {
      this.originPose = originPose;
   }

   public void setGeometryDefinition(GeometryDefinition geometryDefinition)
   {
      this.geometryDefinition = geometryDefinition;
   }

   public void setMaterialDefinition(MaterialDefinition materialDefinition)
   {
      this.materialDefinition = materialDefinition;
   }

   public String getName()
   {
      return name;
   }

   public AffineTransform getOriginPose()
   {
      return originPose;
   }

   public GeometryDefinition getGeometryDefinition()
   {
      return geometryDefinition;
   }

   public MaterialDefinition getMaterialDefinition()
   {
      return materialDefinition;
   }

   @Override
   public String toString()
   {
      return "VisualDefinition [name=" + name + ", originPose=" + originPose + ", geometryDefinition=" + geometryDefinition + ", materialDefinition="
            + materialDefinition + "]";
   }
}
