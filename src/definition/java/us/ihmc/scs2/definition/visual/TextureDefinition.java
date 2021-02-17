package us.ihmc.scs2.definition.visual;

import java.awt.image.BufferedImage;
import java.net.URL;

import us.ihmc.scs2.definition.geometry.ExtrusionDefinition;

public class TextureDefinition
{
   private URL textureFileURL;
   private String textureFilename;
   private BufferedImage textureImage;

   public TextureDefinition()
   {
   }

   public TextureDefinition(TextureDefinition other)
   {
      set(other);
   }

   public void set(TextureDefinition other)
   {
      textureFileURL = other.textureFileURL;
      textureFilename = other.textureFilename;
      textureImage = ExtrusionDefinition.copyImage(other.textureImage);
   }

   public void setTextureFileURL(URL textureFileURL)
   {
      this.textureFileURL = textureFileURL;
   }

   public void setTextureFilename(String textureFilename)
   {
      this.textureFilename = textureFilename;
   }

   public void setTextureImage(BufferedImage textureImage)
   {
      this.textureImage = textureImage;
   }

   public URL getTextureFileURL()
   {
      return textureFileURL;
   }

   public String getTextureFilename()
   {
      return textureFilename;
   }

   public BufferedImage getTextureImage()
   {
      return textureImage;
   }

   public TextureDefinition copy()
   {
      return new TextureDefinition(this);
   }
}