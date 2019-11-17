package us.ihmc.scs2.definition.geometry;

import us.ihmc.euclid.tuple3D.Vector3D;
import us.ihmc.euclid.tuple3D.interfaces.Tuple3DReadOnly;

public class EllipsoidGeometryDefinition implements GeometryDefinition
{
   private final Vector3D radii = new Vector3D();

   public EllipsoidGeometryDefinition()
   {
   }

   public EllipsoidGeometryDefinition(double radiusX, double radiusY, double radiusZ)
   {
      this.radii.set(radiusX, radiusY, radiusZ);
   }

   public EllipsoidGeometryDefinition(Tuple3DReadOnly radii)
   {
      this.radii.set(radii);
   }

   public Vector3D getRadii()
   {
      return radii;
   }
}
