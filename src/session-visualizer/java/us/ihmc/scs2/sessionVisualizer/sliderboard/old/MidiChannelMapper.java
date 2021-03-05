package us.ihmc.scs2.sessionVisualizer.sliderboard.old;

public interface MidiChannelMapper
{
   int getKnobChannel(int knob);

   int getSliderChannel(int slider);

   int getButtonChannel(int button);

   int getKnobButtonChannel(int knob);
}
