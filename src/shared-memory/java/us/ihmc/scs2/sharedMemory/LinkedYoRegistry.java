package us.ihmc.scs2.sharedMemory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import us.ihmc.scs2.sharedMemory.tools.SharedMemoryTools;
import us.ihmc.yoVariables.listener.YoRegistryChangedListener;
import us.ihmc.yoVariables.registry.YoRegistry;
import us.ihmc.yoVariables.variable.YoVariable;

@SuppressWarnings({"rawtypes", "unchecked"})
public class LinkedYoRegistry extends LinkedBuffer
{
   private final YoRegistry rootRegistry;
   private final YoRegistryBuffer yoRegistryBuffer;

   private final ReentrantLock lock;
   private final LinkedBufferArray linkedYoVariables = new LinkedBufferArray();
   private final Map<YoVariable, LinkedYoVariable> linkedYoVariableMap = new HashMap<>();

   LinkedYoRegistry(YoRegistry rootRegistry, YoRegistryBuffer yoRegistryBuffer)
   {
      this.rootRegistry = rootRegistry;
      this.yoRegistryBuffer = yoRegistryBuffer;
      lock = yoRegistryBuffer.getLock();
      setup();
   }

   private void setup()
   {
      YoRegistry bufferRootRegistry = yoRegistryBuffer.getRootRegistry().findRegistry(rootRegistry.getNamespace());
      SharedMemoryTools.duplicateMissingYoVariablesInTarget(bufferRootRegistry, rootRegistry, this::setupNewLinkedYoVariable);

      bufferRootRegistry.addListener(new YoRegistryChangedListener()
      {
         @Override
         public void changed(Change change)
         {
            lock.lock();
            try
            {
               if (change.wasVariableAdded())
               {
                  YoVariable newBufferVariable = change.getTargetVariable();
                  YoRegistry registry = SharedMemoryTools.ensurePathExists(rootRegistry, newBufferVariable.getNamespace());
                  if (registry.getVariable(newBufferVariable.getName()) == null)
                     newBufferVariable.duplicate(registry);
                  setupNewLinkedYoVariable(newBufferVariable);
               }

               if (change.wasRegistryAdded())
               {
                  for (YoVariable newBufferVariable : change.getTargetRegistry().collectSubtreeVariables())
                  {
                     YoRegistry registry = SharedMemoryTools.ensurePathExists(rootRegistry, newBufferVariable.getNamespace());
                     if (registry.getVariable(newBufferVariable.getName()) == null)
                        newBufferVariable.duplicate(registry);
                     setupNewLinkedYoVariable(newBufferVariable);
                  }
               }
            }
            finally
            {
               lock.unlock();
            }
         }
      });

      rootRegistry.addListener(new YoRegistryChangedListener()
      {
         @Override
         public void changed(Change change)
         {
            lock.lock();
            try
            {
               if (change.wasVariableAdded())
                  setupNewLinkedYoVariable(change.getTargetVariable());
               if (change.wasRegistryAdded())
                  change.getTargetRegistry().collectSubtreeVariables().forEach(var -> setupNewLinkedYoVariable(var));
            }
            finally
            {
               lock.unlock();
            }
         }
      });
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
   private void setupNewLinkedYoVariable(YoVariable variableToLink)
   {
      if (linkedYoVariableMap.containsKey(variableToLink))
         return;

      YoVariableBuffer yoVariableBuffer = yoRegistryBuffer.findOrCreateYoVariableBuffer(variableToLink);
      LinkedYoVariable newLinkedYoVariable = yoVariableBuffer.newLinkedYoVariable(variableToLink);
      linkedYoVariables.add(newLinkedYoVariable);
      linkedYoVariableMap.put(newLinkedYoVariable.getLinkedYoVariable(), newLinkedYoVariable);
   }

   /** {@inheritDoc} */
   // Operation for the buffer consumers only.
   @Override
   public void push()
   {
      lock.lock();
      try
      {
         linkedYoVariables.push();
      }
      finally
      {
         lock.unlock();
      }
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
   public void push(YoVariable... yoVariablesToPush)
   {
      Arrays.asList(yoVariablesToPush).stream().map(linkedYoVariableMap::get).filter(v -> v != null).forEach(LinkedYoVariable::push);
   }

   /** {@inheritDoc} */
   @Override
   public boolean pull()
   {
      lock.lock();
      try
      {
         return linkedYoVariables.pull();
      }
      finally
      {
         lock.unlock();
      }
   }

   /** {@inheritDoc} */
   // Operation for the buffer manager only.
   @Override
   boolean processPush(boolean writeBuffer)
   {
      lock.lock();
      try
      {
         return linkedYoVariables.processPush(writeBuffer);
      }
      finally
      {
         lock.unlock();
      }
   }

   /** {@inheritDoc} */
   // Operation for the buffer manager only.
   @Override
   void flushPush()
   {
      lock.lock();
      try
      {
         linkedYoVariables.flushPush();
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
         linkedYoVariables.prepareForPull();
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
      lock.lock();

      try
      {
         return linkedYoVariables.hasRequestPending();
      }
      finally
      {
         lock.unlock();
      }
   }

   public YoRegistry getRootRegistry()
   {
      return rootRegistry;
   }
}
