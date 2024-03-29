package us.ihmc.scs2.simulation.bullet.physicsEngine;

import org.bytedeco.bullet.LinearMath.btTransform;
import us.ihmc.euclid.transform.RigidBodyTransform;
import us.ihmc.euclid.tuple3D.Vector3D;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;
import java.util.ArrayList;
import org.bytedeco.bullet.BulletCollision.btCollisionShape;
import org.bytedeco.bullet.BulletCollision.btCompoundShape;
import org.bytedeco.bullet.BulletDynamics.btMultiBody;
import org.bytedeco.bullet.BulletDynamics.btMultiBodyLinkCollider;
import org.bytedeco.bullet.LinearMath.btVector3;

public class BulletMultiBodyLinkCollider
{
   private final btMultiBodyLinkCollider btMultiBodyLinkCollider;
   private final btMultiBody btMultiBody;
   private final String jointName;
   private final int linkColliderIndex;
   private final btTransform bulletTempConversionBtTransform = new btTransform();
   private final btVector3 bulletTempConversionVector3 = new btVector3();
   private final btVector3 linkForce;
   private final btVector3 linkTorque;
   private int collisionGroup;
   private int collisionGroupMask;
   private btCompoundShape shape;
   private ArrayList<btCollisionShape> btCollisionShapes = new ArrayList<>();

   public BulletMultiBodyLinkCollider(btMultiBody btMultibody, int index, String jointName)
   {
      btMultiBodyLinkCollider = new btMultiBodyLinkCollider(btMultibody, index);
      this.linkColliderIndex = index;
      this.jointName = jointName;
      this.collisionGroup = 2;
      this.collisionGroupMask = 1 + 2;

      btMultiBody = btMultiBodyLinkCollider.m_multiBody();

      linkForce = btMultiBody.getLink(linkColliderIndex).m_appliedConstraintForce();
      linkTorque = btMultiBody.getLink(linkColliderIndex).m_appliedConstraintTorque();
   }

   public void setCollisionGroupMask(int collisionGroup, int collisionGroupMask)
   {
      this.collisionGroup = collisionGroup;
      this.collisionGroupMask = collisionGroupMask;
   }

   public void setCollisionShape(btCompoundShape shape, ArrayList<btCollisionShape> btCollisionShapes)
   {
      this.setShape(shape);
      this.setBtCollisionShapes(btCollisionShapes);
      btMultiBodyLinkCollider.setCollisionShape(shape);
   }

   public void setFriction(double friction)
   {
      btMultiBodyLinkCollider.setFriction(friction);
   }

   public void setRestitution(double restitution)
   {
      btMultiBodyLinkCollider.setRestitution(restitution);
   }

   public void setHitFraction(double hitFraction)
   {
      btMultiBodyLinkCollider.setHitFraction(hitFraction);
   }

   public void setRollingFriction(double rollingFriction)
   {
      btMultiBodyLinkCollider.setRollingFriction(rollingFriction);
   }

   public void setSpinningFriction(double spinningFriction)
   {
      btMultiBodyLinkCollider.setSpinningFriction(spinningFriction);
   }

   public void setContactProcessingThreshold(double contactProcessingThreshold)
   {
      btMultiBodyLinkCollider.setContactProcessingThreshold(contactProcessingThreshold);
   }

   public btMultiBodyLinkCollider getBtMultiBodyLinkCollider()
   {
      return btMultiBodyLinkCollider;
   }

   public int getCollisionGroup()
   {
      return collisionGroup;
   }

   public int getCollisionGroupMask()
   {
      return collisionGroupMask;
   }

   public String getJointName()
   {
      return jointName;
   }

   public void getWorldTransform(RigidBodyTransform jointSuccessorBodyFixedFrameToWorldEuclid)
   {
      BulletTools.toEuclid(btMultiBodyLinkCollider.getWorldTransform(), jointSuccessorBodyFixedFrameToWorldEuclid);
   }

   public void setWorldTransform(RigidBodyTransform bulletColliderCenterOfMassTransformToWorldEuclid)
   {
      BulletTools.toBullet(bulletColliderCenterOfMassTransformToWorldEuclid, bulletTempConversionBtTransform);
      btMultiBodyLinkCollider.setWorldTransform(bulletTempConversionBtTransform);

      if (linkColliderIndex == -1)
         btMultiBody.setBaseWorldTransform(bulletTempConversionBtTransform);
   }

   public void setJointPos(double jointPosition)
   {
      btMultiBody.setJointPos(linkColliderIndex, jointPosition);
   }

   public void setJointVel(double jointVelocity)
   {
      btMultiBody.setJointVel(linkColliderIndex, jointVelocity);
   }

   public void addJointTorque(double jointTau)
   {
      btMultiBody.addJointTorque(linkColliderIndex, jointTau);
   }

   public double getJointPos()
   {
      return btMultiBody.getJointPos(linkColliderIndex);
   }

   public double getJointVel()
   {
      return btMultiBody.getJointVel(linkColliderIndex);
   }

   public void getAppliedConstraintForce(Vector3D force)
   {

      force.set(linkForce.getX(), linkForce.getY(), linkForce.getZ());
   }

   public void getAppliedConstraintTorque(Vector3D torque)
   {
      torque.set(linkTorque.getX(), linkTorque.getY(), linkTorque.getZ());
   }

   public void setBaseVel(Vector3DReadOnly linearVelocityEuclid)
   {
      BulletTools.toBullet(linearVelocityEuclid, bulletTempConversionVector3);
      btMultiBody.setBaseVel(bulletTempConversionVector3);
   }

   public void setBaseOmega(Vector3DReadOnly angularVelocityBulletEuclid)
   {
      BulletTools.toBullet(angularVelocityBulletEuclid, bulletTempConversionVector3);
      btMultiBody.setBaseOmega(bulletTempConversionVector3);
   }

   public void getBaseVel(Vector3D bulletBaseLinearVelocityEuclid)
   {
      BulletTools.toEuclid(btMultiBody.getBaseVel(), bulletBaseLinearVelocityEuclid);
   }

   public void getBaseOmega(Vector3D bulletBaseAngularVelocityEuclid)
   {
      BulletTools.toEuclid(btMultiBody.getBaseOmega(), bulletBaseAngularVelocityEuclid);
   }

   public btCompoundShape getShape()
   {
      return shape;
   }

   public void setShape(btCompoundShape shape)
   {
      this.shape = shape;
   }

   public ArrayList<btCollisionShape> getBtCollisionShapes()
   {
      return btCollisionShapes;
   }

   public void setBtCollisionShapes(ArrayList<btCollisionShape> btCollisionShapes)
   {
      this.btCollisionShapes = btCollisionShapes;
   }
}
