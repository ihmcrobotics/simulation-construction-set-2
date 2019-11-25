package us.ihmc.scs2.simulation.collision;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static us.ihmc.scs2.simulation.collision.CollisionTools.changeFrame;

import java.util.Random;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import us.ihmc.euclid.geometry.Pose3D;
import us.ihmc.euclid.geometry.tools.EuclidGeometryTestTools;
import us.ihmc.euclid.referenceFrame.FramePoint3D;
import us.ihmc.euclid.referenceFrame.FramePose3D;
import us.ihmc.euclid.referenceFrame.FrameVector3D;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.referenceFrame.tools.EuclidFrameRandomTools;
import us.ihmc.euclid.shape.collision.EuclidShape3DCollisionResult;
import us.ihmc.euclid.shape.primitives.Box3D;
import us.ihmc.euclid.shape.primitives.Capsule3D;
import us.ihmc.euclid.shape.primitives.Cylinder3D;
import us.ihmc.euclid.shape.primitives.Ellipsoid3D;
import us.ihmc.euclid.shape.primitives.PointShape3D;
import us.ihmc.euclid.shape.primitives.Ramp3D;
import us.ihmc.euclid.shape.primitives.Sphere3D;
import us.ihmc.euclid.shape.primitives.interfaces.Shape3DReadOnly;
import us.ihmc.euclid.shape.tools.EuclidShapeRandomTools;
import us.ihmc.euclid.shape.tools.EuclidShapeTestTools;
import us.ihmc.euclid.tools.EuclidCoreTestTools;
import us.ihmc.euclid.tuple3D.Vector3D;
import us.ihmc.scs2.simulation.collision.CollisionTools.FrameChanger;
import us.ihmc.scs2.simulation.collision.CollisionTools.FrameShape3DCollisionEvaluator;
import us.ihmc.scs2.simulation.collision.CollisionTools.Shape3DCollisionEvaluator;

class CollisionToolsTest
{
   private static final ReferenceFrame worldFrame = ReferenceFrame.getWorldFrame();
   private static final int ITERATIONS = 1000;
   private static final double EPSILON = 1.0e-12;
   private static final double EPA_EPSILON = 2.0e-4;

