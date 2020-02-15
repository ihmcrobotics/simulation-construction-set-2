package us.ihmc.scs2.sharedMemory;

import java.util.concurrent.ConcurrentLinkedQueue;

import us.ihmc.commons.MathTools;
import us.ihmc.scs2.sharedMemory.interfaces.LinkedYoVariableFactory;
import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;
import us.ihmc.yoVariables.registry.YoVariableRegistry;
import us.ihmc.yoVariables.variable.YoVariable;

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

   public void registerMissingBuffers()
   {
      registryBuffer.registerMissingBuffers();
   }

   public void cropBuffer(CropBufferRequest request)
   {
      int newSize = request.getSize(properties.getSize());
      registryBuffer.resizeBuffer(request.getFrom(), newSize);
      properties.setSize(newSize);
      properties.setInPointIndex(0);
      properties.setOutPointIndex(newSize - 1);
      properties.setCurrentIndexUnsafe(0);
      updateYoVariables();
   }

   public boolean resizeBuffer(int newSize)
   {
      if (newSize == properties.getSize() || newSize <= 0)
         return false;

      int currentIndex = properties.getCurrentIndex();
      int inPoint = properties.getInPoint();
      int outPoint = properties.getOutPoint();

      int startBufferCopyIndex = inPoint;

      int initialSize = properties.getActiveBufferLength();

      if (initialSize > newSize)
      {
         startBufferCopyIndex = outPoint - newSize + 1;
         if (startBufferCopyIndex < 0)
            startBufferCopyIndex += properties.getSize();
      }

      registryBuffer.resizeBuffer(startBufferCopyIndex, newSize);

      if (outPoint < inPoint)
         outPoint += initialSize;
      if (currentIndex < inPoint)
         currentIndex += initialSize;

      int newOutPoint = Math.min(outPoint - startBufferCopyIndex, newSize - 1);
      int newInPoint = 0;
      int newCurrentIndex = MathTools.clamp(currentIndex - startBufferCopyIndex, newInPoint, newOutPoint);

      properties.setSize(newSize);
      properties.setInPointIndex(newInPoint);
      properties.setOutPointIndex(newOutPoint);
      properties.setCurrentIndexUnsafe(newCurrentIndex);
      return true;
   }

   public boolean processLinkedRequests()
   {
      for (LinkedBuffer linkedBuffer : linkedBuffers)
         linkedBuffer.filterPush();

      boolean hasPushedSomething = false;

      for (LinkedBuffer linkedBuffer : linkedBuffers)
         hasPushedSomething |= linkedBuffer.processPush();

      return hasPushedSomething;
   }

   public boolean setCurrentIndex(int newIndex)
   {
      boolean hasChanged = properties.setCurrentIndex(newIndex);
      if (hasChanged)
         updateYoVariables();
      return hasChanged;
   }

   public boolean setInPoint(int newInPoint)
   {
      return properties.setInPointIndex(newInPoint);
   }

   public boolean setOutPoint(int newOutPoint)
   {
      return properties.setOutPointIndex(newOutPoint);
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
      linkedBuffers.parallelStream().forEach(barrier -> barrier.prepareForPull(properties.copy()));
      linkedBufferProperties.forEach(linkedProperties -> linkedProperties.prepareForPull(properties.copy()));
   }

   public boolean hasBufferSampleRequestPending()
   {
      return linkedBuffers.stream().anyMatch(LinkedBuffer::hasBufferSampleRequestPending);
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
      LinkedBufferProperties linkedBufferProperties = new LinkedBufferProperties();
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
