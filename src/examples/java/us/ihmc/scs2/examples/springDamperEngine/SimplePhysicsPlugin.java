package us.ihmc.scs2.examples.springDamperEngine;

import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;
import us.ihmc.mecano.multiBodySystem.interfaces.MultiBodySystemBasics;
import us.ihmc.mecano.yoVariables.multiBodySystem.YoPrismaticJoint;
import us.ihmc.scs2.simulation.physicsEngine.RobotPhysicsEnginePlugin;

public class SimplePhysicsPlugin implements RobotPhysicsEnginePlugin
{
   private MultiBodySystemBasics system;

   @Override
   public void setMultiBodySystem(MultiBodySystemBasics multiBodySystem)
   {
      system = multiBodySystem;
   }

   private double springRest = 1.0;
   private double K = 50.0;
   private double b = 1.0;

   @Override
   public void doScience(double dt, Vector3DReadOnly gravity)
   {
      YoPrismaticJoint joint = (YoPrismaticJoint) system.getJointsToConsider().get(0);

      double m = joint.getSuccessor().getInertia().getMass();

      double q = joint.getQ();
      double qd = joint.getQd();
      double qdd = joint.getQdd();

      double depression = springRest - joint.getQ();

      double depression2 = depression + qd * dt;
      double qd2 =  qd + qdd * dt;
      double qdd2 = - K * depression2 / m - b * qd2 / m;

      double q2 = springRest - depression2;

      joint.setQ(q2);
      joint.setQd(qd2);
      joint.setQdd(qdd2);
   }
}
