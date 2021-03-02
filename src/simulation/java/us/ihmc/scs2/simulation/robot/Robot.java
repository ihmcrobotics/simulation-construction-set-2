package us.ihmc.scs2.simulation.robot;

import us.ihmc.mecano.multiBodySystem.interfaces.MultiBodySystemBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.scs2.definition.robot.RobotDefinition;

public class Robot implements MultiBodySystemBasics
{
   private String name;

   public Robot(RobotDefinition robotDefinition)
   {
   }

   @Override
   public RigidBodyBasics getRootBody()
   {
      return null;
   }
}
