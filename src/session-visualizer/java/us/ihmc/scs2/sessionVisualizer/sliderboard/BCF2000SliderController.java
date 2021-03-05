package us.ihmc.scs2.sessionVisualizer.sliderboard;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import us.ihmc.scs2.sessionVisualizer.sliderboard.BCF2000SliderboardController.Slider;

public class BCF2000SliderController
{
   private final Slider slider;
   private final Receiver midiOut;

   private SliderboardControlVariable controlVariable;

   private double currentSliderValue = Double.NaN;
   private double newSliderValue = Double.NaN;

   public BCF2000SliderController(Slider slider, Receiver midiOut)
   {
      this.slider = slider;
      this.midiOut = midiOut;
   }

   public void setControlVariable(SliderboardControlVariable controlVariable)
   {
      this.controlVariable = controlVariable;
   }

   public void handleMessage(ShortMessage message, long timestamp)
   {
      if (slider.getChannel() != message.getData1()) // Should it use getChannel instead
         return;

      newSliderValue = SliderboardControlVariable.intToDouble(message.getData2(),
                                                              slider.getMin(),
                                                              slider.getMax(),
                                                              controlVariable.getMin(),
                                                              controlVariable.getMax());
   }

   public void moveSlider(double value)
   {
      if (Double.isNaN(value))
         return;

      try
      {
         ShortMessage message = new ShortMessage();
         int intValue = SliderboardControlVariable.doubleToInt(value, controlVariable.getMin(), controlVariable.getMax(), slider.getMin(), slider.getMax());
         message.setMessage(176, 0, slider.getChannel(), intValue);
         midiOut.send(message, -1);
      }
      catch (InvalidMidiDataException e)
      {
         e.printStackTrace();
      }
   }

   public void update()
   {
      if (controlVariable == null)
         return;

      if (!Double.isNaN(newSliderValue))
         currentSliderValue = newSliderValue;

      if (!Double.isNaN(newSliderValue))
      {
         controlVariable.setValue(newSliderValue);
         newSliderValue = Double.NaN;
      }
      else if (controlVariable.getValue() != currentSliderValue)
      {
         moveSlider(controlVariable.getValue());
      }
   }
}
