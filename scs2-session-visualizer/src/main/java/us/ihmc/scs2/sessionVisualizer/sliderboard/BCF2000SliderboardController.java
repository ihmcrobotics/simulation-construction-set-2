package us.ihmc.scs2.sessionVisualizer.sliderboard;

import java.lang.reflect.Array;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

import us.ihmc.commons.thread.ThreadTools;
import us.ihmc.scs2.definition.yoSlider.YoSliderboardType;

public class BCF2000SliderboardController
{
   public enum BCF2000Knob implements MidiChannelConfig
   {
      KNOB_1, KNOB_2, KNOB_3, KNOB_4, KNOB_5, KNOB_6, KNOB_7, KNOB_8;

      public static final BCF2000Knob[] values = values();
      private static final int CHANNEL_OFFSET = 1;

      private final int channel;

      private BCF2000Knob()
      {
         this.channel = ordinal() + CHANNEL_OFFSET;
      }

      @Override
      public int getChannel()
      {
         return channel;
      }

      @Override
      public int getMin()
      {
         return 0;
      }

      @Override
      public int getMax()
      {
         return 127;
      }

      public static BCF2000Knob fromChannel(int channel)
      {
         if (channel < CHANNEL_OFFSET || channel > CHANNEL_OFFSET + 7)
            return null;
         return values[channel - CHANNEL_OFFSET];
      }
   }

   public enum BCF2000Slider implements MidiChannelConfig
   {
      SLIDER_1, SLIDER_2, SLIDER_3, SLIDER_4, SLIDER_5, SLIDER_6, SLIDER_7, SLIDER_8;

      public static final BCF2000Slider[] values = values();
      private static final int CHANNEL_OFFSET = 81;

      private final int channel;

      private BCF2000Slider()
      {
         this.channel = ordinal() + CHANNEL_OFFSET;
      }

      @Override
      public int getChannel()
      {
         return channel;
      }

      @Override
      public int getMin()
      {
         return 0;
      }

      @Override
      public int getMax()
      {
         return 127;
      }

      public static BCF2000Slider fromChannel(int channel)
      {
         if (channel < CHANNEL_OFFSET || channel > CHANNEL_OFFSET + 7)
            return null;
         return values[channel - CHANNEL_OFFSET];
      }
   }

   public enum BCF2000Button implements MidiChannelConfig
   {
      // First row
      BUTTON_1,
      BUTTON_2,
      BUTTON_3,
      BUTTON_4,
      BUTTON_5,
      BUTTON_6,
      BUTTON_7,
      BUTTON_8,
      // Second row
      BUTTON_9,
      BUTTON_10,
      BUTTON_11,
      BUTTON_12,
      BUTTON_13,
      BUTTON_14,
      BUTTON_15,
      BUTTON_16;

      public static final BCF2000Button[] values = values();
      private static final int CHANNEL_OFFSET = 65;

      private final int channel;

      private BCF2000Button()
      {
         channel = ordinal() + CHANNEL_OFFSET;
      }

      @Override
      public int getChannel()
      {
         return channel;
      }

      public static BCF2000Button fromChannel(int channel)
      {
         if (channel < CHANNEL_OFFSET || channel > CHANNEL_OFFSET + 15)
            return null;
         return values[channel - CHANNEL_OFFSET];
      }
   }

   public enum KnobButton implements MidiChannelConfig
   {
      BUTTON_1, BUTTON_2, BUTTON_3, BUTTON_4, BUTTON_5, BUTTON_6, BUTTON_7, BUTTON_8;

      public static final KnobButton[] values = values();
      private static final int CHANNEL_OFFSET = 32;

      private final int channel;

      private KnobButton()
      {
         channel = ordinal() + CHANNEL_OFFSET;
      }

      @Override
      public int getChannel()
      {
         return channel;
      }

      public static KnobButton fromChannel(int channel)
      {
         if (channel < CHANNEL_OFFSET || channel > CHANNEL_OFFSET + 7)
            return null;
         return values[channel - CHANNEL_OFFSET];
      }
   }

   private final Receiver midiOut;
   private final Transmitter midiIn;

