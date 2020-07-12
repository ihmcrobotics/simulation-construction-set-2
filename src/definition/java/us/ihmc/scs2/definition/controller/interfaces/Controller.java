package us.ihmc.scs2.definition.controller.interfaces;

import us.ihmc.yoVariables.registry.YoRegistry;

public interface Controller
{
   default void initialize()
   {
   }

   void doControl();

   default YoRegistry getYoRegistry()
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
