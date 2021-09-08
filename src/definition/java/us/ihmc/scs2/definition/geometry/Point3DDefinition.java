package us.ihmc.scs2.definition.geometry;

import javax.xml.bind.annotation.XmlElement;

import us.ihmc.euclid.tuple3D.Point3D;
import us.ihmc.euclid.tuple3D.interfaces.Point3DBasics;
import us.ihmc.euclid.tuple3D.interfaces.Tuple3DReadOnly;

public class Point3DDefinition extends GeometryDefinition implements Point3DBasics
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
      position.set(other.position);
   }

   public Point3D getPosition()
   {
      return position;
   }

   @XmlElement
   @Override
   public void setX(double x)
   {
      position.setX(x);
   }

   @XmlElement
   @Override
   public void setY(double y)
   {
      position.setY(y);
   }

   @XmlElement
   @Override
   public void setZ(double z)
   {
      position.setZ(z);
   }

   @Override
   public double getX()
   {
      return position.getX();
   }

   @Override
   public double getY()
   {
      return position.getY();
   }

   @Override
   public double getZ()
   {
      return position.getZ();
   }

   @Override
   public Point3DDefinition copy()
   {
      return new Point3DDefinition(this);
   }
}