   private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1,
                                                                                      ThreadTools.createNamedDaemonThreadFactory(getClass().getSimpleName()));
   private ScheduledFuture<?> currentTask;

   private final BehringerSliderController[] sliderControllers = new BehringerSliderController[8];
   private final BehringerButtonController[] buttonControllers = new BehringerButtonController[16];
   private final BehringerKnobController[] knobControllers = new BehringerKnobController[8];
   private final BehringerChannelController[] allControllers;

   private final Receiver receiver = new Receiver()
   {
      @Override
      public void send(MidiMessage message, long timeStamp)
      {
         if (!(message instanceof ShortMessage))
            return;

         ShortMessage shortMessage = (ShortMessage) message;

         BCF2000Slider slider = BCF2000Slider.fromChannel(shortMessage.getData1());

         if (slider != null)
         {
            if (sliderControllers[slider.ordinal()].handleMessage(shortMessage, timeStamp))
               return;
         }

         BCF2000Button button = BCF2000Button.fromChannel(shortMessage.getData1());

         if (button != null)
         {
            if (buttonControllers[button.ordinal()].handleMessage(shortMessage, timeStamp))
               return;
         }

         BCF2000Knob knob = BCF2000Knob.fromChannel(shortMessage.getData1());

         if (knob != null)
         {
            if (knobControllers[knob.ordinal()].handleMessage(shortMessage, timeStamp))
               return;
         }
      }

      @Override
      public void close()
      {
      }
   };

   private BCF2000SliderboardController(Receiver midiOut, Transmitter midiIn)
   {
      this.midiOut = midiOut;
      this.midiIn = midiIn;

      midiIn.setReceiver(receiver);

      for (BCF2000Slider slider : BCF2000Slider.values)
      {
         sliderControllers[slider.ordinal()] = new BehringerSliderController(slider, midiOut);
      }

      for (BCF2000Button button : BCF2000Button.values)
      {
         buttonControllers[button.ordinal()] = new BehringerButtonController(button, midiOut);
      }

      for (BCF2000Knob knob : BCF2000Knob.values)
      {
         knobControllers[knob.ordinal()] = new BehringerKnobController(knob, midiOut);
      }

      allControllers = combineArrays(BehringerChannelController.class, sliderControllers, buttonControllers, knobControllers);
   }

   @SuppressWarnings("unchecked")
   private static <T> T[] combineArrays(Class<T> componentType, T[]... inputArrays)
   {
      int outputLength = 0;
      for (T[] inputArray : inputArrays)
      {
         outputLength += inputArray.length;
      }

      T[] outputArray = (T[]) Array.newInstance(componentType, outputLength);

      int outputIndex = 0;
      for (T[] inputArray : inputArrays)
      {
         for (int i = 0; i < inputArray.length; i++)
         {
            outputArray[outputIndex++] = inputArray[i];
         }
      }

      return outputArray;
   }

   public SliderboardVariable getSlider(BCF2000Slider slider)
   {
      return sliderControllers[slider.ordinal()].getControlVariable();
   }

   public SliderboardVariable getButton(BCF2000Button button)
   {
      return buttonControllers[button.ordinal()].getControlVariable();
   }

   public SliderboardVariable getKnob(BCF2000Knob knob)
   {
      return knobControllers[knob.ordinal()].getControlVariable();
   }

   public void update()
   {
      for (BehringerChannelController controller : allControllers)
      {
         controller.update();
      }
   }

   public void start()
   {
      if (currentTask != null)
         return;

      for (BehringerChannelController controller : allControllers)
      {
         controller.enable();
      }

      currentTask = executor.scheduleAtFixedRate(this::update, 0, 20, TimeUnit.MILLISECONDS);
   }

   public void stop()
   {
      if (currentTask != null)
      {
         currentTask.cancel(false);
         currentTask = null;
      }

      for (BehringerChannelController controller : allControllers)
      {
         controller.disable();
      }
   }

   public void closeAndDispose()
   {
      stop();
      executor.shutdown();

      if (midiOut != null)
         midiOut.close();
      if (midiIn != null)
         midiIn.close();
   }

   public static boolean isBCF2000Sliderboard(MidiDevice.Info info)
   {
      String name = info.getName();
      String description = info.getDescription();
      return name.contains(YoSliderboardType.BCF2000.getTypeString()) || description.contains(YoSliderboardType.BCF2000.getTypeString());
   }

   public static void closeMidiDevices()
   {
      MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();

      for (MidiDevice.Info info : infos)
      {
         if (!isBCF2000Sliderboard(info))
            continue;

         MidiDevice midiDevice = BehringerMidiHelpers.getDevice(info);

         if (midiDevice != null && midiDevice.isOpen())
         {
            midiDevice.close();
            continue;
         }
      }
   }

   public static BCF2000SliderboardController searchAndConnectToDevice()
   {
      MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();

      Receiver out = null;
      Transmitter in = null;

      for (MidiDevice.Info info : infos)
      {
         if (out != null && in != null)
            break;

         if (!isBCF2000Sliderboard(info))
            continue;

         if (out == null)
         {
            out = BehringerMidiHelpers.connectToMidiOutDevice(info);
            if (out != null)
               continue;
         }

         if (in == null)
            in = BehringerMidiHelpers.connectToMidiInDevice(info);
      }

      if (out != null && in != null)
         return new BCF2000SliderboardController(out, in);
      else
         return null;
   }

}
