package us.ihmc.scs2.sharedMemory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import us.ihmc.scs2.sharedMemory.tools.YoRandomTools;
import us.ihmc.yoVariables.registry.YoVariableRegistry;
import us.ihmc.yoVariables.variable.YoEnum;
import us.ihmc.yoVariables.variable.YoVariable;

public class EnumPullRequestTest
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
         YoEnum<?> variableToUpdate = YoRandomTools.nextYoEnum(random, new YoVariableRegistry("Dummy"));
         int initialValue = variableToUpdate.getOrdinal();
         int valueToPull = random.nextInt(variableToUpdate.getEnumSize());
         EnumPullRequest<?> pullRequest = new EnumPullRequest<>(variableToUpdate, valueToPull);

         assertEquals(initialValue, variableToUpdate.getOrdinal());
         assertEquals(valueToPull, pullRequest.getValueToPull());
         pullRequest.pull();
         assertEquals(valueToPull, variableToUpdate.getOrdinal());
      }
   }

}
