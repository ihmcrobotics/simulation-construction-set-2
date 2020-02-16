package us.ihmc.scs2.sharedMemory;

import java.util.*;

import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;
import us.ihmc.scs2.sharedMemory.tools.YoMirroredRegistryTools;
import us.ihmc.yoVariables.registry.NameSpace;
import us.ihmc.yoVariables.registry.YoVariableRegistry;
import us.ihmc.yoVariables.variable.YoVariable;

public class YoVariableRegistryBuffer
{
   private final YoVariableRegistry rootRegistry;
   private final List<YoVariableBuffer<?>> yoVariableBuffers = new ArrayList<>();
   private final Map<String, YoVariableBuffer<?>> yoVariableFullnameToBufferMap = new HashMap<>();
   private final YoBufferPropertiesReadOnly properties;

   public YoVariableRegistryBuffer(YoVariableRegistry rootRegistry, YoBufferPropertiesReadOnly properties)
   {
      this.rootRegistry = rootRegistry;
      this.properties = properties;
      registerMissingBuffers();
   }

   public void registerMissingBuffers()
   {
      List<YoVariable<?>> allYoVariables = rootRegistry.getAllVariables();

      if (allYoVariables.size() != yoVariableBuffers.size())
      {
         for (YoVariable<?> yoVariable : allYoVariables)
         {
            String fullName = yoVariable.getFullNameWithNameSpace();
            if (yoVariableFullnameToBufferMap.containsKey(fullName))
               continue;

            YoVariableBuffer<?> yoVariableBuffer = YoVariableBuffer.newYoVariableBuffer(yoVariable, properties);
            yoVariableBuffer.resizeBuffer(0, properties.getSize());
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
      // FIXME Hack to get the writing faster.
      yoVariableBuffers.parallelStream().forEach(buffer -> buffer.writeBuffer());
   }

   public void readBuffer()
   {
      yoVariableBuffers.forEach(buffer -> buffer.readBuffer());
   }

   YoVariableBuffer<?> findYoVariableBuffer(YoVariable<?> yoVariable)
   {
      return yoVariableFullnameToBufferMap.get(yoVariable.getFullNameWithNameSpace());
   }

   YoVariableBuffer<?> findOrCreateYoVariableBuffer(YoVariable<?> yoVariable)
   {
      String variableFullName = yoVariable.getFullNameWithNameSpace();
      YoVariableBuffer<?> yoVariableBuffer = yoVariableFullnameToBufferMap.get(variableFullName);

      if (yoVariableBuffer == null)
      {
         NameSpace yoVariableNameSpace = new NameSpace(variableFullName);
         YoVariableRegistry registry = ensurePathExists(rootRegistry, yoVariableNameSpace.getParent());
         Optional<YoVariable<?>> duplicateOptional = registry.getAllVariables().stream().filter(v -> v.getFullNameWithNameSpace().equals(variableFullName))
                                                             .findFirst();
         YoVariable<?> duplicate;
         if (duplicateOptional.isPresent())
            duplicate = duplicateOptional.get();
         else
            duplicate = yoVariable.duplicate(registry);

         yoVariableBuffer = YoVariableBuffer.newYoVariableBuffer(duplicate, properties);
         yoVariableBuffer.resizeBuffer(0, properties.getSize());
         yoVariableBuffers.add(yoVariableBuffer);
         yoVariableFullnameToBufferMap.put(variableFullName, yoVariableBuffer);
      }

      return yoVariableBuffer;
   }

   private static YoVariableRegistry ensurePathExists(YoVariableRegistry rootRegistry, NameSpace registryNamespace)
   {
      if (!rootRegistry.getName().equals(registryNamespace.getRootName()))
         return null;

      List<String> subNames = registryNamespace.getSubNames();
      YoVariableRegistry currentRegistry = rootRegistry;

      for (String subName : subNames.subList(1, subNames.size()))
      {
         YoVariableRegistry childRegistry = currentRegistry.getChildren().stream().filter(r -> r.getName().equals(subName)).findFirst().orElse(null);
         if (childRegistry == null)
         {
            childRegistry = new YoVariableRegistry(subName);
            currentRegistry.addChild(childRegistry);
         }

         currentRegistry = childRegistry;
      }

      return currentRegistry;
   }

   LinkedYoVariableRegistry newLinkedYoVariableRegistry(YoVariableRegistry registryToLink)
   {
      YoVariableRegistry bufferRegistry = rootRegistry.getRegistry(registryToLink.getNameSpace());
      YoMirroredRegistryTools.duplicateMissingYoVariablesInTarget(registryToLink, bufferRegistry);
      registerMissingBuffers();

      return new LinkedYoVariableRegistry(registryToLink, this);
   }

   LinkedYoVariableRegistry newLinkedYoVariableRegistry()
   {
      YoVariableRegistry linkedRegistry = new YoVariableRegistry(rootRegistry.getName());
      YoMirroredRegistryTools.duplicateMissingYoVariablesInTarget(rootRegistry, linkedRegistry);
      registerMissingBuffers();

      return new LinkedYoVariableRegistry(linkedRegistry, this);
   }

   public YoVariableRegistry getRootRegistry()
   {
      return rootRegistry;
   }
}
