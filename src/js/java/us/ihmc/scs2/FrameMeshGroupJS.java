package us.ihmc.scs2;

import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.scs2.protobuf.EuclidProto.Pose3D;
import us.ihmc.scs2.protobuf.ThreeProto;

public class FrameMeshGroupJS
{
   private final ReferenceFrame referenceFrame;
   private final ThreeProto.MeshGroup.Builder fullMeshGroupBuilder;
   private final ThreeProto.MeshGroup.Builder lightMeshGroupBuilder;

   public FrameMeshGroupJS(ReferenceFrame referenceFrame, ThreeProto.MeshGroup mesh)
   {
      this.referenceFrame = referenceFrame;
      fullMeshGroupBuilder = mesh.toBuilder();
      lightMeshGroupBuilder = ThreeProto.MeshGroup.newBuilder().setGroupId(mesh.getGroupId());
   }

   public void setGroupId(String groupId)
   {
      fullMeshGroupBuilder.setGroupId(groupId);
      lightMeshGroupBuilder.setGroupId(groupId);
   }

   public void updatePose()
   {
      Pose3D protoPose3D = EuclidProtoTools.toProtoPose3D(referenceFrame.getTransformToRoot());
      fullMeshGroupBuilder.setPose(protoPose3D);
      lightMeshGroupBuilder.setPose(protoPose3D);
   }

   public ThreeProto.MeshGroup getFullMeshGroup()
   {
      return fullMeshGroupBuilder.build();
   }

   public ThreeProto.MeshGroup getLightMeshGroup()
   {
      return lightMeshGroupBuilder.build();
   }
}
