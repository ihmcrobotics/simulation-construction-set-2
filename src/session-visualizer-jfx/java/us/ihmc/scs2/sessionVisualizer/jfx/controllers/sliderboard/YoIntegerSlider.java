package us.ihmc.scs2.sessionVisualizer.jfx.controllers.sliderboard;

import org.apache.commons.lang3.mutable.MutableBoolean;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import us.ihmc.scs2.sessionVisualizer.jfx.properties.YoIntegerProperty;
import us.ihmc.scs2.sessionVisualizer.sliderboard.SliderVariable;
import us.ihmc.yoVariables.variable.YoInteger;

public class YoIntegerSlider implements YoVariableSlider
{
   private final YoIntegerProperty yoIntegerProperty;
   private final IntegerProperty minProperty = new SimpleIntegerProperty(this, "min", 0);
   private final IntegerProperty maxProperty = new SimpleIntegerProperty(this, "max", 1);

   public YoIntegerSlider(YoInteger yoInteger)
   {
      yoIntegerProperty = new YoIntegerProperty(yoInteger, this);
   }

   private ChangeListener<Object> sliderUpdater;
   private ChangeListener<Number> yoIntegerUpdater;
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

         int sliderPosition = SliderVariable.doubleToInt(yoIntegerProperty.get(),
                                                         minProperty.get(),
                                                         maxProperty.get(),
                                                         sliderVariable.getMin(),
                                                         sliderVariable.getMax());
         updating.setTrue();
         sliderVariable.setValue(sliderPosition);
         updating.setFalse();
      };

      yoIntegerUpdater = (o, oldValue, newValue) ->
      {
         if (updating.isTrue())
            return;

         int yoIntegerValue = SliderVariable.doubleToInt(newValue.intValue(),
                                                         sliderVariable.getMin(),
                                                         sliderVariable.getMax(),
                                                         minProperty.get(),
                                                         maxProperty.get());
         updating.setTrue();
         yoIntegerProperty.set(yoIntegerValue);
         updating.setFalse();
      };

      yoIntegerProperty.addListener(sliderUpdater);
      minProperty.addListener(sliderUpdater);
      maxProperty.addListener(sliderUpdater);

      sliderVariable.valueProperty().addListener(yoIntegerUpdater);
   }

   @Override
   public YoInteger getYoVariable()
   {
      return yoIntegerProperty.getYoVariable();
   }

   @Override
   public void dispose()
   {
      yoIntegerProperty.finalize();
      if (sliderVariable != null && yoIntegerUpdater != null)
         sliderVariable.valueProperty().removeListener(yoIntegerUpdater);
      if (sliderUpdater != null)
      {
         yoIntegerProperty.removeListener(sliderUpdater);
         minProperty.removeListener(sliderUpdater);
         maxProperty.removeListener(sliderUpdater);
      }
   }
}
