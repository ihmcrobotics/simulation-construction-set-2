package us.ihmc.scs2.definition.visual;

import javax.xml.bind.annotation.XmlElement;

import us.ihmc.euclid.transform.AffineTransform;
import us.ihmc.euclid.transform.interfaces.AffineTransformReadOnly;
import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.scs2.definition.AffineTransformDefinition;
import us.ihmc.scs2.definition.geometry.GeometryDefinition;

public class VisualDefinition
{
   private String name;
   private AffineTransformDefinition originPose = new AffineTransformDefinition();
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

   public VisualDefinition(AffineTransformReadOnly originPose, GeometryDefinition geometryDefinition, MaterialDefinition materialDefinition)
   {
      this(new AffineTransformDefinition(originPose), geometryDefinition, materialDefinition);
   }

   public VisualDefinition(AffineTransformDefinition originPose, GeometryDefinition geometryDefinition, MaterialDefinition materialDefinition)
   {
      this.originPose = originPose;
      this.geometryDefinition = geometryDefinition;
      this.materialDefinition = materialDefinition;
   }

   @XmlElement
   public void setName(String name)
   {
      this.name = name;
   }

   public void setOriginPose(RigidBodyTransformReadOnly originPose)
   {
      this.originPose = new AffineTransformDefinition(originPose);
   }

   public void setOriginPose(AffineTransformReadOnly originPose)
   {
      this.originPose = new AffineTransformDefinition(originPose);
   }

   @XmlElement
   public void setOriginPose(AffineTransformDefinition originPose)
   {
      this.originPose = originPose;
   }

   @XmlElement
   public void setGeometryDefinition(GeometryDefinition geometryDefinition)
   {
      this.geometryDefinition = geometryDefinition;
   }

   @XmlElement
   public void setMaterialDefinition(MaterialDefinition materialDefinition)
   {
      this.materialDefinition = materialDefinition;
   }

   public String getName()
   {
      return name;
   }

   public AffineTransformDefinition getOriginPose()
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
