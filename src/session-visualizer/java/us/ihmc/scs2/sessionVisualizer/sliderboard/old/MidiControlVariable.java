package us.ihmc.scs2.sessionVisualizer.sliderboard.old;

import us.ihmc.scs2.sessionVisualizer.sliderboard.old.MidiControl.SliderType;

public interface MidiControlVariable
{
   String getName();

   double getMin();

   double getMax();

   default double getExponent()
   {
      return 1.0;
   }

   default double getHires()
   {
      return (getMin() + getMax()) / 2.0;
   }

   double getValueAsDouble();

   SliderType getType();

   void setValueFromDouble(double value);

   void notifyChange();

   void addListener(MidiControlVariableChangedListener listener);

   boolean removeListener(MidiControlVariableChangedListener listener);

   void removeListeners();

   public interface MidiControlVariableChangedListener
   {
      void changed(MidiControlVariable source, double oldValue, double newValue);
   }
}
