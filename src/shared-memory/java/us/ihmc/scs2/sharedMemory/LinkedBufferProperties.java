package us.ihmc.scs2.sharedMemory;

import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;

public class LinkedBufferProperties
{
   private YoBufferProperties requestedBufferProperties;
   private YoBufferPropertiesReadOnly currentBufferProperties;

   LinkedBufferProperties()
   {
   }

   YoBufferPropertiesReadOnly pollBufferPropertiesRequest()
   {
      YoBufferPropertiesReadOnly newRequest = requestedBufferProperties;
      requestedBufferProperties = null;
      return newRequest;
   }

   void prepareForPull(YoBufferPropertiesReadOnly newProperties)
   {
      currentBufferProperties = newProperties;
   }

   public YoBufferPropertiesReadOnly peekCurrentBufferProperties()
   {
      return currentBufferProperties;
   }

   public YoBufferPropertiesReadOnly pollCurrentBufferProperties()
   {
      YoBufferPropertiesReadOnly properties = currentBufferProperties;
      currentBufferProperties = null;
      return properties;
   }
}
