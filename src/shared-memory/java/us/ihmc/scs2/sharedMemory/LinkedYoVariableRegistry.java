package us.ihmc.scs2.sharedMemory;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import us.ihmc.scs2.sharedMemory.tools.YoMirroredRegistryTools;
import us.ihmc.yoVariables.registry.YoVariableRegistry;
import us.ihmc.yoVariables.variable.YoVariable;

// FIXME the implementation is not right. Needs to consider pushing/pulling data from multiple
// threads.
@SuppressWarnings({"rawtypes", "unchecked"})
public class LinkedYoVariableRegistry extends LinkedBuffer
{
   private final YoVariableRegistry rootRegistry;
   private final YoVariableRegistryBuffer yoVariableRegistryBuffer;

   private final ReentrantLock lock = new ReentrantLock();
   private final List<LinkedYoVariable> linkedYoVariables = new ArrayList<>();
   private final Map<YoVariable, LinkedYoVariable> linkedYoVariableMap = new HashMap<>();

   LinkedYoVariableRegistry(YoVariableRegistry rootRegistry, YoVariableRegistryBuffer yoVariableRegistryBuffer)
   {
      this.rootRegistry = rootRegistry;
      this.yoVariableRegistryBuffer = yoVariableRegistryBuffer;
      linkConsumerVariables();
   }

   /**
    * Requests all {@code YoVariable}s declared in the main buffer to duplicate and link them in this
    * linked registry.
    *
    * @return the number of {@code YoVariable}s that were created.
    */
   public int linkManagerVariables()
   {
      YoVariableRegistry bufferRootRegistry = yoVariableRegistryBuffer.getRootRegistry();

      int numberOfNewVariables = 0;

      lock.lock();

      try
      {
         yoVariableRegistryBuffer.registerMissingBuffers();
         numberOfNewVariables = YoMirroredRegistryTools.duplicateMissingYoVariablesInTarget(bufferRootRegistry, rootRegistry, this::setupNewLinkedYoVariable);
      }
      finally
      {
         lock.unlock();
      }

      return numberOfNewVariables;
   }

   public void linkConsumerVariables()
   {
      ArrayList<YoVariable<?>> allYoVariables = rootRegistry.getAllVariables();

      if (allYoVariables.size() == linkedYoVariables.size())
         return;

      List<YoVariable<?>> variablesMissingLink = allYoVariables.stream().filter(v -> !linkedYoVariableMap.containsKey(v)).collect(Collectors.toList());

      lock.lock();

      try
      {
         variablesMissingLink.forEach(this::setupNewLinkedYoVariable);
      }
      finally
      {
         lock.unlock();
      }
   }

   private LinkedYoVariable<?> setupNewLinkedYoVariable(YoVariable<?> variableToLink)
   {
      YoVariableBuffer yoVariableBuffer = yoVariableRegistryBuffer.findOrCreateYoVariableBuffer(variableToLink);
      LinkedYoVariable newLinkedYoVariable = yoVariableBuffer.newLinkedYoVariable(variableToLink);
      linkedYoVariables.add(newLinkedYoVariable);
      linkedYoVariableMap.put(newLinkedYoVariable.getLinkedYoVariable(), newLinkedYoVariable);
      return newLinkedYoVariable;
   }

   // Operation for the buffer consumers only.
   public void push(YoVariable<?>... yoVariablesToPush)
   {
      Arrays.asList(yoVariablesToPush).stream().map(linkedYoVariableMap::get).filter(v -> v != null).forEach(LinkedYoVariable::push);
   }

   // Operation for the buffer consumers only.
   @Override
   public void push()
   {
      linkedYoVariables.forEach(LinkedYoVariable::push);
   }

   // Operation for the buffer consumers only.
   @Override
   public boolean pull()
   {
      boolean hasNewData = false;

      for (LinkedYoVariable linkedYoVariable : linkedYoVariables)
         hasNewData |= linkedYoVariable.pull();

      return hasNewData;
   }

   // Operation for the buffer manager only.
   @Override
   boolean processPush()
   {
      boolean hasPushedSomething = false;

      lock.lock();
      try
      {
         for (LinkedYoVariable<?> linkedYoVariable : linkedYoVariables)
            hasPushedSomething |= linkedYoVariable.processPush();
      }
      finally
      {
         lock.unlock();
      }

      return hasPushedSomething;
   }

   /** {@inheritDoc} */
   // Operation for the buffer manager only.
   @Override
   void flushPush()
   {
      lock.lock();
      try
      {
         for (LinkedYoVariable<?> linkedYoVariable : linkedYoVariables)
         {
            linkedYoVariable.flushPush();
         }
      }
      finally
      {
         lock.unlock();
      }
   }

   // Operation for the buffer manager only.
   @Override
   void prepareForPull()
   {
      lock.lock();
      try
      {
         linkedYoVariables.forEach(LinkedYoVariable::prepareForPull);
      }
      finally
      {
         lock.unlock();
      }
   }

   // Operation for the buffer manager only.
   @Override
   boolean hasRequestPending()
   {
      boolean hasRequestPending = false;

      lock.lock();

      try
      {
         hasRequestPending = linkedYoVariables.stream().anyMatch(LinkedYoVariable::hasRequestPending);
      }
      finally
      {
         lock.unlock();
      }

      return hasRequestPending;
   }

   public LinkedYoVariable<? extends YoVariable<?>> getLinkedYoVariable(YoVariable<?> yoVariable)
   {
      return linkedYoVariableMap.get(yoVariable);
   }

   public YoVariableRegistry getRootRegistry()
   {
      return rootRegistry;
   }
}
