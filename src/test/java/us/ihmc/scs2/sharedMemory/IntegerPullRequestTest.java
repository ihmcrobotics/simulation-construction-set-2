package us.ihmc.scs2.sharedMemory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import us.ihmc.scs2.sharedMemory.tools.YoRandomTools;
import us.ihmc.yoVariables.registry.YoVariableRegistry;
import us.ihmc.yoVariables.variable.YoInteger;
import us.ihmc.yoVariables.variable.YoVariable;

public class IntegerPullRequestTest
{
   private static final int ITERATIONS = 1000;

   @BeforeAll
   public static void disableStackTrace()
   {
      YoVariable.SAVE_STACK_TRACE = false;
   }

   @Test
   public void test()
   {
      Random random = new Random(453465);
      
      for (int i = 0; i < ITERATIONS; i++)
      {
         YoInteger variableToUpdate = YoRandomTools.nextYoInteger(random, new YoVariableRegistry("Dummy"));
         int initialValue = variableToUpdate.getValue();
         int valueToPull = random.nextInt(1000000);
         IntegerPullRequest pullRequest = new IntegerPullRequest(variableToUpdate, valueToPull);

         assertEquals(initialValue, variableToUpdate.getValue());
         assertEquals(valueToPull, pullRequest.getValueToPull());
         pullRequest.pull();
         assertEquals(valueToPull, variableToUpdate.getValue());
      }
   }

}
