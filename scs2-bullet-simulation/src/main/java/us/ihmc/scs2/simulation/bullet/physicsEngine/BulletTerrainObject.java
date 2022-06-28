package us.ihmc.scs2.simulation.bullet.physicsEngine;

import java.util.ArrayList;

import org.bytedeco.bullet.BulletCollision.btCollisionShape;
import org.bytedeco.bullet.LinearMath.btDefaultMotionState;
import org.bytedeco.bullet.LinearMath.btTransform;
import org.bytedeco.bullet.LinearMath.btVector3;
import org.bytedeco.bullet.BulletDynamics.btRigidBody;

public class BulletTerrainObject
{
   private final btDefaultMotionState btMotionState = new btDefaultMotionState()
   {
      @Override
      public void setWorldTransform(btTransform transformToWorld)
      {
         // Should be always 0, the child shapes are statically placed
      }

      @Override
      public void getWorldTransform(btTransform transformToWorld)
      {
         // Should be always 0, the child shapes are statically placed
      }
   };
   private final btRigidBody btRigidBody;
   private final int collisionGroup = 1; // group 1 is rigid and static bodies
   private final int collisionGroupMask = -1; // Allows interaction with all groups (including custom groups)
   private final btCollisionShape btCollisionShape;
   private ArrayList<btCollisionShape> btCollisionShapes = new ArrayList<>();
   private final btVector3 localInertia = new btVector3();
   private ArrayList<btTransform> btTransforms = new ArrayList<>();

   public BulletTerrainObject(float mass, btCollisionShape bulletCompoundCollisionShape, ArrayList<btCollisionShape> btCollisionShapes, ArrayList<btTransform> btTransforms)
   {
      bulletCompoundCollisionShape.calculateLocalInertia(mass, localInertia);

      btRigidBody = new btRigidBody(mass, btMotionState, bulletCompoundCollisionShape, localInertia);
      btCollisionShape = bulletCompoundCollisionShape;
      this.btCollisionShapes = btCollisionShapes;
      this.btTransforms = btTransforms;
   }

   public btRigidBody getBtRigidBody()
   {
      return btRigidBody;
   }

   public int getCollisionGroup()
   {
      return collisionGroup;
   }

   public int getCollisionGroupMask()
   {
      return collisionGroupMask;
   }


}
