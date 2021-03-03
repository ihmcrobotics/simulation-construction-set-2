package us.ihmc.scs2.sessionVisualizer.sliderboard;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;

public class UC33Receiver implements Receiver
{
   public int sliderOffset = 1;

   public int sliderBoardMax = 0;

   protected Hashtable<Integer, MidiControl> controlsHashTable;
   protected ArrayList<MidiSliderBoard.SliderListener> internalListeners;
   private boolean debug = false;

   public UC33Receiver(Hashtable<Integer, MidiControl> controlsHashTable, ArrayList<MidiSliderBoard.SliderListener> internalListeners, int SliderBoardMax)
   {
      this.sliderBoardMax = SliderBoardMax;
      this.controlsHashTable = controlsHashTable;
      this.internalListeners = internalListeners;
   }

   @Override
   public void close()
   {
   }

   @Override
   public void send(MidiMessage message, long timeStamp)
   {
      if (message instanceof ShortMessage)
      {
         decodeMessage((ShortMessage) message);
      } else if (message instanceof SysexMessage)
      {
         decodeMessage((SysexMessage) message);
      } else if (message instanceof MetaMessage)
      {
         decodeMessage((MetaMessage) message);
      } else
      {
         // Unknown message type
      }
   }

   public void decodeMessage(SysexMessage message)
   {
      System.out.println("SysexMessage");
   }

   public void decodeMessage(MetaMessage message)
   {
      System.out.println("MetaMessage");
   }

   public void decodeMessage(ShortMessage message)
   {
      int channel = message.getChannel();

      int data1 = message.getData1();
      int data2 = message.getData2();

      if (data1 == 7)
      {
      } else if (data1 == 10)
      {
         channel = channel + 9;
      } else if (data1 == 12)
      {
         channel = channel + 9 + 8;
      } else if (data1 == 13)
      {
         channel = channel + 9 + 8 + 8;
      }

      channel += sliderOffset;

      // System.out.println("ShortMessage");
      // System.out.println("Data 1: " + data1);
      // System.out.println("Data 2: " + data2);
      // System.out.println();

      MidiControl ctrl = controlsHashTable.get(channel);

      if (ctrl != null)
      {
         double newVal = 0.0;

         newVal = SliderBoardUtils.valueRatioConvertToDoubleWithExponents(ctrl, data2, sliderBoardMax);

         ctrl.var.setValueFromDouble(newVal);

         //ctrl.var.notifyObservers(new Event(EventType.CHANGED));

         // Enumeration<SliderListener> listeners = internalListeners.;
         for (MidiSliderBoard.SliderListener listener : internalListeners)
         {
            listener.valueChanged(ctrl);
         }

         if (ctrl.notify)
         {
            ctrl.var.notifyChange();
         }
         if (debug)
            System.out.println("Changed [" + ctrl.mapping + "] value to : " + ctrl.var.getValueAsDouble());
      }

   }

}
