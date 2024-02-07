package us.ihmc.scs2.sessionVisualizer.jfx.managers;

import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.transform.RigidBodyTransform;
import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;

public class ReferenceFrameWrapper
{
   private static final RigidBodyTransformReadOnly IDENTITY = new RigidBodyTransform();
   private final String name;
   private final String fullName;
   private ReferenceFrame referenceFrame;

   public ReferenceFrameWrapper(String name, String fullName)
   {
      this(name, fullName, null);
   }

   public ReferenceFrameWrapper(ReferenceFrame referenceFrame)
   {
      this(referenceFrame.getName(), referenceFrame.getNameId(), referenceFrame);
   }

   private ReferenceFrameWrapper(String name, String fullName, ReferenceFrame referenceFrame)
   {
      this.name = name;
      this.fullName = fullName;
      this.referenceFrame = referenceFrame;
   }

   public String getName()
   {
      return name;
   }

   public String getFullName()
   {
      return fullName;
   }

   public ReferenceFrame getReferenceFrame()
   {
      return referenceFrame;
   }

   public void setReferenceFrame(ReferenceFrame referenceFrame)
   {
      this.referenceFrame = referenceFrame;
   }

   public boolean isDefined()
   {
      return referenceFrame != null;
   }

   public boolean isRootFrame()
   {
      // Making undefined reference frames root frames.
      return !isDefined() || referenceFrame.isRootFrame();
   }

   public RigidBodyTransformReadOnly getTransformToRoot()
   {
      if (referenceFrame == null)
         return IDENTITY;
      else
         return referenceFrame.getTransformToRoot();
   }
}
