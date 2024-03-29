package us.ihmc.scs2.sessionVisualizer.jfx.yoComposite;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import us.ihmc.euclid.yawPitchRoll.interfaces.YawPitchRollReadOnly;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.ReferenceFrameWrapper;

import java.util.Objects;

import static us.ihmc.scs2.definition.yoComposite.YoYawPitchRollDefinition.YoYawPitchRoll;
import static us.ihmc.scs2.definition.yoComposite.YoYawPitchRollDefinition.YoYawPitchRollIdentifiers;

public class YawPitchRollProperty extends Orientation3DProperty implements YawPitchRollReadOnly
{
   public YawPitchRollProperty()
   {
      super(YoYawPitchRoll, YoYawPitchRollIdentifiers);
   }

   public YawPitchRollProperty(double yaw, double pitch, double roll)
   {
      super(YoYawPitchRoll, YoYawPitchRollIdentifiers, yaw, pitch, roll);
   }

   public YawPitchRollProperty(ReferenceFrameWrapper referenceFrame, double yaw, double pitch, double roll)
   {
      super(YoYawPitchRoll, YoYawPitchRollIdentifiers, referenceFrame, yaw, pitch, roll);
   }

   public YawPitchRollProperty(DoubleProperty[] yawPitchRollProperties)
   {
      super(YoYawPitchRoll, YoYawPitchRollIdentifiers, yawPitchRollProperties);
   }

   public YawPitchRollProperty(Property<ReferenceFrameWrapper> referenceFrame, DoubleProperty[] yawPitchRollProperties)
   {
      super(YoYawPitchRoll, YoYawPitchRollIdentifiers, referenceFrame, yawPitchRollProperties);
   }

   public YawPitchRollProperty(DoubleProperty yawProperty, DoubleProperty pitchProperty, DoubleProperty rollProperty)
   {
      super(YoYawPitchRoll, YoYawPitchRollIdentifiers, yawProperty, pitchProperty, rollProperty);
   }

   public YawPitchRollProperty(Property<ReferenceFrameWrapper> referenceFrameProperty,
                               DoubleProperty yawProperty,
                               DoubleProperty pitchProperty,
                               DoubleProperty rollProperty)
   {
      super(YoYawPitchRoll, YoYawPitchRollIdentifiers, referenceFrameProperty, yawProperty, pitchProperty, rollProperty);
   }

   public YawPitchRollProperty(CompositeProperty other)
   {
      this();
      set(other);
   }

   public void setYaw(double yaw)
   {
      setYawProperty(new SimpleDoubleProperty(yaw));
   }

   public void setYawProperty(DoubleProperty yawProperty)
   {
      componentValueProperties[0] = yawProperty;
   }

   public void setPitch(double pitch)
   {
      setPitchProperty(new SimpleDoubleProperty(pitch));
   }

   public void setPitchProperty(DoubleProperty pitchProperty)
   {
      componentValueProperties[1] = pitchProperty;
   }

   public void setRoll(double roll)
   {
      setRollProperty(new SimpleDoubleProperty(roll));
   }

   public void setRollProperty(DoubleProperty rollProperty)
   {
      componentValueProperties[2] = rollProperty;
   }

   public void set(double yaw, double pitch, double roll)
   {
      setComponentValues(yaw, pitch, roll);
   }

   public void set(DoubleProperty yawProperty, DoubleProperty pitchProperty, DoubleProperty rollProperty)
   {
      setComponentValueProperties(yawProperty, pitchProperty, rollProperty);
   }

   @Override
   public void set(CompositeProperty other)
   {
      Objects.requireNonNull(other.getType());
      if (!getType().equals(other.getType()))
         throw new IllegalArgumentException("Cannot set a " + getClass().getSimpleName() + " to a " + other.getType());
      super.set(other);
   }

   public void set(YawPitchRollProperty other)
   {
      set((CompositeProperty) other);
   }

   @Override
   public double getYaw()
   {
      return yawProperty().get();
   }

   public DoubleProperty yawProperty()
   {
      return componentValueProperties[0];
   }

   @Override
   public double getPitch()
   {
      return pitchProperty().get();
   }

   public DoubleProperty pitchProperty()
   {
      return componentValueProperties[1];
   }

   @Override
   public double getRoll()
   {
      return rollProperty().get();
   }

   public DoubleProperty rollProperty()
   {
      return componentValueProperties[2];
   }

   @Override
   public YawPitchRollProperty clone()
   {
      return new YawPitchRollProperty(this);
   }
}