   @Test
   public void testChangeFrame()
   {
      Random random = new Random(65491);

      for (int i = 0; i < ITERATIONS; i++)
      { // Test for Box3D
         ReferenceFrame frameA = EuclidFrameRandomTools.nextReferenceFrame("frameA", random, worldFrame);
         ReferenceFrame frameB = EuclidFrameRandomTools.nextReferenceFrame("frameB", random, worldFrame);
         Box3D original = EuclidShapeRandomTools.nextBox3D(random);

         FramePose3D expectedPose = new FramePose3D(frameA, original.getPose());
         expectedPose.changeFrame(frameB);
         Vector3D expectedSize = new Vector3D(original.getSize());

         Box3D transformed = CollisionTools.changeFrame(original, frameA, frameB);
         EuclidGeometryTestTools.assertPose3DGeometricallyEquals(expectedPose, new Pose3D(transformed.getPose()), EPSILON);
         EuclidCoreTestTools.assertTuple3DEquals(expectedSize, transformed.getSize(), EPSILON);
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Test for Capsule3D
         ReferenceFrame frameA = EuclidFrameRandomTools.nextReferenceFrame("frameA", random, worldFrame);
         ReferenceFrame frameB = EuclidFrameRandomTools.nextReferenceFrame("frameB", random, worldFrame);
         Capsule3D original = EuclidShapeRandomTools.nextCapsule3D(random);

         FramePoint3D expectedPosition = new FramePoint3D(frameA, original.getPosition());
         expectedPosition.changeFrame(frameB);
         FrameVector3D expectedAxis = new FrameVector3D(frameA, original.getAxis());
         expectedAxis.changeFrame(frameB);

         Capsule3D transformed = changeFrame(original, frameA, frameB);
         EuclidCoreTestTools.assertTuple3DEquals(expectedPosition, transformed.getPosition(), EPSILON);
         EuclidCoreTestTools.assertTuple3DEquals(expectedAxis, transformed.getAxis(), EPSILON);
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Test for Cylinder3D
         ReferenceFrame frameA = EuclidFrameRandomTools.nextReferenceFrame("frameA", random, worldFrame);
         ReferenceFrame frameB = EuclidFrameRandomTools.nextReferenceFrame("frameB", random, worldFrame);
         Cylinder3D original = EuclidShapeRandomTools.nextCylinder3D(random);

         FramePoint3D expectedPosition = new FramePoint3D(frameA, original.getPosition());
         expectedPosition.changeFrame(frameB);
         FrameVector3D expectedAxis = new FrameVector3D(frameA, original.getAxis());
         expectedAxis.changeFrame(frameB);

         Cylinder3D transformed = changeFrame(original, frameA, frameB);
         EuclidCoreTestTools.assertTuple3DEquals(expectedPosition, transformed.getPosition(), EPSILON);
         EuclidCoreTestTools.assertTuple3DEquals(expectedAxis, transformed.getAxis(), EPSILON);
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Test for Ellipsoid3D
         ReferenceFrame frameA = EuclidFrameRandomTools.nextReferenceFrame("frameA", random, worldFrame);
         ReferenceFrame frameB = EuclidFrameRandomTools.nextReferenceFrame("frameB", random, worldFrame);
         Ellipsoid3D original = EuclidShapeRandomTools.nextEllipsoid3D(random);

         FramePose3D expectedPose = new FramePose3D(frameA, original.getPose());
         expectedPose.changeFrame(frameB);
         Vector3D expectedRadii = new Vector3D(original.getRadii());

         Ellipsoid3D transformed = changeFrame(original, frameA, frameB);
         EuclidGeometryTestTools.assertPose3DGeometricallyEquals(expectedPose, new Pose3D(transformed.getPose()), EPSILON);
         EuclidCoreTestTools.assertTuple3DEquals(expectedRadii, transformed.getRadii(), EPSILON);
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Test for PointShape3D
         ReferenceFrame frameA = EuclidFrameRandomTools.nextReferenceFrame("frameA", random, worldFrame);
         ReferenceFrame frameB = EuclidFrameRandomTools.nextReferenceFrame("frameB", random, worldFrame);
         PointShape3D original = EuclidShapeRandomTools.nextPointShape3D(random);

         FramePoint3D expectedPosition = new FramePoint3D(frameA, original);
         expectedPosition.changeFrame(frameB);

         PointShape3D transformed = changeFrame(original, frameA, frameB);
         EuclidCoreTestTools.assertTuple3DEquals(expectedPosition, transformed, EPSILON);
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Test for Ramp3D
         ReferenceFrame frameA = EuclidFrameRandomTools.nextReferenceFrame("frameA", random, worldFrame);
         ReferenceFrame frameB = EuclidFrameRandomTools.nextReferenceFrame("frameB", random, worldFrame);
         Ramp3D original = EuclidShapeRandomTools.nextRamp3D(random);

         FramePose3D expectedPose = new FramePose3D(frameA, original.getPose());
         expectedPose.changeFrame(frameB);
         Vector3D expectedSize = new Vector3D(original.getSize());

         Ramp3D transformed = changeFrame(original, frameA, frameB);
         EuclidGeometryTestTools.assertPose3DGeometricallyEquals(expectedPose, new Pose3D(transformed.getPose()), EPSILON);
         EuclidCoreTestTools.assertTuple3DEquals(expectedSize, transformed.getSize(), EPSILON);
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Test for Sphere3D
         ReferenceFrame frameA = EuclidFrameRandomTools.nextReferenceFrame("frameA", random, worldFrame);
         ReferenceFrame frameB = EuclidFrameRandomTools.nextReferenceFrame("frameB", random, worldFrame);
         Sphere3D original = EuclidShapeRandomTools.nextSphere3D(random);

         FramePoint3D expectedPosition = new FramePoint3D(frameA, original.getPosition());
         expectedPosition.changeFrame(frameB);

         Sphere3D transformed = changeFrame(original, frameA, frameB);
         EuclidCoreTestTools.assertTuple3DEquals(expectedPosition, transformed.getPosition(), EPSILON);
      }
   }

