package us.ihmc.scs2.simulation.bullet.physicsEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bytedeco.bullet.BulletCollision.btCollisionShape;
import org.bytedeco.bullet.BulletCollision.btCompoundShape;
import org.bytedeco.bullet.BulletDynamics.btMultiBodyJointLimitConstraint;
import org.bytedeco.bullet.BulletDynamics.btMultiBodyLinkCollider;
import org.bytedeco.bullet.BulletDynamics.btMultibodyLink;
import org.bytedeco.bullet.LinearMath.btQuaternion;
import org.bytedeco.bullet.LinearMath.btTransform;
import org.bytedeco.bullet.LinearMath.btVector3;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.transform.RigidBodyTransform;
import us.ihmc.euclid.tuple4D.Quaternion;
import us.ihmc.mecano.multiBodySystem.interfaces.JointBasics;
import us.ihmc.scs2.definition.collision.CollisionShapeDefinition;
import us.ihmc.scs2.definition.robot.JointDefinition;
import us.ihmc.scs2.definition.robot.PrismaticJointDefinition;
import us.ihmc.scs2.definition.robot.RevoluteJointDefinition;
import us.ihmc.scs2.definition.robot.RigidBodyDefinition;
import us.ihmc.scs2.simulation.bullet.physicsEngine.parameters.YoBulletMultiBodyJointParameters;
import us.ihmc.scs2.simulation.bullet.physicsEngine.parameters.YoBulletMultiBodyParameters;
import us.ihmc.scs2.simulation.robot.Robot;
import us.ihmc.scs2.simulation.robot.multiBodySystem.SimFloatingRootJoint;
import us.ihmc.scs2.simulation.robot.multiBodySystem.SimPrismaticJoint;
import us.ihmc.scs2.simulation.robot.multiBodySystem.SimRevoluteJoint;

public class BulletMultiBodyRobotFactory
{
   private static HashMap<String, Integer> jointNameToBulletJointIndexMap;
   private static int linkCountingIndex;
   private static int numberOfLinks;

   public static BulletMultiBodyRobot newInstance(Robot robot,
                                                  YoBulletMultiBodyParameters bulletMultiBodyParameters,
                                                  YoBulletMultiBodyJointParameters bulletMultiBodyJointParameters)
   {
      if (robot.getRootBody().getChildrenJoints().size() == 0)
      {
         throw new UnsupportedOperationException("Robot must have at least one joint: " + robot.getClass().getSimpleName());
      }

      JointBasics rootJoint = robot.getRootBody().getChildrenJoints().get(0);
      boolean hasBaseCollider = rootJoint instanceof SimFloatingRootJoint;

      jointNameToBulletJointIndexMap = new HashMap<String, Integer>();
      linkCountingIndex = hasBaseCollider ? 0 : 1;
      numberOfLinks = 0;
      for (JointBasics joint : robot.getRootBody().getChildrenJoints())
      {
         numberOfLinks += countJointsAndCreateIndexMap(joint) - (hasBaseCollider ? 1 : 0);
      }

      RigidBodyDefinition rigidBodyDefinition = robot.getRobotDefinition().getRootBodyDefinition().getChildrenJoints().get(0).getSuccessor();
      double rootBodyMass = rigidBodyDefinition.getMass();

      //      TODO: fix moment of inertia using SVD      
      //      RotationMatrix svd_rotation = new RotationMatrix();
      //      Vector3 rootBodyIntertia = decomposeMomentOfInertia(svd_rotation, rigidBodyDefinition);
      btVector3 rootBodyIntertia = new btVector3(rigidBodyDefinition.getMomentOfInertia().getM00(),
                                                 rigidBodyDefinition.getMomentOfInertia().getM11(),
                                                 rigidBodyDefinition.getMomentOfInertia().getM22());
      boolean fixedBase = hasBaseCollider ? false : true;
      BulletMultiBodyRobot bulletMultiBodyRobot = new BulletMultiBodyRobot(numberOfLinks,
                                                                           rootBodyMass,
                                                                           rootBodyIntertia,
                                                                           fixedBase,
                                                                           bulletMultiBodyParameters.getCanSleep(),
                                                                           jointNameToBulletJointIndexMap);

      for (JointBasics joint : robot.getRootBody().getChildrenJoints())
      {
         int linkIndex = bulletMultiBodyRobot.getJointNameToBulletJointIndexMap().get(joint.getName());
         if (linkIndex == -1)
         {

            //create BaseCollider
            bulletMultiBodyRobot.getBtMultiBody()
                                .setBaseCollider(createBulletLinkCollider(bulletMultiBodyRobot,
                                                                          rigidBodyDefinition.getCollisionShapeDefinitions(),
                                                                          rootJoint.getSuccessor().getBodyFixedFrame(),
                                                                          -1,
                                                                          rootJoint.getName()));
         }
         else
         {
            JointDefinition rootJointDefinition = robot.getRobotDefinition().getJointDefinition(joint.getName());
            btMultibodyLink bulletLink = setupBtMultibodyLink(bulletMultiBodyRobot,
                                                              joint,
                                                              rootJointDefinition,
                                                              linkIndex,
                                                              bulletMultiBodyJointParameters.getJointDisableParentCollision());
            bulletLink.m_collider(createBulletLinkCollider(bulletMultiBodyRobot,
                                                           rootJointDefinition.getSuccessor().getCollisionShapeDefinitions(),
                                                           joint.getSuccessor().getBodyFixedFrame(),
                                                           linkIndex,
                                                           joint.getName()));
         }

         //Create LinkColliders for each root joint 
         createBulletLinkColliderChildren(bulletMultiBodyRobot, robot, joint, bulletMultiBodyJointParameters.getJointDisableParentCollision());
      }

      bulletMultiBodyRobot.finalizeMultiDof(bulletMultiBodyParameters, bulletMultiBodyJointParameters);

      return bulletMultiBodyRobot;
   }

