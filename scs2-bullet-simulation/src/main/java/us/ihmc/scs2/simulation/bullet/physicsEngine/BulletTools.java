package us.ihmc.scs2.simulation.bullet.physicsEngine;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.InternalTickCallback;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btMultiBodyDynamicsWorld;
import com.badlogic.gdx.physics.bullet.linearmath.LinearMath;
import com.badlogic.gdx.physics.bullet.linearmath.btVector3;
import com.badlogic.gdx.physics.bullet.linearmath.btVector3Array;
import us.ihmc.euclid.geometry.interfaces.Pose3DReadOnly;
import us.ihmc.euclid.matrix.RotationMatrix;
import us.ihmc.euclid.transform.AffineTransform;
import us.ihmc.euclid.transform.RigidBodyTransform;
import us.ihmc.euclid.tuple2D.interfaces.Tuple2DReadOnly;
import us.ihmc.euclid.tuple3D.Point3D32;
import us.ihmc.euclid.tuple3D.Vector3D32;
import us.ihmc.euclid.tuple3D.interfaces.Point3DBasics;
import us.ihmc.euclid.tuple3D.interfaces.Point3DReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Tuple3DReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DBasics;
import us.ihmc.log.LogTools;
import us.ihmc.scs2.simulation.bullet.physicsEngine.modelLoader.AssimpLoader;

public class BulletTools
{
   private static boolean bulletInitialized = false;

   public static void ensureBulletInitialized()
   {
      if (!bulletInitialized)
      {
         bulletInitialized = true;
         Bullet.init();
         LogTools.info("Loaded Bullet version {}", LinearMath.btGetVersion());
      }
   }

   public static void toBullet(AffineTransform euclidAffine, Matrix4 bulletAffineToPack)
   {
      bulletAffineToPack.val[Matrix4.M00] = (float) euclidAffine.getM00();
      bulletAffineToPack.val[Matrix4.M01] = (float) euclidAffine.getM01();
      bulletAffineToPack.val[Matrix4.M02] = (float) euclidAffine.getM02();
      bulletAffineToPack.val[Matrix4.M10] = (float) euclidAffine.getM10();
      bulletAffineToPack.val[Matrix4.M11] = (float) euclidAffine.getM11();
      bulletAffineToPack.val[Matrix4.M12] = (float) euclidAffine.getM12();
      bulletAffineToPack.val[Matrix4.M20] = (float) euclidAffine.getM20();
      bulletAffineToPack.val[Matrix4.M21] = (float) euclidAffine.getM21();
      bulletAffineToPack.val[Matrix4.M22] = (float) euclidAffine.getM22();
      bulletAffineToPack.val[Matrix4.M03] = (float) euclidAffine.getM03();
      bulletAffineToPack.val[Matrix4.M13] = (float) euclidAffine.getM13();
      bulletAffineToPack.val[Matrix4.M23] = (float) euclidAffine.getM23();
   }

   public static void toEuclid(Matrix4 bulletAffine, AffineTransform euclidAffine)
   {
      euclidAffine.getLinearTransform().setM00(bulletAffine.val[Matrix4.M00]);
      euclidAffine.getLinearTransform().setM01(bulletAffine.val[Matrix4.M01]);
      euclidAffine.getLinearTransform().setM02(bulletAffine.val[Matrix4.M02]);
      euclidAffine.getLinearTransform().setM10(bulletAffine.val[Matrix4.M10]);
      euclidAffine.getLinearTransform().setM11(bulletAffine.val[Matrix4.M11]);
      euclidAffine.getLinearTransform().setM12(bulletAffine.val[Matrix4.M12]);
      euclidAffine.getLinearTransform().setM20(bulletAffine.val[Matrix4.M20]);
      euclidAffine.getLinearTransform().setM21(bulletAffine.val[Matrix4.M21]);
      euclidAffine.getLinearTransform().setM22(bulletAffine.val[Matrix4.M22]);
      euclidAffine.getLinearTransform().normalize();
      euclidAffine.getTranslation().setX(bulletAffine.val[Matrix4.M03]);
      euclidAffine.getTranslation().setY(bulletAffine.val[Matrix4.M13]);
      euclidAffine.getTranslation().setZ(bulletAffine.val[Matrix4.M23]);
   }

