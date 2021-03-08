package us.ihmc.scs2.sessionVisualizer.jfx.controllers.sliderboard;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXTextField;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.DoubleStringConverter;
import us.ihmc.scs2.sessionVisualizer.jfx.properties.YoDoubleProperty;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.NumberFormatTools;
import us.ihmc.scs2.sessionVisualizer.sliderboard.SliderVariable;
import us.ihmc.yoVariables.variable.YoDouble;

public class YoDoubleSlider implements YoVariableSlider
{
   private final YoDoubleProperty yoDoubleProperty;
   private final DoubleProperty minProperty = new SimpleDoubleProperty(this, "min", 0.0);
   private final DoubleProperty maxProperty = new SimpleDoubleProperty(this, "max", 1.0);
   private final List<Runnable> cleanupTasks = new ArrayList<>();
   private final Runnable pushValueAction;

   public YoDoubleSlider(YoDouble yoDouble, Runnable pushValueAction)
   {
      this.pushValueAction = pushValueAction;
      yoDoubleProperty = new YoDoubleProperty(yoDouble, this);
   }

   @Override
   public void bindMinTextField(JFXTextField minTextField)
   {
      TextFormatter<Double> minTextFormatter = new TextFormatter<>(new DoubleStringConverter());
      minTextFormatter.setValue(Math.floor(yoDoubleProperty.get() - 1.0));
      minTextField.setTextFormatter(minTextFormatter);
      minProperty.bind(minTextFormatter.valueProperty());

      cleanupTasks.add(() -> minProperty.unbind());
   }

   @Override
   public void bindMaxTextField(JFXTextField maxTextField)
   {
      TextFormatter<Double> maxTextFormatter = new TextFormatter<>(new DoubleStringConverter());
      maxTextFormatter.setValue(Math.ceil(yoDoubleProperty.get() + 1.0));
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
            return NumberFormatTools.doubleToString(yoDoubleProperty.get(), 5);
         }
      });

      virtualSlider.minProperty().bind(minProperty);
      virtualSlider.maxProperty().bind(maxProperty);

      ChangeListener<Object> majorTickUnitUpdater = (o, oldValue, newValue) ->
      {
         double range = maxProperty.get() - minProperty.get();
         if (range <= 0.0)
            return;
         virtualSlider.setMajorTickUnit(range / 25.0);
      };

      minProperty.addListener(majorTickUnitUpdater);
      maxProperty.addListener(majorTickUnitUpdater);

      MutableBoolean updating = new MutableBoolean(false);
      MutableBoolean puhingValue = new MutableBoolean(false);
      DoubleProperty userInputProperty = yoDoubleProperty.userInputProperty();

      virtualSlider.setValue(yoDoubleProperty.get());

      ChangeListener<Object> sliderUpdater = (o, oldValue, newValue) ->
      {
         if (updating.isTrue())
            return;

         if (puhingValue.isTrue())
         {
            if (yoDoubleProperty.get() == userInputProperty.get())
               puhingValue.setFalse();
         }

         if (puhingValue.isTrue())
            return;

         updating.setTrue();
         virtualSlider.valueProperty().set(yoDoubleProperty.get());
         updating.setFalse();
      };

      ChangeListener<Number> yoDoubleUpdater = (o, oldValue, newValue) ->
      {
         if (updating.isTrue())
            return;

         updating.setTrue();
         yoDoubleProperty.set(virtualSlider.valueProperty().get());
         puhingValue.setTrue();
         pushValueAction.run();
         updating.setFalse();
      };

      yoDoubleProperty.addListener(sliderUpdater);
      virtualSlider.valueProperty().addListener(yoDoubleUpdater);

      cleanupTasks.add(() ->
      {
         yoDoubleProperty.removeListener(sliderUpdater);
         minProperty.removeListener(majorTickUnitUpdater);
         maxProperty.removeListener(majorTickUnitUpdater);
         virtualSlider.valueProperty().removeListener(yoDoubleUpdater);
         virtualSlider.minProperty().unbind();
         virtualSlider.maxProperty().unbind();
      });
   }

   @Override
   public void bindSliderVariable(SliderVariable sliderVariable)
   {
      MutableBoolean updating = new MutableBoolean(false);
      MutableBoolean puhingValue = new MutableBoolean(false);
      DoubleProperty userInputProperty = yoDoubleProperty.userInputProperty();

      if (!minProperty.isBound())
         minProperty.set(Math.floor(yoDoubleProperty.get() - 1.0));
      if (!maxProperty.isBound())
         maxProperty.set(Math.ceil(yoDoubleProperty.get() + 1.0));

      sliderVariable.setValue(SliderVariable.doubleToInt(yoDoubleProperty.get(),
                                                         minProperty.get(),
                                                         maxProperty.get(),
                                                         sliderVariable.getMin(),
                                                         sliderVariable.getMax()));

      ChangeListener<Object> sliderUpdater = (o, oldValue, newValue) ->
      {
         if (updating.isTrue())
            return;

         if (puhingValue.isTrue())
         {
            if (yoDoubleProperty.get() == userInputProperty.get())
               puhingValue.setFalse();
         }

         if (puhingValue.isTrue())
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

      ChangeListener<Number> yoDoubleUpdater = (o, oldValue, newValue) ->
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
         puhingValue.setTrue();
         pushValueAction.run();
         updating.setFalse();
      };

      yoDoubleProperty.addListener(sliderUpdater);
      minProperty.addListener(sliderUpdater);
      maxProperty.addListener(sliderUpdater);

      sliderVariable.valueProperty().addListener(yoDoubleUpdater);

      cleanupTasks.add(() ->
      {
         yoDoubleProperty.removeListener(sliderUpdater);
         minProperty.removeListener(sliderUpdater);
         maxProperty.removeListener(sliderUpdater);
         sliderVariable.valueProperty().removeListener(yoDoubleUpdater);
      });
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
      cleanupTasks.forEach(Runnable::run);
   }
}