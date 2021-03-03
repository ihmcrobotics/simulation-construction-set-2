package us.ihmc.scs2.sessionVisualizer.sliderboard;

public class MidiControl
{
   public int mapping = -1;
   public double reset = -1;
   public double max = 127;
   public double min = 0;
   public double exponent = 1;
   public double hires = (min + max) / 2.0;
   public double currentVal = -1;
   public MidiControlVariable var = null;
   public String name = null;

   public enum SliderType
   {
      BOOLEAN, ENUM, NUMBER, INT
   }

   public SliderType sliderType;

   public enum ControlType
   {
      SLIDER, BUTTON, KNOB
   }

   public ControlType controlType;
   public boolean notify = true;

   public MidiControl(int mapping, MidiControlVariable var)
   {
      this(mapping, var, true);
   }

   public MidiControl(int mapping, MidiControlVariable var, boolean notify)
   {
      this.mapping = mapping;
      this.currentVal = var.getValueAsDouble();
      this.reset = this.currentVal;
      this.min = var.getMin();
      this.max = var.getMax();
      this.var = var;
      this.exponent = var.getExponent();
      this.hires = var.getHires();
      this.name = var.getName();
      this.sliderType = var.getType();
   }
}