   private <A extends Shape3DReadOnly, B extends Shape3DReadOnly> void performAssertionsForCollisionEvaluator(Random random,
                                                                                                              FrameShape3DCollisionEvaluator<A, B> evaluatorToTest,
                                                                                                              Shape3DCollisionEvaluator<A, B> referenceForTesting,
                                                                                                              Function<Random, A> shapeARandomGenerator,
                                                                                                              Function<Random, B> shapeBRandomGenerator,
                                                                                                              FrameChanger<A> shapeAFrameChanger,
                                                                                                              FrameChanger<B> shapeBFrameChanger,
                                                                                                              boolean testNormal, double epsilon)
   {
      for (int i = 0; i < ITERATIONS; i++)
      { // Test in world frame
         CollisionResult actual = new CollisionResult();
         EuclidShape3DCollisionResult expected = new EuclidShape3DCollisionResult();
         A shapeA = shapeARandomGenerator.apply(random);
         ReferenceFrame frameA = worldFrame;
         B shapeB = shapeBRandomGenerator.apply(random);
         ReferenceFrame frameB = worldFrame;
         evaluatorToTest.evaluateCollision(shapeA, frameA, shapeB, frameB, actual);
         referenceForTesting.evaluateCollision(shapeA, shapeB, expected);
         assertCollisionResultsEqual(expected, frameA, frameB, actual, testNormal, epsilon);
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Test in a random frame (frameA == frameB)
         CollisionResult actual = new CollisionResult();
         EuclidShape3DCollisionResult expected = new EuclidShape3DCollisionResult();
         A shapeA = shapeARandomGenerator.apply(random);
         ReferenceFrame frameA = EuclidFrameRandomTools.nextReferenceFrame("aFrame", random, worldFrame);
         B shapeB = shapeBRandomGenerator.apply(random);
         ReferenceFrame frameB = frameA;
         evaluatorToTest.evaluateCollision(shapeA, frameA, shapeB, frameB, actual);
         referenceForTesting.evaluateCollision(shapeAFrameChanger.changeFrame(shapeA, frameA, worldFrame),
                                               shapeBFrameChanger.changeFrame(shapeB, frameB, worldFrame),
                                               expected);
         expected.setShapeA(shapeA);
         expected.setShapeB(shapeB);
         actual.getPointOnA().changeFrame(worldFrame);
         actual.getPointOnB().changeFrame(worldFrame);
         actual.getNormalOnA().changeFrame(worldFrame);
         actual.getNormalOnB().changeFrame(worldFrame);
         assertCollisionResultsEqual(expected, frameA, frameB, actual, testNormal, epsilon);
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Test in random frames (frameA != frameB)
         CollisionResult actual = new CollisionResult();
         EuclidShape3DCollisionResult expected = new EuclidShape3DCollisionResult();
         A shapeA = shapeARandomGenerator.apply(random);
         ReferenceFrame frameA = EuclidFrameRandomTools.nextReferenceFrame("frameA", random, worldFrame);
         B shapeB = shapeBRandomGenerator.apply(random);
         ReferenceFrame frameB = EuclidFrameRandomTools.nextReferenceFrame("frameB", random, worldFrame);
         evaluatorToTest.evaluateCollision(shapeA, frameA, shapeB, frameB, actual);
         referenceForTesting.evaluateCollision(shapeAFrameChanger.changeFrame(shapeA, frameA, worldFrame),
                                               shapeBFrameChanger.changeFrame(shapeB, frameB, worldFrame),
                                               expected);
         expected.setShapeA(shapeA);
         expected.setShapeB(shapeB);
         actual.getPointOnA().changeFrame(worldFrame);
         actual.getPointOnB().changeFrame(worldFrame);
         actual.getNormalOnA().changeFrame(worldFrame);
         actual.getNormalOnB().changeFrame(worldFrame);
         assertCollisionResultsEqual(expected, frameA, frameB, actual, testNormal, epsilon);
      }
   }

