package us.ihmc.scs2.sharedMemory;

import java.util.concurrent.ConcurrentLinkedQueue;

import us.ihmc.scs2.sharedMemory.interfaces.LinkedYoVariableFactory;
import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;
import us.ihmc.scs2.sharedMemory.tools.BufferTools;
import us.ihmc.yoVariables.registry.YoVariableRegistry;
import us.ihmc.yoVariables.variable.YoVariable;

/**
 * {@code YoSharedBuffer} allows to control read/write on {@code YoVariable} buffers of a session.
 * It is meant to be used by a single thread that is responsible for writing from the
 * {@code YoVariable}s into the internal buffers via {@link #writeBuffer()}, or reading from them to
 * update the {@code YoVariable}s.
 * <p>
 * For operations on the buffer from other threads, that is read/write or even modifying the list of
 * {@code YoVariable}s to record, this class can be shared via the interface
 * {@code LinkedYoVariableFactory}. The interface restricts the API to the creation of linked
 * registries, variable, or buffer properties. These linked elements represent a thread-safe barrier
 * with their own internal memory which allow to make read/write request on this
 * {@code YoSharedBuffer}. These requests will then be consumed on the owner thread of the
 * {@code YoSharedBuffer} and will trigger a notification to the linked elements, when needed, that
 * for instance new data is available.
 * </p>
 * <p>
 * The owner of the {@code YoSharedBuffer} is in the following named as the <i>buffer manager</i>
 * while the threads owning linked elements are referred to as <i>buffer consumers</i>.
 * </p>
 * <p>
 * Here are 3 typical use-case for a {@code YoSharedBuffer} from the buffer manager's perspective:
 * <ol>
 * <li>The buffer manager is computing data and storing it over time:
 * 
 * <pre>
 * yoSharedBuffer.processLinkedPushRequests(); // Apply linked requests if desired, skip this to only give read-only access to the buffer consumers. 
 * // Do calculation and write result in yoVariables
 * yoSharedBuffer.writeBuffer();
 * yoSharedBuffer.prepareLinkedBuffersForPull(); // Allow the buffer consumers to read the new data.
 * yoSharedBuffer.incrementBufferIndex(true); // Increment the current read/write index in the buffer.
 * </pre>
 * 
 * <li>The buffer manager is playing back some pre-recorded data from the buffer:
 * 
 * <pre>
 * yoSharedBuffer.readBuffer(); // Load values for the yoVariables from the buffer.
 * yoSharedBuffer.prepareLinkedBuffersForPull(); // Allow the buffer consumers to read the new data.
 * yoSharedBuffer.incrementBufferIndex(false, stepSizePerPlaybackTick); // Step forward in the buffer. stepSizePerPlaybackTick is typically greater than 0 to read forward, to play backward use a negative value.
 * </pre>
 * 
 * <li>The buffer manager is paused, i.e. the buffer index is not changing, but it authorizes
 * modifications from the buffer consumers:
 * 
 * <pre>
 * boolean isBufferModified = yoSharedBuffer.processLinkedPushRequests(); // Apply requested changes.
 * if (isBufferModified)
 *    yoSharedBuffer.writeBuffer(); // Write the changes in the buffer (they were only applied onto the yoVariables).
 * // The following assumes the buffer manager can handle requests for changing the current reading index in the buffer.
 * // This type of requests cannot be achieved via the linked buffer interface.
 * boolean hasConsumerChangedReadingIndex = yoSharedBuffer.setCurrentIndex(userRequestedIndex);
 * if (hasConsumerChangedReadingIndex)
 *    yoSharedBuffer.readBuffer(); // In case the reading index has been modified.
 * if (hasConsumerChangedReadingIndex || isBufferModified)
 *    yoSharedBuffer.prepareLinkedBuffersForPull(); // Make sure the buffer consumers have the updated data.
 * </pre>
 * </ol>
 * </p>
 * 
 * @author Sylvain Bertrand
 */
public class YoSharedBuffer implements LinkedYoVariableFactory
{
   private final YoVariableRegistryBuffer registryBuffer;

   private final ConcurrentLinkedQueue<LinkedBuffer> linkedBuffers = new ConcurrentLinkedQueue<>();
   private final ConcurrentLinkedQueue<LinkedBufferProperties> linkedBufferProperties = new ConcurrentLinkedQueue<>();

   private final YoBufferProperties properties = new YoBufferProperties();

   public YoSharedBuffer(YoVariableRegistry rootRegistry, int initialBufferSize)
   {
      properties.setSize(initialBufferSize);
      registryBuffer = new YoVariableRegistryBuffer(rootRegistry, properties);
   }

   /**
    * Creates new buffers if needed to ensure that each {@code YoVariable} is backed by one.
    * <p>
    * Operation for the buffer manager only.
    * </p>
    */
   public void registerMissingBuffers()
   {
      registryBuffer.registerMissingBuffers();
   }

