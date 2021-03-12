package us.ihmc.scs2.definition.yoSlider;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class YoSliderboardDefinition
{
   private String name;
   private YoSliderDefinition slider1;
   private YoSliderDefinition slider2;
   private YoSliderDefinition slider3;
   private YoSliderDefinition slider4;
   private YoSliderDefinition slider5;
   private YoSliderDefinition slider6;
   private YoSliderDefinition slider7;
   private YoSliderDefinition slider8;

   @XmlAttribute
   public void setName(String name)
   {
      this.name = name;
   }

   @XmlElement
   public void setSlider1(YoSliderDefinition slider1)
   {
      this.slider1 = slider1;
   }

   @XmlElement
   public void setSlider2(YoSliderDefinition slider2)
   {
      this.slider2 = slider2;
   }

   @XmlElement
   public void setSlider3(YoSliderDefinition slider3)
   {
      this.slider3 = slider3;
   }

   @XmlElement
   public void setSlider4(YoSliderDefinition slider4)
   {
      this.slider4 = slider4;
   }

   @XmlElement
   public void setSlider5(YoSliderDefinition slider5)
   {
      this.slider5 = slider5;
   }

   @XmlElement
   public void setSlider6(YoSliderDefinition slider6)
   {
      this.slider6 = slider6;
   }

   @XmlElement
   public void setSlider7(YoSliderDefinition slider7)
   {
      this.slider7 = slider7;
   }

   @XmlElement
   public void setSlider8(YoSliderDefinition slider8)
   {
      this.slider8 = slider8;
   }

   public String getName()
   {
      return name;
   }

   public YoSliderDefinition getSlider1()
   {
      return slider1;
   }

   public YoSliderDefinition getSlider2()
   {
      return slider2;
   }

   public YoSliderDefinition getSlider3()
   {
      return slider3;
   }

   public YoSliderDefinition getSlider4()
   {
      return slider4;
   }

   public YoSliderDefinition getSlider5()
   {
      return slider5;
   }

   public YoSliderDefinition getSlider6()
   {
      return slider6;
   }

   public YoSliderDefinition getSlider7()
   {
      return slider7;
   }

   public YoSliderDefinition getSlider8()
   {
      return slider8;
   }

   @Override
   public boolean equals(Object object)
   {
      if (object == this)
      {
         return true;
      }
      else if (object instanceof YoSliderboardDefinition)
      {
         YoSliderboardDefinition other = (YoSliderboardDefinition) object;

         if (name == null ? other.name != null : name.equals(other.name))
            return false;
         if (slider1 == null ? other.slider1 != null : slider1.equals(other.slider1))
            return false;
         if (slider2 == null ? other.slider2 != null : slider2.equals(other.slider2))
            return false;
         if (slider3 == null ? other.slider3 != null : slider3.equals(other.slider3))
            return false;
         if (slider4 == null ? other.slider4 != null : slider4.equals(other.slider4))
            return false;
         if (slider5 == null ? other.slider5 != null : slider5.equals(other.slider5))
            return false;
         if (slider6 == null ? other.slider6 != null : slider6.equals(other.slider6))
            return false;
         if (slider7 == null ? other.slider7 != null : slider7.equals(other.slider7))
            return false;
         if (slider8 == null ? other.slider8 != null : slider8.equals(other.slider8))
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
      return "slider1:" + slider1 + ", slider2:" + slider2 + ", slider3:" + slider3 + ", slider4:" + slider4 + ", slider5:" + slider5 + ", slider6:" + slider6
            + ", slider7:" + slider7 + ", slider8:" + slider8;
   }
}
