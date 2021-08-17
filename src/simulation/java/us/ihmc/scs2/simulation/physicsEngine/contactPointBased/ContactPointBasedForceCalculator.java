package us.ihmc.scs2.simulation.physicsEngine.contactPointBased;

import java.util.List;
import java.util.stream.Collectors;

import us.ihmc.euclid.Axis3D;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.referenceFrame.interfaces.FrameVector3DReadOnly;
import us.ihmc.euclid.tuple3D.Point3D;
import us.ihmc.euclid.tuple3D.Vector3D;
import us.ihmc.log.LogTools;
import us.ihmc.scs2.simulation.collision.Collidable;
import us.ihmc.scs2.simulation.collision.CollidableHolder;
import us.ihmc.scs2.simulation.robot.multiBodySystem.interfaces.SimJointBasics;
import us.ihmc.scs2.simulation.robot.trackers.GroundContactPoint;
import us.ihmc.yoVariables.euclid.referenceFrame.YoFramePose3D;
import us.ihmc.yoVariables.euclid.referenceFrame.YoFramePoseUsingYawPitchRoll;
import us.ihmc.yoVariables.registry.YoRegistry;
import us.ihmc.yoVariables.variable.YoBoolean;
import us.ihmc.yoVariables.variable.YoDouble;

public class ContactPointBasedForceCalculator
{
   private static final double DEFAULT_K_XY = 1422, DEFAULT_B_XY = 15.6, DEFAULT_K_Z = 125, DEFAULT_B_Z = 300;
   private static final double DEFAULT_STIFFENING_LENGTH = 0.008;
   private static final double DEFAULT_ALPHA_SLIP = 0.7;
   private static final double DEFAULT_ALPHA_STICK = 0.7;

   private final YoRegistry registry = new YoRegistry(getClass().getSimpleName());
   private final YoDouble groundKxy = new YoDouble("groundKxy", "LinearStickSlipGroundContactModel x and y spring constant", registry);
   private final YoDouble groundBxy = new YoDouble("groundBxy", "LinearStickSlipGroundContactModel x and y damping constant", registry);
   private final YoDouble groundKz = new YoDouble("groundKz", "LinearStickSlipGroundContactModel z spring constant", registry);
   private final YoDouble groundBz = new YoDouble("groundBz", "LinearStickSlipGroundContactModel z damping constant", registry);
   private final YoDouble groundStiffeningLength = new YoDouble("groundStiffeningLength",
                                                                "LinearStickSlipGroundContactModel z spring nominal stiffening length",
                                                                registry);
   private final YoDouble groundAlphaSlip = new YoDouble("groundAlphaSlip", "LinearStickSlipGroundContactModel slip coefficient of friction", registry);
   private final YoDouble groundAlphaStick = new YoDouble("groundAlphaStick", "LinearStickSlipGroundContactModel stick coefficient of friction", registry);

   private final YoBoolean groundEnableSlip = new YoBoolean("groundEnableSlip", "LinearStickSlipGroundContactModel. If true can slip", registry);

   private final ReferenceFrame inertialFrame;

   public ContactPointBasedForceCalculator(ReferenceFrame inertialFrame, YoRegistry parentRegistry)
   {
      this.inertialFrame = inertialFrame;
      groundKxy.set(DEFAULT_K_XY);
      groundBxy.set(DEFAULT_B_XY);
      groundKz.set(DEFAULT_K_Z);
      groundBz.set(DEFAULT_B_Z);
      groundStiffeningLength.set(DEFAULT_STIFFENING_LENGTH);
      groundAlphaSlip.set(DEFAULT_ALPHA_SLIP);
      groundAlphaStick.set(DEFAULT_ALPHA_STICK);

      parentRegistry.addChild(registry);
   }

   public void resolveContactForces(List<ContactPointBasedRobot> robots, CollidableHolder staticCollidableHolder)
   {
      for (ContactPointBasedRobot robot : robots)
      {
         resolveContactForces(robot, staticCollidableHolder);
      }
   }

   private final Point3D closestPointOnSurface = new Point3D();
   private final Vector3D normalAtClosestPoint = new Vector3D();
   private final Vector3D deltaPositionFromTouchdown = new Vector3D();

