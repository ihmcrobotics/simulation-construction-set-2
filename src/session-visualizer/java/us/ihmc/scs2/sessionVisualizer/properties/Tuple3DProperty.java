package us.ihmc.scs2.sessionVisualizer.properties;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import us.ihmc.euclid.tuple3D.interfaces.Tuple3DBasics;
import us.ihmc.euclid.tuple3D.interfaces.Tuple3DReadOnly;

public abstract class Tuple3DProperty implements Tuple3DBasics
{
   private final ObjectProperty<DoubleProperty> x = new SimpleObjectProperty<>(new SimpleDoubleProperty());
   private final ObjectProperty<DoubleProperty> y = new SimpleObjectProperty<>(new SimpleDoubleProperty());
   private final ObjectProperty<DoubleProperty> z = new SimpleObjectProperty<>(new SimpleDoubleProperty());

   public Tuple3DProperty()
   {
      setToZero();
   }

   public Tuple3DProperty(Tuple3DProperty other)
   {
      set(other);
   }

   public void set(Tuple3DProperty other)
   {
      set(other.x.get(), other.y.get(), other.z.get());
   }

   public void set(DoubleProperty xProperty, DoubleProperty yProperty, DoubleProperty zProperty)
   {
      setXProperty(xProperty);
      setYProperty(yProperty);
      setZProperty(zProperty);
   }

   public void addListener(ChangeListener<Tuple3DReadOnly> listener)
   {
      
   }

   @Override
   public void setX(double x)
   {
      this.x.get().set(x);
   }

   @Override
   public void setY(double y)
   {
      this.y.get().set(y);
   }

   @Override
   public void setZ(double z)
   {
      this.z.get().set(z);
   }

   public void setXProperty(DoubleProperty xProperty)
   {
      this.x.set(xProperty);
   }

   public void setYProperty(DoubleProperty yProperty)
   {
      this.y.set(yProperty);
   }

   public void setZProperty(DoubleProperty zProperty)
   {
      this.z.set(zProperty);
   }

   @Override
   public double getX()
   {
      return x.get().get();
   }

   @Override
   public double getY()
   {
      return y.get().get();
   }

   @Override
   public double getZ()
   {
      return z.get().get();
   }

   public DoubleProperty getXProperty()
   {
      return x.get();
   }

   public DoubleProperty getYProperty()
   {
      return y.get();
   }

   public DoubleProperty getZProperty()
   {
      return z.get();
   }
}
