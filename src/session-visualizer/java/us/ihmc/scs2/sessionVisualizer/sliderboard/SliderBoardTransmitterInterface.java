package us.ihmc.scs2.sessionVisualizer.sliderboard;

public interface SliderBoardTransmitterInterface
{
   public abstract void moveControl(MidiControl midiControl);
   public abstract void moveControl(MidiControl midiControl, int sliderValue);
   public abstract void closeAndDispose();
}
