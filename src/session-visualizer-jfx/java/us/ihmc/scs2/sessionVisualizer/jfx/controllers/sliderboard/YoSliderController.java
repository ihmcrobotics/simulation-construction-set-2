package us.ihmc.scs2.sessionVisualizer.jfx.controllers.sliderboard;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXTextField;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.input.TransferMode;
import javafx.scene.text.Text;
import javafx.util.converter.DoubleStringConverter;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.YoCompositeSearchManager;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.DragAndDropTools;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.IntegerConverter;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.PositiveIntegerValueFilter;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.YoComposite;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.YoCompositeTools;
import us.ihmc.scs2.sessionVisualizer.sliderboard.BCF2000SliderboardController;
import us.ihmc.scs2.sessionVisualizer.sliderboard.BCF2000SliderboardController.Slider;
import us.ihmc.scs2.sessionVisualizer.sliderboard.SliderboardControlVariable;
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

   private TextFormatter<Double> minTextFormatter;
   private TextFormatter<Double> maxTextFormatter;
   private SliderboardControlVariable controlVariable = new SliderboardControlVariable();

   private YoCompositeSearchManager yoCompositeSearchManager;
   private BCF2000SliderboardController sliderboard;
   private YoVariable yoVariable;

   public void initialize(SessionVisualizerToolkit toolkit, BCF2000SliderboardController sliderboard)
   {
      this.sliderboard = sliderboard;
      yoCompositeSearchManager = toolkit.getYoCompositeSearchManager();

      yoVariableDropLabel.setOnDragDetected(this::handleDragDetected);
      yoVariableDropLabel.setOnDragOver(this::handleDragOver);
      yoVariableDropLabel.setOnDragDropped(this::handleDragDropped);
      yoVariableDropLabel.setOnDragEntered(this::handleDragEntered);
      yoVariableDropLabel.setOnDragExited(this::handleDragExited);

      sliderMaxTextField.setText("1.0");
      sliderMinTextField.setText("0.0");

      minTextFormatter = new TextFormatter<>(new DoubleStringConverter());
      maxTextFormatter = new TextFormatter<>(new DoubleStringConverter());

      sliderMinTextField.setTextFormatter(minTextFormatter);
      sliderMaxTextField.setTextFormatter(maxTextFormatter);

      controlVariable.minProperty().bind(minTextFormatter.valueProperty());
      controlVariable.maxProperty().bind(maxTextFormatter.valueProperty());
   }

   private void setSlider(YoVariable yoVariable)
   {
      this.yoVariable = yoVariable;
      yoVariableDropLabel.setText(yoVariable.getName());


      MutableBoolean updating = new MutableBoolean(false);

      yoVariable.addListener(v ->
      {
         if (updating.isTrue())
            return;
         updating.setTrue();
         controlVariable.setValue(yoVariable.getValueAsDouble());
         updating.setFalse();
      });

      controlVariable.valueProperty().addListener((o, oldValue, newValue) ->
      {
         if (updating.isTrue())
            return;
         updating.setTrue();
         yoVariable.setValueFromDouble(newValue.doubleValue());
         updating.setFalse();
      });

      sliderboard.setSlider(Slider.SLIDER_1, controlVariable);
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
