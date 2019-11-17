package us.ihmc.scs2.simulation.collision;

import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.mecano.spatial.Wrench;

public class CollisionResult
{
   private RigidBodyBasics bodyA;
   private RigidBodyBasics bodyB;

   private final Wrench wrenchOnA = new Wrench();
   private final Wrench wrenchOnB = new Wrench();

   public CollisionResult()
   {
   }

   public void setBodyA(RigidBodyBasics bodyA)
   {
      this.bodyA = bodyA;
   }

   public void setBodyB(RigidBodyBasics bodyB)
   {
      this.bodyB = bodyB;
   }

   public RigidBodyBasics getBodyA()
   {
      return bodyA;
   }

   public Wrench getWrenchOnA()
   {
      return wrenchOnA;
   }

   public RigidBodyBasics getBodyB()
   {
      return bodyB;
   }

   public Wrench getWrenchOnB()
   {
      return wrenchOnB;
   }
}