   public void resolveContactForces(ContactPointBasedRobot robot, CollidableHolder staticCollidableHolder)
   {
      for (SimJointBasics joint : robot.getRootBody().childrenSubtreeIterable())
      {
         if (joint.getAuxialiryData().getGroundContactPoints().isEmpty())
            continue;

         for (GroundContactPoint gcp : joint.getAuxialiryData().getGroundContactPoints())
         {
            YoFramePoseUsingYawPitchRoll gcpPose = gcp.getPose();

            List<Collidable> activeCollidables = staticCollidableHolder.getCollidables().stream().filter(collidable ->
            {
               if (!collidable.getBoundingBox().isInsideInclusive(gcpPose.getPosition()))
                  return false;
               return collidable.getShape().isPointInside(gcpPose.getPosition());
            }).collect(Collectors.toList());

            if (activeCollidables.isEmpty())
            {
               gcp.getInContact().set(false);
               gcp.getIsSlipping().set(false);
               gcp.getWrench().setToZero();
               continue;
            }

            if (activeCollidables.size() > 1)
               LogTools.error("Cannot handle collision to more than one collidable");

            Collidable collidable = activeCollidables.get(0);

            collidable.getShape().evaluatePoint3DCollision(gcpPose.getPosition(), closestPointOnSurface, normalAtClosestPoint);

            YoFramePose3D gcpTouchdownPose = gcp.getTouchdownPose();

            if (!gcp.getInContact().getValue())
            {
               gcp.getInContact().set(true);
               gcpTouchdownPose.set(gcpPose);
               gcp.getContactNormal().set(normalAtClosestPoint);
            }

            resolveContactForceUsingSurfaceNormal(gcp);
            checkIfSlipping(gcp);
         }
      }
   }

   private final Vector3D linearVelocityWorld = new Vector3D();
   private final Vector3D inPlaneVector1 = new Vector3D();
   private final Vector3D inPlaneVector2 = new Vector3D();
   private final Vector3D forceParallel = new Vector3D();
   private final Vector3D forceNormal = new Vector3D();

   private void resolveContactForceUsingSurfaceNormal(GroundContactPoint gcp)
   {
      YoFramePoseUsingYawPitchRoll gcpPose = gcp.getPose();
      YoFramePose3D gcpTouchdownPose = gcp.getTouchdownPose();
      FrameVector3DReadOnly contactNormal = gcp.getContactNormal();

      deltaPositionFromTouchdown.sub(gcpTouchdownPose.getPosition(), gcpPose.getPosition());

      linearVelocityWorld.set(gcp.getTwist().getLinearPart());
      gcp.getFrame().transformFromThisToDesiredFrame(inertialFrame, linearVelocityWorld);

      inPlaneVector1.set(Axis3D.Y);

      if (Math.abs(inPlaneVector1.dot(contactNormal)) == 1.0) // check if they are parallel, in which case UNIT_X will do.
      {
         inPlaneVector1.set(Axis3D.X);
      }

      inPlaneVector1.cross(inPlaneVector1, contactNormal);
      inPlaneVector1.normalize();

      inPlaneVector2.cross(contactNormal, inPlaneVector1);
      inPlaneVector2.normalize();

      // Spring part
      double xPrime = inPlaneVector1.dot(deltaPositionFromTouchdown);
      double yPrime = inPlaneVector2.dot(deltaPositionFromTouchdown);
      double zPrime = contactNormal.dot(deltaPositionFromTouchdown);

      forceParallel.setAndScale(xPrime, inPlaneVector1);
      forceParallel.scaleAdd(yPrime, inPlaneVector2, forceParallel);
      forceParallel.scale(groundKxy.getDoubleValue());

      forceNormal.set(contactNormal);

      if (groundStiffeningLength.getDoubleValue() - zPrime > 0.002)
      {
         forceNormal.scale(groundKz.getDoubleValue() * zPrime / (groundStiffeningLength.getDoubleValue() - zPrime));
      }
      else
      {
         forceNormal.scale(groundKz.getDoubleValue() * zPrime / 0.002);
      }

      // Damping part
      xPrime = inPlaneVector1.dot(linearVelocityWorld);
      yPrime = inPlaneVector2.dot(linearVelocityWorld);
      zPrime = contactNormal.dot(linearVelocityWorld);
      forceParallel.scaleAdd(-groundBxy.getDoubleValue() * xPrime, inPlaneVector1, forceParallel);
      forceParallel.scaleAdd(-groundBxy.getDoubleValue() * yPrime, inPlaneVector2, forceParallel);
      forceNormal.scaleAdd(-groundBz.getDoubleValue() * zPrime, contactNormal, forceNormal);

      double magnitudeOfForceNormal = forceNormal.dot(contactNormal);

      if (magnitudeOfForceNormal < 0.0)
      {
         // If both the ground is pulling the point in rather than pushing it out,
         // and the point is higher than the touchdown point, then set not in contact so that
         // the touchdown point can be reset next tick if still below the ground...
         if (zPrime < 0.0)
         {
            forceParallel.setToZero();
            forceNormal.setToZero();
            gcp.getInContact().set(false);
         }
         else
         {
            //            forceParallel.set(0.0, 0.0, 0.0);
            forceNormal.set(0.0, 0.0, 0.0);
         }
      }

      // Sum the total
      gcp.getWrench().getLinearPart().add(forceParallel, forceNormal);
      inertialFrame.transformFromThisToDesiredFrame(gcp.getFrame(), gcp.getWrench().getLinearPart());
   }

