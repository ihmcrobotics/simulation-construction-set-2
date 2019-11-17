package us.ihmc.scs2.simulation.physicsEngine;

import us.ihmc.mecano.multiBodySystem.interfaces.MultiBodySystemBasics;
import us.ihmc.scs2.definition.controller.interfaces.ControllerOutputReadOnly;

public interface RobotPhysicsEnginePlugin extends PhysicsEnginePlugin
{
   void setMultiBodySystem(MultiBodySystemBasics multiBodySystem);

   default void submitControllerOutput(ControllerOutputReadOnly controllerOutput)
   {
   }

   default void submitExternalInteractions(ExternalInteractionProvider externalInteractionProvider)
   {
   }
}