   public static void toEuclid(Matrix4 bulletAffine, RotationMatrix euclidRotationMatrix)
   {
      euclidRotationMatrix.setAndNormalize(bulletAffine.val[Matrix4.M00],
                                           bulletAffine.val[Matrix4.M01],
                                           bulletAffine.val[Matrix4.M02],
                                           bulletAffine.val[Matrix4.M10],
                                           bulletAffine.val[Matrix4.M11],
                                           bulletAffine.val[Matrix4.M12],
                                           bulletAffine.val[Matrix4.M20],
                                           bulletAffine.val[Matrix4.M21],
                                           bulletAffine.val[Matrix4.M22]);
   }

   public static void toBullet(RigidBodyTransform rigidBodyTransform, Matrix4 bulletAffineToPack)
   {
      bulletAffineToPack.val[Matrix4.M00] = (float) rigidBodyTransform.getM00();
      bulletAffineToPack.val[Matrix4.M01] = (float) rigidBodyTransform.getM01();
      bulletAffineToPack.val[Matrix4.M02] = (float) rigidBodyTransform.getM02();
      bulletAffineToPack.val[Matrix4.M10] = (float) rigidBodyTransform.getM10();
      bulletAffineToPack.val[Matrix4.M11] = (float) rigidBodyTransform.getM11();
      bulletAffineToPack.val[Matrix4.M12] = (float) rigidBodyTransform.getM12();
      bulletAffineToPack.val[Matrix4.M20] = (float) rigidBodyTransform.getM20();
      bulletAffineToPack.val[Matrix4.M21] = (float) rigidBodyTransform.getM21();
      bulletAffineToPack.val[Matrix4.M22] = (float) rigidBodyTransform.getM22();
      bulletAffineToPack.val[Matrix4.M03] = (float) rigidBodyTransform.getM03();
      bulletAffineToPack.val[Matrix4.M13] = (float) rigidBodyTransform.getM13();
      bulletAffineToPack.val[Matrix4.M23] = (float) rigidBodyTransform.getM23();
   }

   public static void toEuclid(Matrix4 bulletAffine, RigidBodyTransform rigidBodyTransform)
   {
      rigidBodyTransform.getRotation().setAndNormalize(bulletAffine.val[Matrix4.M00],
                                                       bulletAffine.val[Matrix4.M01],
                                                       bulletAffine.val[Matrix4.M02],
                                                       bulletAffine.val[Matrix4.M10],
                                                       bulletAffine.val[Matrix4.M11],
                                                       bulletAffine.val[Matrix4.M12],
                                                       bulletAffine.val[Matrix4.M20],
                                                       bulletAffine.val[Matrix4.M21],
                                                       bulletAffine.val[Matrix4.M22]);
      rigidBodyTransform.getTranslation().setX(bulletAffine.val[Matrix4.M03]);
      rigidBodyTransform.getTranslation().setY(bulletAffine.val[Matrix4.M13]);
      rigidBodyTransform.getTranslation().setZ(bulletAffine.val[Matrix4.M23]);
   }

   public static void toBullet(RotationMatrix euclidRotationMatrix, Matrix4 bulletRotationMatrix)
   {
      bulletRotationMatrix.val[Matrix4.M00] = (float) euclidRotationMatrix.getM00();
      bulletRotationMatrix.val[Matrix4.M01] = (float) euclidRotationMatrix.getM01();
      bulletRotationMatrix.val[Matrix4.M02] = (float) euclidRotationMatrix.getM02();
      bulletRotationMatrix.val[Matrix4.M10] = (float) euclidRotationMatrix.getM10();
      bulletRotationMatrix.val[Matrix4.M11] = (float) euclidRotationMatrix.getM11();
      bulletRotationMatrix.val[Matrix4.M12] = (float) euclidRotationMatrix.getM12();
      bulletRotationMatrix.val[Matrix4.M20] = (float) euclidRotationMatrix.getM20();
      bulletRotationMatrix.val[Matrix4.M21] = (float) euclidRotationMatrix.getM21();
      bulletRotationMatrix.val[Matrix4.M22] = (float) euclidRotationMatrix.getM22();
   }

