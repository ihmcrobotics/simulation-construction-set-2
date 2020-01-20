package us.ihmc.scs2.sharedMemory.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import us.ihmc.commons.RandomNumbers;
import us.ihmc.yoVariables.registry.YoVariableRegistry;
import us.ihmc.yoVariables.variable.YoVariable;

public class YoMirroredRegistryToolsTest
{
   private static final int ITERATIONS = 100;

   @BeforeAll
   private static void disableStacktrace()
   {
      YoVariable.SAVE_STACK_TRACE = false;
   }

   @Test
   public void testDuplicateMissingYoVariablesInTarget()
   {
      Random random = new Random(469);

      for (int i = 0; i < ITERATIONS; i++)
      { // Test with single registry
         int numberOfVariables = RandomNumbers.nextInt(random, 0, 500);
         YoVariableRegistry original = YoRandomTools.nextYoVariableRegistry(random, numberOfVariables);
         YoVariableRegistry target = new YoVariableRegistry(YoRandomTools.nextAlphanumericString(random, 1, 50));
         int numberOfYoVariablesCreated = YoMirroredRegistryTools.duplicateMissingYoVariablesInTarget(original, target);

         assertEquals(numberOfVariables, numberOfYoVariablesCreated);

         for (YoVariable<?> originalVariable : original.getAllVariablesInThisListOnly())
         {
            YoVariable<?> targetVariable = target.getVariable(originalVariable.getName());
            assertNotNull(targetVariable);
            assertEquals(originalVariable.getName(), targetVariable.getName());
         }
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Test duplicating entire registry tree
         int numberOfVariables = RandomNumbers.nextInt(random, 0, 500);
         YoVariableRegistry originalRoot = YoRandomTools.nextYoVariableRegistryTree(random, numberOfVariables, 50)[0];
         YoVariableRegistry targetRoot = new YoVariableRegistry(originalRoot.getName());
         int numberOfYoVariablesCreated = YoMirroredRegistryTools.duplicateMissingYoVariablesInTarget(originalRoot, targetRoot);

         assertEquals(originalRoot.getAllVariables().size(), numberOfYoVariablesCreated);

         for (YoVariableRegistry originalRegistry : originalRoot.getAllRegistriesIncludingChildren())
         {
            YoVariableRegistry targetRegistry = targetRoot.getRegistry(originalRegistry.getNameSpace());
            assertNotNull(targetRegistry);
            assertEquals(originalRegistry.getNumberOfYoVariables(), targetRegistry.getNumberOfYoVariables());
         }

         for (YoVariable<?> originalVariable : originalRoot.getAllVariables())
         {
            YoVariable<?> targetVariable = targetRoot.getVariable(originalVariable.getNameSpace().toString(), originalVariable.getName());
            assertNotNull(targetVariable);
         }
      }


      for (int i = 0; i < ITERATIONS; i++)
      { // Test completing single registry
         int numberOfVariables = RandomNumbers.nextInt(random, 0, 250);
         YoVariableRegistry original = YoRandomTools.nextYoVariableRegistry(random, numberOfVariables);
         YoVariableRegistry target = new YoVariableRegistry(YoRandomTools.nextAlphanumericString(random, 1, 50));
         YoMirroredRegistryTools.duplicateMissingYoVariablesInTarget(original, target);

         int numberOfMissingVariables = RandomNumbers.nextInt(random, 0, 250);
         YoRandomTools.nextYoVariables(random, numberOfMissingVariables, original);

         int numberOfYoVariablesCreated = YoMirroredRegistryTools.duplicateMissingYoVariablesInTarget(original, target);
         assertEquals(numberOfMissingVariables, numberOfYoVariablesCreated);

         for (YoVariable<?> originalVariable : original.getAllVariablesInThisListOnly())
         {
            YoVariable<?> targetVariable = target.getVariable(originalVariable.getName());
            assertNotNull(targetVariable);
            assertEquals(originalVariable.getName(), targetVariable.getName());
         }
      }

   }

}
