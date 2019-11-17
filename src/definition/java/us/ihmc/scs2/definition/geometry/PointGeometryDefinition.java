package us.ihmc.scs2.definition.geometry;

import us.ihmc.euclid.tuple3D.Point3D;
import us.ihmc.euclid.tuple3D.interfaces.Tuple3DReadOnly;

public class PointGeometryDefinition implements GeometryDefinition
{
   private final Point3D position = new Point3D();

   public PointGeometryDefinition()
   {
   }

   public PointGeometryDefinition(Tuple3DReadOnly position)
   {
      this.position.set(position);
   }
   
   public Point3D getPosition()
   {
      return position;
   }
}
