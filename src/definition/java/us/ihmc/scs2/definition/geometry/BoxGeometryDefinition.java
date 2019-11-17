package us.ihmc.scs2.definition.geometry;

import us.ihmc.euclid.tuple3D.Vector3D;
import us.ihmc.euclid.tuple3D.interfaces.Tuple3DReadOnly;

public class BoxGeometryDefinition implements GeometryDefinition
{
   private final Vector3D size = new Vector3D();

   public BoxGeometryDefinition()
   {
   }

   public BoxGeometryDefinition(double sizeX, double sizeY, double sizeZ)
   {
      this.size.set(sizeX, sizeY, sizeZ);
   }

   public BoxGeometryDefinition(Tuple3DReadOnly size)
   {
      this.size.set(size);
   }

   public Vector3D getSize()
   {
      return size;
   }
}
