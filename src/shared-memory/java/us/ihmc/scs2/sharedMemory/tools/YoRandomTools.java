package us.ihmc.scs2.sharedMemory.tools;

import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.mutable.MutableObject;

import us.ihmc.commons.RandomNumbers;
import us.ihmc.euclid.tools.EuclidCoreRandomTools;
import us.ihmc.yoVariables.registry.YoVariableRegistry;
import us.ihmc.yoVariables.variable.YoBoolean;
import us.ihmc.yoVariables.variable.YoDouble;
import us.ihmc.yoVariables.variable.YoEnum;
import us.ihmc.yoVariables.variable.YoInteger;
import us.ihmc.yoVariables.variable.YoLong;
import us.ihmc.yoVariables.variable.YoVariable;

public class YoRandomTools
{
   public enum EnumTypeA
   {
      A1, A2, A4, A5
   };

   public enum EnumTypeB
   {
      B1, B2, B4
   };

   public enum EnumTypeC
   {
      C1, C2
   };

   public enum EnumTypeD
   {
      D1
   };

   public static Class<? extends Enum<?>> nextEnumType(Random random)
   {
      switch (random.nextInt(4))
      {
         case 0:
            return EnumTypeA.class;
         case 1:
            return EnumTypeB.class;
         case 2:
            return EnumTypeC.class;
         case 3:
            return EnumTypeD.class;
         default:
            throw new IllegalStateException("Should not reached this state.");
      }
   }

   public static String nextString(Random random, int minLengthInclusive, int maxLengthInclusive)
   {
      int length = RandomNumbers.nextInt(random, minLengthInclusive, maxLengthInclusive);
      return RandomStringUtils.random(length, 0, 0, false, false, null, random);
   }

   public static String nextAlphanumericString(Random random, int minLengthInclusive, int maxLengthInclusive)
   {
      int length = RandomNumbers.nextInt(random, minLengthInclusive, maxLengthInclusive);
      return RandomStringUtils.random(length, 0, 0, true, true, null, random);
   }

   public static String nextAvailableVariableName(Random random, int minLengthInclusive, int maxLengthInclusive, YoVariableRegistry registry)
   {
      String next = nextAlphanumericString(random, minLengthInclusive, maxLengthInclusive);
      while (registry.getVariable(next) != null)
         next = nextAlphanumericString(random, minLengthInclusive, maxLengthInclusive);
      return next;
   }

   public static String nextAvailableRegistryName(Random random, int minLengthInclusive, int maxLengthInclusive, YoVariableRegistry registry)
   {
      MutableObject<String> next = new MutableObject<String>(nextAlphanumericString(random, minLengthInclusive, maxLengthInclusive));
      while (registry.getChildren().stream().anyMatch(child -> child.getName().equals(next.getValue())))
         next.setValue(nextAlphanumericString(random, minLengthInclusive, maxLengthInclusive));
      return next.getValue();
   }

   public static YoVariable<?> nextYoVariable(Random random, YoVariableRegistry registry)
   {
      switch (random.nextInt(5))
      {
         case 0:
            return nextYoBoolean(random, registry);
         case 1:
            return nextYoDouble(random, registry);
         case 2:
            return nextYoInteger(random, registry);
         case 3:
            return nextYoLong(random, registry);
         case 4:
            return nextYoEnum(random, registry);
         default:
            throw new IllegalStateException("Should not reached this state.");
      }
   }

   public static YoVariable<?> nextYoVariable(Random random, String name, YoVariableRegistry registry)
   {
      switch (random.nextInt(5))
      {
         case 0:
            return nextYoBoolean(random, name, registry);
         case 1:
            return nextYoDouble(random, name, registry);
         case 2:
            return nextYoInteger(random, name, registry);
         case 3:
            return nextYoLong(random, name, registry);
         case 4:
            return nextYoEnum(random, name, registry);
         default:
            throw new IllegalStateException("Should not reached this state.");
      }
   }

