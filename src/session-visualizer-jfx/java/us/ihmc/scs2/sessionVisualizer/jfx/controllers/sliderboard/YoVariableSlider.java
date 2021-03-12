package us.ihmc.scs2.sessionVisualizer.jfx.controllers.sliderboard;

import com.jfoenix.controls.JFXTextField;

import javafx.scene.control.Slider;
import us.ihmc.scs2.definition.yoSlider.YoSliderDefinition;
import us.ihmc.scs2.sessionVisualizer.sliderboard.SliderVariable;
import us.ihmc.yoVariables.variable.YoBoolean;
import us.ihmc.yoVariables.variable.YoDouble;
import us.ihmc.yoVariables.variable.YoInteger;
import us.ihmc.yoVariables.variable.YoLong;
import us.ihmc.yoVariables.variable.YoVariable;

public interface YoVariableSlider
{
   public static YoVariableSlider newYoVariableSlider(YoVariable yoVariable, Runnable pushValueAction)
   {
      if (yoVariable instanceof YoDouble)
         return new YoDoubleSlider((YoDouble) yoVariable, pushValueAction);
      if (yoVariable instanceof YoBoolean)
         return new YoBooleanSlider((YoBoolean) yoVariable, pushValueAction);
      if (yoVariable instanceof YoInteger)
         return new YoIntegerSlider((YoInteger) yoVariable, pushValueAction);
      if (yoVariable instanceof YoLong)
         return new YoLongSlider((YoLong) yoVariable, pushValueAction);
      throw new IllegalStateException("Unexpected YoVariable type: " + yoVariable);
   }

   void bindVirtualSlider(Slider virtualSlider);

   void bindSliderVariable(SliderVariable sliderVariable);

   void bindMinTextField(JFXTextField minTextField);

   void bindMaxTextField(JFXTextField maxTextField);

   YoVariable getYoVariable();

   YoSliderDefinition toYoSliderDefinition();

   void dispose();
}
