package us.ihmc.scs2.sessionVisualizer.jfx.controllers.sliderboard;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXTextField;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.IntegerStringConverter;
import us.ihmc.scs2.sessionVisualizer.jfx.properties.YoIntegerProperty;
import us.ihmc.scs2.sessionVisualizer.sliderboard.SliderVariable;
import us.ihmc.yoVariables.variable.YoInteger;

public class YoIntegerSlider implements YoVariableSlider
{
   private final YoIntegerProperty yoIntegerProperty;
   private final IntegerProperty minProperty = new SimpleIntegerProperty(this, "min", 0);
   private final IntegerProperty maxProperty = new SimpleIntegerProperty(this, "max", 1);
   private final List<Runnable> cleanupTasks = new ArrayList<>();

   public YoIntegerSlider(YoInteger yoInteger, Runnable pushValueAction)
   {
      yoIntegerProperty = new YoIntegerProperty(yoInteger, this);
   }

   @Override
   public void bindMinTextField(JFXTextField minTextField)
   {
      TextFormatter<Integer> minTextFormatter = new TextFormatter<>(new IntegerStringConverter());
      minTextField.setTextFormatter(minTextFormatter);
      minProperty.bind(minTextFormatter.valueProperty());

      cleanupTasks.add(() -> minProperty.unbind());
   }

   @Override
   public void bindMaxTextField(JFXTextField maxTextField)
   {
      TextFormatter<Integer> maxTextFormatter = new TextFormatter<>(new IntegerStringConverter());
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
            return Integer.toString(yoIntegerProperty.get());
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
         virtualSlider.valueProperty().set(yoIntegerProperty.get());
         updating.setFalse();
      };

      ChangeListener<Number> yoIntegerUpdater = (o, oldValue, newValue) ->
      {
         if (updating.isTrue())
            return;

         updating.setTrue();
         yoIntegerProperty.set(virtualSlider.valueProperty().getValue().intValue());
         updating.setFalse();
      };

      yoIntegerProperty.addListener(sliderUpdater);
      virtualSlider.valueProperty().addListener(yoIntegerUpdater);

      cleanupTasks.add(() ->
      {
         yoIntegerProperty.removeListener(sliderUpdater);
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

         int sliderPosition = SliderVariable.doubleToInt(yoIntegerProperty.get(),
                                                         minProperty.get(),
                                                         maxProperty.get(),
                                                         sliderVariable.getMin(),
                                                         sliderVariable.getMax());
         updating.setTrue();
         sliderVariable.setValue(sliderPosition);
         updating.setFalse();
      };

      ChangeListener<Number> yoIntegerUpdater = (o, oldValue, newValue) ->
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

      cleanupTasks.add(() ->
      {
         yoIntegerProperty.removeListener(sliderUpdater);
         minProperty.removeListener(sliderUpdater);
         maxProperty.removeListener(sliderUpdater);
         sliderVariable.valueProperty().removeListener(yoIntegerUpdater);
      });
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
      cleanupTasks.forEach(Runnable::run);
   }
}
