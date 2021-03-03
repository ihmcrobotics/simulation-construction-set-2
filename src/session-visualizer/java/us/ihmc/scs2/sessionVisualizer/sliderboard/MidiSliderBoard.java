package us.ihmc.scs2.sessionVisualizer.sliderboard;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;

import us.ihmc.log.LogTools;
import us.ihmc.scs2.sessionVisualizer.sliderboard.MidiControl.ControlType;
import us.ihmc.scs2.sessionVisualizer.sliderboard.MidiControlVariable.MidiControlVariableChangedListener;
import us.ihmc.tools.thread.CloseableAndDisposable;
import us.ihmc.tools.thread.CloseableAndDisposableRegistry;

public class MidiSliderBoard implements ExitActionListener, CloseableAndDisposable
{
   private enum Devices
   {
      VIRTUAL, BCF2000, XTOUCH_COMPACT, GENERIC
   }

   public static final int CHECK_TIME = 10;

   // public int sliderOffset = 0;
   public int sliderBoardMax = 127;

   protected Hashtable<Integer, MidiControl> controlsHashTable = new Hashtable<Integer, MidiControl>(40);
   protected ArrayList<SliderListener> internalListeners = new ArrayList<SliderListener>();

   private ArrayList<SliderBoardControlAddedListener> controlAddedListeners = new ArrayList<SliderBoardControlAddedListener>();

   private Devices preferedDevice = Devices.XTOUCH_COMPACT;
   private int preferdDeviceNumber = -1;

   private static final boolean DEBUG = false;

   private MidiDevice inDevice = null;
   private Receiver midiOut = null;
   private SliderBoardTransmitterInterface transmitter = null;
   private MidiControlVariableChangedListener listener;

   private MidiChannelMapper channelMapper = new GenericChannelMapper();

   private CloseableAndDisposableRegistry closeableAndDisposableRegistry = new CloseableAndDisposableRegistry();

   public MidiSliderBoard()
   {
      try
      {
         MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();

         init(infos);

         // if a motorized slider board is found
         if (preferedDevice.equals(Devices.BCF2000))
         {
            LogTools.info("Setting Up Motorized Slider Board");
            channelMapper = new BCF2000ChannelMapper();

            // sliderOffset = 80;
            sliderBoardMax = 127;

            try
            {
               inDevice.getTransmitter().setReceiver(new BCF2000Receiver(controlsHashTable, internalListeners, sliderBoardMax, this));

               transmitter = new BCF2000Transmitter(midiOut, sliderBoardMax);
            }
            catch (Exception e)
            {
               if (DEBUG)
                  System.err.println("Exception when trying to get MIDI transmitter 1: " + e);
            }

            final Object self = this;
            listener = new MidiControlVariableChangedListener()
            {
               @Override
               public void changed(MidiControlVariable source, double oldValue, double newValue)
               {
                  synchronized (self)
                  {
                     for (MidiControl tmpControl : controlsHashTable.values())
                     {
                        if (tmpControl.var.equals(source))
                        {
                           double value = 0.0;
                           value = (tmpControl).var.getValueAsDouble();
                           yoVariableChanged((tmpControl).mapping, value);
                        }
                     }
                  }
               }
            };
         }
         else if (preferedDevice.equals(Devices.GENERIC))
         { // if a regular slider board is found
            System.out.println("Setting Up Physical Slider Board");
            channelMapper = new BCF2000ChannelMapper(); // Code was the same when making this
            sliderBoardMax = 127;

            try
            {
               inDevice.getTransmitter().setReceiver(new UC33Receiver(controlsHashTable, internalListeners, sliderBoardMax));
            }
            catch (Exception e)
            {
               if (DEBUG)
                  System.err.println("Exception when trying to get MIDI transmitter 1: " + e);
            }
         }
         else if (preferedDevice.equals(Devices.XTOUCH_COMPACT))
         {
            LogTools.info("Setting Up XTouch Slider Board");
            channelMapper = new XTouchChannelMapper();

            // sliderOffset = 80;
            sliderBoardMax = 127;

            try
            {
               inDevice.getTransmitter().setReceiver(new XTouchCompactReceiver(controlsHashTable, internalListeners, sliderBoardMax, this));

               transmitter = new XTouchCompactTransmitter(midiOut, sliderBoardMax);
            }
            catch (Exception e)
            {
               if (DEBUG)
               {
                  System.err.println("Exception when trying to get MIDI transmitter 1: " + e);
                  e.printStackTrace();
               }
            }

            final Object self = this;
            listener = new MidiControlVariableChangedListener()
            {
               @Override
               public void changed(MidiControlVariable source, double oldValue, double newValue)
               {
                  synchronized (self)
                  {
                     for (MidiControl tmpControl : controlsHashTable.values())
                     {
                        if (tmpControl.var.equals(source))
                        {
                           double value = 0.0;
                           value = (tmpControl).var.getValueAsDouble();
                           yoVariableChanged((tmpControl).mapping, value);
                        }
                     }
                  }
               }
            };

         }

         addListener(new SliderListener()
         {
            @Override
            public void valueChanged(MidiControl midiControl)
            {
               if (DEBUG)
                  System.out.println("EVL::valueChanged [" + midiControl.mapping + "] value to : " + midiControl.var.getValueAsDouble() + " YoVal:"
                        + midiControl.var.getValueAsDouble());
            }
         });
      }
      catch (Exception e)
      {
         e.printStackTrace();

      }
   }