   public static void toBullet(us.ihmc.euclid.tuple4D.Quaternion euclidQuaternion, Quaternion bulletQuaternion)
   {
      bulletQuaternion.x = euclidQuaternion.getX32();
      bulletQuaternion.y = euclidQuaternion.getY32();
      bulletQuaternion.z = euclidQuaternion.getZ32();
      bulletQuaternion.w = euclidQuaternion.getS32();
   }

   public static Vector3 toBullet(Tuple3DReadOnly euclidTuple)
   {
      return new Vector3(euclidTuple.getX32(), euclidTuple.getY32(), euclidTuple.getZ32());
   }

   public static Vector2 toBullet(Tuple2DReadOnly euclidTuple)
   {
      return new Vector2(euclidTuple.getX32(), euclidTuple.getY32());
   }

   public static void toBullet(Tuple3DReadOnly euclidTuple, Vector3 bulletVector3)
   {
      bulletVector3.set(euclidTuple.getX32(), euclidTuple.getY32(), euclidTuple.getZ32());
   }

   public static void toEuclid(Vector3 bulletVector3, Vector3DBasics euclidVector3D32)
   {
      euclidVector3D32.set(bulletVector3.x, bulletVector3.y, bulletVector3.z);
   }

   public static void toEuclid(Vector3 bulletVector3, Point3DBasics euclidPoint3D32)
   {
      euclidPoint3D32.set(bulletVector3.x, bulletVector3.y, bulletVector3.z);
   }

   public static void toEuclid(Matrix4 bulletAffine, Point3DBasics euclidPoint)
   {
      euclidPoint.set(bulletAffine.val[Matrix4.M03], bulletAffine.val[Matrix4.M13], bulletAffine.val[Matrix4.M23]);
   }

   public static void toBullet(Point3DReadOnly euclidPoint, Matrix4 bulletAffine)
   {
      bulletAffine.setTranslation(euclidPoint.getX32(), euclidPoint.getY32(), euclidPoint.getZ32());
   }

   public static void toBullet(Pose3DReadOnly euclidPose, RigidBodyTransform tempTransform, Matrix4 bulletAffine)
   {
      euclidPose.get(tempTransform);
      toBullet(tempTransform, bulletAffine);
   }

   public static void toBullet(javafx.scene.paint.Color javaFXColor, Color bulletColor)
   {
      bulletColor.set((float) javaFXColor.getRed(), (float) javaFXColor.getGreen(), (float) javaFXColor.getBlue(), (float) javaFXColor.getOpacity());
   }

   public static Color toBullet(javafx.scene.paint.Color javaFXColor)
   {
      return new Color((float) javaFXColor.getRed(), (float) javaFXColor.getGreen(), (float) javaFXColor.getBlue(), (float) javaFXColor.getOpacity());
   }

   public static List<btConvexPointCloudShape> loadConvexPointCloudShapesFromFile(String modelFilePath)
   {
      List<HashSet<Vector3D32>> vertexSets = AssimpLoader.loadUniqueVertexPositions(modelFilePath);
      List<btConvexPointCloudShape> shapes = new ArrayList<>();
      for (HashSet<Vector3D32> vertexSet : vertexSets)
      {
         btVector3Array bulletVector3Array = new btVector3Array();

         for (Vector3D32 vector3D32 : vertexSet)
         {
            bulletVector3Array.expand(new Vector3(vector3D32.getX32(), vector3D32.getY32(), vector3D32.getZ32()));
         }
         bulletVector3Array.releaseOwnership();

         btVector3 points = new btVector3(bulletVector3Array.getCPointer(), bulletVector3Array.hasOwnership());
         int numberOfPoints = vertexSet.size();
         Vector3 localScaling = new Vector3(1.0f, 1.0f, 1.0f);
         bulletVector3Array.releaseOwnership();
         btConvexPointCloudShape bulletConvexPointCloudShape = new btConvexPointCloudShape(points, numberOfPoints, localScaling, true);
         shapes.add(bulletConvexPointCloudShape);
      }
      return shapes;
   }