   private static void assertCollisionResultsEqual(EuclidShape3DCollisionResult expected, ReferenceFrame expectedFrameA, ReferenceFrame expectedFrameB,
                                                   CollisionResult actual, boolean testNormal, double epsilon)
   {
      assertTrue(actual.getFrameA() == expectedFrameA);
      assertTrue(actual.getFrameB() == expectedFrameB);

      if (!testNormal)
      {
         expected.getNormalOnA().setToNaN();
         expected.getNormalOnB().setToNaN();
      }
      else
      {
         expected.getNormalOnA().normalize();
         expected.getNormalOnB().normalize();
         actual.getNormalOnA().normalize();
         actual.getNormalOnB().normalize();
      }

      EuclidShapeTestTools.assertEuclidShape3DCollisionResultEquals(expected, actual, epsilon);
   }

   @Test
   public void testEvaluateCapsule3DCapsule3DCollision()
   {
      Random random = new Random(345);
      performAssertionsForCollisionEvaluator(random,
                                             CollisionTools::evaluateCapsule3DCapsule3DCollision,
                                             CollisionTools.capsule3DToCapsule3DEvaluator,
                                             EuclidShapeRandomTools::nextCapsule3D,
                                             EuclidShapeRandomTools::nextCapsule3D,
                                             CollisionTools::changeFrame,
                                             CollisionTools::changeFrame,
                                             true,
                                             EPSILON);

      performAssertionsForCollisionEvaluator(random,
                                             CollisionTools::evaluateShape3DCapsule3DCollisionEPA,
                                             CollisionTools.capsule3DToCapsule3DEvaluator,
                                             EuclidShapeRandomTools::nextCapsule3D,
                                             EuclidShapeRandomTools::nextCapsule3D,
                                             CollisionTools::changeFrame,
                                             CollisionTools::changeFrame,
                                             false,
                                             EPA_EPSILON);

      performAssertionsForCollisionEvaluator(random,
                                             CollisionTools::evaluateShape3DShape3DCollisionEPA,
                                             CollisionTools.capsule3DToCapsule3DEvaluator,
                                             EuclidShapeRandomTools::nextCapsule3D,
                                             EuclidShapeRandomTools::nextCapsule3D,
                                             CollisionTools::changeFrame,
                                             CollisionTools::changeFrame,
                                             false,
                                             EPA_EPSILON);
   }

   @Test
   public void testEvaluatePointShape3DBox3DCollision()
   {
      Random random = new Random(5165);
      performAssertionsForCollisionEvaluator(random,
                                             CollisionTools::evaluatePointShape3DBox3DCollision,
                                             CollisionTools.pointShape3DToBox3DEvaluator,
                                             EuclidShapeRandomTools::nextPointShape3D,
                                             EuclidShapeRandomTools::nextBox3D,
                                             CollisionTools::changeFrame,
                                             CollisionTools::changeFrame,
                                             true,
                                             EPSILON);

      performAssertionsForCollisionEvaluator(random,
                                             CollisionTools::evaluateShape3DBox3DCollisionEPA,
                                             CollisionTools.pointShape3DToBox3DEvaluator,
                                             EuclidShapeRandomTools::nextPointShape3D,
                                             EuclidShapeRandomTools::nextBox3D,
                                             CollisionTools::changeFrame,
                                             CollisionTools::changeFrame,
                                             false,
                                             EPA_EPSILON);

      performAssertionsForCollisionEvaluator(random,
                                             CollisionTools::evaluateShape3DShape3DCollisionEPA,
                                             CollisionTools.pointShape3DToBox3DEvaluator,
                                             EuclidShapeRandomTools::nextPointShape3D,
                                             EuclidShapeRandomTools::nextBox3D,
                                             CollisionTools::changeFrame,
                                             CollisionTools::changeFrame,
                                             false,
                                             EPA_EPSILON);
   }

