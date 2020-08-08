package us.ihmc.scs2.sharedMemory.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Random;

import org.junit.jupiter.api.Test;

import us.ihmc.commons.RandomNumbers;
import us.ihmc.yoVariables.registry.YoRegistry;
import us.ihmc.yoVariables.variable.YoVariable;

public class YoMirroredRegistryToolsTest
{
   private static final int ITERATIONS = 100;

   @Test
   public void testDuplicateMissingYoVariablesInTarget()
   {
      Random random = new Random(469);

      for (int i = 0; i < ITERATIONS; i++)
      { // Test with single registry
         int numberOfVariables = RandomNumbers.nextInt(random, 0, 100);
         YoRegistry original = YoRandomTools.nextYoRegistry(random, numberOfVariables);
         YoRegistry target = new YoRegistry(YoRandomTools.nextAlphanumericString(random, 1, 50));
         int numberOfYoVariablesCreated = YoMirroredRegistryTools.duplicateMissingYoVariablesInTarget(original, target);

         assertEquals(numberOfVariables, numberOfYoVariablesCreated);

         for (YoVariable originalVariable : original.getVariables())
         {
            YoVariable targetVariable = target.findVariable(originalVariable.getName());
            assertNotNull(targetVariable);
            assertEquals(originalVariable.getClass(), targetVariable.getClass());
         }
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Test duplicating entire registry tree
         int numberOfVariables = RandomNumbers.nextInt(random, 0, 100);
         YoRegistry originalRoot = YoRandomTools.nextYoRegistryTree(random, numberOfVariables, 50)[0];
         YoRegistry targetRoot = new YoRegistry(originalRoot.getName());
         int numberOfYoVariablesCreated = YoMirroredRegistryTools.duplicateMissingYoVariablesInTarget(originalRoot, targetRoot);

         assertEquals(originalRoot.subtreeVariables().size(), numberOfYoVariablesCreated);

         for (YoRegistry originalRegistry : originalRoot.subtreeRegistries())
         {
            YoRegistry targetRegistry = targetRoot.findRegistry(originalRegistry.getNamespace());
            assertNotNull(targetRegistry);
            assertEquals(originalRegistry.getNumberOfVariables(), targetRegistry.getNumberOfVariables());
         }

         for (YoVariable originalVariable : originalRoot.subtreeVariables())
         {
            YoVariable targetVariable = targetRoot.findVariable(originalVariable.getNamespace().toString(), originalVariable.getName());
            assertNotNull(targetVariable);
            assertEquals(originalVariable.getClass(), targetVariable.getClass());
         }
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Test completing single registry
         int numberOfVariables = RandomNumbers.nextInt(random, 0, 50);
         YoRegistry original = YoRandomTools.nextYoRegistry(random, numberOfVariables);
         YoRegistry target = new YoRegistry(YoRandomTools.nextAlphanumericString(random, 1, 50));
         YoMirroredRegistryTools.duplicateMissingYoVariablesInTarget(original, target);

         int numberOfMissingVariables = RandomNumbers.nextInt(random, 0, 50);
         YoRandomTools.nextYoVariables(random, numberOfMissingVariables, original);

         int numberOfYoVariablesCreated = YoMirroredRegistryTools.duplicateMissingYoVariablesInTarget(original, target);
         assertEquals(numberOfMissingVariables, numberOfYoVariablesCreated);

         for (YoVariable originalVariable : original.getVariables())
         {
            YoVariable targetVariable = target.findVariable(originalVariable.getName());
            assertNotNull(targetVariable);
            assertEquals(originalVariable.getClass(), targetVariable.getClass());
         }
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Test completing registry tree
         int numberOfVariables = RandomNumbers.nextInt(random, 0, 50);
         YoRegistry[] originalRegistries = YoRandomTools.nextYoRegistryTree(random, numberOfVariables, 25);
         YoRegistry originalRoot = originalRegistries[0];
         YoRegistry targetRoot = new YoRegistry(originalRoot.getName());
         YoMirroredRegistryTools.duplicateMissingYoVariablesInTarget(originalRoot, targetRoot);

         int numberOfMissingVariables = 0;

         for (int j = 0; j < 25; j++)
         {
            int n = RandomNumbers.nextInt(random, 0, 50);
            YoRegistry parent = originalRegistries[random.nextInt(originalRegistries.length)];
            YoRegistry registry = YoRandomTools.nextYoRegistry(random, YoRandomTools.nextAvailableRegistryName(random, 1, 50, parent), n);
            parent.addChild(registry);
            numberOfMissingVariables += n;
         }

         int numberOfYoVariablesCreated = YoMirroredRegistryTools.duplicateMissingYoVariablesInTarget(originalRoot, targetRoot);
         assertEquals(numberOfMissingVariables, numberOfYoVariablesCreated);

         for (YoRegistry originalRegistry : originalRoot.subtreeRegistries())
         {
            YoRegistry targetRegistry = targetRoot.findRegistry(originalRegistry.getNamespace());
            assertNotNull(targetRegistry);
            assertEquals(originalRegistry.getNumberOfVariables(), targetRegistry.getNumberOfVariables());
         }

         for (YoVariable originalVariable : originalRoot.subtreeVariables())
         {
            YoVariable targetVariable = targetRoot.findVariable(originalVariable.getNamespace().toString(), originalVariable.getName());
            assertNotNull(targetVariable);
            assertEquals(originalVariable.getClass(), targetVariable.getClass());
         }
      }
   }
}
