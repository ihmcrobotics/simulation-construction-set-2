package us.ihmc.scs2.sharedMemory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

import us.ihmc.log.LogTools;
import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;
import us.ihmc.scs2.sharedMemory.tools.SharedMemoryTools;
import us.ihmc.yoVariables.listener.YoRegistryChangedListener;
import us.ihmc.yoVariables.registry.YoNamespace;
import us.ihmc.yoVariables.registry.YoRegistry;
import us.ihmc.yoVariables.variable.YoVariable;

public class YoRegistryBuffer
{
   private final YoRegistry rootRegistry;
   private final List<YoVariableBuffer<?>> yoVariableBuffers = new ArrayList<>();
   private final Map<String, YoVariableBuffer<?>> yoVariableFullnameToBufferMap = new HashMap<>();
   private final YoBufferPropertiesReadOnly properties;

   private final ReentrantLock lock = new ReentrantLock();

   public YoRegistryBuffer(YoRegistry rootRegistry, YoBufferPropertiesReadOnly properties)
   {
      this.rootRegistry = rootRegistry;
      this.properties = properties;
      registerMissingBuffers();

      this.rootRegistry.addListener(new YoRegistryChangedListener()
      {
         @Override
         public void changed(Change change)
         {
            if (change.wasVariableAdded())
               registerNewYoVariable(change.getTargetVariable(), true);
         }
      });
   }

   public void registerMissingBuffers()
   {
      List<YoVariable> allYoVariables = rootRegistry.collectSubtreeVariables();

      if (allYoVariables.size() != yoVariableBuffers.size())
      {
         for (YoVariable yoVariable : allYoVariables)
         {
            registerNewYoVariable(yoVariable, false);
         }
      }
   }

   private void registerNewYoVariable(YoVariable yoVariable, boolean printNameCollisionWarning)
   {
      String fullName = yoVariable.getFullNameString();

      if (yoVariableFullnameToBufferMap.containsKey(fullName))
      {
         if (printNameCollisionWarning)
            LogTools.warn("Name collision while trying to register new YoVariable: " + fullName);
         return;
      }

      YoVariableBuffer<?> yoVariableBuffer = YoVariableBuffer.newYoVariableBuffer(yoVariable, properties);
      yoVariableBuffers.add(yoVariableBuffer);
      yoVariableFullnameToBufferMap.put(fullName, yoVariableBuffer);
   }

   public void resizeBuffer(int from, int length)
   {
      yoVariableBuffers.parallelStream().forEach(buffer -> buffer.resizeBuffer(from, length));
   }

   public void fillBuffer(boolean zeroFill, int from, int length)
   {
      if (length <= 0)
         return;

      yoVariableBuffers.forEach(buffer -> buffer.fillBuffer(zeroFill, from, length));
   }

   public void writeBuffer()
   {
      writeBufferAt(properties.getCurrentIndex());
   }

   public void writeBufferAt(int index)
   {
      // FIXME Hack to get the writing faster.
      yoVariableBuffers.parallelStream().forEach(buffer -> buffer.writeBufferAt(index));
   }

   public void readBuffer()
   {
      readBufferAt(properties.getCurrentIndex());
   }

   public void readBufferAt(int index)
   {
      yoVariableBuffers.forEach(buffer -> buffer.readBufferAt(index));
   }

   public List<YoVariableBuffer<?>> getYoVariableBuffers()
   {
      return yoVariableBuffers;
   }

   public YoVariableBuffer<?> findYoVariableBuffer(YoVariable yoVariable)
   {
      return yoVariableFullnameToBufferMap.get(yoVariable.getFullNameString());
   }

   public YoVariableBuffer<?> findOrCreateYoVariableBuffer(YoVariable yoVariable)
   {
      String variableFullName = yoVariable.getFullNameString();
      YoVariableBuffer<?> yoVariableBuffer = yoVariableFullnameToBufferMap.get(variableFullName);

      if (yoVariableBuffer == null)
      {
         YoNamespace yoVariableNamespace = new YoNamespace(variableFullName);
         YoRegistry registry = SharedMemoryTools.ensurePathExists(rootRegistry, yoVariableNamespace.getParent());
         Optional<YoVariable> duplicateOptional = registry.collectSubtreeVariables().stream().filter(v -> v.getFullNameString().equals(variableFullName))
                                                          .findFirst();
         YoVariable duplicate;
         if (duplicateOptional.isPresent())
            duplicate = duplicateOptional.get();
         else
            duplicate = yoVariable.duplicate(registry);

         yoVariableBuffer = YoVariableBuffer.newYoVariableBuffer(duplicate, properties);
         yoVariableBuffers.add(yoVariableBuffer);
         yoVariableFullnameToBufferMap.put(variableFullName, yoVariableBuffer);
      }

      return yoVariableBuffer;
   }

   LinkedYoRegistry newLinkedYoRegistry(YoRegistry registryToLink)
   {
      return new LinkedYoRegistry(registryToLink, this);
   }

   LinkedYoRegistry newLinkedYoRegistry()
   {
      return new LinkedYoRegistry(new YoRegistry(rootRegistry.getName()), this);
   }

   ReentrantLock getLock()
   {
      return lock;
   }

   public YoRegistry getRootRegistry()
   {
      return rootRegistry;
   }

   public YoBufferPropertiesReadOnly getProperties()
   {
      return properties;
   }
}