   // TODO Should we read the buffer
   /**
    * Consumes a request for cropping the size of the buffers, i.e. resizing the buffers to only keep
    * the part that is in between the {@code from} and {@code to} points as defined in the given
    * {@code request}.
    * <p>
    * Operation for the buffer manager only.
    * </p>
    * <p>
    * Upon completion, the current index is set to the in-point and {@code YoVariable}s are reloaded
    * from the buffers.
    * </p>
    * 
    * @param request request defining the new size as well as the part of the buffer to preserve.
    */
   public void cropBuffer(CropBufferRequest request)
   {
      int newSize = request.getCroppedSize(properties.getSize());
      registryBuffer.resizeBuffer(request.getFrom(), newSize);
      properties.setSize(newSize);
      properties.setInPointIndex(0);
      properties.setOutPointIndex(newSize - 1);
      properties.setCurrentIndexUnsafe(0);
      readBuffer();
   }

   // TODO Should we read the buffer
   /**
    * Resize the buffer. This is typically used to increase its size.
    * <p>
    * When resizing, the buffer attempts to preserve the active part, i.e. data between the in and out
    * points, and the position of the current index relative to the in and out points.
    * </p>
    * <p>
    * Operation for the buffer manager only.
    * </p>
    * <p>
    * Upon completion, the current index is set to the in-point and {@code YoVariable}s are reloaded
    * from the buffers.
    * </p>
    * 
    * @param newSize the new size for this buffer.
    * @return {@code true} if the buffer was resized, {@code false} otherwise.
    */
   public boolean resizeBuffer(int newSize)
   {
      if (newSize == properties.getSize() || newSize <= 0)
         return false;

      int oldCurrentIndex = properties.getCurrentIndex();
      int oldInPoint = properties.getInPoint();
      int oldOutPoint = properties.getOutPoint();

      int startBufferCopyIndex = oldInPoint;

      int oldSize = properties.getSize();

      int newOutPoint, newInPoint, newCurrentIndex;

      if (newSize < properties.getActiveBufferLength())
      { // Cannot keep all of the active buffer, keeping data closest to out-point.
         startBufferCopyIndex = BufferTools.computeFromIndex(oldOutPoint, newSize, oldSize);

         newInPoint = 0;
         newOutPoint = newSize - 1;
         if (BufferTools.isInsideBounds(oldCurrentIndex, BufferTools.computeFromIndex(oldOutPoint, newSize, oldSize), oldOutPoint, oldSize))
            newCurrentIndex = newOutPoint - BufferTools.computeSubLength(oldCurrentIndex, oldOutPoint, oldSize) + 1;
         else
            newCurrentIndex = newOutPoint;
      }
      else
      {
         newInPoint = 0;
         newOutPoint = properties.getActiveBufferLength() - 1;
         if (properties.isIndexBetweenBounds(oldCurrentIndex))
            newCurrentIndex = BufferTools.computeSubLength(oldInPoint, oldCurrentIndex, oldSize) - 1;
         else
            newCurrentIndex = newOutPoint;
      }

      registryBuffer.resizeBuffer(startBufferCopyIndex, newSize);

      properties.setSize(newSize);
      properties.setInPointIndex(newInPoint);
      properties.setOutPointIndex(newOutPoint);
      properties.setCurrentIndexUnsafe(newCurrentIndex);
      readBuffer();
      return true;
   }

   // FIXME Inconsistent with incrementIndex
   /**
    * Changes the current position in the buffer and load data from the buffers at the new index into
    * the {@code YoVariable}s.
    * <p>
    * Operation for the buffer manager only.
    * </p>
    * 
    * @param newIndex the new position in the buffer.
    * @return {@code true} if the index was changed successfully, {@code false} otherwise.
    */
   public boolean setCurrentIndex(int newIndex)
   {
      boolean hasChanged = properties.setCurrentIndex(newIndex);
      if (hasChanged)
         readBuffer();
      return hasChanged;
   }

   /**
    * Changes the in-point index.
    * <p>
    * The in and out points are used to define a interval of interest in the buffer. They usually start
    * at zero and as data is being recorded into the buffer, the out point is being pushed such that
    * the interval represents the set of indices for which the data has been written.
    * </p>
    * <p>
    * Operation for the buffer manager only.
    * </p>
    * 
    * @param newInPoint the new index for the in point.
    * @return {@code true} if the index has changed, {@code false} otherwise.
    */
   public boolean setInPoint(int newInPoint)
   {
      return properties.setInPointIndex(newInPoint);
   }

   /**
    * Changes the out-point index.
    * <p>
    * The in and out points are used to define a interval of interest in the buffer. They usually start
    * at zero and as data is being recorded into the buffer, the out point is being pushed such that
    * the interval represents the set of indices for which the data has been written.
    * </p>
    * <p>
    * Operation for the buffer manager only.
    * </p>
    * 
    * @param newOutPoint the new index for the out point.
    * @return {@code true} if the index has changed, {@code false} otherwise.
    */
   public boolean setOutPoint(int newOutPoint)
   {
      return properties.setOutPointIndex(newOutPoint);
   }

