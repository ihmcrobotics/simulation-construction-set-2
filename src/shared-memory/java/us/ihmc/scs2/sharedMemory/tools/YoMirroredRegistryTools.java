package us.ihmc.scs2.sharedMemory.tools;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import us.ihmc.yoVariables.registry.NameSpace;
import us.ihmc.yoVariables.registry.YoVariableRegistry;
import us.ihmc.yoVariables.variable.YoEnum;
import us.ihmc.yoVariables.variable.YoVariable;

public class YoMirroredRegistryTools
{
   public static YoVariableRegistry newEmptyCloneRegistry(YoVariableRegistry original)
   {
      YoVariableRegistry clone = new YoVariableRegistry(original.getName());

      YoVariableRegistry originalParent = original.getParent();
      YoVariableRegistry currentClone = clone;

      while (originalParent != null)
      {
         YoVariableRegistry parentClone = new YoVariableRegistry(originalParent.getName());
         parentClone.addChild(currentClone);
         currentClone = parentClone;
         originalParent = originalParent.getParent();
      }

      return clone;
   }

   public static YoVariableRegistry newRegistryFromNameSpace(String... nameSpace)
   {
      return newRegistryFromNameSpace(new NameSpace(Arrays.asList(nameSpace)));
   }

   public static YoVariableRegistry newRegistryFromNameSpace(NameSpace nameSpace)
   {
      YoVariableRegistry registry = null;

      for (String subName : nameSpace.getSubNames())
      {
         YoVariableRegistry child = new YoVariableRegistry(subName);
         if (registry != null)
            registry.addChild(child);
         registry = child;
      }

      return registry;
   }

   public static int duplicateMissingYoVariablesInTarget(YoVariableRegistry original, YoVariableRegistry target)
   {
      return duplicateMissingYoVariablesInTarget(original, target, yoVariable ->
      {
      });
   }

   public static int duplicateMissingYoVariablesInTarget(YoVariableRegistry original, YoVariableRegistry target, Consumer<YoVariable<?>> newYoVariableConsumer)
   {
      int numberOfYoVariablesCreated = 0;

      // Check for missing variables
      Set<String> targetVariableNames = target.getAllVariablesInThisListOnly().stream().map(YoVariable::getName).collect(Collectors.toSet());

      for (YoVariable<?> originalVariable : original.getAllVariablesInThisListOnly())
      {
         if (!targetVariableNames.contains(originalVariable.getName()))
         { // FIXME YoEnum.duplicate needs to handle this case.
            YoVariable<?> newYoVariable;
            if (originalVariable instanceof YoEnum && !((YoEnum<?>) originalVariable).isBackedByEnum())
            {
               YoEnum<?> originalEnum = (YoEnum<?>) originalVariable;
               newYoVariable = new YoEnum<>(originalEnum.getName(),
                                            originalEnum.getDescription(),
                                            target,
                                            originalEnum.getAllowNullValue(),
                                            originalEnum.getEnumValuesAsString());
            }
            else
            {
               newYoVariable = originalVariable.duplicate(target);
            }
            newYoVariableConsumer.accept(newYoVariable);
            numberOfYoVariablesCreated++;
         }
      }

      // Check for missing registries
      Map<String, YoVariableRegistry> targetChildren = target.getChildren().stream().collect(Collectors.toMap(reg -> reg.getName(), Function.identity()));

      for (YoVariableRegistry originalChild : original.getChildren())
      {
         String childName = originalChild.getName();
         YoVariableRegistry targetChild = targetChildren.get(childName);

         if (targetChild == null)
         {
            targetChild = new YoVariableRegistry(childName);
            target.addChild(targetChild);
         }

         // Recurse down the tree
         numberOfYoVariablesCreated += duplicateMissingYoVariablesInTarget(originalChild, targetChild, newYoVariableConsumer);
      }

      return numberOfYoVariablesCreated;
   }
}
