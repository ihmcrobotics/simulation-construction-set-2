package us.ihmc.scs2.simulation.robot.controller;

import java.util.ArrayList;
import java.util.List;

import us.ihmc.mecano.multiBodySystem.interfaces.MultiBodySystemReadOnly;
import us.ihmc.scs2.definition.controller.ControllerInput;
import us.ihmc.scs2.definition.controller.ControllerOutput;
import us.ihmc.scs2.definition.controller.interfaces.Controller;
import us.ihmc.scs2.definition.controller.interfaces.ControllerDefinition;
import us.ihmc.yoVariables.registry.YoRegistry;

public class RobotControllerManager
{
   private final YoRegistry registry;
   private final ControllerInput controllerInput;
   private final ControllerOutput controllerOutput;
   private List<Controller> controllers = new ArrayList<>();

   public RobotControllerManager(MultiBodySystemReadOnly input, YoRegistry registry)
   {
      this.registry = registry;
      controllerInput = new ControllerInput(input);
      controllerOutput = new ControllerOutput(input);
   }

   public void addController(Controller controller)
   {
      if (controllers.add(controller))
      {
         registry.addChild(controller.getYoRegistry());
      }
   }

   public void addController(ControllerDefinition controllerDefinition)
   {
      addController(controllerDefinition.newController(controllerInput, controllerOutput));
   }
}
