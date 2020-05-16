package us.ihmc.scs2;

import us.ihmc.euclid.axisAngle.AxisAngle;
import us.ihmc.euclid.axisAngle.interfaces.AxisAngleReadOnly;
import us.ihmc.euclid.geometry.interfaces.Pose2DReadOnly;
import us.ihmc.euclid.geometry.interfaces.Pose3DReadOnly;
import us.ihmc.euclid.matrix.RotationMatrix;
import us.ihmc.euclid.matrix.interfaces.RotationMatrixReadOnly;
import us.ihmc.euclid.orientation.interfaces.Orientation2DReadOnly;
import us.ihmc.euclid.orientation.interfaces.Orientation3DReadOnly;
import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.euclid.tuple2D.interfaces.Tuple2DReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Tuple3DReadOnly;
import us.ihmc.euclid.tuple4D.Quaternion;
import us.ihmc.euclid.tuple4D.interfaces.QuaternionReadOnly;
import us.ihmc.euclid.yawPitchRoll.YawPitchRoll;
import us.ihmc.euclid.yawPitchRoll.interfaces.YawPitchRollReadOnly;
import us.ihmc.scs2.protobuf.EuclidProto;

public class EuclidProtoTools
{
   public static EuclidProto.Vector2D toProtoVector2D(Tuple2DReadOnly vector2D)
   {
      if (vector2D == null)
         return null;
      return toProtoVector2D(vector2D.getX(), vector2D.getY());
   }

   public static EuclidProto.Vector2D toProtoVector2D(double x, double y)
   {
      return EuclidProto.Vector2D.newBuilder().setX(x).setY(y).build();
   }

   public static EuclidProto.Point2D toProtoPoint2D(Tuple2DReadOnly point2D)
   {
      if (point2D == null)
         return null;
      return toProtoPoint2D(point2D.getX(), point2D.getY());
   }

   public static EuclidProto.Point2D toProtoPoint2D(double x, double y)
   {
      return EuclidProto.Point2D.newBuilder().setX(x).setY(y).build();
   }

   public static EuclidProto.Orientation2D toProtoOrientation2D(Orientation2DReadOnly orientation2D)
   {
      if (orientation2D == null)
         return null;
      return toProtoOrientation2D(orientation2D.getYaw());
   }

   public static EuclidProto.Orientation2D toProtoOrientation2D(double yaw)
   {
      return EuclidProto.Orientation2D.newBuilder().setYaw(yaw).build();
   }

   public static EuclidProto.Vector3D toProtoVector3D(Tuple3DReadOnly vector3D)
   {
      if (vector3D == null)
         return null;
      return toProtoVector3D(vector3D.getX(), vector3D.getY(), vector3D.getZ());
   }

   public static EuclidProto.Vector3D toProtoVector3D(double x, double y, double z)
   {
      return EuclidProto.Vector3D.newBuilder().setX(x).setY(y).setZ(z).build();
   }

   public static EuclidProto.Point3D toProtoPoint3D(Tuple3DReadOnly point3D)
   {
      if (point3D == null)
         return null;
      return toProtoPoint3D(point3D.getX(), point3D.getY(), point3D.getZ());
   }

   public static EuclidProto.Point3D toProtoPoint3D(double x, double y, double z)
   {
      return EuclidProto.Point3D.newBuilder().setX(x).setY(y).setZ(z).build();
   }

   public static EuclidProto.Quaternion toProtoQuaternion(Orientation3DReadOnly orientation3D)
   {
      if (orientation3D == null)
         return null;
      if (orientation3D instanceof QuaternionReadOnly)
         return toProtoQuaternion((QuaternionReadOnly) orientation3D);
      return toProtoQuaternion(new Quaternion(orientation3D));
   }

   public static EuclidProto.Quaternion toProtoQuaternion(QuaternionReadOnly quaternion)
   {
      if (quaternion == null)
         return null;
      return toProtoQuaternion(quaternion.getX(), quaternion.getY(), quaternion.getZ(), quaternion.getS());
   }

   public static EuclidProto.Quaternion toProtoQuaternion(double x, double y, double z, double s)
   {
      return EuclidProto.Quaternion.newBuilder().setX(x).setY(y).setZ(z).setS(s).build();
   }

   public static EuclidProto.YawPitchRoll toProtoYawPitchRoll(Orientation3DReadOnly orientation3D)
   {
      if (orientation3D == null)
         return null;
      if (orientation3D instanceof YawPitchRollReadOnly)
         return toProtoYawPitchRoll((YawPitchRollReadOnly) orientation3D);
      return toProtoYawPitchRoll(new YawPitchRoll(orientation3D));
   }

   public static EuclidProto.YawPitchRoll toProtoYawPitchRoll(YawPitchRollReadOnly yawPitchRoll)
   {
      if (yawPitchRoll == null)
         return null;
      return toProtoYawPitchRoll(yawPitchRoll.getYaw(), yawPitchRoll.getPitch(), yawPitchRoll.getRoll());
   }