   @Test
   public void testEvaluatePointShape3DCapsule3DCollision()
   {
      Random random = new Random(54687);
      performAssertionsForCollisionEvaluator(random,
                                             CollisionTools::evaluatePointShape3DCapsule3DCollision,
                                             CollisionTools.pointShape3DToCapsule3DEvaluator,
                                             EuclidShapeRandomTools::nextPointShape3D,
                                             EuclidShapeRandomTools::nextCapsule3D,
                                             CollisionTools::changeFrame,
                                             CollisionTools::changeFrame,
                                             true,
                                             EPSILON);

      performAssertionsForCollisionEvaluator(random,
                                             CollisionTools::evaluateShape3DCapsule3DCollisionEPA,
                                             CollisionTools.pointShape3DToCapsule3DEvaluator,
                                             EuclidShapeRandomTools::nextPointShape3D,
                                             EuclidShapeRandomTools::nextCapsule3D,
                                             CollisionTools::changeFrame,
                                             CollisionTools::changeFrame,
                                             false,
                                             EPA_EPSILON);

      performAssertionsForCollisionEvaluator(random,
                                             CollisionTools::evaluateShape3DShape3DCollisionEPA,
                                             CollisionTools.pointShape3DToCapsule3DEvaluator,
                                             EuclidShapeRandomTools::nextPointShape3D,
                                             EuclidShapeRandomTools::nextCapsule3D,
                                             CollisionTools::changeFrame,
                                             CollisionTools::changeFrame,
                                             false,
                                             EPA_EPSILON);
   }

   @Test
   public void testEvaluatePointShape3DCylinder3DCollision()
   {
      Random random = new Random(86);
      performAssertionsForCollisionEvaluator(random,
                                             CollisionTools::evaluatePointShape3DCylinder3DCollision,
                                             CollisionTools.pointShape3DToCylinder3DEvaluator,
                                             EuclidShapeRandomTools::nextPointShape3D,
                                             EuclidShapeRandomTools::nextCylinder3D,
                                             CollisionTools::changeFrame,
                                             CollisionTools::changeFrame,
                                             true,
                                             EPSILON);

      performAssertionsForCollisionEvaluator(random,
                                             CollisionTools::evaluateShape3DCylinder3DCollisionEPA,
                                             CollisionTools.pointShape3DToCylinder3DEvaluator,
                                             EuclidShapeRandomTools::nextPointShape3D,
                                             EuclidShapeRandomTools::nextCylinder3D,
                                             CollisionTools::changeFrame,
                                             CollisionTools::changeFrame,
                                             false,
                                             EPA_EPSILON);

      performAssertionsForCollisionEvaluator(random,
                                             CollisionTools::evaluateShape3DShape3DCollisionEPA,
                                             CollisionTools.pointShape3DToCylinder3DEvaluator,
                                             EuclidShapeRandomTools::nextPointShape3D,
                                             EuclidShapeRandomTools::nextCylinder3D,
                                             CollisionTools::changeFrame,
                                             CollisionTools::changeFrame,
                                             false,
                                             EPA_EPSILON);
   }

   @Test
   public void testEvaluatePointShape3DEllipsoid3DCollision()
   {
      Random random = new Random(285);
      performAssertionsForCollisionEvaluator(random,
                                             CollisionTools::evaluatePointShape3DEllipsoid3DCollision,
                                             CollisionTools.pointShape3DToEllipsoid3DEvaluator,
                                             EuclidShapeRandomTools::nextPointShape3D,
                                             EuclidShapeRandomTools::nextEllipsoid3D,
                                             CollisionTools::changeFrame,
                                             CollisionTools::changeFrame,
                                             true,
                                             EPSILON);

      performAssertionsForCollisionEvaluator(random,
                                             CollisionTools::evaluateShape3DEllipsoid3DCollisionEPA,
                                             CollisionTools.pointShape3DToEllipsoid3DEvaluator,
                                             EuclidShapeRandomTools::nextPointShape3D,
                                             EuclidShapeRandomTools::nextEllipsoid3D,
                                             CollisionTools::changeFrame,
                                             CollisionTools::changeFrame,
                                             false,
                                             EPA_EPSILON);

      performAssertionsForCollisionEvaluator(random,
                                             CollisionTools::evaluateShape3DShape3DCollisionEPA,
                                             CollisionTools.pointShape3DToEllipsoid3DEvaluator,
                                             EuclidShapeRandomTools::nextPointShape3D,
                                             EuclidShapeRandomTools::nextEllipsoid3D,
                                             CollisionTools::changeFrame,
                                             CollisionTools::changeFrame,
                                             false,
                                             EPA_EPSILON);
   }

