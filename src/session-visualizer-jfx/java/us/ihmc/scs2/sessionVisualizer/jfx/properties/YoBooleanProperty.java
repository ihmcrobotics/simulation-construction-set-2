package us.ihmc.scs2.sessionVisualizer.jfx.properties;

import java.lang.ref.WeakReference;

import org.apache.commons.lang3.mutable.MutableBoolean;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import us.ihmc.scs2.sharedMemory.LinkedYoBoolean;
import us.ihmc.yoVariables.exceptions.IllegalOperationException;
import us.ihmc.yoVariables.listener.YoVariableChangedListener;
import us.ihmc.yoVariables.variable.YoBoolean;
import us.ihmc.yoVariables.variable.YoVariable;

public class YoBooleanProperty extends BooleanPropertyBase implements YoVariableProperty<YoBoolean, Boolean>
{
   private final YoBoolean yoBoolean;
   private final Object bean;
   private final YoVariableChangedListener propertyUpdater = new YoBooleanPropertyUpdater(this);

   private SimpleBooleanProperty lastUserInput;

   private LinkedYoBoolean linkedBuffer;

   public YoBooleanProperty(YoBoolean yoBoolean)
   {
      this(yoBoolean, null);
   }

   public YoBooleanProperty(YoBoolean yoBoolean, Object bean)
   {
      this.yoBoolean = yoBoolean;
      this.bean = bean;
      pullYoBooleanValue();
      yoBoolean.addListener(propertyUpdater);
   }

   private Object userObject;

   public void setLinkedBuffer(LinkedYoBoolean linkedBuffer)
   {
      if (this.linkedBuffer != null)
         throw new IllegalOperationException();

      this.linkedBuffer = linkedBuffer;
      userObject = new Object();
      linkedBuffer.addUser(userObject);
   }

   @Override
   public LinkedYoBoolean getLinkedBuffer()
   {
      return linkedBuffer;
   }

   @Override
   public void finalize()
   {
      try
      {
         yoBoolean.removeListener(propertyUpdater);
         linkedBuffer.removeUser(userObject);
      }
      finally
      {
      }
   }

   @Override
   public void set(boolean newValue)
   {
      if (lastUserInput != null)
         lastUserInput.set(newValue);
      super.set(newValue);
      yoBoolean.set(newValue);
   }

   public void setAndPush(boolean newValue)
   {
      set(newValue);
      if (linkedBuffer != null)
         linkedBuffer.push();
   }

   private void pullYoBooleanValue()
   {
      super.set(yoBoolean.getValue());
   }

   public void bindBooleanProperty(Property<Boolean> property)
   {
      property.setValue(getValue());

      MutableBoolean updatingControl = new MutableBoolean(false);
      MutableBoolean updatingThis = new MutableBoolean(false);

      addListener((o, oldValue, newValue) ->
      { // YoVariable changed, updating control
         if (updatingThis.isTrue())
            return;

         updatingControl.setTrue();
         property.setValue(newValue);
         updatingControl.setFalse();
      });

      property.addListener((o, oldValue, newValue) ->
      {
         if (updatingControl.isTrue())
            return;

         updatingThis.setTrue();
         setAndPush(newValue);
         updatingThis.setFalse();
      });
   }

   @Override
   public BooleanProperty userInputProperty()
   {
      if (lastUserInput == null)
         lastUserInput = new SimpleBooleanProperty(this, getName() + "LastUserInput", get());
      return lastUserInput;
   }

   @Override
   public YoBoolean getYoVariable()
   {
      return yoBoolean;
   }

   @Override
   public Object getBean()
   {
      return bean;
   }

   @Override
   public String getName()
   {
      return yoBoolean.getName();
   }

   private static class YoBooleanPropertyUpdater implements YoVariableChangedListener
   {
      private final WeakReference<YoBooleanProperty> propertyRef;

      public YoBooleanPropertyUpdater(YoBooleanProperty property)
      {
         propertyRef = new WeakReference<>(property);
      }

      @Override
      public void changed(YoVariable source)
      {
         YoBooleanProperty property = propertyRef.get();
         if (property != null)
            property.pullYoBooleanValue();
      }
   }
}