   public static YoVariable<?>[] nextYoVariables(Random random, int numberOfVariables, YoVariableRegistry registry)
   {
      YoVariable<?>[] next = new YoVariable[numberOfVariables];
      for (int i = 0; i < numberOfVariables; i++)
         next[i] = nextYoVariable(random, registry);
      return next;
   }

   public static YoVariable<?>[] nextYoVariables(Random random, String namePrefix, int numberOfVariables, YoVariableRegistry registry)
   {
      YoVariable<?>[] next = new YoVariable[numberOfVariables];
      for (int i = 0; i < numberOfVariables; i++)
         next[i] = nextYoVariable(random, namePrefix + i, registry);
      return next;
   }

   public static YoBoolean nextYoBoolean(Random random, YoVariableRegistry registry)
   {
      return nextYoBoolean(random, nextAvailableVariableName(random, 1, 50, registry), registry);
   }

   public static YoBoolean nextYoBoolean(Random random, String name, YoVariableRegistry registry)
   {
      String description = random.nextBoolean() ? null : nextString(random, 0, 50);
      YoBoolean next = new YoBoolean(name, description, registry);
      randomizeYoBoolean(random, next);
      return next;
   }

   public static YoDouble nextYoDouble(Random random, YoVariableRegistry registry)
   {
      return nextYoDouble(random, nextAvailableVariableName(random, 1, 50, registry), registry);
   }

   public static YoDouble nextYoDouble(Random random, String name, YoVariableRegistry registry)
   {
      String description = random.nextBoolean() ? null : nextString(random, 0, 50);
      YoDouble next = new YoDouble(name, description, registry);
      randomizeYoDouble(random, next);
      return next;
   }

   public static YoInteger nextYoInteger(Random random, YoVariableRegistry registry)
   {
      return nextYoInteger(random, nextAvailableVariableName(random, 1, 50, registry), registry);
   }

   public static YoInteger nextYoInteger(Random random, String name, YoVariableRegistry registry)
   {
      String description = random.nextBoolean() ? null : nextString(random, 0, 50);
      YoInteger next = new YoInteger(name, description, registry);
      randomizeYoInteger(random, next);
      return next;
   }

   public static YoLong nextYoLong(Random random, YoVariableRegistry registry)
   {
      return nextYoLong(random, nextAvailableVariableName(random, 1, 50, registry), registry);
   }

   public static YoLong nextYoLong(Random random, String name, YoVariableRegistry registry)
   {
      String description = random.nextBoolean() ? null : nextString(random, 0, 50);
      YoLong next = new YoLong(name, description, registry);
      randomizeYoLong(random, next);
      return next;
   }

   public static <E extends Enum<E>> YoEnum<E> nextYoEnum(Random random, YoVariableRegistry registry)
   {
      return nextYoEnum(random, nextAvailableVariableName(random, 1, 50, registry), registry);
   }

   public static <E extends Enum<E>> YoEnum<E> nextYoEnum(Random random, Class<E> enumType, YoVariableRegistry registry)
   {
      return nextYoEnum(random, nextAvailableVariableName(random, 1, 50, registry), enumType, registry);
   }

   @SuppressWarnings("unchecked")
   public static <E extends Enum<E>> YoEnum<E> nextYoEnum(Random random, String name, YoVariableRegistry registry)
   {
      return nextYoEnum(random, name, (Class<E>) nextEnumType(random), registry);
   }

   public static <E extends Enum<E>> YoEnum<E> nextYoEnum(Random random, String name, Class<E> enumType, YoVariableRegistry registry)
   {
      String description = random.nextBoolean() ? null : nextString(random, 0, 50);
      YoEnum<E> next = new YoEnum<>(name, description, registry, enumType, random.nextBoolean());
      randomizeYoEnum(random, next);
      return next;
   }

   public static void randomizeYoVariable(Random random, YoVariable<?> yoVariable)
   {
      if (yoVariable instanceof YoBoolean)
         randomizeYoBoolean(random, (YoBoolean) yoVariable);
      else if (yoVariable instanceof YoDouble)
         randomizeYoDouble(random, (YoDouble) yoVariable);
      else if (yoVariable instanceof YoInteger)
         randomizeYoInteger(random, (YoInteger) yoVariable);
      else if (yoVariable instanceof YoLong)
         randomizeYoLong(random, (YoLong) yoVariable);
      else if (yoVariable instanceof YoEnum)
         randomizeYoEnum(random, (YoEnum<?>) yoVariable);
   }

