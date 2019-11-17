package us.ihmc.scs2.examples.fallingBall;

import us.ihmc.euclid.transform.RigidBodyTransform;
import us.ihmc.scs2.definition.collision.CollisionShapeDefinition;
import us.ihmc.scs2.definition.geometry.BoxGeometryDefinition;
import us.ihmc.scs2.definition.geometry.GeometryDefinition;
import us.ihmc.scs2.definition.terrain.TerrainObjectDefinition;
import us.ihmc.scs2.definition.visual.ColorDefinitions;
import us.ihmc.scs2.definition.visual.VisualDefinition;
import us.ihmc.scs2.definition.visual.VisualDefinition.MaterialDefinition;

public class SlopeGroundDefinition extends TerrainObjectDefinition
{
   public SlopeGroundDefinition()
   {
      super();
      RigidBodyTransform originPose = new RigidBodyTransform();
      originPose.setRotationPitch(Math.toRadians(15.0));

      GeometryDefinition groundGeometryDefinition = new BoxGeometryDefinition(10.0, 10.0, 0.20);
      addVisualDefinition(new VisualDefinition(originPose, groundGeometryDefinition, new MaterialDefinition(ColorDefinitions.DeepSkyBlue())));
      addCollisionShapeDefinition(new CollisionShapeDefinition(originPose, groundGeometryDefinition));
   }
}
