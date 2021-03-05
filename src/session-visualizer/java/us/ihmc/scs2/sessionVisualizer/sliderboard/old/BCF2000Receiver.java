package us.ihmc.scs2.sessionVisualizer.sliderboard.old;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

public class BCF2000Receiver implements Receiver
{
   public int SLIDER_OFFSET = 80;

   public int SLIDERBOARDMAX = 127;

   protected Hashtable<Integer, MidiControl> controlsHashTable;
   protected ArrayList<MidiSliderBoard.SliderListener> internalListeners;
   private boolean debug = false;
   private MidiSliderBoard board;

   public BCF2000Receiver(Hashtable<Integer, MidiControl> controlsHashTable, ArrayList<MidiSliderBoard.SliderListener> internalListeners, int SliderBoardMax, MidiSliderBoard board)
   {
      this.board = board;
      this.SLIDERBOARDMAX = SliderBoardMax;
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
         ShortMessage sm = (ShortMessage) message;
         if (debug)
         {
            System.out.println("*************************");
            System.out.println("Channel " + sm.getChannel());
            System.out.println("Command " + sm.getCommand());
            System.out.println("data1 " + sm.getData1());
            System.out.println("data2 " + sm.getData2());
            System.out.println("Status " + sm.getStatus());
         }

         MidiControl ctrl = controlsHashTable.get(sm.getData1() - SLIDER_OFFSET);
         if (ctrl != null)
         {
            double newVal = 0.0;

            newVal = SliderBoardUtils.valueRatioConvertToDoubleWithExponents(ctrl, sm.getData2(), SLIDERBOARDMAX);

            ctrl.currentVal = newVal;
            ctrl.var.setValueFromDouble(newVal);

            // snap it in place
            if (ctrl.sliderType == MidiControl.SliderType.BOOLEAN)
            {
               //System.out.println("newVal: "+newVal);
               if (newVal <= 0.5)
               {
                  //System.out.println("moving to 0");
                  board.moveControl(ctrl, 0);
               } else
               {
                  //System.out.println("movint to "+SLIDERBOARDMAX);
                  board.moveControl(ctrl, SLIDERBOARDMAX);
               }
            }

            else if (ctrl.sliderType == MidiControl.SliderType.ENUM)
            {
               board.moveControl(ctrl, SliderBoardUtils.valueRatioConvertToIntWithExponents(ctrl, SLIDERBOARDMAX));
            }

            // ctrl.var.notifyObservers(new Event(EventType.CHANGED));

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

}
