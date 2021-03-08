package us.ihmc.scs2.sessionVisualizer.jfx.controllers.sliderboard;

import org.apache.commons.lang3.mutable.MutableBoolean;

import javafx.beans.value.ChangeListener;
import us.ihmc.scs2.sessionVisualizer.jfx.properties.YoBooleanProperty;
import us.ihmc.scs2.sessionVisualizer.sliderboard.SliderVariable;
import us.ihmc.yoVariables.variable.YoBoolean;

public class YoBooleanSlider implements YoVariableSlider
{
   private final YoBooleanProperty yoBooleanProperty;

   public YoBooleanSlider(YoBoolean yoBoolean)
   {
      yoBooleanProperty = new YoBooleanProperty(yoBoolean, this);
   }

   private ChangeListener<Object> sliderUpdater;
   private ChangeListener<Number> yoBooleanUpdater;
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

         int sliderPosition = SliderVariable.booleanToInt(yoBooleanProperty.get(), sliderVariable.getMin(), sliderVariable.getMax());
         updating.setTrue();
         sliderVariable.setValue(sliderPosition);
         updating.setFalse();
      };

      yoBooleanUpdater = (o, oldValue, newValue) ->
      {
         if (updating.isTrue())
            return;

         boolean yoBooleanValue = SliderVariable.intToBoolean(newValue.intValue(), sliderVariable.getMin(), sliderVariable.getMax());
         updating.setTrue();
         yoBooleanProperty.set(yoBooleanValue);
         updating.setFalse();
      };

      yoBooleanProperty.addListener(sliderUpdater);

      sliderVariable.valueProperty().addListener(yoBooleanUpdater);
   }

   @Override
   public YoBoolean getYoVariable()
   {
      return yoBooleanProperty.getYoVariable();
   }

   @Override
   public void dispose()
   {
      yoBooleanProperty.finalize();
      if (sliderVariable != null && yoBooleanUpdater != null)
         sliderVariable.valueProperty().removeListener(yoBooleanUpdater);
      if (sliderUpdater != null)
      {
         yoBooleanProperty.removeListener(sliderUpdater);
      }
   }
}
