package us.ihmc.scs2.sessionVisualizer.jfx.controllers.sliderboard;

import org.apache.commons.lang3.mutable.MutableBoolean;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ChangeListener;
import us.ihmc.scs2.sessionVisualizer.jfx.properties.YoLongProperty;
import us.ihmc.scs2.sessionVisualizer.sliderboard.SliderVariable;
import us.ihmc.yoVariables.variable.YoLong;

public class YoLongSlider implements YoVariableSlider
{
   private final YoLongProperty yoLongProperty;
   private final LongProperty minProperty = new SimpleLongProperty(this, "min", 0);
   private final LongProperty maxProperty = new SimpleLongProperty(this, "max", 1);

   public YoLongSlider(YoLong yoLong)
   {
      yoLongProperty = new YoLongProperty(yoLong, this);
   }

   private ChangeListener<Object> sliderUpdater;
   private ChangeListener<Number> yoLongUpdater;
   private SliderVariable sliderVariable;

   @Override
   public void bindSliderVariable(SliderVariable sliderVariable)
   {
      this.sliderVariable = sliderVariable;
      MutableBoolean updating = new MutableBoolean(false);

      sliderUpdater = (o, oldValue, newValue) ->
      {
         if (updating.isTrue())
            return;

         int sliderPosition = SliderVariable.longToInt(yoLongProperty.get(),
                                                       minProperty.get(),
                                                       maxProperty.get(),
                                                       sliderVariable.getMin(),
                                                       sliderVariable.getMax());
         updating.setTrue();
         sliderVariable.setValue(sliderPosition);
         updating.setFalse();
      };

      yoLongUpdater = (o, oldValue, newValue) ->
      {
         if (updating.isTrue())
            return;

         long yoLongValue = SliderVariable.intToLong(newValue.intValue(),
                                                     sliderVariable.getMin(),
                                                     sliderVariable.getMax(),
                                                     minProperty.get(),
                                                     maxProperty.get());
         updating.setTrue();
         yoLongProperty.set(yoLongValue);
         updating.setFalse();
      };

      yoLongProperty.addListener(sliderUpdater);
      minProperty.addListener(sliderUpdater);
      maxProperty.addListener(sliderUpdater);

      sliderVariable.valueProperty().addListener(yoLongUpdater);
   }

   @Override
   public YoLong getYoVariable()
   {
      return yoLongProperty.getYoVariable();
   }

   @Override
   public void dispose()
   {
      yoLongProperty.finalize();
      if (sliderVariable != null && yoLongUpdater != null)
         sliderVariable.valueProperty().removeListener(yoLongUpdater);
      if (sliderUpdater != null)
      {
         yoLongProperty.removeListener(sliderUpdater);
         minProperty.removeListener(sliderUpdater);
         maxProperty.removeListener(sliderUpdater);
      }
   }
}
