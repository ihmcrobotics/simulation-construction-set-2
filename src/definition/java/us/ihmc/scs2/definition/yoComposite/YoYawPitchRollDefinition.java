package us.ihmc.scs2.definition.yoComposite;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "YoYawPitchRoll")
public class YoYawPitchRollDefinition extends YoOrientation3DDefinition
{
   public static final String YoYawPitchRoll = "YoYawPitchRoll";
   public static final String[] YoYawPitchRollIdentifiers = new String[] {"yaw", "pitch", "roll"};

   private String yaw, pitch, roll;
   private String referenceFrame;

   public void setYaw(double yaw)
   {
      this.yaw = Double.toString(yaw);
   }

   @XmlElement
   public void setYaw(String yaw)
   {
      this.yaw = yaw;
   }

   public void setPitch(double pitch)
   {
      this.pitch = Double.toString(pitch);
   }

   @XmlElement
   public void setPitch(String pitch)
   {
      this.pitch = pitch;
   }

   public void setRoll(double roll)
   {
      this.roll = Double.toString(roll);
   }

   @XmlElement
   public void setRoll(String roll)
   {
      this.roll = roll;
   }

   @XmlElement
   public void setReferenceFrame(String referenceFrame)
   {
      this.referenceFrame = referenceFrame;
   }

   public String getYaw()
   {
      return yaw;
   }

   public String getPitch()
   {
      return pitch;
   }

   public String getRoll()
   {
      return roll;
   }

   @Override
   public String getReferenceFrame()
   {
      return referenceFrame;
   }

   @Override
   public String getType()
   {
      return YoYawPitchRoll;
   }

   @Override
   public String[] getComponentIdentifiers()
   {
      return YoYawPitchRollIdentifiers;
   }

   @Override
   public String[] getComponentValues()
   {
      return new String[] {yaw, pitch, roll};
   }
}
