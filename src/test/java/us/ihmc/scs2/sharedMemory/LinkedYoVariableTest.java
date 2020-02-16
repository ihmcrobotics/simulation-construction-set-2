package us.ihmc.scs2.sharedMemory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.Test;

import us.ihmc.scs2.sharedMemory.tools.YoBufferRandomTools;
import us.ihmc.scs2.sharedMemory.tools.YoRandomTools;
import us.ihmc.yoVariables.variable.YoVariable;

public abstract class LinkedYoVariableTest<T extends YoVariable<T>>
{
   protected static final int ITERATIONS = 1000;

   abstract T copy(T original);

   abstract T nextYoVariable(Random random);

   @SuppressWarnings("unchecked")
   @Test
   public void testPullSequence()
   {
      Random random = new Random(8907543);

      for (int i = 0; i < ITERATIONS; i++)
      {
         T linkedVariable = nextYoVariable(random);
         YoVariableBuffer<T> buffer = (YoVariableBuffer<T>) YoBufferRandomTools.nextYoVariableBuffer(random, nextYoVariable(random));
         LinkedYoVariable<T> linkedYoBoolean = (LinkedYoVariable<T>) LinkedYoVariable.newLinkedYoVariable(linkedVariable, buffer);

         // Single prepare-pull happens before user actually grabs an update.
         T prePullLinkedValue = copy(linkedVariable);
         T prePullBufferVariableValue = copy(buffer.getYoVariable());
         linkedYoBoolean.prepareForPull(); // Called from the buffer manager, internally stores the buffer's variable's value to be pulled later from the user thread.
         assertYoEquals(prePullLinkedValue, linkedVariable);
         assertYoEquals(prePullBufferVariableValue, buffer.getYoVariable());
         YoRandomTools.randomizeYoVariable(random, buffer.getYoVariable()); // Simulates that the buffer manager is pursuing its changes on the buffer before the user has pulled its update.
         linkedYoBoolean.pull(); // The user is actually grabbing an update.
         assertYoEquals(linkedVariable, prePullBufferVariableValue);

         // Multiple prepare-pulls happen before user actually grabs an update.
         prePullLinkedValue = linkedVariable;
         prePullBufferVariableValue = buffer.getYoVariable();

         T expectedUpdate = copy(prePullBufferVariableValue);

         for (int j = 0; j < random.nextInt(20) + 2; j++)
         {
            YoRandomTools.randomizeYoVariable(random, buffer.getYoVariable());
            linkedYoBoolean.prepareForPull();
            expectedUpdate = copy(buffer.getYoVariable());
         }

         linkedYoBoolean.pull(); // The user is actually grabbing an update.
         assertYoEquals(linkedVariable, expectedUpdate);

         // No prepare-pull, the user's variable remains constant regardless of what changes are being done on the buffer.
         prePullLinkedValue = copy(linkedVariable);
         for (int j = 0; j < random.nextInt(20) + 1; j++)
         {
            YoRandomTools.randomizeYoVariable(random, buffer.getYoVariable());
            linkedYoBoolean.pull();
            assertYoEquals(linkedVariable, prePullLinkedValue);
         }
      }
   }

   private static <T extends YoVariable<T>> void assertYoEquals(T expected, T actual)
   {
      assertEquals(expected.getValueAsLongBits(), actual.getValueAsLongBits());
   }
}