   public static void randomizeYoBoolean(Random random, YoBoolean yoBoolean)
   {
      yoBoolean.set(random.nextBoolean());
   }

   public static void randomizeYoDouble(Random random, YoDouble yoDouble)
   {
      yoDouble.set(EuclidCoreRandomTools.nextDouble(random, 1000.0));
   }

   public static void randomizeYoInteger(Random random, YoInteger yoInteger)
   {
      yoInteger.set(RandomNumbers.nextInt(random, -100000, 100000));
   }

   public static void randomizeYoLong(Random random, YoLong yoLong)
   {
      yoLong.set(RandomNumbers.nextInt(random, -100000, 100000));
   }

   public static void randomizeYoEnum(Random random, YoEnum<?> yoEnum)
   {
      if (yoEnum.getAllowNullValue())
         yoEnum.set(random.nextInt(yoEnum.getEnumSize() + 1) - 1);
      else
         yoEnum.set(random.nextInt(yoEnum.getEnumSize()));
   }

   public static YoVariableRegistry nextYoVariableRegistry(Random random, int numberOfVariables)
   {
      return nextYoVariableRegistry(random, nextAlphanumericString(random, 1, 50), numberOfVariables);
   }

   public static YoVariableRegistry nextYoVariableRegistry(Random random, String name, int numberOfVariables)
   {
      YoVariableRegistry next = new YoVariableRegistry(name);
      nextYoVariables(random, name, numberOfVariables, next);
      return next;
   }

   public static YoVariableRegistry[] nextYoVariableRegistryChain(Random random, int maxNumberOfVariablesPerRegistry, int numberOfRegistries)
   {
      return nextYoVariableRegistryChain(random, nextAlphanumericString(random, 1, 50), maxNumberOfVariablesPerRegistry, numberOfRegistries);
   }

   public static YoVariableRegistry[] nextYoVariableRegistryChain(Random random, String namePrefix, int maxNumberOfVariablesPerRegistry, int numberOfRegistries)
   {
      YoVariableRegistry root = nextYoVariableRegistry(random, namePrefix + "0", random.nextInt(maxNumberOfVariablesPerRegistry + 1));
      YoVariableRegistry[] registries = new YoVariableRegistry[numberOfRegistries];
      registries[0] = root;

      for (int i = 1; i < numberOfRegistries; i++)
      {
         YoVariableRegistry next = nextYoVariableRegistry(random, namePrefix + i, random.nextInt(maxNumberOfVariablesPerRegistry + 1));
         registries[i] = next;
         registries[i - 1].addChild(next);
      }

      return registries;
   }

   public static YoVariableRegistry[] nextYoVariableRegistryTree(Random random, int maxNumberOfVariablesPerRegistry, int numberOfRegistries)
   {
      return nextYoVariableRegistryTree(random, nextAlphanumericString(random, 1, 50), maxNumberOfVariablesPerRegistry, numberOfRegistries);
   }

   public static YoVariableRegistry[] nextYoVariableRegistryTree(Random random, String namePrefix, int maxNumberOfVariablesPerRegistry, int numberOfRegistries)
   {
      YoVariableRegistry root = nextYoVariableRegistry(random, namePrefix + "0", random.nextInt(maxNumberOfVariablesPerRegistry + 1));
      YoVariableRegistry[] registries = new YoVariableRegistry[numberOfRegistries];
      registries[0] = root;

      for (int i = 1; i < numberOfRegistries; i++)
      {
         YoVariableRegistry next = nextYoVariableRegistry(random, namePrefix + i, random.nextInt(maxNumberOfVariablesPerRegistry + 1));
         registries[i] = next;
         registries[random.nextInt(i)].addChild(next);
      }

      return registries;
   }
}
