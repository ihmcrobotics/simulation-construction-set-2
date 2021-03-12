package us.ihmc.scs2.sessionVisualizer.jfx.controllers.sliderboard;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.jfoenix.controls.JFXTextField;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.Slider;
import us.ihmc.scs2.sessionVisualizer.jfx.properties.YoEnumAsStringProperty;
import us.ihmc.scs2.sessionVisualizer.sliderboard.SliderVariable;
import us.ihmc.yoVariables.variable.YoEnum;

public class YoEnumSlider implements YoVariableSlider
{
   private final YoEnumAsStringProperty<?> yoEnumProperty;
   private final List<Runnable> cleanupTasks = new ArrayList<>();

   public YoEnumSlider(YoEnum<?> yoEnum)
   {
      yoEnumProperty = new YoEnumAsStringProperty<>(yoEnum, this);
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
      virtualSlider.setMax(yoEnumProperty.getYoVariable().getEnumValuesAsString().length);
      virtualSlider.setMajorTickUnit(1.0);

      MutableBoolean updating = new MutableBoolean(false);

      ChangeListener<Object> sliderUpdater = (o, oldValue, newValue) ->
      {
         if (updating.isTrue())
            return;

         updating.setTrue();
         virtualSlider.valueProperty().set(yoEnumProperty.toEnumOrdinal(yoEnumProperty.get()));
         updating.setFalse();
      };

      ChangeListener<Number> yoEnumUpdater = (o, oldValue, newValue) ->
      {
         if (updating.isTrue())
            return;

         updating.setTrue();
         yoEnumProperty.set(yoEnumProperty.toEnumString(virtualSlider.valueProperty().getValue().intValue()));
         updating.setFalse();
      };

      yoEnumProperty.addListener(sliderUpdater);
      virtualSlider.valueProperty().addListener(yoEnumUpdater);

      cleanupTasks.add(() ->
      {
         yoEnumProperty.removeListener(sliderUpdater);
         virtualSlider.valueProperty().removeListener(yoEnumUpdater);
      });
   }

   @Override
   public void bindSliderVariable(SliderVariable sliderVariable)
   {
      MutableBoolean updating = new MutableBoolean(false);

      ChangeListener<Object> sliderUpdater = (o, oldValue, newValue) ->
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

      ChangeListener<Number> yoEnumUpdater = (o, oldValue, newValue) ->
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

      cleanupTasks.add(() ->
      {
         sliderVariable.valueProperty().removeListener(yoEnumUpdater);
         yoEnumProperty.removeListener(sliderUpdater);
      });
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
      cleanupTasks.forEach(Runnable::run);
   }
}
