package us.ihmc.scs2.sessionVisualizer.jfx.properties;

import java.lang.ref.WeakReference;

import org.apache.commons.lang3.mutable.MutableBoolean;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import us.ihmc.scs2.sharedMemory.LinkedYoDouble;
import us.ihmc.yoVariables.listener.YoVariableChangedListener;
import us.ihmc.yoVariables.variable.YoDouble;
import us.ihmc.yoVariables.variable.YoVariable;

public class YoDoubleProperty extends DoublePropertyBase implements YoVariableProperty<YoDouble, Number>
{
   private final YoDouble yoDouble;
   private final Object bean;
   private final YoVariableChangedListener propertyUpdater = new YoDoublePropertyUpdater(this);

   private SimpleDoubleProperty lastUserInput;

   private LinkedYoDouble linkedBuffer;

   public YoDoubleProperty(YoDouble yoDouble)
   {
      this(yoDouble, null);
   }

   public YoDoubleProperty(YoDouble yoDouble, Object bean)
   {
      this.yoDouble = yoDouble;
      this.bean = bean;
      pullYoDoubleValue();
      yoDouble.addListener(propertyUpdater);
   }

   private Object userObject;

   public void setLinkedBuffer(LinkedYoDouble linkedBuffer)
   {
      if (this.linkedBuffer != null)
         this.linkedBuffer.removeUser(userObject);

      this.linkedBuffer = linkedBuffer;

      if (userObject == null)
         userObject = new Object();

      if (linkedBuffer != null)
         linkedBuffer.addUser(userObject);
   }

   @Override
   public LinkedYoDouble getLinkedBuffer()
   {
      return linkedBuffer;
   }

   @Override
   public void finalize()
   {
      try
      {
         yoDouble.removeListener(propertyUpdater);
         if (linkedBuffer != null)
            linkedBuffer.removeUser(userObject);
      }
      finally
      {
      }
   }

   @Override
   public void set(double newValue)
   {
      if (lastUserInput != null)
         lastUserInput.set(newValue);
      super.set(newValue);
      yoDouble.set(newValue);
   }

   public void setAndPush(double newValue)
   {
      set(newValue);
      if (linkedBuffer != null)
         linkedBuffer.push();
   }

   private void pullYoDoubleValue()
   {
      super.set(yoDouble.getValue());
   }

   public void bindDoubleProperty(Property<Double> property)
   {
      property.setValue(getValue());

      MutableBoolean updatingControl = new MutableBoolean(false);
      MutableBoolean updatingThis = new MutableBoolean(false);

      addListener((o, oldValue, newValue) ->
      { // YoVariable changed, updating control
         if (updatingThis.isTrue())
            return;

         updatingControl.setTrue();
         property.setValue(Double.valueOf(newValue.doubleValue()));
         updatingControl.setFalse();
      });

      property.addListener((o, oldValue, newValue) ->
      {
         if (updatingControl.isTrue())
            return;

         updatingThis.setTrue();
         setAndPush(newValue.doubleValue());
         updatingThis.setFalse();
      });
   }

   @Override
   public DoubleProperty userInputProperty()
   {
      if (lastUserInput == null)
         lastUserInput = new SimpleDoubleProperty(this, getName() + "LastUserInput", get());
      return lastUserInput;
   }

   @Override
   public YoDouble getYoVariable()
   {
      return yoDouble;
   }

   @Override
   public Object getBean()
   {
      return bean;
   }

   @Override
   public String getName()
   {
      return yoDouble.getName();
   }

   private static class YoDoublePropertyUpdater implements YoVariableChangedListener
   {
      private final WeakReference<YoDoubleProperty> propertyRef;

      public YoDoublePropertyUpdater(YoDoubleProperty property)
      {
         propertyRef = new WeakReference<>(property);
      }

      @Override
      public void changed(YoVariable source)
      {
         YoDoubleProperty property = propertyRef.get();
         if (property != null)
            property.pullYoDoubleValue();
      }
   }
}