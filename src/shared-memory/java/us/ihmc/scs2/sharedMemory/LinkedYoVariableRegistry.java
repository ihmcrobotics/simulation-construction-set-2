package us.ihmc.scs2.sharedMemory;

import java.util.*;

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

   private final List<LinkedYoVariable> linkedYoVariables = new ArrayList<>();
   private final Map<YoVariable, LinkedYoVariable> linkedYoVariableMap = new HashMap<>();

   LinkedYoVariableRegistry(YoVariableRegistry rootRegistry, YoVariableRegistryBuffer yoVariableRegistryBuffer)
   {
      this.rootRegistry = rootRegistry;
      this.yoVariableRegistryBuffer = yoVariableRegistryBuffer;
      List<YoVariable<?>> allVariables = rootRegistry.getAllVariablesIncludingDescendants();
      allVariables.forEach(this::setupNewLinkedYoVariable);
   }

   public int pullMissingYoVariables()
   {
      synchronized (linkedYoVariables)
      {
         int numberOfNewYoVariables = YoMirroredRegistryTools.duplicateMissingYoVariablesInTarget(yoVariableRegistryBuffer.getRootRegistry(),
                                                                                                  rootRegistry,
                                                                                                  this::setupNewLinkedYoVariable);
         return numberOfNewYoVariables;
      }
   }

   public void linkNewYoVariables()
   {
      synchronized (linkedYoVariables)
      {
         List<YoVariable<?>> allYoVariables = rootRegistry.getAllVariables();

         if (allYoVariables.size() != linkedYoVariables.size())
         {
            for (YoVariable<?> yoVariable : allYoVariables)
            {
               LinkedYoVariable linkedYoVariable = linkedYoVariableMap.get(yoVariable);

               if (linkedYoVariable == null)
                  setupNewLinkedYoVariable(yoVariable);
            }
         }
      }
   }

   private void setupNewLinkedYoVariable(YoVariable<?> variableToLink)
   {
      YoVariableBuffer yoVariableBuffer = yoVariableRegistryBuffer.findOrCreateYoVariableBuffer(variableToLink);
      LinkedYoVariable linkedYoVariable = yoVariableBuffer.newLinkedYoVariable(variableToLink);
      linkedYoVariables.add(linkedYoVariable);
      linkedYoVariableMap.put(variableToLink, linkedYoVariable);
   }

   public void push(YoVariable<?>... yoVariablesToPush)
   {
      Arrays.asList(yoVariablesToPush).stream().map(linkedYoVariableMap::get).filter(v -> v != null).forEach(LinkedYoVariable::push);
   }

   @Override
   public void push()
   {
      synchronized (linkedYoVariables)
      {
         linkedYoVariables.forEach(LinkedYoVariable::push);
      }
   }

   @Override
   boolean processPush()
   {
      synchronized (linkedYoVariables)
      {
         boolean hasPushedSomething = false;
         for (LinkedYoVariable<?> linkedYoVariable : linkedYoVariables)
            hasPushedSomething |= linkedYoVariable.processPush();

         return hasPushedSomething;
      }
   }

   @Override
   void prepareForPull()
   {
      synchronized (linkedYoVariables)
      {
         linkedYoVariables.forEach(LinkedYoVariable::prepareForPull);
      }
   }

   @Override
   public boolean pull()
   {
      synchronized (linkedYoVariables)
      {
         boolean hasNewData = false;

         for (LinkedYoVariable linkedYoVariable : linkedYoVariables)
            hasNewData |= linkedYoVariable.pull();

         return hasNewData;
      }
   }

   @Override
   boolean hasBufferSampleRequestPending()
   {
      synchronized (linkedYoVariables)
      {
         return linkedYoVariables.stream().anyMatch(LinkedYoVariable::hasBufferSampleRequestPending);
      }
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