   @Test
   public void testEvaluatePointShape3DPointShape3DCollision()
   {
      Random random = new Random(6);
      performAssertionsForCollisionEvaluator(random,
                                             CollisionTools::evaluatePointShape3DPointShape3DCollision,
                                             CollisionTools.pointShape3DToPointShape3DEvaluator,
                                             EuclidShapeRandomTools::nextPointShape3D,
                                             EuclidShapeRandomTools::nextPointShape3D,
                                             CollisionTools::changeFrame,
                                             CollisionTools::changeFrame,
                                             true,
                                             EPSILON);

      performAssertionsForCollisionEvaluator(random,
                                             CollisionTools::evaluateShape3DPointShape3DCollisionEPA,
                                             CollisionTools.pointShape3DToPointShape3DEvaluator,
                                             EuclidShapeRandomTools::nextPointShape3D,
                                             EuclidShapeRandomTools::nextPointShape3D,
                                             CollisionTools::changeFrame,
                                             CollisionTools::changeFrame,
                                             false,
                                             EPA_EPSILON);

      performAssertionsForCollisionEvaluator(random,
                                             CollisionTools::evaluateShape3DShape3DCollisionEPA,
                                             CollisionTools.pointShape3DToPointShape3DEvaluator,
                                             EuclidShapeRandomTools::nextPointShape3D,
                                             EuclidShapeRandomTools::nextPointShape3D,
                                             CollisionTools::changeFrame,
                                             CollisionTools::changeFrame,
                                             false,
                                             EPA_EPSILON);
   }

   @Test
   public void testEvaluatePointShape3DRamp3DCollision()
   {
      Random random = new Random(98587621);
      performAssertionsForCollisionEvaluator(random,
                                             CollisionTools::evaluatePointShape3DRamp3DCollision,
                                             CollisionTools.pointShape3DToRamp3DEvaluator,
                                             EuclidShapeRandomTools::nextPointShape3D,
                                             EuclidShapeRandomTools::nextRamp3D,
                                             CollisionTools::changeFrame,
                                             CollisionTools::changeFrame,
                                             true,
                                             EPSILON);

      performAssertionsForCollisionEvaluator(random,
                                             CollisionTools::evaluateShape3DRamp3DCollisionEPA,
                                             CollisionTools.pointShape3DToRamp3DEvaluator,
                                             EuclidShapeRandomTools::nextPointShape3D,
                                             EuclidShapeRandomTools::nextRamp3D,
                                             CollisionTools::changeFrame,
                                             CollisionTools::changeFrame,
                                             false,
                                             EPA_EPSILON);

      performAssertionsForCollisionEvaluator(random,
                                             CollisionTools::evaluateShape3DShape3DCollisionEPA,
                                             CollisionTools.pointShape3DToRamp3DEvaluator,
                                             EuclidShapeRandomTools::nextPointShape3D,
                                             EuclidShapeRandomTools::nextRamp3D,
                                             CollisionTools::changeFrame,
                                             CollisionTools::changeFrame,
                                             false,
                                             EPA_EPSILON);
   }

   @Test
   public void testEvaluatePointShape3DSphere3DCollision()
   {
      Random random = new Random(74275);
      performAssertionsForCollisionEvaluator(random,
                                             CollisionTools::evaluatePointShape3DSphere3DCollision,
                                             CollisionTools.pointShape3DToSphere3DEvaluator,
                                             EuclidShapeRandomTools::nextPointShape3D,
                                             EuclidShapeRandomTools::nextSphere3D,
                                             CollisionTools::changeFrame,
                                             CollisionTools::changeFrame,
                                             true,
                                             EPSILON);

      performAssertionsForCollisionEvaluator(random,
                                             CollisionTools::evaluateShape3DSphere3DCollisionEPA,
                                             CollisionTools.pointShape3DToSphere3DEvaluator,
                                             EuclidShapeRandomTools::nextPointShape3D,
                                             EuclidShapeRandomTools::nextSphere3D,
                                             CollisionTools::changeFrame,
                                             CollisionTools::changeFrame,
                                             false,
                                             EPA_EPSILON);

      performAssertionsForCollisionEvaluator(random,
                                             CollisionTools::evaluateShape3DShape3DCollisionEPA,
                                             CollisionTools.pointShape3DToSphere3DEvaluator,
                                             EuclidShapeRandomTools::nextPointShape3D,
                                             EuclidShapeRandomTools::nextSphere3D,
                                             CollisionTools::changeFrame,
                                             CollisionTools::changeFrame,
                                             false,
                                             EPA_EPSILON);
   }

