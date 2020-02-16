package us.ihmc.scs2.sharedMemory;

import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;

public abstract class LinkedBuffer
{
   public abstract boolean pull();

   public abstract void push();

   abstract boolean processPush();

   abstract void prepareForPull(YoBufferPropertiesReadOnly newProperties);

   abstract boolean hasBufferSampleRequestPending();
}
