package us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.graphic2D;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicLine2DDefinition;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.YoGraphicFXCreatorController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.editor.YoCompositeEditorPaneController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.editor.YoGraphic2DStyleEditorPaneController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.editor.YoGraphicNameEditorPaneController;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.Tuple2DProperty;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGraphicFX2D;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGraphicTools;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoLineFX2D;

public class YoLineFX2DEditorController implements YoGraphicFXCreatorController<YoLineFX2D>
{
   public static final double DEFAULT_STROKE_WIDTH = YoGraphicFX2D.DEFAULT_STROKE_WIDTH.get();

   @FXML
   private VBox mainPane;
   @FXML
   private YoCompositeEditorPaneController originEditorController, directionEditorController, destinationEditorController;
   @FXML
   private RadioButton directionRadioButton, destinationRadioButton;
   @FXML
   private YoGraphic2DStyleEditorPaneController styleEditorController;
   @FXML
   private YoGraphicNameEditorPaneController nameEditorController;

   private ObservableBooleanValue inputsValidityProperty;

   private YoLineFX2D yoGraphicToEdit;
   private YoGraphicLine2DDefinition definitionBeforeEdits;
   private BooleanProperty hasChangesPendingProperty = new SimpleBooleanProperty(this, "hasChangesPending", false);

   @Override
   public void initialize(SessionVisualizerToolkit toolkit, YoLineFX2D yoGraphicToEdit)
   {
      this.yoGraphicToEdit = yoGraphicToEdit;
      definitionBeforeEdits = YoGraphicTools.toYoGraphicLine2DDefinition(yoGraphicToEdit);
      yoGraphicToEdit.visibleProperty().addListener((observable, oldValue, newValue) -> definitionBeforeEdits.setVisible(newValue));

      new ToggleGroup().getToggles().addAll(directionRadioButton, destinationRadioButton);
      directionEditorController.getMainPane().disableProperty().bind(destinationRadioButton.selectedProperty());
      destinationEditorController.getMainPane().disableProperty().bind(directionRadioButton.selectedProperty());
      originEditorController.initialize(toolkit, toolkit.getYoCompositeSearchManager().getYoTuple2DCollection(), true);
      originEditorController.setCompositeName("Origin");
      directionEditorController.initialize(toolkit, toolkit.getYoCompositeSearchManager().getYoTuple2DCollection(), true);
      directionEditorController.setCompositeName("Direction");
      destinationEditorController.initialize(toolkit, toolkit.getYoCompositeSearchManager().getYoTuple2DCollection(), true);
      destinationEditorController.setCompositeName("Destination");
      styleEditorController.initialize(toolkit);

      nameEditorController.initialize(toolkit, yoGraphicToEdit);

      inputsValidityProperty = Bindings.and(originEditorController.inputsValidityProperty(), directionEditorController.inputsValidityProperty())
                                       .and(destinationEditorController.inputsValidityProperty()).and(styleEditorController.inputsValidityProperty())
                                       .and(nameEditorController.inputsValidityProperty());

      originEditorController.bindYoCompositeDoubleProperty(yoGraphicToEdit.getOrigin());
      directionEditorController.bindYoCompositeDoubleProperty(yoGraphicToEdit.getDirection());
      destinationEditorController.addInputListener((xy, frame) -> yoGraphicToEdit.getDestination().set(frame, xy));
      directionRadioButton.selectedProperty().addListener((o, oldValue, newValue) ->
      {
         if (newValue)
         {
            if (directionEditorController.inputsValidityProperty().get())
               yoGraphicToEdit.setDirection(new Tuple2DProperty(directionEditorController.frameSupplierProperty().getValue(),
                                                                      directionEditorController.compositeSupplierProperty().get()));
            yoGraphicToEdit.setDestination(null);
         }
         else
         {
            yoGraphicToEdit.setDirection(null);
            if (destinationEditorController.inputsValidityProperty().get())
               yoGraphicToEdit.setDestination(new Tuple2DProperty(destinationEditorController.frameSupplierProperty().getValue(),
                                                                        destinationEditorController.compositeSupplierProperty().get()));
         }
      });
      styleEditorController.bindYoGraphicFX2D(yoGraphicToEdit);

      nameEditorController.bindYoGraphicFXItem(yoGraphicToEdit);

      originEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
      directionEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
      destinationEditorController.compositeSupplierProperty().addListener(this::updateHasChangesPendingProperty);
      styleEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
      nameEditorController.addAnyChangeListener(this::updateHasChangesPendingProperty);

      resetFields();
   }

   private <T> void updateHasChangesPendingProperty(ObservableValue<? extends T> observable, T oldValue, T newValue)
   {
      hasChangesPendingProperty.set(!definitionBeforeEdits.equals(YoGraphicTools.toYoGraphicLine2DDefinition(yoGraphicToEdit)));
   }

   @Override
   public void resetFields()
   {
      originEditorController.setInput(definitionBeforeEdits.getOrigin());
      if (definitionBeforeEdits.getDirection() != null)
      {
         directionRadioButton.setSelected(true);
         directionEditorController.setInput(definitionBeforeEdits.getDirection());
      }
      else
      {
         destinationRadioButton.setSelected(true);
         destinationEditorController.setInput(definitionBeforeEdits.getDestination());
      }
      styleEditorController.setInput(definitionBeforeEdits);
      nameEditorController.setInput(definitionBeforeEdits.getName(), yoGraphicToEdit.getNamespace());
   }

   @Override
   public ObservableBooleanValue inputsValidityProperty()
   {
      return inputsValidityProperty;
   }

   @Override
   public void saveChanges()
   {
      definitionBeforeEdits = YoGraphicTools.toYoGraphicLine2DDefinition(yoGraphicToEdit);
      hasChangesPendingProperty.set(false);
   }

   @Override
   public ReadOnlyBooleanProperty hasChangesPendingProperty()
   {
      return hasChangesPendingProperty;
   }

   @Override
   public YoLineFX2D getYoGraphicFX()
   {
      return yoGraphicToEdit;
   }

   @Override
   public VBox getMainPane()
   {
      return mainPane;
   }
}