   public static EuclidProto.YawPitchRoll toProtoYawPitchRoll(double yaw, double pitch, double roll)
   {
      return EuclidProto.YawPitchRoll.newBuilder().setYaw(yaw).setPitch(pitch).setRoll(roll).build();
   }

   public static EuclidProto.AxisAngle toProtoAxisAngle(Orientation3DReadOnly orientation3D)
   {
      if (orientation3D == null)
         return null;
      if (orientation3D instanceof AxisAngleReadOnly)
         return toProtoAxisAngle((AxisAngleReadOnly) orientation3D);
      return toProtoAxisAngle(new AxisAngle(orientation3D));
   }

   public static EuclidProto.AxisAngle toProtoAxisAngle(AxisAngleReadOnly axisAngle)
   {
      if (axisAngle == null)
         return null;
      return toProtoAxisAngle(axisAngle.getX(), axisAngle.getY(), axisAngle.getZ(), axisAngle.getAngle());
   }

   public static EuclidProto.AxisAngle toProtoAxisAngle(double x, double y, double z, double angle)
   {
      return EuclidProto.AxisAngle.newBuilder().setX(x).setY(y).setZ(z).setAngle(angle).build();
   }

   public static EuclidProto.RotationMatrix toProtoRotationMatrix(Orientation3DReadOnly orientation3D)
   {
      if (orientation3D == null)
         return null;
      return toProtoRotationMatrix(new RotationMatrix(orientation3D));
   }

   public static EuclidProto.RotationMatrix toProtoRotationMatrix(RotationMatrixReadOnly rotationMatrix)
   {
      if (rotationMatrix == null)
         return null;
      return toProtoRotationMatrix(rotationMatrix.getM00(),
                                   rotationMatrix.getM01(),
                                   rotationMatrix.getM02(),
                                   rotationMatrix.getM10(),
                                   rotationMatrix.getM11(),
                                   rotationMatrix.getM12(),
                                   rotationMatrix.getM20(),
                                   rotationMatrix.getM21(),
                                   rotationMatrix.getM22());
   }

   public static EuclidProto.RotationMatrix toProtoRotationMatrix(double m00, double m01, double m02, double m10, double m11, double m12, double m20,
                                                                  double m21, double m22)
   {
      return EuclidProto.RotationMatrix.newBuilder().setM00(m00).setM01(m01).setM02(m02).setM10(m10).setM11(m11).setM12(m12).setM20(m20).setM21(m21).setM22(m22)
                                       .build();
   }

   public static EuclidProto.RigidBodyTransform toProtoRigidBodyTransform(RigidBodyTransformReadOnly rigidBodyTransform)
   {
      if (rigidBodyTransform == null)
         return null;
      return toProtoRigidBodyTransform(rigidBodyTransform.getRotation(), rigidBodyTransform.getTranslation());
   }

   public static EuclidProto.RigidBodyTransform toProtoRigidBodyTransform(Orientation3DReadOnly rotation, Tuple3DReadOnly translation)
   {
      if (rotation == null && translation == null)
         return null;
      EuclidProto.RigidBodyTransform.Builder builder = EuclidProto.RigidBodyTransform.newBuilder();
      if (rotation != null)
         builder.setRotation(toProtoRotationMatrix(rotation));
      if (translation != null)
         builder.setTranslation(toProtoVector3D(translation));
      return builder.build();
   }

   public static EuclidProto.Pose2D toProtoPose2D(Pose2DReadOnly pose2D)
   {
      if (pose2D == null)
         return null;
      return toProtoPose2D(pose2D.getOrientation(), pose2D.getPosition());
   }

   public static EuclidProto.Pose2D toProtoPose2D(Orientation2DReadOnly orientation, Tuple2DReadOnly position)
   {
      if (orientation == null && position == null)
         return null;
      EuclidProto.Pose2D.Builder builder = EuclidProto.Pose2D.newBuilder();
      if (orientation != null)
         builder.setOrientation(toProtoOrientation2D(orientation));
      if (position != null)
         builder.setPosition(toProtoPoint2D(position));
      return builder.build();
   }

   public static EuclidProto.Pose3D toProtoPose3D(Pose3DReadOnly pose3D)
   {
      if (pose3D == null)
         return null;
      return toProtoPose3D(pose3D.getOrientation(), pose3D.getPosition());
   }

   public static EuclidProto.Pose3D toProtoPose3D(RigidBodyTransformReadOnly rigidBodyTransform)
   {
      if (rigidBodyTransform == null)
         return null;
      return toProtoPose3D(rigidBodyTransform.getRotation(), rigidBodyTransform.getTranslation());
   }

   public static EuclidProto.Pose3D toProtoPose3D(Orientation3DReadOnly orientation, Tuple3DReadOnly position)
   {
      if (orientation == null && position == null)
         return null;
      EuclidProto.Pose3D.Builder builder = EuclidProto.Pose3D.newBuilder();
      if (orientation != null)
         builder.setOrientation(toProtoQuaternion(orientation));
      if (position != null)
         builder.setPosition(toProtoPoint3D(position));
      return builder.build();
   }
}
