package us.ihmc.scs2.definition.robot;

import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Tuple3DReadOnly;

public class CameraSensorDefinition extends SensorDefinition
{
   private double fieldOfView;
   private double clipNear;
   private double clipFar;

   private int imageWidth;
   private int imageHeight;

   public CameraSensorDefinition()
   {
   }

   public CameraSensorDefinition(String name, Tuple3DReadOnly offsetFromJoint)
   {
      super(name, offsetFromJoint);
   }

   public CameraSensorDefinition(String name, RigidBodyTransformReadOnly transformToJoint)
   {
      super(name, transformToJoint);
   }

   public CameraSensorDefinition(String name, RigidBodyTransformReadOnly transformToJoint, double fieldOfView, double clipNear, double clipFar)
   {
      super(name, transformToJoint);

      setFieldOfView(fieldOfView);

      setClipNear(clipNear);
      setClipFar(clipFar);
   }

   public CameraSensorDefinition(CameraSensorDefinition other)
   {
      super(other);

      fieldOfView = other.fieldOfView;
      clipNear = other.clipNear;
      clipFar = other.clipFar;
      imageWidth = other.imageWidth;
      imageHeight = other.imageHeight;
   }

   public double getFieldOfView()
   {
      return fieldOfView;
   }

   public void setFieldOfView(double fieldOfView)
   {
      this.fieldOfView = fieldOfView;
   }

   public double getClipNear()
   {
      return clipNear;
   }

   public void setClipNear(double clipNear)
   {
      this.clipNear = clipNear;
   }

   public double getClipFar()
   {
      return clipFar;
   }

   public void setClipFar(double clipFar)
   {
      this.clipFar = clipFar;
   }

   public int getImageWidth()
   {
      return imageWidth;
   }

   public void setImageWidth(int imageWidth)
   {
      this.imageWidth = imageWidth;
   }

   public int getImageHeight()
   {
      return imageHeight;
   }

   public void setImageHeight(int imageHeight)
   {
      this.imageHeight = imageHeight;
   }

   @Override
   public CameraSensorDefinition copy()
   {
      return new CameraSensorDefinition(this);
   }
}