   /**
    * Applies the changes requested from the difference linked variables and registries to the buffers.
    * <p>
    * When a change of value of a {@code YoVariable} is requested, it is applied to the
    * {@code YoVariable} not the buffer. So when this operation results in a actual modification, i.e.
    * this methods returns {@code true}, the buffers need to be updated by calling
    * {@link #writeBuffer()}.
    * </p>
    * <p>
    * Operation for the buffer manager only.
    * </p>
    * 
    * @return {@code true} if this operation actually resulted in at least one modification,
    *         {@code false} otherwise.
    */
   public boolean processLinkedPushRequests()
   {
      // TODO Should the linked buffers write their changes into the buffers?
      boolean hasPushedSomething = false;

      for (LinkedBuffer linkedBuffer : linkedBuffers)
         hasPushedSomething |= linkedBuffer.processPush();

      return hasPushedSomething;
   }

   /**
    * Discards all push requested from buffer consumers for all the internal buffer.
    * <p>
    * Operation for the buffer manager only.
    * </p>
    */
   public void flushLinkedPushRequests()
   {
      for (LinkedBuffer linkedBuffer : linkedBuffers)
         linkedBuffer.flushPush();
   }

   /**
    * Reads the buffers at the current index, i.e. {@code properties.getCurrentIndex()}, and update
    * their respective {@code YoVariable}.
    * <p>
    * Operation for the buffer manager only.
    * </p>
    */
   public void readBuffer()
   {
      registryBuffer.readBuffer();
   }

   /**
    * Writes into the buffers at the current index, i.e. {@code properties.getCurrentIndex()}, the
    * value of their respective {@code YoVariable}.
    * <p>
    * Operation for the buffer manager only.
    * </p>
    */
   public void writeBuffer()
   {
      registryBuffer.writeBuffer();
   }

   /**
    * Packs the values of this buffer's {@code YoVariable}s to be available to the linked variables.
    * <p>
    * Operation for the buffer manager only.
    * </p>
    */
   public void prepareLinkedBuffersForPull()
   {
      // FIXME hack to get the publish method faster.
      linkedBuffers.parallelStream().forEach(LinkedBuffer::prepareForPull);
      linkedBufferProperties.forEach(LinkedBufferProperties::prepareForPull);
   }

   /**
    * Indicates whether a buffer consumer is still awaiting for a response.
    * <p>
    * When a buffer consumer is awaiting for response, {@link #prepareLinkedBuffersForPull()} should be
    * called to resolve the request.
    * </p>
    * <p>
    * Operation for the buffer manager only.
    * </p>
    * 
    * @return {@code true} if at least one request is still pending, {@code false} otherwise.
    */
   public boolean hasRequestPending()
   {
      return linkedBuffers.stream().anyMatch(LinkedBuffer::hasRequestPending);
   }

   // TODO Should we read buffers?
   public int incrementBufferIndex(boolean updateBufferBounds)
   {
      return properties.incrementIndex(updateBufferBounds);
   }

   // TODO Should we read buffers?
   public int incrementBufferIndex(boolean updateBufferBounds, int stepSize)
   {
      return properties.incrementIndex(updateBufferBounds, stepSize);
   }

   // TODO Should we read buffers?
   public int decrementBufferIndex(int stepSize)
   {
      return properties.decrementIndex(stepSize);
   }

   public YoBufferPropertiesReadOnly getProperties()
   {
      return properties;
   }

   public YoVariableRegistry getRootRegistry()
   {
      return registryBuffer.getRootRegistry();
   }

   public YoVariableRegistryBuffer getRegistryBuffer()
   {
      return registryBuffer;
   }

   @Override
   public LinkedYoVariableRegistry newLinkedYoVariableRegistry(YoVariableRegistry registryToLink)
   {
      LinkedYoVariableRegistry linkedYoVariableRegistry = registryBuffer.newLinkedYoVariableRegistry(registryToLink);
      linkedBuffers.add(linkedYoVariableRegistry);
      return linkedYoVariableRegistry;
   }

   @Override
   public LinkedYoVariableRegistry newLinkedYoVariableRegistry()
   {
      LinkedYoVariableRegistry linkedYoVariableRegistry = registryBuffer.newLinkedYoVariableRegistry();
      linkedBuffers.add(linkedYoVariableRegistry);
      return linkedYoVariableRegistry;
   }

   @Override
   public LinkedBufferProperties newLinkedBufferProperties()
   {
      LinkedBufferProperties linkedBufferProperties = new LinkedBufferProperties(properties);
      this.linkedBufferProperties.add(linkedBufferProperties);
      return linkedBufferProperties;
   }

   @Override
   public LinkedYoVariable<?> newLinkedYoVariable(YoVariable<?> variableToLink)
   {
      LinkedYoVariable<?> linkedYoVariable = LinkedYoVariable.newLinkedYoVariable(variableToLink, registryBuffer.findYoVariableBuffer(variableToLink));
      linkedBuffers.add(linkedYoVariable);
      return linkedYoVariable;
   }
}
