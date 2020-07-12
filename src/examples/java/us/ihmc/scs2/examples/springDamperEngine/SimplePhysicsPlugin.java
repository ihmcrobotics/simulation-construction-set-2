package us.ihmc.scs2.examples.springDamperEngine;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;

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

   private DMatrixRMaj A = new DMatrixRMaj(2, 2);
   private DMatrixRMaj B = new DMatrixRMaj(2, 2);
   private DMatrixRMaj x = new DMatrixRMaj(2, 1);
   private DMatrixRMaj u = new DMatrixRMaj(2, 1);
   private DMatrixRMaj xd = new DMatrixRMaj(2, 1);

   // temp
   DMatrixRMaj left = new DMatrixRMaj(2, 1);
   DMatrixRMaj right = new DMatrixRMaj(2, 1);

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

      CommonOps_DDRM.mult(A, x, left);
      CommonOps_DDRM.mult(B, u, right);

      CommonOps_DDRM.add(left, right, xd);

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