   private static void createBulletLinkColliderChildren(BulletMultiBodyRobot bulletMultiBodyRobot,
                                                        Robot robot,
                                                        JointBasics joint,
                                                        boolean disableParentCollision)
   {
      for (JointBasics childJoint : joint.getSuccessor().getChildrenJoints())
      {
         JointDefinition childJointDefinition = robot.getRobotDefinition().getJointDefinition(childJoint.getName());

         int linkIndex = bulletMultiBodyRobot.getJointNameToBulletJointIndexMap().get(childJoint.getName());

         btMultibodyLink btMultibodyLink = setupBtMultibodyLink(bulletMultiBodyRobot, childJoint, childJointDefinition, linkIndex, disableParentCollision);
         btMultibodyLink.m_collider(createBulletLinkCollider(bulletMultiBodyRobot,
                                                             childJointDefinition.getSuccessor().getCollisionShapeDefinitions(),
                                                             childJoint.getSuccessor().getBodyFixedFrame(),
                                                             linkIndex,
                                                             childJoint.getName()));

         createBulletLinkColliderChildren(bulletMultiBodyRobot, robot, childJoint, disableParentCollision);
      }
   }

   private static final btQuaternion rotationFromParentBullet = new btQuaternion();
   private static final Quaternion euclidRotationFromParent = new Quaternion();
   private static final RigidBodyTransform parentLinkCenterOfMassToParentJointBeforeJointFrameTransformEuclid = new RigidBodyTransform();
   private static final btVector3 parentLinkCenterOfMassToParentJointBeforeJointFrameTranslationBullet = new btVector3();
   private static final RigidBodyTransform parentJointAfterFrameToLinkCenterOfMassTransformEuclid = new RigidBodyTransform();
   private static final btVector3 parentJointAfterFrameToLinkCenterOfMassTranslationBullet = new btVector3();

