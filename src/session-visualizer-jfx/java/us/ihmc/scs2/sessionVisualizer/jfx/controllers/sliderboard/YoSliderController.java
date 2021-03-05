package us.ihmc.scs2.sessionVisualizer.jfx.controllers.sliderboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXTextField;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.input.TransferMode;
import javafx.scene.text.Text;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.YoCompositeSearchManager;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.DragAndDropTools;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.YoComposite;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.YoCompositeTools;
import us.ihmc.scs2.sessionVisualizer.sliderboard.MidiControl.SliderType;
import us.ihmc.scs2.sessionVisualizer.sliderboard.MidiControlVariable;
import us.ihmc.scs2.sessionVisualizer.sliderboard.MidiSliderBoard;
import us.ihmc.yoVariables.variable.YoVariable;

public class YoSliderController
{
   @FXML
   private JFXTextField sliderMaxTextField;
   @FXML
   private JFXTextField sliderMinTextField;
   @FXML
   private JFXSlider slider;
   @FXML
   private Label yoVariableDropLabel;

   private YoCompositeSearchManager yoCompositeSearchManager;
   private MidiSliderBoard midiSliderBoard;
   private YoVariable yoVariable;

   public void initialize(SessionVisualizerToolkit toolkit, MidiSliderBoard midiSliderBoard)
   {
      this.midiSliderBoard = midiSliderBoard;
      yoCompositeSearchManager = toolkit.getYoCompositeSearchManager();

      yoVariableDropLabel.setOnDragDetected(this::handleDragDetected);
      yoVariableDropLabel.setOnDragOver(this::handleDragOver);
      yoVariableDropLabel.setOnDragDropped(this::handleDragDropped);
      yoVariableDropLabel.setOnDragEntered(this::handleDragEntered);
      yoVariableDropLabel.setOnDragExited(this::handleDragExited);
   }

   private void setSlider(YoVariable yoVariable)
   {
      this.yoVariable = yoVariable;
      yoVariableDropLabel.setText(yoVariable.getName());

      midiSliderBoard.setSlider(1, new MidiControlVariable()
      {
         private final List<MidiControlVariableChangedListener> listeners = new ArrayList<>();

         {
            yoVariable.addListener(v -> notifyChange());
         }

         @Override
         public void setValueFromDouble(double value)
         {
            yoVariable.setValueFromDouble(value);
         }

         @Override
         public void addListener(MidiControlVariableChangedListener listener)
         {
            listeners.add(listener);
         }

         @Override
         public void removeListeners()
         {
            listeners.clear();
         }

         @Override
         public boolean removeListener(MidiControlVariableChangedListener listener)
         {
            return listeners.remove(listener);
         }

         @Override
         public void notifyChange()
         {
            listeners.forEach(l -> l.changed(this, Double.NaN, getValueAsDouble()));
         }

         @Override
         public double getValueAsDouble()
         {
            return yoVariable.getValueAsDouble();
         }

         @Override
         public SliderType getType()
         {
            return SliderType.NUMBER;
         }

         @Override
         public String getName()
         {
            return yoVariable.getName();
         }

         @Override
         public double getMin()
         {
            return 0;
         }

         @Override
         public double getMax()
         {
            return 1;
         }
      });
   }

   public void handleDragDetected(MouseEvent event)
   {
      if (event == null || yoVariable == null)
         return;

      if (!event.isPrimaryButtonDown())
         return;

      PickResult pickResult = event.getPickResult();

      if (pickResult == null)
         return;

      Node intersectedNode = pickResult.getIntersectedNode();

      if (intersectedNode == null)
         return;

      if (intersectedNode instanceof Text)
      {
         Text legend = (Text) intersectedNode;
         String yoVariableName = legend.getText().split("\\s+")[0];
         if (!yoVariableName.equals(yoVariable.getName()))
            return;
         Dragboard dragBoard = legend.startDragAndDrop(TransferMode.ANY);
         ClipboardContent clipboardContent = new ClipboardContent();
         clipboardContent.put(DragAndDropTools.YO_COMPOSITE_REFERENCE, Arrays.asList(YoCompositeTools.YO_VARIABLE, yoVariable.getFullNameString()));
         dragBoard.setContent(clipboardContent);
      }

      event.consume();
   }

   public void handleDragEntered(DragEvent event)
   {
      if (!event.isAccepted() && acceptDragEventForDrop(event))
         setSelectionHighlight(true);
      event.consume();
   }

   public void handleDragExited(DragEvent event)
   {
      if (acceptDragEventForDrop(event))
         setSelectionHighlight(false);
      event.consume();
   }

   public void handleDragOver(DragEvent event)
   {
      if (!event.isAccepted() && acceptDragEventForDrop(event))
         event.acceptTransferModes(TransferMode.ANY);
      event.consume();
   }

   public void handleDragDropped(DragEvent event)
   {
      // TODO
      //      if (event.isAccepted())
      //         return;

      Dragboard db = event.getDragboard();
      boolean success = false;
      List<YoComposite> yoComposites = DragAndDropTools.retrieveYoCompositesFromDragBoard(db, yoCompositeSearchManager);
      if (yoComposites != null)
      {
         // TODO
         //         for (YoComposite yoComposite : yoComposites)
         setSlider(yoComposites.get(0).getYoComponents().get(0));
         success = true;
      }
      event.setDropCompleted(success);
      event.consume();
   }

   private boolean acceptDragEventForDrop(DragEvent event)
   {
      if (event.getGestureSource() == yoVariableDropLabel)
         return false;

      Dragboard dragboard = event.getDragboard();
      return DragAndDropTools.retrieveYoCompositesFromDragBoard(dragboard, yoCompositeSearchManager) != null;
   }

   public void setSelectionHighlight(boolean isSelected)
   {
      if (isSelected)
         yoVariableDropLabel.setStyle("-fx-border-color:green; -fx-border-radius:5;");
      else
         yoVariableDropLabel.setStyle("-fx-border-color: null;");
   }

}
