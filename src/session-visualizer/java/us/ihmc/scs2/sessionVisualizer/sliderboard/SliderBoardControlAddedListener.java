package us.ihmc.scs2.sessionVisualizer.sliderboard;

public interface SliderBoardControlAddedListener
{
   public void controlAdded(MidiControl ctrl);

   public void controlRemoved(MidiControl ctrl);
}