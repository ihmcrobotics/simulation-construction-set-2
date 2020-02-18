package us.ihmc.scs2.sharedMemory;

public abstract class LinkedBuffer
{
   /**
    * Polls new data from the main buffer, typically used to update a linked variable.
    * <p>
    * Operation for the buffer consumers only.
    * </p>
    * 
    * @return {@code true} if the pull actually changed data for the buffer consumer.
    */
   public abstract boolean pull();

   /**
    * Creates request for modifying the main buffer when possible. This is typically used to push the
    * value of a linked {@code YoVariable} that has been changed in a buffer consumer thread.
    * <p>
    * Operation for the buffer consumers only.
    * </p>
    */
   public abstract void push();

   /**
    * Called by the buffer manager to apply requested changes from the linked variables and registries
    * in the main buffer.
    * <p>
    * Operation for the buffer manager only.
    * </p>
    */
   abstract boolean processPush();

   /**
    * Called by the buffer manager when wrapping writing operations on the buffers, this is where next
    * data available to the linked variables and registries is stored.
    * <p>
    * Operation for the buffer manager only.
    * </p>
    */
   abstract void prepareForPull();

   /**
    * Tests whether a buffer consumer is still waiting for a buffer sample.
    * <p>
    * Operation for the buffer manager only.
    * </p>
    * 
    * @return {@code true} if there is still a {@link BufferSampleRequest} awaiting to be processed,
    *         {@code false} otherwise.
    */
   abstract boolean hasRequestPending();
}
