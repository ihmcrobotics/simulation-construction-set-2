package us.ihmc.scs2.examples.springDamperEngine;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
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

   private double a = 1.0;

   private DenseMatrix64F A = new DenseMatrix64F(2, 2);
   private DenseMatrix64F B = new DenseMatrix64F(2, 2);
   private DenseMatrix64F x = new DenseMatrix64F(2, 1);
   private DenseMatrix64F u = new DenseMatrix64F(2, 1);
   private DenseMatrix64F xd = new DenseMatrix64F(2, 1);

   // temp
   DenseMatrix64F left = new DenseMatrix64F(2, 1);
   DenseMatrix64F right = new DenseMatrix64F(2, 1);

   double t = 0.0;

   @Override
   public void doScience(double dt, Vector3DReadOnly gravity)
   {
      t += dt;
      YoPrismaticJoint joint = (YoPrismaticJoint) system.getJointsToConsider().get(0);
      double m = joint.getSuccessor().getInertia().getMass();
      double rawQ = joint.getQ();
      double qd = joint.getQd();
      double qdd = joint.getQdd();
      double q = springRest - rawQ;

      x.set(0, 0, q);
      x.set(1, 0, qd);
      A.set(0, 0, 0.0);
      A.set(0, 1, 1.0);
      A.set(1, 0, - K / m);
      A.set(1, 1, - b / m);

      u.set(0, 0, 1.0);
      u.set(1, 0, Math.cos(2.0 * Math.PI * t));
      B.set(0, 0, 0.0);
      B.set(0, 1, 0.0);
      B.set(1, 0, 0.0);
      B.set(1, 1, a);

      CommonOps.mult(A, x, left);
      CommonOps.mult(B, u, right);

      CommonOps.add(left, right, xd);

      //      double qd2 = xd.get(0, 0);
      double qdd2 = xd.get(1, 0);

      // euler integration
      double qd2 =  qd + qdd * dt;
      double q2 = q + qd2 * dt;

      double rawQ2 = springRest - q2;

      joint.setQ(rawQ2);
      joint.setQd(qd2);
      joint.setQdd(qdd2);
   }
}