package us.ihmc.scs2.sessionVisualizer.jfx.controllers.sliderboard;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.jfoenix.controls.JFXToggleButton;

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
import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.log.LogTools;
import us.ihmc.messager.MessagerAPIFactory.Topic;
import us.ihmc.scs2.definition.yoSlider.YoButtonDefinition;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.YoCompositeSearchManager;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.YoManager;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.DragAndDropTools;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.YoComposite;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.YoCompositeTools;
import us.ihmc.scs2.sessionVisualizer.sliderboard.SliderboardVariable;
import us.ihmc.yoVariables.variable.YoBoolean;
import us.ihmc.yoVariables.variable.YoVariable;

public class YoButtonController
{
   private static final String DEFAULT_TEXT = "Drop YoBoolean here";

   @FXML
   private VBox rootPane;
   @FXML
   private JFXToggleButton button;
   @FXML
   private Label yoVariableDropLabel;

   private final SimpleObjectProperty<ContextMenu> contextMenuProperty = new SimpleObjectProperty<>(this, "buttonContextMenu", null);

   private SliderboardVariable sliderVariable;

   private JavaFXMessager messager;
   private Topic<List<String>> yoCompositeSelectedTopic;
   private AtomicReference<List<String>> yoCompositeSelected;

   private YoCompositeSearchManager yoCompositeSearchManager;
   private YoBooleanSlider yoBooleanSlider;
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

      button.setDisable(true);

      contextMenuProperty.addListener((ChangeListener<ContextMenu>) (observable, oldValue, newValue) ->
      {
         if (oldValue != null)
            oldValue.hide();
      });

      messager = toolkit.getMessager();
      yoCompositeSelectedTopic = toolkit.getTopics().getYoCompositeSelected();
      yoCompositeSelected = messager.createInput(yoCompositeSelectedTopic);
   }

   public void setInput(YoButtonDefinition definition)
   {
      if (definition == null)
      {
         setButton(null);
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

      setButton(yoVariable);
   }

   private void setButton(YoVariable yoVariable)
   {
      if (yoBooleanSlider != null)
      {
         yoBooleanSlider.dispose();
         yoBooleanSlider.getYoBooleanProperty().unbind();
      }

      if (yoVariable != null && yoVariable instanceof YoBoolean)
      {
         rootPane.setStyle("-fx-background-color: #c5fcee88");
         button.setDisable(false);
         yoVariableDropLabel.setText(yoVariable.getName());

         yoBooleanSlider = (YoBooleanSlider) YoVariableSlider.newYoVariableSlider(yoVariable, () -> yoManager.getLinkedRootRegistry().push(yoVariable));
         if (sliderVariable != null)
            yoBooleanSlider.bindSliderVariable(sliderVariable);
         yoBooleanSlider.getYoBooleanProperty().bindBooleanProperty(button.selectedProperty(), () -> yoManager.getLinkedRootRegistry().push(yoVariable));
      }
      else
      {
         rootPane.setStyle("-fx-background-color: null");
         button.setDisable(true);
         yoBooleanSlider = null;
         yoVariableDropLabel.setText(DEFAULT_TEXT);
      }
   }

   public void close()
   {
      if (yoBooleanSlider != null)
      {
         setButton(null);
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
      if (event.getButton() == MouseButton.PRIMARY)
      {
         if (yoBooleanSlider != null && event.isStillSincePress())
         {
            messager.submitMessage(yoCompositeSelectedTopic, Arrays.asList(YoCompositeTools.YO_VARIABLE, yoBooleanSlider.getYoVariable().getFullNameString()));
         }
      }
      else if (event.getButton() == MouseButton.SECONDARY)
      {
         if (yoBooleanSlider != null && event.isStillSincePress())
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
      else if (event.getButton() == MouseButton.MIDDLE)
      {
         if (yoCompositeSelected.get() != null)
         {
            String type = yoCompositeSelected.get().get(0);
            if (type.equals(YoCompositeTools.YO_VARIABLE))
            {
               String fullname = yoCompositeSelected.get().get(1);
               YoComposite yoComposite = yoCompositeSearchManager.getYoComposite(type, fullname);

               if (yoComposite != null && yoComposite.getYoComponents().get(0) instanceof YoBoolean)
               {
                  setButton(yoComposite.getYoComponents().get(0));
                  messager.submitMessage(yoCompositeSelectedTopic, null);
               }
            }
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
      if (yoBooleanSlider == null)
         return null;

      ContextMenu contextMenu = new ContextMenu();
      MenuItem menuItem = new MenuItem("Remove " + yoBooleanSlider.getYoVariable().getName());
      menuItem.setMnemonicParsing(false);
      menuItem.setOnAction(e -> setButton(null));
      contextMenu.getItems().add(menuItem);
      return contextMenu;
   }

   public void handleDragDetected(MouseEvent event)
   {
      if (event == null || yoBooleanSlider == null)
         return;

      if (!event.isPrimaryButtonDown())
         return;

      PickResult pickResult = event.getPickResult();

      if (pickResult == null)
         return;

      Node intersectedNode = pickResult.getIntersectedNode();

      if (intersectedNode == null)
         return;

      YoVariable yoVariable = yoBooleanSlider.getYoVariable();

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
         setButton(yoComposites.get(0).getYoComponents().get(0));
         success = true;
      }
      event.setDropCompleted(success);
      event.consume();
   }

   private boolean acceptDragEventForDrop(DragEvent event)
   {
      if (event.getGestureSource() == rootPane)
         return false;

      Dragboard dragboard = event.getDragboard();
      List<YoComposite> result = DragAndDropTools.retrieveYoCompositesFromDragBoard(dragboard, yoCompositeSearchManager);
      if (result == null || result.isEmpty())
         return false;
      if (result.get(0).getYoComponents().isEmpty())
         return false;
      return result.get(0).getYoComponents().get(0) instanceof YoBoolean;
   }

   public void setSelectionHighlight(boolean isSelected)
   {
      if (isSelected)
         rootPane.setStyle("-fx-border-color:green; -fx-border-radius:5;");
      else
         rootPane.setStyle("-fx-border-color: null;");
   }

   public YoButtonDefinition toYoButtonDefinition()
   {
      YoButtonDefinition definition = new YoButtonDefinition();
      if (yoBooleanSlider != null)
         definition.setVariableName(yoBooleanSlider.getYoVariable().getFullNameString());
      return definition;
   }
}
