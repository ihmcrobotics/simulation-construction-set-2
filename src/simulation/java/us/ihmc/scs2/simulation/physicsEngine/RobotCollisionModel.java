package us.ihmc.scs2.simulation.physicsEngine;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import us.ihmc.mecano.multiBodySystem.interfaces.MultiBodySystemBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.mecano.tools.MultiBodySystemTools;
import us.ihmc.scs2.simulation.collision.Collidable;

/**
 * Defines a factory for creating the collision shapes of a humanoid robot.
 * 
 * @author Sylvain Bertrand
 */
public interface RobotCollisionModel
{
   /**
    * Creates the collision shapes to be used with the given robot model.
    * 
    * @param rootBody the root body of the robot that will be used with the collision shapes.
    * @return the list of collision shapes.
    */
   default List<Collidable> getRobotCollidables(RigidBodyBasics rootBody)
   {
      return getRobotCollidables(MultiBodySystemBasics.toMultiBodySystemBasics(rootBody));
   }

   /**
    * Creates the collision shapes to be used with the given robot model.
    * 
    * @param multiBodySystem the model that will be used with the collision shapes.
    * @return the list of collision shapes.
    */
   List<Collidable> getRobotCollidables(MultiBodySystemBasics multiBodySystem);

   public static RobotCollisionModel singleBodyCollisionModel(String bodyName, Function<RigidBodyBasics, Collidable> collidableBuilder)
   {
      return multiBodySystem -> Collections.singletonList(collidableBuilder.apply(MultiBodySystemTools.findRigidBody(multiBodySystem.getRootBody(), bodyName)));
   }
}
