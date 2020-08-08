package us.ihmc.scs2.sharedMemory;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;
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
   }

   public void registerMissingBuffers()
   {
      List<YoVariable> allYoVariables = rootRegistry.subtreeVariables();

      if (allYoVariables.size() != yoVariableBuffers.size())
      {
         for (YoVariable yoVariable : allYoVariables)
         {
            String fullName = yoVariable.getFullNameString();
            if (yoVariableFullnameToBufferMap.containsKey(fullName))
               continue;

            YoVariableBuffer<?> yoVariableBuffer = YoVariableBuffer.newYoVariableBuffer(yoVariable, properties);
            yoVariableBuffers.add(yoVariableBuffer);
            yoVariableFullnameToBufferMap.put(fullName, yoVariableBuffer);
         }
      }
   }

   public void resizeBuffer(int from, int length)
   {
      yoVariableBuffers.parallelStream().forEach(buffer -> buffer.resizeBuffer(from, length));
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

   YoVariableBuffer<?> findYoVariableBuffer(YoVariable yoVariable)
   {
      return yoVariableFullnameToBufferMap.get(yoVariable.getFullNameString());
   }

   YoVariableBuffer<?> findOrCreateYoVariableBuffer(YoVariable yoVariable)
   {
      String variableFullName = yoVariable.getFullNameString();
      YoVariableBuffer<?> yoVariableBuffer = yoVariableFullnameToBufferMap.get(variableFullName);

      if (yoVariableBuffer == null)
      {
         YoNamespace yoVariableNamespace = new YoNamespace(variableFullName);
         YoRegistry registry = ensurePathExists(rootRegistry, yoVariableNamespace.getParent());
         Optional<YoVariable> duplicateOptional = registry.subtreeVariables().stream().filter(v -> v.getFullNameString().equals(variableFullName))
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

   private static YoRegistry ensurePathExists(YoRegistry rootRegistry, YoNamespace registryNamespace)
   {
      if (!rootRegistry.getName().equals(registryNamespace.getRootName()))
         return null;

      List<String> subNames = registryNamespace.getSubNames();
      YoRegistry currentRegistry = rootRegistry;

      for (String subName : subNames.subList(1, subNames.size()))
      {
         YoRegistry childRegistry = currentRegistry.getChildren().stream().filter(r -> r.getName().equals(subName)).findFirst().orElse(null);
         if (childRegistry == null)
         {
            childRegistry = new YoRegistry(subName);
            currentRegistry.addChild(childRegistry);
         }

         currentRegistry = childRegistry;
      }

      return currentRegistry;
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