   @Test
   public void testEvaluateSphere3DBox3DCollision()
   {
      Random random = new Random(5165);
      performAssertionsForCollisionEvaluator(random,
                                             CollisionTools::evaluateSphere3DBox3DCollision,
                                             CollisionTools.sphere3DToBox3DEvaluator,
                                             EuclidShapeRandomTools::nextSphere3D,
                                             EuclidShapeRandomTools::nextBox3D,
                                             CollisionTools::changeFrame,
                                             CollisionTools::changeFrame,
                                             true,
                                             EPSILON);
   }

   @Test
   public void testEvaluateSphere3DCapsule3DCollision()
   {
      Random random = new Random(54687);
      performAssertionsForCollisionEvaluator(random,
                                             CollisionTools::evaluateSphere3DCapsule3DCollision,
                                             CollisionTools.sphere3DToCapsule3DEvaluator,
                                             EuclidShapeRandomTools::nextSphere3D,
                                             EuclidShapeRandomTools::nextCapsule3D,
                                             CollisionTools::changeFrame,
                                             CollisionTools::changeFrame,
                                             true,
                                             EPSILON);
   }

   @Test
   public void testEvaluateSphere3DCylinder3DCollision()
   {
      Random random = new Random(86);
      performAssertionsForCollisionEvaluator(random,
                                             CollisionTools::evaluateSphere3DCylinder3DCollision,
                                             CollisionTools.sphere3DToCylinder3DEvaluator,
                                             EuclidShapeRandomTools::nextSphere3D,
                                             EuclidShapeRandomTools::nextCylinder3D,
                                             CollisionTools::changeFrame,
                                             CollisionTools::changeFrame,
                                             true,
                                             EPSILON);
   }

   @Test
   public void testEvaluateSphere3DEllipsoid3DCollision()
   {
      Random random = new Random(285);
      performAssertionsForCollisionEvaluator(random,
                                             CollisionTools::evaluateSphere3DEllipsoid3DCollision,
                                             CollisionTools.sphere3DToEllipsoid3DEvaluator,
                                             EuclidShapeRandomTools::nextSphere3D,
                                             EuclidShapeRandomTools::nextEllipsoid3D,
                                             CollisionTools::changeFrame,
                                             CollisionTools::changeFrame,
                                             true,
                                             EPSILON);
   }

   @Test
   public void testEvaluateSphere3DRamp3DCollision()
   {
      Random random = new Random(98587621);
      performAssertionsForCollisionEvaluator(random,
                                             CollisionTools::evaluateSphere3DRamp3DCollision,
                                             CollisionTools.sphere3DToRamp3DEvaluator,
                                             EuclidShapeRandomTools::nextSphere3D,
                                             EuclidShapeRandomTools::nextRamp3D,
                                             CollisionTools::changeFrame,
                                             CollisionTools::changeFrame,
                                             true,
                                             EPSILON);
   }

   @Test
   public void testEvaluateSphere3DSphere3DCollision()
   {
      Random random = new Random(74275);
      performAssertionsForCollisionEvaluator(random,
                                             CollisionTools::evaluateSphere3DSphere3DCollision,
                                             CollisionTools.sphere3DToSphere3DEvaluator,
                                             EuclidShapeRandomTools::nextSphere3D,
                                             EuclidShapeRandomTools::nextSphere3D,
                                             CollisionTools::changeFrame,
                                             CollisionTools::changeFrame,
                                             true,
                                             EPSILON);
   }

}
