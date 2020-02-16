package us.ihmc.scs2.sharedMemory.interfaces;

import us.ihmc.scs2.sharedMemory.*;
import us.ihmc.yoVariables.registry.YoVariableRegistry;
import us.ihmc.yoVariables.variable.*;

public interface LinkedYoVariableFactory
{
   LinkedBufferProperties newLinkedBufferProperties();

   LinkedYoVariableRegistry newLinkedYoVariableRegistry();

   LinkedYoVariableRegistry newLinkedYoVariableRegistry(YoVariableRegistry registryToLink);

   LinkedYoVariable<?> newLinkedYoVariable(YoVariable<?> variableToLink);

   default LinkedYoDouble newLinkedYoDouble(YoDouble variableToLink)
   {
      return (LinkedYoDouble) newLinkedYoVariable(variableToLink);
   }

   default LinkedYoBoolean newLinkedYoBoolean(YoBoolean variableToLink)
   {
      return (LinkedYoBoolean) newLinkedYoVariable(variableToLink);
   }

   default LinkedYoInteger newLinkedYoInteger(YoInteger variableToLink)
   {
      return (LinkedYoInteger) newLinkedYoVariable(variableToLink);
   }

   default LinkedYoLong newLinkedYoLong(YoLong variableToLink)
   {
      return (LinkedYoLong) newLinkedYoVariable(variableToLink);
   }

   @SuppressWarnings("unchecked")
   default <E extends Enum<E>> LinkedYoEnum<E> newLinkedYoEnum(YoEnum<E> variableToLink)
   {
      return (LinkedYoEnum<E>) newLinkedYoVariable(variableToLink);
   }
}