   @Override
   public void closeAndDispose()
   {
      closeableAndDisposableRegistry.closeAndDispose();

      controlsHashTable.clear();
      controlsHashTable = null;

      internalListeners.clear();
      internalListeners = null;

      controlAddedListeners.clear();
      controlAddedListeners = null;

      if (inDevice != null)
      {
         try
         {
            inDevice.close();
         }
         catch (Exception e)
         {
            System.err.println("Exception when trying to close inDevice in MidiSliderBoard.closeAndDispose()");
         }
      }

      if (midiOut != null)
      {
         try
         {
            midiOut.close();
         }
         catch (Exception e)
         {
            System.err.println("Exception when trying to close midiOut in MidiSliderBoard.closeAndDispose()");
         }
      }

      if (transmitter != null)
      {
         transmitter.closeAndDispose();
      }

      inDevice = null;
      midiOut = null;
   }

   public int init(MidiDevice.Info[] infos)
   {
      MidiDevice outDevice = null;
      if (DEBUG)
         System.out.println("EvolutionUC33E::init found " + infos.length + " MIDI device infos.");

      int rc = 0;

      String name = null;
      String description = null;
      MidiDevice current = null;
      for (int i = 0; i < infos.length; i++)
      {
         if (DEBUG)
         {
            System.out.println("  Device[" + i + "] " + infos[i].getName());
            System.out.println("          Vendor: " + infos[i].getVendor());
            System.out.println("         Version: " + infos[i].getVersion());
            System.out.println("     Description: " + infos[i].getDescription());
         }

         name = infos[i].getName();
         description = infos[i].getDescription();

         if ((name.indexOf("UC-33") >= 0) || (infos[i].getDescription().indexOf("UC-33") >= 0))
         {
            if (preferdDeviceNumber == -1)
            {
               preferdDeviceNumber = i;

               if (DEBUG)
                  System.out.println("Found Generic SliderBoard");
               preferedDevice = Devices.GENERIC;

               try
               {
                  inDevice = MidiSystem.getMidiDevice(infos[i]);
                  System.out.println("PHYSICAL " + inDevice.getDeviceInfo().getName() + " - " + inDevice.getDeviceInfo().getDescription());
               }
               catch (MidiUnavailableException e)
               {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
               }

               try
               {
                  if (DEBUG)
                  {
                     System.out.println("\nGot device with info" + inDevice.getDeviceInfo());
                     System.out.println("Max Receivers:" + inDevice.getMaxReceivers());
                     System.out.println("Max Transmitters:" + inDevice.getMaxTransmitters());
                  }

                  if (inDevice.isOpen())
                  {
                     if (DEBUG)
                        System.out.println("\nDevice started open. Closing Device\n");
                     inDevice.close();

                     if (inDevice.isOpen())
                     {
                        System.err.println("\nMIDI device started open. Attempted to close it, but could not. You may need to reboot to close the device.");
                     }
                  }

                  else
                  {
                     if (DEBUG)
                        System.out.println("Device started closed");
                  }

                  if (DEBUG)
                     System.out.println("\nOpening Device\n");

                  try
                  {
                     inDevice.open();
                  }
                  catch (Exception e)
                  {
                     if (DEBUG)
                        System.out.println("Exception while trying to open device: " + e);
                  }

               }

               catch (Exception e)
               {
                  System.out.println("Exception: " + e);
               }
            }
         }
         else if (name.contains("BCF2000") || description.contains("BCF2000"))
         {
            preferdDeviceNumber = i;
            preferedDevice = Devices.BCF2000;
            if (DEBUG)
               System.out.println("Found motorizedSliderBoard");

            try
            {
               current = MidiSystem.getMidiDevice(infos[i]);
            }
            catch (MidiUnavailableException e)
            {
               if (DEBUG)
               {
                  System.out.println("   - Unable to get a handle to this Midi Device.");
                  e.printStackTrace();
               }

               continue;
            }

            if ((outDevice == null) && (current.getMaxReceivers() != 0))
            {
               outDevice = current;

               if (!outDevice.isOpen())
               {
                  if (DEBUG)
                     System.out.println("   - Opening Output Device");

                  try
                  {
                     outDevice.open();
                  }
                  catch (MidiUnavailableException e)
                  {
                     outDevice = null;

                     if (DEBUG)
                     {
                        System.out.println("   - Unable to open device.");
                        e.printStackTrace();
                     }

                     continue;
                  }
               }

               if (DEBUG)
                  System.out.println("   - Device is Now open trying to obtain the receiver.");

               try
               {
                  midiOut = outDevice.getReceiver();
               }
               catch (MidiUnavailableException e)
               {
                  outDevice = null;
                  midiOut = null;

                  if (DEBUG)
                  {
                     System.out.println("   - Error getting the device's receiver.");
                     e.printStackTrace();
                  }

                  continue;
               }

               if (DEBUG)
                  System.out.println("   - Obtained a handle to the devices receiver.");
               rc += 2;
            }

            if ((inDevice == null) && (current.getMaxTransmitters() != 0))
            {
               inDevice = current;

               if (DEBUG)
               {
                  System.out.println("\nGot device with info" + inDevice.getDeviceInfo());
                  System.out.println("Max Receivers:" + inDevice.getMaxReceivers());
                  System.out.println("Max Transmitters:" + inDevice.getMaxTransmitters());
               }

               if (DEBUG)
                  System.out.println("   - Opening Input Device.");

               try
               {
                  inDevice.open();
               }
               catch (MidiUnavailableException e1)
               {
                  inDevice = null;

                  if (DEBUG)
                  {
                     System.out.println("   - Exception while trying to open device.");
                     e1.printStackTrace();
                  }

                  continue;
               }

               if (DEBUG)
                  System.out.println("   - Device is Now open trying to obtain the transmitter.");

               rc += 1;
            }
         }
         else if (name.contains("X-TOUCH COMPACT") || description.contains("X-TOUCH COMPACT"))
         {
            preferdDeviceNumber = i;
            preferedDevice = Devices.XTOUCH_COMPACT;
            if (DEBUG)
               System.out.println("Found X-Touch motorizedSliderBoard");

            try
            {
               current = MidiSystem.getMidiDevice(infos[i]);
            }
            catch (MidiUnavailableException e)
            {
               if (DEBUG)
               {
                  System.out.println("   - Unable to get a handle to this Midi Device.");
                  e.printStackTrace();
               }

               continue;
            }

            if ((outDevice == null) && (current.getMaxReceivers() != 0))
            {
               outDevice = current;

               if (!outDevice.isOpen())
               {
                  if (DEBUG)
                     System.out.println("   - Opening Output Device");

                  try
                  {
                     outDevice.open();
                  }
                  catch (MidiUnavailableException e)
                  {
                     outDevice = null;

                     if (DEBUG)
                     {
                        System.out.println("   - Unable to open device.");
                        e.printStackTrace();
                     }

                     continue;
                  }
               }

               if (DEBUG)
                  System.out.println("   - Device is Now open trying to obtain the receiver.");

               try
               {
                  midiOut = outDevice.getReceiver();
               }
               catch (MidiUnavailableException e)
               {
                  outDevice = null;
                  midiOut = null;

                  if (DEBUG)
                  {
                     System.out.println("   - Error getting the device's receiver.");
                     e.printStackTrace();
                  }

                  continue;
               }

               if (DEBUG)
                  System.out.println("   - Obtained a handle to the devices receiver.");
               rc += 2;
            }

            if ((inDevice == null) && (current.getMaxTransmitters() != 0))
            {
               inDevice = current;

               if (DEBUG)
               {
                  System.out.println("\nGot device with info" + inDevice.getDeviceInfo());
                  System.out.println("Max Receivers:" + inDevice.getMaxReceivers());
                  System.out.println("Max Transmitters:" + inDevice.getMaxTransmitters());
               }

               if (DEBUG)
                  System.out.println("   - Opening Input Device.");

               try
               {
                  inDevice.open();
               }
               catch (MidiUnavailableException e1)
               {
                  inDevice = null;

                  if (DEBUG)
                  {
                     System.out.println("   - Exception while trying to open device.");
                     e1.printStackTrace();
                  }

                  continue;
               }

               if (DEBUG)
                  System.out.println("   - Device is Now open trying to obtain the transmitter.");

               rc += 1;
            }
         }
      }

      return rc;
   }

