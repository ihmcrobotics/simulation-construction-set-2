package us.ihmc.scs2.sessionVisualizer.sliderboard;

public class BCF2000ChannelMapper implements MidiChannelMapper
{

   @Override
   public int getKnobChannel(int channel)
   {
      return channel - 80;
   }

   @Override
   public int getSliderChannel(int channel)
   {
      if (channel < 1 || channel > 8)
         throw new IllegalArgumentException("Invalid slider channel: " + channel + ", should be in [1, 8]");
      return channel;
   }

   @Override
   public int getButtonChannel(int channel)
   {
      int offset;
      if ((channel >= 17) && (channel <= 20))
         offset = -8;
      else
         offset = -16;

      return channel + offset;
   }

   @Override
   public int getKnobButtonChannel(int channel)
   {
      return channel - 48;
   }

}
