package us.ihmc.scs2.sessionVisualizer.jfx.controllers.sliderboard;

import org.apache.commons.lang3.mutable.MutableBoolean;

import javafx.beans.value.ChangeListener;
import us.ihmc.scs2.sessionVisualizer.jfx.properties.YoEnumAsStringProperty;
import us.ihmc.scs2.sessionVisualizer.sliderboard.SliderVariable;
import us.ihmc.yoVariables.variable.YoEnum;

public class YoEnumSlider implements YoVariableSlider
{
   private final YoEnumAsStringProperty<?> yoEnumProperty;

   public YoEnumSlider(YoEnum<?> yoEnum)
   {
      yoEnumProperty = new YoEnumAsStringProperty<>(yoEnum, this);
   }

   private ChangeListener<Object> sliderUpdater;
   private ChangeListener<Number> yoEnumUpdater;
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

         int sliderPosition = SliderVariable.doubleToInt(yoEnumProperty.getYoVariable().getOrdinal(),
                                                         0,
                                                         yoEnumProperty.getYoVariable().getEnumValuesAsString().length - 1,
                                                         sliderVariable.getMin(),
                                                         sliderVariable.getMax());
         updating.setTrue();
         sliderVariable.setValue(sliderPosition);
         updating.setFalse();
      };

      yoEnumUpdater = (o, oldValue, newValue) ->
      {
         if (updating.isTrue())
            return;

         int yoEnumOrdinal = SliderVariable.doubleToInt(newValue.intValue(),
                                                        sliderVariable.getMin(),
                                                        sliderVariable.getMax(),
                                                        0,
                                                        yoEnumProperty.getYoVariable().getEnumValuesAsString().length - 1);
         updating.setTrue();
         yoEnumProperty.set(yoEnumProperty.toEnumString(yoEnumOrdinal));
         updating.setFalse();
      };

      yoEnumProperty.addListener(sliderUpdater);
      sliderVariable.valueProperty().addListener(yoEnumUpdater);
   }

   @Override
   public YoEnum<?> getYoVariable()
   {
      return yoEnumProperty.getYoVariable();
   }

   @Override
   public void dispose()
   {
      yoEnumProperty.finalize();
      if (sliderVariable != null && yoEnumUpdater != null)
         sliderVariable.valueProperty().removeListener(yoEnumUpdater);
      if (sliderUpdater != null)
      {
         yoEnumProperty.removeListener(sliderUpdater);
      }
   }
}
