package us.ihmc.scs2.sharedMemory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import us.ihmc.scs2.sharedMemory.tools.YoMirroredRegistryTools;
import us.ihmc.yoVariables.registry.YoVariableRegistry;
import us.ihmc.yoVariables.variable.YoVariable;

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
    * Blocking operation that registers all {@code YoVariable}s declared in the buffer as linked
    * variables in this linked registry.
    * <p>
    * The newly created linked variables can then be used to perform read/write operations with the
    * buffer.
    * </p>
    * <p>
    * Operation for the buffer consumers only.
    * </p>
    *
    * @return the number of {@code YoVariable}s that were created.
    */
   public int linkManagerVariables()
   {
      YoVariableRegistry bufferRootRegistry = yoVariableRegistryBuffer.getRootRegistry().getRegistry(rootRegistry.getNameSpace());

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

   /**
    * Blocking operation that pushes locally created {@code YoVariable}s to the buffer so they get a
    * buffer attributed.
    * <p>
    * The variables can then be used to perform read/write operations with the buffer.
    * </p>
    * <p>
    * Operation for the buffer consumers only.
    * </p>
    */
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

   /**
    * Creates a new {@code LinkedYoVariable} for the given {@code variableToLink}, ensures a buffer
    * exists for that variable, and adds the newly create linked variable to {@link #linkedYoVariables}
    * and {@link #linkedYoVariableMap}.
    * <p>
    * This operation requires the consumer and manager threads to be synchronized.
    * </p>
    *
    * @param variableToLink the variable to be linked to the buffer.
    */
   private void setupNewLinkedYoVariable(YoVariable<?> variableToLink)
   {
      YoVariableBuffer yoVariableBuffer = yoVariableRegistryBuffer.findOrCreateYoVariableBuffer(variableToLink);
      LinkedYoVariable newLinkedYoVariable = yoVariableBuffer.newLinkedYoVariable(variableToLink);
      linkedYoVariables.add(newLinkedYoVariable);
      linkedYoVariableMap.put(newLinkedYoVariable.getLinkedYoVariable(), newLinkedYoVariable);
   }

   /** {@inheritDoc} */
   // Operation for the buffer consumers only.
   @Override
   public void push()
   {
      linkedYoVariables.forEach(LinkedYoVariable::push);
   }

   /**
    * Creates request for modifying the buffer when possible on a sub-selection of variables. This is
    * typically used to push the value of a linked {@code YoVariable} that has been changed in a buffer
    * consumer thread.
    * <p>
    * Operation for the buffer consumers only.
    * </p>
    *
    * @param yoVariablesToPush the variables to push their value to the buffer.
    */
   public void push(YoVariable<?>... yoVariablesToPush)
   {
      Arrays.asList(yoVariablesToPush).stream().map(linkedYoVariableMap::get).filter(v -> v != null).forEach(LinkedYoVariable::push);
   }

   /** {@inheritDoc} */
   @Override
   public boolean pull()
   {
      boolean hasNewData = false;

      for (LinkedYoVariable linkedYoVariable : linkedYoVariables)
         hasNewData |= linkedYoVariable.pull();

      return hasNewData;
   }

   /** {@inheritDoc} */
   // Operation for the buffer manager only.
   @Override
   boolean processPush(boolean writeBuffer)
   {
      boolean hasPushedSomething = false;

      lock.lock();
      try
      {
         for (LinkedYoVariable<?> linkedYoVariable : linkedYoVariables)
            hasPushedSomething |= linkedYoVariable.processPush(writeBuffer);
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

   /** {@inheritDoc} */
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

   /** {@inheritDoc} */
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

   public YoVariableRegistry getRootRegistry()
   {
      return rootRegistry;
   }
}