   private static btMultibodyLink setupBtMultibodyLink(BulletMultiBodyRobot bulletMultiBodyRobot,
                                                       JointBasics joint,
                                                       JointDefinition jointDefinition,
                                                       int bulletJointIndex,
                                                       boolean disableParentCollision)
   {

      //      TODO: fix moment of inertia using SVD      
      //      RotationMatrix svd_rotation = new RotationMatrix();
      //      Vector3 linkInertiaDiagonal = decomposeMomentOfInertia(svd_rotation, jointDefinition.getSuccessor());
      btVector3 linkInertiaDiagonal = new btVector3(jointDefinition.getSuccessor().getMomentOfInertia().getM00(),
                                                    jointDefinition.getSuccessor().getMomentOfInertia().getM11(),
                                                    jointDefinition.getSuccessor().getMomentOfInertia().getM22());

      euclidRotationFromParent.set(jointDefinition.getTransformToParent().getRotation());
      euclidRotationFromParent.invert();
      BulletTools.toBullet(euclidRotationFromParent, rotationFromParentBullet);

      joint.getPredecessor().getBodyFixedFrame().getTransformToDesiredFrame(parentLinkCenterOfMassToParentJointBeforeJointFrameTransformEuclid,
                                                                            joint.getFrameBeforeJoint());
      parentLinkCenterOfMassToParentJointBeforeJointFrameTransformEuclid.invert();
      BulletTools.toBullet(parentLinkCenterOfMassToParentJointBeforeJointFrameTransformEuclid.getTranslation(),
                           parentLinkCenterOfMassToParentJointBeforeJointFrameTranslationBullet);

      joint.getFrameAfterJoint().getTransformToDesiredFrame(parentJointAfterFrameToLinkCenterOfMassTransformEuclid, joint.getSuccessor().getBodyFixedFrame());
      parentJointAfterFrameToLinkCenterOfMassTransformEuclid.invert();
      BulletTools.toBullet(parentJointAfterFrameToLinkCenterOfMassTransformEuclid.getTranslation(), parentJointAfterFrameToLinkCenterOfMassTranslationBullet);

      double linkMass = jointDefinition.getSuccessor().getMass();

      if (joint instanceof SimRevoluteJoint)
      {
         RevoluteJointDefinition revoluteJointDefinition = (RevoluteJointDefinition) jointDefinition;

         int parentBulletJointIndex;
         if (joint.getPredecessor().getParentJoint() == null)
            parentBulletJointIndex = -1;
         else
            parentBulletJointIndex = bulletMultiBodyRobot.getJointNameToBulletJointIndexMap().get(joint.getPredecessor().getParentJoint().getName());

         btVector3 jointAxis = new btVector3();
         BulletTools.toBullet(revoluteJointDefinition.getAxis(), jointAxis);

         bulletMultiBodyRobot.getBtMultiBody().setupRevolute(bulletJointIndex,
                                                             linkMass,
                                                             linkInertiaDiagonal,
                                                             parentBulletJointIndex,
                                                             rotationFromParentBullet,
                                                             jointAxis,
                                                             parentLinkCenterOfMassToParentJointBeforeJointFrameTranslationBullet,
                                                             parentJointAfterFrameToLinkCenterOfMassTranslationBullet,
                                                             disableParentCollision);

         btMultiBodyJointLimitConstraint multiBodyJointLimitConstraint = new btMultiBodyJointLimitConstraint(bulletMultiBodyRobot.getBtMultiBody(),
                                                                                                             bulletJointIndex,
                                                                                                             revoluteJointDefinition.getPositionLowerLimit(),
                                                                                                             revoluteJointDefinition.getPositionUpperLimit());

         bulletMultiBodyRobot.addBtMultiBodyConstraint(multiBodyJointLimitConstraint);
      }
      else if (joint instanceof SimPrismaticJoint)
      {
         PrismaticJointDefinition primaticJointDefinition = (PrismaticJointDefinition) jointDefinition;

         int parentBulletJointIndex;
         if (joint.getPredecessor().getParentJoint() == null)
            parentBulletJointIndex = -1;
         else
            parentBulletJointIndex = bulletMultiBodyRobot.getJointNameToBulletJointIndexMap().get(joint.getPredecessor().getParentJoint().getName());

         btVector3 jointAxis = new btVector3();
         BulletTools.toBullet(primaticJointDefinition.getAxis(), jointAxis);

         bulletMultiBodyRobot.getBtMultiBody().setupPrismatic(bulletJointIndex,
                                                              linkMass,
                                                              linkInertiaDiagonal,
                                                              parentBulletJointIndex,
                                                              rotationFromParentBullet,
                                                              jointAxis,
                                                              parentLinkCenterOfMassToParentJointBeforeJointFrameTranslationBullet,
                                                              parentJointAfterFrameToLinkCenterOfMassTranslationBullet,
                                                              disableParentCollision);

         btMultiBodyJointLimitConstraint multiBodyJointLimitConstraint = new btMultiBodyJointLimitConstraint(bulletMultiBodyRobot.getBtMultiBody(),
                                                                                                             bulletJointIndex,
                                                                                                             primaticJointDefinition.getPositionLowerLimit(),
                                                                                                             primaticJointDefinition.getPositionUpperLimit());

         bulletMultiBodyRobot.addBtMultiBodyConstraint(multiBodyJointLimitConstraint);
      }
      else
      {
         throw new UnsupportedOperationException("Unsupported joint: " + joint.getClass().getSimpleName());
      }

      return bulletMultiBodyRobot.getBtMultiBody().getLink(bulletJointIndex);
   }

