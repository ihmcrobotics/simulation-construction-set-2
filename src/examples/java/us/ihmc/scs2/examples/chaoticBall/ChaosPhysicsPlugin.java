package us.ihmc.scs2.examples.chaoticBall;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import us.ihmc.euclid.tuple3D.Point3D;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;
import us.ihmc.mecano.multiBodySystem.interfaces.MultiBodySystemBasics;
import us.ihmc.mecano.yoVariables.multiBodySystem.YoSixDoFJoint;
import us.ihmc.scs2.simulation.physicsEngine.RobotPhysicsEnginePlugin;

public class ChaosPhysicsPlugin implements RobotPhysicsEnginePlugin
{
   private MultiBodySystemBasics system;

   @Override
   public void setMultiBodySystem(MultiBodySystemBasics multiBodySystem)
   {
      system = multiBodySystem;
   }

   private DenseMatrix64F A = new DenseMatrix64F(3, 3);
   {
      A.set(0, 0, -5.0);
      A.set(0, 1, 1.0);
      A.set(0, 2, 2.0);
      A.set(1, 0, 3.0);
      A.set(1, 1, -7.0);
      A.set(1, 2, 6.0);
      A.set(2, 0, 1.0);
      A.set(2, 1, -4.0);
      A.set(2, 2, -12.0);
   }
   private DenseMatrix64F B = new DenseMatrix64F(3, 2);
   {
      B.set(0, 0, 1.0);
      B.set(0, 1, 2.0);
      B.set(1, 0, 3.0);
      B.set(1, 1, 4.0);
      B.set(2, 0, 5.0);
      B.set(2, 1, 6.0);
   }
   private DenseMatrix64F x = new DenseMatrix64F(3, 1);
   private DenseMatrix64F u = new DenseMatrix64F(2, 1);
   private DenseMatrix64F xd = new DenseMatrix64F(3, 1);

   // temp
   DenseMatrix64F left = new DenseMatrix64F(3, 1);
   DenseMatrix64F right = new DenseMatrix64F(3, 1);

   double t = 0.0;

   private Point3D q = new Point3D();

   @Override
   public void doScience(double dt, Vector3DReadOnly gravity)
   {
      YoSixDoFJoint joint = (YoSixDoFJoint) system.getJointsToConsider().get(0);

      x.set(0, 0, joint.getJointPose().getX());
      x.set(1, 0, joint.getJointPose().getY());
      x.set(2, 0, joint.getJointPose().getZ());

      u.set(0, 0, Math.cos(2.0 * Math.PI * t));
      u.set(1, 0, Math.cos(6.0 * Math.PI * t + (Math.PI / 8.0)));

      CommonOps.mult(A, x, left);
      CommonOps.mult(B, u, right);

      CommonOps.add(left, right, xd);

      q.setX(x.get(0, 0) + xd.get(0, 0) * dt);
      q.setY(x.get(1, 0) + xd.get(1, 0) * dt);
      q.setZ(x.get(2, 0) + xd.get(2, 0) * dt);
      joint.setJointPosition(q);

      t += dt;
   }
}