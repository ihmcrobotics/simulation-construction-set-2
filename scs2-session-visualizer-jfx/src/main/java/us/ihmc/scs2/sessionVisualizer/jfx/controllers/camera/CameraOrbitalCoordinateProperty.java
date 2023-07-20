package us.ihmc.scs2.sessionVisualizer.jfx.controllers.camera;

import static us.ihmc.scs2.definition.camera.YoCameraOrbitalCoordinateDefinition.YoCameraOrbital;
import static us.ihmc.scs2.definition.camera.YoCameraOrbitalCoordinateDefinition.YoCameraOrbitalIdentifiers;

import java.util.Objects;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.CompositeProperty;

public class CameraOrbitalCoordinateProperty extends CompositeProperty
{
   public CameraOrbitalCoordinateProperty()
   {
      super(YoCameraOrbital, YoCameraOrbitalIdentifiers);
   }

   public CameraOrbitalCoordinateProperty(double distance, double longitude, double latitude)
   {
      super(YoCameraOrbital, YoCameraOrbitalIdentifiers, distance, longitude, latitude);
   }

   public CameraOrbitalCoordinateProperty(ReferenceFrame referenceFrame, double distance, double longitude, double latitude)
   {
      super(YoCameraOrbital, YoCameraOrbitalIdentifiers, referenceFrame, distance, longitude, latitude);
   }

   public CameraOrbitalCoordinateProperty(DoubleProperty[] distanceLongitudeLatitudeProperties)
   {
      super(YoCameraOrbital, YoCameraOrbitalIdentifiers, distanceLongitudeLatitudeProperties);
   }

   public CameraOrbitalCoordinateProperty(Property<ReferenceFrame> referenceFrameProperty, DoubleProperty[] distanceLongitudeLatitudeProperties)
   {
      super(YoCameraOrbital, YoCameraOrbitalIdentifiers, referenceFrameProperty, distanceLongitudeLatitudeProperties);
   }

   public CameraOrbitalCoordinateProperty(DoubleProperty distanceProperty, DoubleProperty longitudeProperty, DoubleProperty latitudeProperty)
   {
      super(YoCameraOrbital, YoCameraOrbitalIdentifiers, distanceProperty, longitudeProperty, latitudeProperty);
   }

   public CameraOrbitalCoordinateProperty(Property<ReferenceFrame> referenceFrameProperty,
                                          DoubleProperty distanceProperty,
                                          DoubleProperty longitudeProperty,
                                          DoubleProperty latitudeProperty)
   {
      super(YoCameraOrbital, YoCameraOrbitalIdentifiers, referenceFrameProperty, distanceProperty, longitudeProperty, latitudeProperty);
   }

   public CameraOrbitalCoordinateProperty(CompositeProperty other)
   {
      this();
      set(other);
   }

   public void setDistance(double distance)
   {
      setDistanceProperty(new SimpleDoubleProperty(distance));
   }

   public void setDistanceProperty(DoubleProperty distanceProperty)
   {
      componentValueProperties[0] = distanceProperty;
   }

   public void setLongitude(double longitude)
   {
      setLongitudeProperty(new SimpleDoubleProperty(longitude));
   }

   public void setLongitudeProperty(DoubleProperty longitudeProperty)
   {
      componentValueProperties[1] = longitudeProperty;
   }

   public void setLatitude(double latitude)
   {
      setLatitudeProperty(new SimpleDoubleProperty(latitude));
   }

   public void setLatitudeProperty(DoubleProperty latitudeProperty)
   {
      componentValueProperties[2] = latitudeProperty;
   }

   public void set(double distance, double longitude, double latitude)
   {
      setComponentValues(distance, longitude, latitude);
   }

   public void set(ReferenceFrame referenceFrame, double distance, double longitude, double latitude)
   {
      set(referenceFrame, distance, longitude, latitude);
   }

   public void set(DoubleProperty distanceProperty, DoubleProperty longitudeProperty, DoubleProperty latitudeProperty)
   {
      setComponentValueProperties(distanceProperty, longitudeProperty, latitudeProperty);
   }

   public void set(Property<ReferenceFrame> referenceFrameProperty,
                   DoubleProperty distanceProperty,
                   DoubleProperty longitudeProperty,
                   DoubleProperty latitudeProperty)
   {
      set(referenceFrameProperty, distanceProperty, longitudeProperty, latitudeProperty);
   }

   @Override
   public void set(CompositeProperty other)
   {
      Objects.requireNonNull(other.getType());
      if (!getType().equals(other.getType()))
         throw new IllegalArgumentException("Cannot set a " + getClass().getSimpleName() + " to a " + other.getType());
      super.set(other);
   }

   public void set(CameraOrbitalCoordinateProperty other)
   {
      set((CompositeProperty) other);
   }

   public double getDistance()
   {
      return distanceProperty().get();
   }

   public DoubleProperty distanceProperty()
   {
      return componentValueProperties == null ? null : componentValueProperties[0];
   }

   public double getLongitude()
   {
      return longitudeProperty().get();
   }

   public DoubleProperty longitudeProperty()
   {
      return componentValueProperties == null ? null : componentValueProperties[1];
   }

   public double getLatitude()
   {
      return latitudeProperty().get();
   }

   public DoubleProperty latitudeProperty()
   {
      return componentValueProperties == null ? null : componentValueProperties[2];
   }

   @Override
   public CameraOrbitalCoordinateProperty clone()
   {
      return new CameraOrbitalCoordinateProperty(this);
   }
}