package us.ihmc.scs2.definition.geometry;

import us.ihmc.euclid.tools.EuclidCoreTools;
import us.ihmc.euclid.tools.EuclidHashCodeTools;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.Objects;

public class ExtrusionDefinition extends GeometryDefinition
{
   private BufferedImage image;
   private double thickness;

   public ExtrusionDefinition(String text, double thickness)
   {
      this(textToImage(text), thickness);
   }

   public ExtrusionDefinition(BufferedImage image, double thickness)
   {
      this.image = image;
      this.thickness = thickness;
   }

   public ExtrusionDefinition(ExtrusionDefinition other)
   {
      setName(other.getName());
      image = copyImage(other.image);
      thickness = other.thickness;
   }

   public static BufferedImage textToImage(String text)
   {
      return textToImage(text, new Font("Lucida Sans", Font.PLAIN, 40));
   }

   public static BufferedImage textToImage(String text, Font font)
   {
      BufferedImage measurementImage = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
      Graphics2D measurementGraphics = measurementImage.createGraphics();
      FontRenderContext fontRenderContext = measurementGraphics.getFontRenderContext();

      Rectangle2D bounds = font.getStringBounds(text, fontRenderContext);

      int width = (int) bounds.getWidth();
      int height = (int) bounds.getHeight();

      BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
      Graphics graphics = bufferedImage.getGraphics();

      graphics.setColor(Color.white);
      graphics.fillRect(0, 0, width, height);
      graphics.setColor(Color.black);
      graphics.setFont(font);
      graphics.drawString(text, 0, (int) (height + bounds.getCenterY()));
      graphics.dispose();

      return bufferedImage;
   }

   public static BufferedImage copyImage(BufferedImage original)
   {
      if (original == null)
         return null;
      ColorModel colorModel = original.getColorModel();
      boolean isAlphaPremultiplied = colorModel.isAlphaPremultiplied();
      WritableRaster raster = original.copyData(null);
      return new BufferedImage(colorModel, raster, isAlphaPremultiplied, null);
   }

   public void setImage(BufferedImage image)
   {
      this.image = image;
   }

   public void setThickness(double thickness)
   {
      this.thickness = thickness;
   }

   public BufferedImage getImage()
   {
      return image;
   }

   public double getThickness()
   {
      return thickness;
   }

   @Override
   public ExtrusionDefinition copy()
   {
      return new ExtrusionDefinition(this);
   }

   @Override
   public int hashCode()
   {
      long bits = super.hashCode();
      bits = EuclidHashCodeTools.addToHashCode(bits, image);
      bits = EuclidHashCodeTools.addToHashCode(bits, thickness);
      return EuclidHashCodeTools.toIntHashCode(bits);
   }

   @Override
   public boolean equals(Object object)
   {
      if (object == this)
         return true;
      if (!super.equals(object))
         return false;

      ExtrusionDefinition other = (ExtrusionDefinition) object;

      if (!Objects.equals(image, other.image))
         return false;
      if (!EuclidCoreTools.equals(thickness, other.thickness))
         return false;

      return true;
   }
}
