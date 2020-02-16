package us.ihmc.scs2.sharedMemory;

public abstract class LinkedBuffer
{
   public abstract boolean pull();

   public abstract void push();

   abstract boolean processPush();

   abstract void prepareForPull();

   abstract boolean hasBufferSampleRequestPending();
}
