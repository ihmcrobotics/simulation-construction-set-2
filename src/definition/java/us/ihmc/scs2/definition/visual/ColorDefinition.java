package us.ihmc.scs2.definition.visual;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Color")
public class ColorDefinition
{
   private double red, green, blue, alpha;

   public ColorDefinition()
   {
   }

   public ColorDefinition(int rgb)
   {
      this(rgb, false);
   }

   public ColorDefinition(int rgba, boolean hasAlpha)
   {
      int value = hasAlpha ? rgba : 0xff000000 | rgba;
      red = ((value >> 16) & 0xFF) / 255.0;
      blue = ((value >> 8) & 0xFF) / 255.0;
      green = ((value >> 0) & 0xFF) / 255.0;
      alpha = ((value >> 24) & 0xFF) / 255.0;
   }

   public ColorDefinition(int[] rgba)
   {
      this(rgba[0], rgba[1], rgba[2], rgba.length >= 4 ? rgba[3] : 1.0);
   }

   public ColorDefinition(int red, int green, int blue)
   {
      this(red, green, blue, 1.0);
   }

   public ColorDefinition(int red, int green, int blue, double alpha)
   {
      this(red / 255.0, green / 255.0, blue / 255.0, alpha);
   }

   public ColorDefinition(double[] rgba)
   {
      this(rgba[0], rgba[1], rgba[2], rgba.length >= 4 ? rgba[3] : 1.0);
   }

   public ColorDefinition(double red, double green, double blue)
   {
      this(red, green, blue, 1.0);
   }

   public ColorDefinition(double red, double green, double blue, double alpha)
   {
      this.red = red;
      this.green = green;
      this.blue = blue;
      this.alpha = alpha;
   }

   @XmlAttribute
   public void setRed(double red)
   {
      this.red = red;
   }

   @XmlAttribute
   public void setGreen(double green)
   {
      this.green = green;
   }

   @XmlAttribute
   public void setBlue(double blue)
   {
      this.blue = blue;
   }

   @XmlAttribute
   public void setAlpha(double alpha)
   {
      this.alpha = alpha;
   }

   public double getRed()
   {
      return red;
   }

   public double getGreen()
   {
      return green;
   }

   public double getBlue()
   {
      return blue;
   }

   public double getAlpha()
   {
      return alpha;
   }

   public ColorDefinition copy()
   {
      return new ColorDefinition(red, green, blue, alpha);
   }

   @Override
   public boolean equals(Object object)
   {
      if (object == this)
         return true;
      else if (object instanceof ColorDefinition)
         return equals((ColorDefinition) object);
      else
         return false;
   }

   public boolean equals(ColorDefinition other)
   {
      if (other == this)
         return true;
      if (other == null)
         return false;
      return red == other.red && green == other.green && blue == other.blue && alpha == other.alpha;
   }
}