   //   TODO: fix moment of inertia using SVD   
   //   private static Vector3 decomposeMomentOfInertia(RotationMatrix svd_rotation, RigidBodyDefinition rigidBodyDefinition)
   //   {
   //      Vector3 localInertiaDiagonal = new Vector3();
   //      if (rigidBodyDefinition.getMomentOfInertia().getM01() == 0 && rigidBodyDefinition.getMomentOfInertia().getM02() == 0
   //            && rigidBodyDefinition.getMomentOfInertia().getM21() == 0)
   //      {
   //         localInertiaDiagonal.x = rigidBodyDefinition.getMomentOfInertia().getM00();
   //         localInertiaDiagonal.y = rigidBodyDefinition.getMomentOfInertia().getM11();
   //         localInertiaDiagonal.z = rigidBodyDefinition.getMomentOfInertia().getM22();
   //      }
   //      else {
   //         SingularValueDecomposition3D svd = new SingularValueDecomposition3D();
   //         
   //         if (svd.decompose(rigidBodyDefinition.getMomentOfInertia()))
   //         {
   //            svd_rotation.set(svd.getU());
   //
   //            localInertiaDiagonal.x = svd.getW().getX();
   //            localInertiaDiagonal.y = svd.getW().getY();
   //            localInertiaDiagonal.z = svd.getW().getZ();
   //         }
   //         else
   //         {
   //            LogTools.warn("SVD did not decompose", rigidBodyDefinition.getName());
   //         }
   //      }
   //      if (localInertiaDiagonal.x < 0 || localInertiaDiagonal.x > (localInertiaDiagonal.y + localInertiaDiagonal.z) || localInertiaDiagonal.y < 0
   //            || localInertiaDiagonal.y > (localInertiaDiagonal.x + localInertiaDiagonal.z) || localInertiaDiagonal.z < 0
   //            || localInertiaDiagonal.z > (localInertiaDiagonal.x + localInertiaDiagonal.y))
   //      {
   //         LogTools.warn("Bad inertia tensor properties, setting inertia to zero for link:", rigidBodyDefinition.getName());
   //         localInertiaDiagonal.x = 0.f;
   //         localInertiaDiagonal.y = 0.f;
   //         localInertiaDiagonal.z = 0.f;
   //      }
   //      return localInertiaDiagonal;
   //   }

   private static btMultiBodyLinkCollider createBulletLinkCollider(BulletMultiBodyRobot bulletMultiBodyRobot,
                                                                   List<CollisionShapeDefinition> collisionShapeDefinitions,
                                                                   ReferenceFrame linkCenterOfMassFrame,
                                                                   int bulletJointIndex,
                                                                   String jointName)
   {
      BulletMultiBodyLinkCollider bulletMultiBodyLinkCollider = new BulletMultiBodyLinkCollider(bulletMultiBodyRobot.getBtMultiBody(),
                                                                                                bulletJointIndex,
                                                                                                jointName);

      btCompoundShape bulletCompoundShape = new btCompoundShape();
      int collisionGroup = 2;
      int collisionGroupMask = 1 + 2;

      if (collisionShapeDefinitions.size() > 0)
      {
         collisionGroupMask = (int) collisionShapeDefinitions.get(0).getCollisionGroup();
         collisionGroup = (int) collisionShapeDefinitions.get(0).getCollisionMask();
      }

      bulletMultiBodyLinkCollider.setCollisionGroupMask(collisionGroup, collisionGroupMask);

      ArrayList<btCollisionShape> btCollisionShapes = new ArrayList<>();
      for (CollisionShapeDefinition shapeDefinition : collisionShapeDefinitions)
      {
         btCollisionShape bulletCollisionShape = BulletTools.createBulletCollisionShape(shapeDefinition);
         btTransform bulletCollisionShapeLocalTransform = bulletCollisionShapeLocalTransform(shapeDefinition, linkCenterOfMassFrame);
         bulletCompoundShape.addChildShape(bulletCollisionShapeLocalTransform, bulletCollisionShape);
         btCollisionShapes.add(bulletCollisionShape);
      }

      bulletMultiBodyLinkCollider.setCollisionShape(bulletCompoundShape, btCollisionShapes);

      bulletMultiBodyRobot.addBulletMuliBodyLinkCollider(bulletMultiBodyLinkCollider);

      return bulletMultiBodyLinkCollider.getBtMultiBodyLinkCollider();
   }

   private static final btTransform bulletCollisionShapeLocalTransform = new btTransform();
   private static final RigidBodyTransform collisionShapeDefinitionToCenterOfMassFrameTransformEuclid = new RigidBodyTransform();

   public static btTransform bulletCollisionShapeLocalTransform(CollisionShapeDefinition shapeDefinition, ReferenceFrame linkCenterOfMassFrame)
   {
      collisionShapeDefinitionToCenterOfMassFrameTransformEuclid.setAndInvert(linkCenterOfMassFrame.getTransformToParent());
      collisionShapeDefinitionToCenterOfMassFrameTransformEuclid.multiply(new RigidBodyTransform(shapeDefinition.getOriginPose().getRotation(),
                                                                                                 shapeDefinition.getOriginPose().getTranslation()));

      BulletTools.toBullet(collisionShapeDefinitionToCenterOfMassFrameTransformEuclid, bulletCollisionShapeLocalTransform);

      return bulletCollisionShapeLocalTransform;
   }

   private static int countJointsAndCreateIndexMap(JointBasics joint)
   {
      jointNameToBulletJointIndexMap.put(joint.getName(), linkCountingIndex - 1);
      ++linkCountingIndex;

      int numberOfJoints = 1;
      for (JointBasics childrenJoint : joint.getSuccessor().getChildrenJoints())
      {
         numberOfJoints += countJointsAndCreateIndexMap(childrenJoint);
      }
      return numberOfJoints;
   }

}