   private final Vector3D forceWorld = new Vector3D();

   private void checkIfSlipping(GroundContactPoint gcp)
   {
      if (!groundEnableSlip.getBooleanValue())
      {
         gcp.getIsSlipping().set(false);
         return;
      }

      // Stick-Slip code below
      // Compute the horizontal to vertical force ratio:
      forceWorld.set(gcp.getWrench().getLinearPart());
      gcp.getFrame().transformFromThisToDesiredFrame(gcp.getFrame(), forceWorld);
      FrameVector3DReadOnly contactNormal = gcp.getContactNormal();

      forceNormal.set(contactNormal);
      forceNormal.scale(contactNormal.dot(forceWorld));

      forceParallel.sub(forceWorld, forceNormal);

      double parallelSpringForce = forceParallel.length();
      double normalSpringForce = forceNormal.length();

      double ratio = parallelSpringForce / normalSpringForce;

      // It's slipping if it already was and forces are above dynamic ratio.  It starts slipping if above static ratio.
      // But don't slip if inside the ground by more than 1 cm since this probably only occurs when in a wedge and keep sliding
      // perpendicular to the normal into the chasm..
      // +++JEP: 140626: Revisit the chasm thing later. For now take the heightAt check out...
      //    if ((gc.getZ() > heightAt - 0.010) && ((ratio > groundAlphaStick.getDoubleValue()) || ((gc.isSlipping()) && (ratio > groundAlphaSlip.getDoubleValue()))))
      if (ratio > groundAlphaStick.getDoubleValue() || gcp.getIsSlipping().getValue() && ratio > groundAlphaSlip.getDoubleValue())
      {
         gcp.getIsSlipping().set(true);
         double parallelSlipForce = groundAlphaSlip.getDoubleValue() * normalSpringForce;

         double parallelScale = parallelSlipForce / parallelSpringForce;
         if (parallelScale < 1.0)
            forceParallel.scale(parallelScale);

         forceWorld.add(forceNormal, forceParallel);
         gcp.getWrench().getLinearPart().setMatchingFrame(inertialFrame, forceWorld);

         // Move touch-down values along the perp direction to follow the slipping.

         double len = forceParallel.length();
         if (len > 1e-7)
            forceParallel.scale(1.0 / len);

         YoFramePoseUsingYawPitchRoll gcpPose = gcp.getPose();
         YoFramePose3D gcpTouchdownPose = gcp.getTouchdownPose();
         gcpTouchdownPose.getPosition()
                         .scaleAdd(-0.05 * gcpPose.getPosition().distance(gcpTouchdownPose.getPosition()), forceParallel, gcpTouchdownPose.getPosition());

         // Only update the contact normal when slipping.
         gcp.getContactNormal().set(normalAtClosestPoint);
      }
      else
      {
         gcp.getIsSlipping().set(false);
      }
   }
}
