package us.ihmc.scs2.simulation.collision;

import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.mecano.yoVariables.spatial.YoFixedFrameSpatialVector;
import us.ihmc.yoVariables.euclid.referenceFrame.YoFramePoint3D;
import us.ihmc.yoVariables.euclid.referenceFrame.YoFrameVector3D;
import us.ihmc.yoVariables.registry.YoRegistry;

/**
 * Purely for visualization.
 * 
 * @author Sylvain Bertrand
 */
public class YoCollisionResult
{
   private final YoFramePoint3D pointOnA, pointOnB;
   private final YoFrameVector3D normalOnA, normalOnB;
   private final YoFixedFrameSpatialVector wrenchOnA, wrenchOnB;

   public YoCollisionResult(String nameSuffix, ReferenceFrame referenceFrame, YoRegistry registry)
   {
      pointOnA = new YoFramePoint3D("pointOnA" + nameSuffix, referenceFrame, registry);
      pointOnB = new YoFramePoint3D("pointOnB" + nameSuffix, referenceFrame, registry);
      normalOnA = new YoFrameVector3D("normalOnA" + nameSuffix, referenceFrame, registry);
      normalOnB = new YoFrameVector3D("normalOnB" + nameSuffix, referenceFrame, registry);
      wrenchOnA = new YoFixedFrameSpatialVector("wrenchOnA" + nameSuffix, referenceFrame, registry);
      wrenchOnB = new YoFixedFrameSpatialVector("wrenchOnB" + nameSuffix, referenceFrame, registry);
   }

   public void update(CollisionResult collisionResult)
   {
      pointOnA.setMatchingFrame(collisionResult.getPointOnA());
      pointOnB.setMatchingFrame(collisionResult.getPointOnB());
      normalOnA.setMatchingFrame(collisionResult.getNormalOnA());
      normalOnB.setMatchingFrame(collisionResult.getNormalOnB());
      wrenchOnA.setMatchingFrame(collisionResult.getWrenchOnA());
      wrenchOnB.setMatchingFrame(collisionResult.getWrenchOnB());
   }

   public void setToNaN()
   {
      pointOnA.setToNaN();
      pointOnB.setToNaN();
      normalOnA.setToNaN();
      normalOnB.setToNaN();
      wrenchOnA.setToNaN();
      wrenchOnB.setToNaN();
   }
}
