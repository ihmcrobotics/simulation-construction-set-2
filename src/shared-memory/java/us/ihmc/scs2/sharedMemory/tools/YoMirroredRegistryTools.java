package us.ihmc.scs2.sharedMemory.tools;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import us.ihmc.yoVariables.registry.NameSpace;
import us.ihmc.yoVariables.registry.YoRegistry;
import us.ihmc.yoVariables.variable.YoEnum;
import us.ihmc.yoVariables.variable.YoVariable;

public class YoMirroredRegistryTools
{
   public static YoRegistry newEmptyCloneRegistry(YoRegistry original)
   {
      YoRegistry clone = new YoRegistry(original.getName());

      YoRegistry originalParent = original.getParent();
      YoRegistry currentClone = clone;

      while (originalParent != null)
      {
         YoRegistry parentClone = new YoRegistry(originalParent.getName());
         parentClone.addChild(currentClone);
         currentClone = parentClone;
         originalParent = originalParent.getParent();
      }

      return clone;
   }

   public static YoRegistry newRegistryFromNameSpace(String... nameSpace)
   {
      return newRegistryFromNameSpace(new NameSpace(Arrays.asList(nameSpace)));
   }

   public static YoRegistry newRegistryFromNameSpace(NameSpace nameSpace)
   {
      YoRegistry registry = null;

      for (String subName : nameSpace.getSubNames())
      {
         YoRegistry child = new YoRegistry(subName);
         if (registry != null)
            registry.addChild(child);
         registry = child;
      }

      return registry;
   }

   public static int duplicateMissingYoVariablesInTarget(YoRegistry original, YoRegistry target)
   {
      return duplicateMissingYoVariablesInTarget(original, target, yoVariable ->
      {
      });
   }

   public static int duplicateMissingYoVariablesInTarget(YoRegistry original, YoRegistry target, Consumer<YoVariable> newYoVariableConsumer)
   {
      int numberOfYoVariablesCreated = 0;

      // Check for missing variables
      Set<String> targetVariableNames = target.getVariables().stream().map(YoVariable::getName).collect(Collectors.toSet());

      for (YoVariable originalVariable : original.getVariables())
      {
         if (!targetVariableNames.contains(originalVariable.getName()))
         { // FIXME YoEnum.duplicate needs to handle this case.
            YoVariable newYoVariable;
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
      Map<String, YoRegistry> targetChildren = target.getChildren().stream().collect(Collectors.toMap(reg -> reg.getName(), Function.identity()));

      for (YoRegistry originalChild : original.getChildren())
      {
         String childName = originalChild.getName();
         YoRegistry targetChild = targetChildren.get(childName);

         if (targetChild == null)
         {
            targetChild = new YoRegistry(childName);
            target.addChild(targetChild);
         }

         // Recurse down the tree
         numberOfYoVariablesCreated += duplicateMissingYoVariablesInTarget(originalChild, targetChild, newYoVariableConsumer);
      }

      return numberOfYoVariablesCreated;
   }
}
