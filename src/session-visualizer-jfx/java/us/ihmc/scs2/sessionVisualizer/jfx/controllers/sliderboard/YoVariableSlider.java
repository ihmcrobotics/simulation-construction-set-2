package us.ihmc.scs2.sessionVisualizer.jfx.controllers.sliderboard;

import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXTextField;

import us.ihmc.scs2.sessionVisualizer.sliderboard.SliderVariable;
import us.ihmc.yoVariables.variable.YoBoolean;
import us.ihmc.yoVariables.variable.YoDouble;
import us.ihmc.yoVariables.variable.YoInteger;
import us.ihmc.yoVariables.variable.YoLong;
import us.ihmc.yoVariables.variable.YoVariable;

public interface YoVariableSlider
{
   public static YoVariableSlider newYoVariableSlider(YoVariable yoVariable)
   {
      if (yoVariable instanceof YoDouble)
         return new YoDoubleSlider((YoDouble) yoVariable);
      if (yoVariable instanceof YoBoolean)
         return new YoBooleanSlider((YoBoolean) yoVariable);
      if (yoVariable instanceof YoInteger)
         return new YoIntegerSlider((YoInteger) yoVariable);
      if (yoVariable instanceof YoLong)
         return new YoLongSlider((YoLong) yoVariable);
      throw new IllegalStateException("Unexpected YoVariable type: " + yoVariable);
   }

   void bindVirtualSlider(JFXSlider virtualSlider);

   void bindSliderVariable(SliderVariable sliderVariable);

   void bindMinTextField(JFXTextField minTextField);

   void bindMaxTextField(JFXTextField maxTextField);

   YoVariable getYoVariable();

   void dispose();
}
