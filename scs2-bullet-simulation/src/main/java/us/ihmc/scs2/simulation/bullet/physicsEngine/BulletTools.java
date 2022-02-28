package us.ihmc.scs2.simulation.bullet.physicsEngine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import us.ihmc.euclid.geometry.interfaces.Pose3DReadOnly;
import us.ihmc.euclid.matrix.RotationMatrix;
import us.ihmc.euclid.transform.AffineTransform;
import us.ihmc.euclid.transform.RigidBodyTransform;
import us.ihmc.euclid.tuple2D.interfaces.Tuple2DReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Point3DBasics;
import us.ihmc.euclid.tuple3D.interfaces.Point3DReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Tuple3DReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DBasics;

public class BulletTools
{

   public static void toBullet(AffineTransform euclidAffine, Matrix4 BulletAffineToPack)
   {
      BulletAffineToPack.val[Matrix4.M00] = (float) euclidAffine.getM00();
      BulletAffineToPack.val[Matrix4.M01] = (float) euclidAffine.getM01();
      BulletAffineToPack.val[Matrix4.M02] = (float) euclidAffine.getM02();
      BulletAffineToPack.val[Matrix4.M10] = (float) euclidAffine.getM10();
      BulletAffineToPack.val[Matrix4.M11] = (float) euclidAffine.getM11();
      BulletAffineToPack.val[Matrix4.M12] = (float) euclidAffine.getM12();
      BulletAffineToPack.val[Matrix4.M20] = (float) euclidAffine.getM20();
      BulletAffineToPack.val[Matrix4.M21] = (float) euclidAffine.getM21();
      BulletAffineToPack.val[Matrix4.M22] = (float) euclidAffine.getM22();
      BulletAffineToPack.val[Matrix4.M03] = (float) euclidAffine.getM03();
      BulletAffineToPack.val[Matrix4.M13] = (float) euclidAffine.getM13();
      BulletAffineToPack.val[Matrix4.M23] = (float) euclidAffine.getM23();
   }

   public static void toEuclid(Matrix4 BulletAffine, AffineTransform euclidAffine)
   {
      euclidAffine.getLinearTransform().setM00(BulletAffine.val[Matrix4.M00]);
      euclidAffine.getLinearTransform().setM01(BulletAffine.val[Matrix4.M01]);
      euclidAffine.getLinearTransform().setM02(BulletAffine.val[Matrix4.M02]);
      euclidAffine.getLinearTransform().setM10(BulletAffine.val[Matrix4.M10]);
      euclidAffine.getLinearTransform().setM11(BulletAffine.val[Matrix4.M11]);
      euclidAffine.getLinearTransform().setM12(BulletAffine.val[Matrix4.M12]);
      euclidAffine.getLinearTransform().setM20(BulletAffine.val[Matrix4.M20]);
      euclidAffine.getLinearTransform().setM21(BulletAffine.val[Matrix4.M21]);
      euclidAffine.getLinearTransform().setM22(BulletAffine.val[Matrix4.M22]);
      euclidAffine.getLinearTransform().normalize();
      euclidAffine.getTranslation().setX(BulletAffine.val[Matrix4.M03]);
      euclidAffine.getTranslation().setY(BulletAffine.val[Matrix4.M13]);
      euclidAffine.getTranslation().setZ(BulletAffine.val[Matrix4.M23]);
   }

   public static void toEuclid(Matrix4 BulletAffine, RotationMatrix euclidRotationMatrix)
   {
      euclidRotationMatrix.setAndNormalize(BulletAffine.val[Matrix4.M00],
                                           BulletAffine.val[Matrix4.M01],
                                           BulletAffine.val[Matrix4.M02],
                                           BulletAffine.val[Matrix4.M10],
                                           BulletAffine.val[Matrix4.M11],
                                           BulletAffine.val[Matrix4.M12],
                                           BulletAffine.val[Matrix4.M20],
                                           BulletAffine.val[Matrix4.M21],
                                           BulletAffine.val[Matrix4.M22]);
   }

   public static void toBullet(RigidBodyTransform rigidBodyTransform, Matrix4 BulletAffineToPack)
   {
      BulletAffineToPack.val[Matrix4.M00] = (float) rigidBodyTransform.getM00();
      BulletAffineToPack.val[Matrix4.M01] = (float) rigidBodyTransform.getM01();
      BulletAffineToPack.val[Matrix4.M02] = (float) rigidBodyTransform.getM02();
      BulletAffineToPack.val[Matrix4.M10] = (float) rigidBodyTransform.getM10();
      BulletAffineToPack.val[Matrix4.M11] = (float) rigidBodyTransform.getM11();
      BulletAffineToPack.val[Matrix4.M12] = (float) rigidBodyTransform.getM12();
      BulletAffineToPack.val[Matrix4.M20] = (float) rigidBodyTransform.getM20();
      BulletAffineToPack.val[Matrix4.M21] = (float) rigidBodyTransform.getM21();
      BulletAffineToPack.val[Matrix4.M22] = (float) rigidBodyTransform.getM22();
      BulletAffineToPack.val[Matrix4.M03] = (float) rigidBodyTransform.getM03();
      BulletAffineToPack.val[Matrix4.M13] = (float) rigidBodyTransform.getM13();
      BulletAffineToPack.val[Matrix4.M23] = (float) rigidBodyTransform.getM23();
   }