   public void setButton(int channel, MidiControlVariable var)
   {
      setControl(channelMapper.getButtonChannel(channel), var, ControlType.BUTTON);
   }

   public void setKnobButton(int channel, MidiControlVariable var)
   {
      setControl(channelMapper.getKnobButtonChannel(channel), var, ControlType.BUTTON);
   }

   public void setKnob(int channel, MidiControlVariable var)
   {
      setControl(channelMapper.getKnobChannel(channel), var, ControlType.KNOB);
   }

   public void setSlider(int channel, MidiControlVariable var)
   {
      setControl(channelMapper.getSliderChannel(channel), var, ControlType.SLIDER);
   }

   private synchronized void setControl(int channel, MidiControlVariable var, ControlType controlType)
   {
      MidiControl midiControl = new MidiControl(channel, var);
      midiControl.controlType = controlType;

      setControl(midiControl);
      setToInitialPosition(midiControl);

      for (SliderBoardControlAddedListener listener : controlAddedListeners)
      {
         listener.controlAdded(midiControl);
      }
   }

   public void setRange(int channel, double min, double max)
   {
      setRange(channel, min, max, 1.0);
   }

   public void setRange(int channel, double min, double max, double exponent)
   {
      if (exponent <= 0.0)
      {
         System.err.println("Peavey PC1600X: Exponent must be positive. Setting it to 1.0");
         exponent = 1.0;
      }

      MidiControl control = controlsHashTable.get(channel);
      control.min = min;
      control.max = max;
      control.exponent = exponent;
   }

