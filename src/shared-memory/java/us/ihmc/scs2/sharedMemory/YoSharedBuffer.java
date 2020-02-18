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
 * {@code YoVariable}s into the internal buffers via {@link #updateBuffer()}, or reading from them
 * to update the {@code YoVariable}s.
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
      updateYoVariables();
   }

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
      updateYoVariables();
      return true;
   }

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
         updateYoVariables();
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

   public boolean processLinkedRequests()
   {
      boolean hasPushedSomething = false;

      for (LinkedBuffer linkedBuffer : linkedBuffers)
         hasPushedSomething |= linkedBuffer.processPush();

      return hasPushedSomething;
   }

   public void updateYoVariablesAndPublish()
   {
      updateYoVariables();
      publish();
   }

   public void updateYoVariables()
   {
      registryBuffer.readBuffer();
   }

   public void updateBufferAndPublish()
   {
      updateBuffer();
      publish();
   }

   public void updateBuffer()
   {
      registryBuffer.writeBuffer();
   }

   public void publish()
   {
      // FIXME hack to get the publish method faster.
      linkedBuffers.parallelStream().forEach(LinkedBuffer::prepareForPull);
      linkedBufferProperties.forEach(LinkedBufferProperties::prepareForPull);
   }

   public boolean hasRequestPending()
   {
      return linkedBuffers.stream().anyMatch(LinkedBuffer::hasRequestPending);
   }

   public int incrementBufferIndex(boolean updateBufferBounds)
   {
      return properties.incrementIndex(updateBufferBounds);
   }

   public int incrementBufferIndex(boolean updateBufferBounds, int stepSize)
   {
      return properties.incrementIndex(updateBufferBounds, stepSize);
   }

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
      LinkedYoVariableRegistry yoVariableBarrier = registryBuffer.newLinkedYoVariableRegistry(registryToLink);
      linkedBuffers.add(yoVariableBarrier);
      return yoVariableBarrier;
   }

   @Override
   public LinkedYoVariableRegistry newLinkedYoVariableRegistry()
   {
      LinkedYoVariableRegistry barrier = registryBuffer.newLinkedYoVariableRegistry();
      linkedBuffers.add(barrier);
      return barrier;
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
