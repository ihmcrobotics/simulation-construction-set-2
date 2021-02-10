package us.ihmc.scs2.definition.geometry;

import us.ihmc.euclid.tuple3D.Point3D;
import us.ihmc.euclid.tuple3D.interfaces.Tuple3DReadOnly;

public class Point3DDefinition extends GeometryDefinition
{
   private final Point3D position = new Point3D();

   public Point3DDefinition()
   {
   }

   public Point3DDefinition(Tuple3DReadOnly position)
   {
      this.position.set(position);
   }
   
   public Point3DDefinition(Point3DDefinition other)
   {
      setName(other.getName());
      this.position.set(other.position);
   }
   
   public Point3D getPosition()
   {
      return position;
   }

   @Override
   public Point3DDefinition copy()
   {
      return new Point3DDefinition(this);
   }
}
