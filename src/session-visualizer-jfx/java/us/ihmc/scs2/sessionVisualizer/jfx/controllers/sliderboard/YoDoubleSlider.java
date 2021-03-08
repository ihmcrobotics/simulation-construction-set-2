package us.ihmc.scs2.sessionVisualizer.jfx.controllers.sliderboard;

import org.apache.commons.lang3.mutable.MutableBoolean;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import us.ihmc.scs2.sessionVisualizer.jfx.properties.YoDoubleProperty;
import us.ihmc.scs2.sessionVisualizer.sliderboard.SliderVariable;
import us.ihmc.yoVariables.variable.YoDouble;

public class YoDoubleSlider implements YoVariableSlider
{
   private final YoDoubleProperty yoDoubleProperty;
   private final DoubleProperty minProperty = new SimpleDoubleProperty(this, "min", 0.0);
   private final DoubleProperty maxProperty = new SimpleDoubleProperty(this, "max", 1.0);

   public YoDoubleSlider(YoDouble yoDouble)
   {
      yoDoubleProperty = new YoDoubleProperty(yoDouble, this);
   }

   private ChangeListener<Object> sliderUpdater;
   private ChangeListener<Number> yoDoubleUpdater;
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

         int sliderPosition = SliderVariable.doubleToInt(yoDoubleProperty.get(),
                                                         minProperty.get(),
                                                         maxProperty.get(),
                                                         sliderVariable.getMin(),
                                                         sliderVariable.getMax());
         updating.setTrue();
         sliderVariable.setValue(sliderPosition);
         updating.setFalse();
      };

      yoDoubleUpdater = (o, oldValue, newValue) ->
      {
         if (updating.isTrue())
            return;

         double yoDoubleValue = SliderVariable.intToDouble(newValue.intValue(),
                                                           sliderVariable.getMin(),
                                                           sliderVariable.getMax(),
                                                           minProperty.get(),
                                                           maxProperty.get());
         updating.setTrue();
         yoDoubleProperty.set(yoDoubleValue);
         updating.setFalse();
      };

      yoDoubleProperty.addListener(sliderUpdater);
      minProperty.addListener(sliderUpdater);
      maxProperty.addListener(sliderUpdater);

      sliderVariable.valueProperty().addListener(yoDoubleUpdater);
   }

   @Override
   public YoDouble getYoVariable()
   {
      return yoDoubleProperty.getYoVariable();
   }

   @Override
   public void dispose()
   {
      yoDoubleProperty.finalize();
      if (sliderVariable != null && yoDoubleUpdater != null)
         sliderVariable.valueProperty().removeListener(yoDoubleUpdater);
      if (sliderUpdater != null)
      {
         yoDoubleProperty.removeListener(sliderUpdater);
         minProperty.removeListener(sliderUpdater);
         maxProperty.removeListener(sliderUpdater);
      }
   }
}
