package us.ihmc.scs2.definition.visual;

public class MaterialDefinition
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