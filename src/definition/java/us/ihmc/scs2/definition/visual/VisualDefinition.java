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

   public static class MaterialDefinition
   {
      private String name;
      private TextureDefinition textureDefinition;
      private double lighting;
      private ColorDefinition ambientcolorDefinition;
      private ColorDefinition diffuseColorDefinition;
      private ColorDefinition specularColorDefinition;
      private ColorDefinition emissiveColorDefinition;

      public MaterialDefinition()
      {
      }

      public MaterialDefinition(TextureDefinition textureDefinition)
      {
         this.textureDefinition = textureDefinition;
      }

      public MaterialDefinition(ColorDefinition diffuseColorDefinition)
      {
         this.diffuseColorDefinition = diffuseColorDefinition;
      }

      public void setName(String name)
      {
         this.name = name;
      }

      public void setTextureDefinition(TextureDefinition textureDefinition)
      {
         this.textureDefinition = textureDefinition;
      }

      public void setLighting(double lighting)
      {
         this.lighting = lighting;
      }

      public void setAmbientcolorDefinition(ColorDefinition ambientcolorDefinition)
      {
         this.ambientcolorDefinition = ambientcolorDefinition;
      }

      public void setDiffuseColorDefinition(ColorDefinition diffuseColorDefinition)
      {
         this.diffuseColorDefinition = diffuseColorDefinition;
      }

      public void setSpecularColorDefinition(ColorDefinition specularColorDefinition)
      {
         this.specularColorDefinition = specularColorDefinition;
      }

      public void setEmissiveColorDefinition(ColorDefinition emissiveColorDefinition)
      {
         this.emissiveColorDefinition = emissiveColorDefinition;
      }

      public String getName()
      {
         return name;
      }

      public TextureDefinition getTextureDefinition()
      {
         return textureDefinition;
      }

      public double getLighting()
      {
         return lighting;
      }

      public ColorDefinition getAmbientcolorDefinition()
      {
         return ambientcolorDefinition;
      }

      public ColorDefinition getDiffuseColorDefinition()
      {
         return diffuseColorDefinition;
      }

      public ColorDefinition getSpecularColorDefinition()
      {
         return specularColorDefinition;
      }

      public ColorDefinition getEmissiveColorDefinition()
      {
         return emissiveColorDefinition;
      }
   }

   public static class TextureDefinition
   {
      private String filename;

      public void setFilename(String filename)
      {
         this.filename = filename;
      }

      public String getFilename()
      {
         return filename;
      }
   }
}
