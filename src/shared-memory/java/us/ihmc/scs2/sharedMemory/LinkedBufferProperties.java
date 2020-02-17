package us.ihmc.scs2.sharedMemory;

import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;

public class LinkedBufferProperties
{
   private final YoBufferProperties bufferProperties;

   private YoBufferProperties requestedBufferProperties;
   private YoBufferPropertiesReadOnly currentBufferProperties;

   LinkedBufferProperties(YoBufferProperties bufferProperties)
   {
      this.bufferProperties = bufferProperties;
   }

   YoBufferPropertiesReadOnly pollBufferPropertiesRequest()
   {
      YoBufferPropertiesReadOnly newRequest = requestedBufferProperties;
      requestedBufferProperties = null;
      return newRequest;
   }

   void prepareForPull()
   {
      currentBufferProperties = bufferProperties.copy();
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
