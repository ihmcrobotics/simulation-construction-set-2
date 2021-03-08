package us.ihmc.scs2.sessionVisualizer.jfx.controllers.sliderboard;

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
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.YoCompositeSearchManager;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.YoManager;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.DragAndDropTools;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.YoComposite;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.YoCompositeTools;
import us.ihmc.scs2.sessionVisualizer.sliderboard.SliderVariable;
import us.ihmc.yoVariables.variable.YoDouble;
import us.ihmc.yoVariables.variable.YoVariable;

public class YoSliderController
{
   @FXML
   private VBox rootPane;
   @FXML
   private JFXTextField sliderMaxTextField;
   @FXML
   private JFXTextField sliderMinTextField;
   @FXML
   private JFXSlider slider;
   @FXML
   private Label yoVariableDropLabel;

   private SliderVariable sliderVariable;

   private YoCompositeSearchManager yoCompositeSearchManager;
   private YoVariableSlider yoVariableSlider;
   private YoManager yoManager;

   public void initialize(SessionVisualizerToolkit toolkit, SliderVariable sliderVariable)
   {
      this.sliderVariable = sliderVariable;
      yoManager = toolkit.getYoManager();
      yoCompositeSearchManager = toolkit.getYoCompositeSearchManager();

      rootPane.setOnDragDetected(this::handleDragDetected);
      rootPane.setOnDragOver(this::handleDragOver);
      rootPane.setOnDragDropped(this::handleDragDropped);
      rootPane.setOnDragEntered(this::handleDragEntered);
      rootPane.setOnDragExited(this::handleDragExited);

      sliderMaxTextField.setText("1.0");
      sliderMinTextField.setText("0.0");
   }

   private void setSlider(YoVariable yoVariable)
   {
      if (yoVariableSlider != null)
      {
         yoVariableSlider.dispose();
      }

      yoVariableDropLabel.setText(yoVariable.getName());

      yoVariableSlider = YoVariableSlider.newYoVariableSlider(yoVariable, () -> yoManager.getLinkedRootRegistry().push(yoVariable));
      yoVariableSlider.bindMinTextField(sliderMinTextField);
      yoVariableSlider.bindMaxTextField(sliderMaxTextField);
      yoVariableSlider.bindSliderVariable(sliderVariable);
      yoVariableSlider.bindVirtualSlider(slider);
   }

   public void updateSlider()
   {
   }

   public void handleDragDetected(MouseEvent event)
   {
      if (event == null || yoVariableSlider == null)
         return;

      if (!event.isPrimaryButtonDown())
         return;

      PickResult pickResult = event.getPickResult();

      if (pickResult == null)
         return;

      Node intersectedNode = pickResult.getIntersectedNode();

      if (intersectedNode == null)
         return;

      YoVariable yoVariable = yoVariableSlider.getYoVariable();

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
      List<YoComposite> result = DragAndDropTools.retrieveYoCompositesFromDragBoard(dragboard, yoCompositeSearchManager);
      if (result == null || result.isEmpty())
         return false;
      if (result.get(0).getYoComponents().isEmpty())
         return false;
      if (!(result.get(0).getYoComponents().get(0) instanceof YoDouble))
         return false; // TODO Keeping only YoDoubles
      return true;
   }

   public void setSelectionHighlight(boolean isSelected)
   {
      if (isSelected)
         rootPane.setStyle("-fx-border-color:green; -fx-border-radius:5;");
      else
         rootPane.setStyle("-fx-border-color: null;");
   }

}
