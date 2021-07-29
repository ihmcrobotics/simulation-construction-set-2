package us.ihmc.scs2.sessionVisualizer.jfx.properties;

import org.apache.commons.lang3.mutable.MutableBoolean;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import us.ihmc.scs2.sharedMemory.LinkedYoBoolean;
import us.ihmc.yoVariables.exceptions.IllegalOperationException;
import us.ihmc.yoVariables.listener.YoVariableChangedListener;
import us.ihmc.yoVariables.variable.YoBoolean;

public class YoBooleanProperty extends BooleanPropertyBase implements YoVariableProperty<YoBoolean, Boolean>
{
   private final YoBoolean yoBoolean;
   private final Object bean;
   private final YoVariableChangedListener propertyUpdater = v -> pullYoBooleanValue();

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

   public void setLinkedBuffer(LinkedYoBoolean linkedBuffer)
   {
      if (this.linkedBuffer != null)
         throw new IllegalOperationException();

      this.linkedBuffer = linkedBuffer;
      linkedBuffer.addUser(this);
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
         linkedBuffer.removeUser(this);
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
}
