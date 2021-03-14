package us.ihmc.scs2.sessionVisualizer.jfx.controllers.sliderboard;

import java.util.Arrays;
import java.util.List;

import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import us.ihmc.log.LogTools;
import us.ihmc.scs2.definition.yoSlider.YoKnobDefinition;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.YoCompositeSearchManager;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.YoManager;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.DragAndDropTools;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.YoComposite;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.YoCompositeTools;
import us.ihmc.scs2.sessionVisualizer.sliderboard.SliderboardVariable;
import us.ihmc.yoVariables.variable.YoVariable;

public class YoKnobController
{
   private static final String DEFAULT_TEXT = "Drop YoVariable here";

   @FXML
   private VBox rootPane;
   @FXML
   private JFXTextField knobMaxTextField;
   @FXML
   private JFXTextField knobMinTextField;
   @FXML
   private JFXSpinner spinner;
   @FXML
   private Label yoVariableDropLabel;

   private final SimpleObjectProperty<ContextMenu> contextMenuProperty = new SimpleObjectProperty<>(this, "knobContextMenu", null);

   private SliderboardVariable sliderVariable;

   private YoCompositeSearchManager yoCompositeSearchManager;
   private YoVariableSlider yoVariableSlider;
   private YoManager yoManager;

   public void initialize(SessionVisualizerToolkit toolkit, SliderboardVariable sliderVariable)
   {
      this.sliderVariable = sliderVariable;
      yoManager = toolkit.getYoManager();
      yoCompositeSearchManager = toolkit.getYoCompositeSearchManager();

      rootPane.setOnDragDetected(this::handleDragDetected);
      rootPane.setOnDragOver(this::handleDragOver);
      rootPane.setOnDragDropped(this::handleDragDropped);
      rootPane.setOnDragEntered(this::handleDragEntered);
      rootPane.setOnDragExited(this::handleDragExited);
      rootPane.setOnMousePressed(this::handleMousePressed);
      rootPane.setOnMouseReleased(this::handleMouseReleased);

      knobMaxTextField.setText("");
      knobMinTextField.setText("");
      knobMaxTextField.setDisable(true);
      knobMinTextField.setDisable(true);

      contextMenuProperty.addListener((ChangeListener<ContextMenu>) (observable, oldValue, newValue) ->
      {
         if (oldValue != null)
            oldValue.hide();
      });
   }

   public void setInput(YoKnobDefinition definition)
   {
      if (definition == null)
      {
         setKnob(null);
         return;
      }

      YoVariable yoVariable;
      if (definition.getVariableName() != null)
      {
         yoVariable = yoManager.getRootRegistryDatabase().searchExact(definition.getVariableName());
         if (yoVariable == null)
            LogTools.warn("Could not find variable for slider: " + definition.getVariableName());
      }
      else
      {
         yoVariable = null;
      }

      setKnob(yoVariable, definition.getMinValue(), definition.getMaxValue());
   }

   private void setKnob(YoVariable yoVariable)
   {
      setKnob(yoVariable, null, null);
   }

   private void setKnob(YoVariable yoVariable, String minValue, String maxValue)
   {
      if (yoVariableSlider != null)
      {
         yoVariableSlider.dispose();
      }

      if (yoVariable != null)
      {
         rootPane.setStyle("-fx-background-color: #c5fcee88");
         yoVariableDropLabel.setText(yoVariable.getName());

         yoVariableSlider = YoVariableSlider.newYoVariableSlider(yoVariable, () -> yoManager.getLinkedRootRegistry().push(yoVariable));
         yoVariableSlider.bindMinTextField(knobMinTextField);
         yoVariableSlider.bindMaxTextField(knobMaxTextField);
         if (sliderVariable != null)
            yoVariableSlider.bindSliderVariable(sliderVariable);
         yoVariableSlider.bindVirtualKnob(spinner);

         if (minValue != null && !knobMinTextField.isDisabled())
            knobMinTextField.setText(minValue);
         if (maxValue != null && !knobMaxTextField.isDisabled())
            knobMaxTextField.setText(maxValue);
      }
      else
      {
         rootPane.setStyle("-fx-background-color: null");
         yoVariableSlider = null;
         yoVariableDropLabel.setText(DEFAULT_TEXT);
         knobMaxTextField.setText("");
         knobMinTextField.setText("");
      }
   }

   public void close()
   {
      if (yoVariableSlider != null)
      {
         setKnob(null);
      }
   }

   private void handleMousePressed(MouseEvent event)
   {
      if (event.getButton() == MouseButton.PRIMARY)
      {
         hideContextMenu();
      }
   }

   private void handleMouseReleased(MouseEvent event)
   {
      if (yoVariableSlider == null)
         return;

      if (event.getButton() == MouseButton.SECONDARY)
      {
         if (event.isStillSincePress())
         {
            ContextMenu contextMenu = newGraphContextMenu();
            if (!contextMenu.getItems().isEmpty())
            {
               contextMenuProperty.set(contextMenu);
               contextMenu.show(rootPane, event.getScreenX(), event.getScreenY());
            }
            event.consume();
         }
      }
   }

   private void hideContextMenu()
   {
      if (contextMenuProperty.get() != null)
         contextMenuProperty.set(null);
   }

   private ContextMenu newGraphContextMenu()
   {
      if (yoVariableSlider == null)
         return null;

      ContextMenu contextMenu = new ContextMenu();
      MenuItem menuItem = new MenuItem("Remove " + yoVariableSlider.getYoVariable().getName());
      menuItem.setMnemonicParsing(false);
      menuItem.setOnAction(e -> setKnob(null));
      contextMenu.getItems().add(menuItem);
      return contextMenu;
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
         setKnob(yoComposites.get(0).getYoComponents().get(0));
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
      return true;
   }

   public void setSelectionHighlight(boolean isSelected)
   {
      if (isSelected)
         rootPane.setStyle("-fx-border-color:green; -fx-border-radius:5;");
      else
         rootPane.setStyle("-fx-border-color: null;");
   }

   public YoKnobDefinition toYoKnobDefinition()
   {
      return yoVariableSlider == null ? new YoKnobDefinition() : yoVariableSlider.toYoKnobDefinition();
   }
}
