package us.ihmc.scs2.definition.controller.interfaces;

import us.ihmc.yoVariables.registry.YoVariableRegistry;

public interface Controller
{
   default void initialize()
   {
   }

   void doControl();

   default YoVariableRegistry getYoVariableRegistry()
   {
      return null;
   }

   default String getName()
   {
      return getClass().getSimpleName();
   }

   public static Controller emptyController()
   {
      return () -> { /* Do nothing */ };
   }
}
