package us.ihmc.scs2.sessionVisualizer.jfx.controllers.sliderboard;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXTextField;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.LongStringConverter;
import us.ihmc.scs2.sessionVisualizer.jfx.properties.YoLongProperty;
import us.ihmc.scs2.sessionVisualizer.sliderboard.SliderVariable;
import us.ihmc.yoVariables.variable.YoLong;

public class YoLongSlider implements YoVariableSlider
{
   private final YoLongProperty yoLongProperty;
   private final LongProperty minProperty = new SimpleLongProperty(this, "min", 0);
   private final LongProperty maxProperty = new SimpleLongProperty(this, "max", 1);
   private final List<Runnable> cleanupTasks = new ArrayList<>();

   public YoLongSlider(YoLong yoLong, Runnable pushValueAction)
   {
      yoLongProperty = new YoLongProperty(yoLong, this);
   }

   @Override
   public void bindMinTextField(JFXTextField minTextField)
   {
      TextFormatter<Long> minTextFormatter = new TextFormatter<>(new LongStringConverter());
      minTextField.setTextFormatter(minTextFormatter);
      minProperty.bind(minTextFormatter.valueProperty());

      cleanupTasks.add(() -> minProperty.unbind());
   }

   @Override
   public void bindMaxTextField(JFXTextField maxTextField)
   {
      TextFormatter<Long> maxTextFormatter = new TextFormatter<>(new LongStringConverter());
      maxTextField.setTextFormatter(maxTextFormatter);
      maxProperty.bind(maxTextFormatter.valueProperty());

      cleanupTasks.add(() -> maxProperty.unbind());
   }

   @Override
   public void bindVirtualSlider(JFXSlider virtualSlider)
   {
      virtualSlider.setValueFactory(param -> new StringBinding()
      {
         @Override
         protected String computeValue()
         {
            return Long.toString(yoLongProperty.get());
         }
      });

      virtualSlider.minProperty().bind(minProperty);
      virtualSlider.maxProperty().bind(maxProperty);
      // TODO Add rounding for the major and minor tick unit
      virtualSlider.majorTickUnitProperty().bind(maxProperty.subtract(minProperty).divide(25.0));
      virtualSlider.setMinorTickCount(4);

      MutableBoolean updating = new MutableBoolean(false);

      ChangeListener<Object> sliderUpdater = (o, oldValue, newValue) ->
      {
         if (updating.isTrue())
            return;

         updating.setTrue();
         virtualSlider.valueProperty().set(yoLongProperty.get());
         updating.setFalse();
      };

      ChangeListener<Number> yoIntegerUpdater = (o, oldValue, newValue) ->
      {
         if (updating.isTrue())
            return;

         updating.setTrue();
         yoLongProperty.set(virtualSlider.valueProperty().getValue().longValue());
         updating.setFalse();
      };

      yoLongProperty.addListener(sliderUpdater);
      virtualSlider.valueProperty().addListener(yoIntegerUpdater);

      cleanupTasks.add(() ->
      {
         yoLongProperty.removeListener(sliderUpdater);
         virtualSlider.valueProperty().removeListener(yoIntegerUpdater);
         virtualSlider.minProperty().unbind();
         virtualSlider.maxProperty().unbind();
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

         int sliderPosition = SliderVariable.longToInt(yoLongProperty.get(),
                                                       minProperty.get(),
                                                       maxProperty.get(),
                                                       sliderVariable.getMin(),
                                                       sliderVariable.getMax());
         updating.setTrue();
         sliderVariable.setValue(sliderPosition);
         updating.setFalse();
      };

      ChangeListener<Number> yoLongUpdater = (o, oldValue, newValue) ->
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

      cleanupTasks.add(() ->
      {
         yoLongProperty.removeListener(sliderUpdater);
         minProperty.removeListener(sliderUpdater);
         maxProperty.removeListener(sliderUpdater);
         sliderVariable.valueProperty().removeListener(yoLongUpdater);
      });
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
      cleanupTasks.forEach(Runnable::run);
   }
}