   public static List<btConvexHullShape> loadConvexHullShapeFromFile(String modelFilePath)
   {
      List<FloatBuffer> vertexBuffers = AssimpLoader.loadTriangleVertexPositionsAsFloatBuffer(modelFilePath);
      List<btConvexHullShape> shapes = new ArrayList<>();

      for (FloatBuffer vertexBuffer : vertexBuffers)
      {
         int bytesPerVertex = 3 * Float.BYTES;
         int numberOfPoints = vertexBuffer.limit() / bytesPerVertex;
         btConvexHullShape concaveHullShapeFromMesh = createConcaveHullShapeFromMesh(vertexBuffer, numberOfPoints, bytesPerVertex);
         shapes.add(concaveHullShapeFromMesh);
      }
      return shapes;
   }

   public static List<btConvexTriangleMeshShape> loadConvexTriangleMeshShapeFromFile(String modelFilePath)
   {
      List<List<Point3D32>> vertexLists = AssimpLoader.loadTriangleVertexPositionsAsList(modelFilePath);
      List<btConvexTriangleMeshShape> shapes = new ArrayList<>();

      for (List<Point3D32> vertexList : vertexLists)
      {
         btTriangleMesh bulletTriangleMesh = new btTriangleMesh(false, false);

         for (int i = 0; i < vertexList.size(); i += 3)
         {
            bulletTriangleMesh.addTriangle(new Vector3(vertexList.get(i).getX32(),
                                                       vertexList.get(i).getY32(),
                                                       vertexList.get(i).getZ32()),
                                           new Vector3(vertexList.get(i + 1).getX32(),
                                                       vertexList.get(i + 1).getY32(),
                                                       vertexList.get(i + 1).getZ32()),
                                           new Vector3(vertexList.get(i + 2).getX32(),
                                                       vertexList.get(i + 2).getY32(),
                                                       vertexList.get(i + 2).getZ32()));
         }
         bulletTriangleMesh.releaseOwnership();

         btConvexTriangleMeshShape bulletConvexTriangleMeshShape = new btConvexTriangleMeshShape(bulletTriangleMesh);

         shapes.add(bulletConvexTriangleMeshShape);
      }
      return shapes;
   }

   public static List<btGImpactMeshShape> loadConcaveGImpactMeshShapeFromFile(String modelFilePath)
   {
      List<List<Point3D32>> vertexLists = AssimpLoader.loadTriangleVertexPositionsAsList(modelFilePath);
      List<btGImpactMeshShape> shapes = new ArrayList<>();

      for (List<Point3D32> vertexList : vertexLists)
      {
         btTriangleMesh bulletTriangleMesh = new btTriangleMesh(false, false);

         for (int i = 0; i < vertexList.size(); i += 3)
         {
            bulletTriangleMesh.addTriangle(new Vector3(vertexList.get(i).getX32(),
                                                       vertexList.get(i).getY32(),
                                                       vertexList.get(i).getZ32()),
                                           new Vector3(vertexList.get(i + 1).getX32(),
                                                       vertexList.get(i + 1).getY32(),
                                                       vertexList.get(i + 1).getZ32()),
                                           new Vector3(vertexList.get(i + 2).getX32(),
                                                       vertexList.get(i + 2).getY32(),
                                                       vertexList.get(i + 2).getZ32()));
         }
         bulletTriangleMesh.releaseOwnership();

         btGImpactMeshShape bulletConvexTriangleMeshShape = new btGImpactMeshShape(bulletTriangleMesh);
         bulletConvexTriangleMeshShape.updateBound();

         shapes.add(bulletConvexTriangleMeshShape);
      }
      return shapes;
   }

   public static btConvexHullShape createConcaveHullShapeFromMesh(FloatBuffer floatBuffer, int numberOfPoints, int stride)
   {
      floatBuffer.rewind();
      return new btConvexHullShape(floatBuffer, numberOfPoints, stride);
   }

   public static void setupPostTickCallback(btMultiBodyDynamicsWorld multiBodyDynamicsWorld, ArrayList<Runnable> postTickCallbacks)
   {
      // Note: Apparently you can't have both pre and post tick callbacks, so we'll just do with post
      new InternalTickCallback(multiBodyDynamicsWorld, false)
      {
         @Override
         public void onInternalTick(btDynamicsWorld dynamicsWorld, float timeStep)
         {
            for (Runnable postTickRunnable : postTickCallbacks)
            {
               postTickRunnable.run();
            }
         }
      };
   }
}