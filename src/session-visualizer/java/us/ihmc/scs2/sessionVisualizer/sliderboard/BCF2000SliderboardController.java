package us.ihmc.scs2.sessionVisualizer.sliderboard;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;

import us.ihmc.log.LogTools;

public class BCF2000SliderboardController
{
   public static boolean DEBUG = false;

   public BCF2000SliderboardController()
   {
   }

   public boolean isBCF2000Sliderboard(MidiDevice.Info info)
   {
      String name = info.getName();
      String description = info.getDescription();
      return name.contains("BCF2000") || description.contains("BCF2000");
   }

   public boolean connectMidiDevice(MidiDevice.Info info)
   {
      MidiDevice midiDevice;
      try
      {
         midiDevice = MidiSystem.getMidiDevice(info);
      }
      catch (MidiUnavailableException e)
      {
         if (DEBUG)
         {
            LogTools.info("   - Unable to get a handle to this Midi Device.");
            e.printStackTrace();
         }

         return false;
      }

      if (midiDevice.getMaxReceivers() == 0)
      {
         if (DEBUG)
         {
            LogTools.error("Cannot add receiver to the device: " + midiDevice);
            return false;
         }
      }

      if (midiDevice.getMaxTransmitters() == 0)
      {
         if (DEBUG)
         {
            LogTools.error("Cannot add transmitter to the device: " + midiDevice);
            return false;
         }
      }

      if (!midiDevice.isOpen())
      {
         if (DEBUG)
            LogTools.info("   - Opening Output Device");

         try
         {
            midiDevice.open();
         }
         catch (MidiUnavailableException e)
         {
            midiDevice = null;

            if (DEBUG)
            {
               LogTools.info("   - Unable to open device.");
               e.printStackTrace();
            }

            return false;
         }
      }

      if (DEBUG)
         System.out.println("   - Device is Now open trying to obtain the receiver.");

      Receiver midiOut = null;

      try
      {
         midiOut = midiDevice.getReceiver();
      }
      catch (MidiUnavailableException e)
      {
         midiDevice = null;
         midiOut = null;

         if (DEBUG)
         {
            LogTools.info("   - Error getting the device's receiver.");
            e.printStackTrace();
         }

         return false;
      }

      if (DEBUG)
         LogTools.info("   - Obtained a handle to the devices receiver.");

      return true;
   }
}
