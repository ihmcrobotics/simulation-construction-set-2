package us.ihmc.scs2.definition.yoComposite;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAttribute;

import us.ihmc.scs2.definition.visual.PaintDefinition;
import us.ihmc.scs2.definition.visual.ColorDefinition;

public class YoColorRGBASingleDefinition extends PaintDefinition
{
   /**
    * The color is defined by a single 32-bit integer.
    * 
    * @see ColorDefinition#toRGBA()
    * @see ColorDefinition#rgba(int)
    */
   private String rgba;

   public YoColorRGBASingleDefinition()
   {
   }

   public YoColorRGBASingleDefinition(String rgba)
   {
      this.rgba = rgba;
   }

   public YoColorRGBASingleDefinition(YoColorRGBASingleDefinition other)
   {
      rgba = other.rgba;
   }

   @XmlAttribute
   public void setRGBA(String rgba)
   {
      this.rgba = rgba;
   }

   public String getRGBA()
   {
      return rgba;
   }

   @Override
   public YoColorRGBASingleDefinition copy()
   {
      return new YoColorRGBASingleDefinition(this);
   }

   @Override
   public boolean equals(Object object)
   {
      if (object == this)
      {
         return true;
      }
      else if (object instanceof YoColorRGBASingleDefinition other)
      {
         if (!Objects.equals(rgba, other.rgba))
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
      return "[rgba=" + rgba + "]";
   }
}
