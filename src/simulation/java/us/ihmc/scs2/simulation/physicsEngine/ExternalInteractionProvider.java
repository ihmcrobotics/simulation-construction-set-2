package us.ihmc.scs2.simulation.physicsEngine;

import java.util.Collections;
import java.util.List;

import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.mecano.spatial.interfaces.WrenchReadOnly;

public interface ExternalInteractionProvider
{
   List<? extends RigidBodyBasics> getRigidBodies();

   WrenchReadOnly getExternalWrench(RigidBodyBasics rigidBody);

   public static ExternalInteractionProvider emptyExternalInteractionProvider()
   {
      return new ExternalInteractionProvider()
      {
         @Override
         public List<? extends RigidBodyBasics> getRigidBodies()
         {
            return Collections.emptyList();
         }

         @Override
         public WrenchReadOnly getExternalWrench(RigidBodyBasics rigidBody)
         {
            return null;
         }
      };
   }
}
