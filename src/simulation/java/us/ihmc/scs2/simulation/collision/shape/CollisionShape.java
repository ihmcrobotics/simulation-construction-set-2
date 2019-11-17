package us.ihmc.scs2.simulation.collision.shape;

import java.util.ArrayList;
import java.util.List;

import us.ihmc.euclid.geometry.BoundingBox3D;
import us.ihmc.euclid.shape.primitives.interfaces.Shape3DReadOnly;
import us.ihmc.euclid.transform.RigidBodyTransform;

public class CollisionShape
{
   private final RigidBodyTransform transformToWorld = new RigidBodyTransform();
   private final List<Shape3DReadOnly> shapes = new ArrayList<>();
   private final BoundingBox3D boundingBox = new BoundingBox3D();

   public CollisionShape()
   {
   }

   public void addShape(Shape3DReadOnly shape)
   {
      shapes.add(shape);
   }

   public void updateBoundingBox()
   {
      boundingBox.setToNaN();
   }

   public RigidBodyTransform getTransformToWorld()
   {
      return transformToWorld;
   }

   public List<Shape3DReadOnly> getShapes()
   {
      return shapes;
   }

   public BoundingBox3D getBoundingBox()
   {
      return boundingBox;
   }
}
