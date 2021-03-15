package us.ihmc.scs2.sessionVisualizer.jfx.controllers.sliderboard;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.Slider;
import us.ihmc.scs2.definition.yoSlider.YoKnobDefinition;
import us.ihmc.scs2.definition.yoSlider.YoSliderDefinition;
import us.ihmc.scs2.sessionVisualizer.jfx.properties.YoBooleanProperty;
import us.ihmc.scs2.sessionVisualizer.sliderboard.SliderboardVariable;
import us.ihmc.yoVariables.variable.YoBoolean;

public class YoBooleanSlider implements YoVariableSlider
{
   private final YoBooleanProperty yoBooleanProperty;
   private final List<Runnable> cleanupTasks = new ArrayList<>();
   private final Runnable pushValueAction;

   public YoBooleanSlider(YoBoolean yoBoolean, Runnable pushValueAction)
   {
      this.pushValueAction = pushValueAction;
      yoBooleanProperty = new YoBooleanProperty(yoBoolean, this);
   }

   @Override
   public void bindMaxTextField(JFXTextField maxTextField)
   {
      maxTextField.setDisable(true);
      cleanupTasks.add(() -> maxTextField.setDisable(false));
   }

   @Override
   public void bindMinTextField(JFXTextField minTextField)
   {
      minTextField.setDisable(true);
      cleanupTasks.add(() -> minTextField.setDisable(false));
   }

   @Override
   public void bindVirtualSlider(Slider virtualSlider)
   {
      virtualSlider.setMin(0.0);
      virtualSlider.setMax(1.0);
      virtualSlider.setMajorTickUnit(1.0);

      MutableBoolean updating = new MutableBoolean(false);

      virtualSlider.valueProperty().set(yoBooleanProperty.get() ? 1.0 : 0.0);

      ChangeListener<Object> sliderUpdater = (o, oldValue, newValue) ->
      {
         if (updating.isTrue())
            return;

         boolean currentSliderValue = virtualSlider.valueProperty().get() > 0.5;

         if (currentSliderValue == yoBooleanProperty.get())
            return;

         updating.setTrue();
         virtualSlider.valueProperty().set(yoBooleanProperty.get() ? 1.0 : 0.0);
         updating.setFalse();
      };

      ChangeListener<Number> yoBooleanUpdater = (o, oldValue, newValue) ->
      {
         if (updating.isTrue())
            return;

         boolean currentSliderValue = virtualSlider.valueProperty().get() > 0.5;

         if (currentSliderValue == yoBooleanProperty.get())
            return;

         updating.setTrue();
         yoBooleanProperty.set(currentSliderValue);
         pushValueAction.run();
         updating.setFalse();
      };

      yoBooleanProperty.addListener(sliderUpdater);
      virtualSlider.valueProperty().addListener(yoBooleanUpdater);

      cleanupTasks.add(() ->
      {
         yoBooleanProperty.removeListener(sliderUpdater);
         virtualSlider.valueProperty().removeListener(yoBooleanUpdater);
      });
   }

   @Override
   public void bindVirtualKnob(JFXSpinner virtualKnob)
   {
      ChangeListener<Boolean> knobUpdater = (o, oldValue, newValue) ->
      {
         virtualKnob.setProgress(newValue ? 1.0 : 0.0);
      };

      yoBooleanProperty.addListener(knobUpdater);

      cleanupTasks.add(() ->
      {
         yoBooleanProperty.removeListener(knobUpdater);
      });
   }

   @Override
   public void bindSliderVariable(SliderboardVariable sliderVariable)
   {
      MutableBoolean updating = new MutableBoolean(false);

      sliderVariable.setValue(SliderboardVariable.booleanToInt(yoBooleanProperty.get(), sliderVariable.getMin(), sliderVariable.getMax()));

      ChangeListener<Object> sliderUpdater = (o, oldValue, newValue) ->
      {
         if (updating.isTrue())
            return;

         boolean currentSliderValue = sliderVariable.valueProperty().get() > 0.5;

         if (currentSliderValue == yoBooleanProperty.get())
            return;

         int sliderPosition = SliderboardVariable.booleanToInt(yoBooleanProperty.get(), sliderVariable.getMin(), sliderVariable.getMax());
         updating.setTrue();
         sliderVariable.setValue(sliderPosition);
         updating.setFalse();
      };

      ChangeListener<Number> yoBooleanUpdater = (o, oldValue, newValue) ->
      {
         if (updating.isTrue())
            return;

         boolean currentSliderValue = sliderVariable.valueProperty().get() > 0.5;

         if (currentSliderValue == yoBooleanProperty.get())
            return;

         boolean yoBooleanValue = SliderboardVariable.intToBoolean(newValue.intValue(), sliderVariable.getMin(), sliderVariable.getMax());
         updating.setTrue();
         yoBooleanProperty.set(yoBooleanValue);
         pushValueAction.run();
         updating.setFalse();
      };

      yoBooleanProperty.addListener(sliderUpdater);

      sliderVariable.valueProperty().addListener(yoBooleanUpdater);

      cleanupTasks.add(() ->
      {
         yoBooleanProperty.removeListener(sliderUpdater);
         sliderVariable.valueProperty().removeListener(yoBooleanUpdater);
      });
   }

   public YoBooleanProperty getYoBooleanProperty()
   {
      return yoBooleanProperty;
   }

   @Override
   public YoBoolean getYoVariable()
   {
      return yoBooleanProperty.getYoVariable();
   }

   @Override
   public YoSliderDefinition toYoSliderDefinition()
   {
      YoSliderDefinition definition = new YoSliderDefinition();
      definition.setVariableName(getYoVariable().getFullNameString());
      return definition;
   }

   @Override
   public YoKnobDefinition toYoKnobDefinition()
   {
      YoKnobDefinition definition = new YoKnobDefinition();
      definition.setVariableName(getYoVariable().getFullNameString());
      return definition;
   }

   @Override
   public void dispose()
   {
      yoBooleanProperty.finalize();
      cleanupTasks.forEach(Runnable::run);
   }
}
