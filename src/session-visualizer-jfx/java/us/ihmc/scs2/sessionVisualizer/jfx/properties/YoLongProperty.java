package us.ihmc.scs2.sessionVisualizer.jfx.properties;

import org.apache.commons.lang3.mutable.MutableBoolean;

import javafx.beans.property.LongProperty;
import javafx.beans.property.LongPropertyBase;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleLongProperty;
import us.ihmc.scs2.sharedMemory.LinkedYoLong;
import us.ihmc.yoVariables.exceptions.IllegalOperationException;
import us.ihmc.yoVariables.listener.YoVariableChangedListener;
import us.ihmc.yoVariables.variable.YoLong;

public class YoLongProperty extends LongPropertyBase implements YoVariableProperty<YoLong, Number>
{
   private final YoLong yoLong;
   private final Object bean;
   private final YoVariableChangedListener propertyUpdater = v -> pullYoLongValue();

   private SimpleLongProperty lastUserInput;

   private LinkedYoLong linkedBuffer;

   public YoLongProperty(YoLong yoLong)
   {
      this(yoLong, null);
   }

   public YoLongProperty(YoLong yoLong, Object bean)
   {
      this.yoLong = yoLong;
      this.bean = bean;
      pullYoLongValue();
      yoLong.addListener(propertyUpdater);
   }

   public void setLinkedBuffer(LinkedYoLong linkedBuffer)
   {
      if (this.linkedBuffer != null)
         throw new IllegalOperationException();

      this.linkedBuffer = linkedBuffer;
      linkedBuffer.addUser(this);
   }

   @Override
   public LinkedYoLong getLinkedBuffer()
   {
      return linkedBuffer;
   }

   @Override
   public void finalize()
   {
      try
      {
         yoLong.removeListener(propertyUpdater);
         linkedBuffer.removeUser(this);
      }
      finally
      {
      }
   }

   @Override
   public void set(long newValue)
   {
      if (lastUserInput != null)
         lastUserInput.set(newValue);
      super.set(newValue);
      yoLong.set(newValue);
   }

   public void setAndPush(long newValue)
   {
      set(newValue);
      if (linkedBuffer != null)
         linkedBuffer.push();
   }

   private void pullYoLongValue()
   {
      super.set(yoLong.getValue());
   }

   public void bindLongProperty(Property<Long> property)
   {
      property.setValue(getValue());

      MutableBoolean updatingControl = new MutableBoolean(false);
      MutableBoolean updatingThis = new MutableBoolean(false);

      addListener((o, oldValue, newValue) ->
      { // YoVariable changed, updating control
         if (updatingThis.isTrue())
            return;

         updatingControl.setTrue();
         property.setValue(Long.valueOf(newValue.longValue()));
         updatingControl.setFalse();
      });

      property.addListener((o, oldValue, newValue) ->
      {
         if (updatingControl.isTrue())
            return;

         updatingThis.setTrue();
         setAndPush(newValue.longValue());
         updatingThis.setFalse();
      });
   }

   @Override
   public LongProperty userInputProperty()
   {
      if (lastUserInput == null)
         lastUserInput = new SimpleLongProperty(this, getName() + "LastUserInput", get());
      return lastUserInput;
   }

   @Override
   public YoLong getYoVariable()
   {
      return yoLong;
   }

   @Override
   public Object getBean()
   {
      return bean;
   }

   @Override
   public String getName()
   {
      return yoLong.getName();
   }
}