   public static void toEuclid(Matrix4 BulletAffine, RigidBodyTransform rigidBodyTransform)
   {
      rigidBodyTransform.getRotation().setAndNormalize(BulletAffine.val[Matrix4.M00],
                                                       BulletAffine.val[Matrix4.M01],
                                                       BulletAffine.val[Matrix4.M02],
                                                       BulletAffine.val[Matrix4.M10],
                                                       BulletAffine.val[Matrix4.M11],
                                                       BulletAffine.val[Matrix4.M12],
                                                       BulletAffine.val[Matrix4.M20],
                                                       BulletAffine.val[Matrix4.M21],
                                                       BulletAffine.val[Matrix4.M22]);
      rigidBodyTransform.getTranslation().setX(BulletAffine.val[Matrix4.M03]);
      rigidBodyTransform.getTranslation().setY(BulletAffine.val[Matrix4.M13]);
      rigidBodyTransform.getTranslation().setZ(BulletAffine.val[Matrix4.M23]);
   }

   public static void toBullet(RotationMatrix euclidRotationMatrix, Matrix4 BulletRotationMatrix)
   {
      BulletRotationMatrix.val[Matrix4.M00] = (float) euclidRotationMatrix.getM00();
      BulletRotationMatrix.val[Matrix4.M01] = (float) euclidRotationMatrix.getM01();
      BulletRotationMatrix.val[Matrix4.M02] = (float) euclidRotationMatrix.getM02();
      BulletRotationMatrix.val[Matrix4.M10] = (float) euclidRotationMatrix.getM10();
      BulletRotationMatrix.val[Matrix4.M11] = (float) euclidRotationMatrix.getM11();
      BulletRotationMatrix.val[Matrix4.M12] = (float) euclidRotationMatrix.getM12();
      BulletRotationMatrix.val[Matrix4.M20] = (float) euclidRotationMatrix.getM20();
      BulletRotationMatrix.val[Matrix4.M21] = (float) euclidRotationMatrix.getM21();
      BulletRotationMatrix.val[Matrix4.M22] = (float) euclidRotationMatrix.getM22();
   }

   public static void toBullet(us.ihmc.euclid.tuple4D.Quaternion euclidQuaternion, Quaternion BulletQuaternion)
   {
      BulletQuaternion.x = euclidQuaternion.getX32();
      BulletQuaternion.y = euclidQuaternion.getY32();
      BulletQuaternion.z = euclidQuaternion.getZ32();
      BulletQuaternion.w = euclidQuaternion.getS32();
   }

   public static Vector3 toBullet(Tuple3DReadOnly euclidTuple)
   {
      return new Vector3(euclidTuple.getX32(), euclidTuple.getY32(), euclidTuple.getZ32());
   }

   public static Vector2 toBullet(Tuple2DReadOnly euclidTuple)
   {
      return new Vector2(euclidTuple.getX32(), euclidTuple.getY32());
   }

   public static void toBullet(Tuple3DReadOnly euclidTuple, Vector3 BulletVector3)
   {
      BulletVector3.set(euclidTuple.getX32(), euclidTuple.getY32(), euclidTuple.getZ32());
   }

   public static void toEuclid(Vector3 BulletVector3, Vector3DBasics euclidVector3D32)
   {
      euclidVector3D32.set(BulletVector3.x, BulletVector3.y, BulletVector3.z);
   }

   public static void toEuclid(Vector3 BulletVector3, Point3DBasics euclidPoint3D32)
   {
      euclidPoint3D32.set(BulletVector3.x, BulletVector3.y, BulletVector3.z);
   }

   public static void toEuclid(Matrix4 BulletAffine, Point3DBasics euclidPoint)
   {
      euclidPoint.set(BulletAffine.val[Matrix4.M03],
                      BulletAffine.val[Matrix4.M13],
                      BulletAffine.val[Matrix4.M23]);
   }

   public static void toBullet(Point3DReadOnly euclidPoint, Matrix4 BulletAffine)
   {
      BulletAffine.setTranslation(euclidPoint.getX32(), euclidPoint.getY32(), euclidPoint.getZ32());
   }

   public static void toBullet(Pose3DReadOnly euclidPose, RigidBodyTransform tempTransform, Matrix4 BulletAffine)
   {
      euclidPose.get(tempTransform);
      toBullet(tempTransform, BulletAffine);
   }

   public static void toBullet(javafx.scene.paint.Color javaFXColor, Color BulletColor)
   {
      BulletColor.set((float) javaFXColor.getRed(), (float) javaFXColor.getGreen(), (float) javaFXColor.getBlue(), (float) javaFXColor.getOpacity());
   }

   public static Color toBullet(javafx.scene.paint.Color javaFXColor)
   {
      return new Color((float) javaFXColor.getRed(), (float) javaFXColor.getGreen(), (float) javaFXColor.getBlue(), (float) javaFXColor.getOpacity());
   }
 
}