   private synchronized void setControl(MidiControl newMidiControl)
   {

      MidiControl oldMidiControl = controlsHashTable.get(newMidiControl.mapping);
      if (oldMidiControl != null)
      {
         if (listener != null)
            oldMidiControl.var.removeListener(listener);
      }

      if (listener != null)
         newMidiControl.var.addListener(listener);
      controlsHashTable.put(newMidiControl.mapping, newMidiControl);
   }

   public synchronized void clearControls()
   {
      for (MidiControl toBeRemoved : controlsHashTable.values())
      {
         for (SliderBoardControlAddedListener listener : controlAddedListeners)
         {
            listener.controlRemoved(toBeRemoved);
         }

      }

      controlsHashTable.clear();
   }

   protected void moveControl(MidiControl midiControl)
   {
      if (transmitter != null)
      {
         transmitter.moveControl(midiControl);
      }
   }

   protected void moveControl(MidiControl midiControl, int sliderValue)
   {
      if (transmitter != null)
      {
         transmitter.moveControl(midiControl, sliderValue);
      }

   }

   public void setToInitialPosition(MidiControl midiControl)
   {
      // ctrl.var.setValueFromDouble(ctrl.reset);

      for (SliderListener listener : internalListeners)
      {
         listener.valueChanged(midiControl);
      }

      moveControl(midiControl);
   }

   public void reset()
   {
      MidiControl midiControl = null;
      Enumeration<MidiControl> midiControllers = controlsHashTable.elements();
      while (midiControllers.hasMoreElements())
      {
         midiControl = midiControllers.nextElement();
         midiControl.var.setValueFromDouble(midiControl.reset);

         for (SliderListener listener : internalListeners)
         {
            listener.valueChanged(midiControl);
         }

         moveControl(midiControl);
      }
   }

   public double getValue(int mapping)
   {
      MidiControl midiControl = controlsHashTable.get(mapping);
      if (midiControl != null)
         return midiControl.var.getValueAsDouble();

      return -1;
   }

   public void setValue(int mapping, double value) throws InvalidParameterException
   {
      MidiControl midiControl = controlsHashTable.get(mapping);
      if (midiControl == null)
         throw new InvalidParameterException("name does not map to a control");

      if (midiControl.currentVal == value)
         return;

      midiControl.currentVal = value;

      for (SliderListener listener : internalListeners)
      {
         listener.valueChanged(midiControl);
      }

      moveControl(midiControl);
   }

   public void yoVariableChanged(int mapping, double value) throws InvalidParameterException
   {
      MidiControl midiControl = controlsHashTable.get(mapping);
      if (midiControl == null)
         throw new InvalidParameterException("name does not map to a control");

      moveControl(midiControl);
   }

   public void addListener(SliderListener listener)
   {
      internalListeners.add(listener);
   }

   public void removeListener(SliderListener listener)
   {
      internalListeners.remove(listener);
   }

   public void addListener(SliderBoardControlAddedListener listener)
   {
      controlAddedListeners.add(listener);
   }

   public void removeListener(SliderBoardControlAddedListener listener)
   {
      controlAddedListeners.remove(listener);
   }

   @Override
   public void exitActionPerformed()
   {
      printIfDebug("Exit Action was performed. Closing and disposing " + getClass().getSimpleName());

      this.closeAndDispose();
   }

   public interface SliderListener
   {
      public void valueChanged(MidiControl midiControl);
   }

   private void printIfDebug(String string)
   {
      if (DEBUG)
         System.out.println(string);
   }
}
