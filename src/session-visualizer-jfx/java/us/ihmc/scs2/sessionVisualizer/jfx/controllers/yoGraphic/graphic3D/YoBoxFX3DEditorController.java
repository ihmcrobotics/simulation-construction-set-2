package us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.graphic3D;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicBox3DDefinition;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.YoGraphicFXCreatorController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.editor.YoCompositeEditorPaneController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.editor.YoGraphic3DStyleEditorPaneController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.editor.YoGraphicNameEditorPaneController;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.YoCompositeSearchManager;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.YawPitchRollProperty;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoBoxFX3D;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGraphicTools;

public class YoBoxFX3DEditorController implements YoGraphicFXCreatorController<YoBoxFX3D>
{
   @FXML
   private VBox mainPane;
   @FXML
   private YoCompositeEditorPaneController positionEditorController, orientationEditorController;
   @FXML
   private YoCompositeEditorPaneController sizeEditorController;
   @FXML
   private YoGraphic3DStyleEditorPaneController styleEditorController;
   @FXML
   private YoGraphicNameEditorPaneController nameEditorController;

   private ObservableBooleanValue inputsValidityProperty;

   private YoBoxFX3D yoGraphicToEdit;
   private YoGraphicBox3DDefinition definitionBeforeEdits;

   private BooleanProperty hasChangesPendingProperty = new SimpleBooleanProperty(this, "hasChangesPending", false);

   @Override
   public void initialize(SessionVisualizerToolkit toolkit, YoBoxFX3D yoGraphicToEdit)
   {
      this.yoGraphicToEdit = yoGraphicToEdit;
      definitionBeforeEdits = YoGraphicTools.toYoGraphicBox3DDefinition(yoGraphicToEdit);
      yoGraphicToEdit.visibleProperty().addListener((observable, oldValue, newValue) -> definitionBeforeEdits.setVisible(newValue));

      positionEditorController.initialize(toolkit, toolkit.getYoCompositeSearchManager().getYoTuple3DCollection(), true);
      positionEditorController.setCompositeName("Position");
      if (yoGraphicToEdit.getOrientation() != null && yoGraphicToEdit.getOrientation() instanceof YawPitchRollProperty)
         orientationEditorController.initialize(toolkit, toolkit.getYoCompositeSearchManager().getYoYawPitchRollCollection(), true);
      else
         orientationEditorController.initialize(toolkit, toolkit.getYoCompositeSearchManager().getYoQuaternionCollection(), true);
      orientationEditorController.setCompositeName("Orientation");

      YoCompositeSearchManager yoCompositeSearchManager = toolkit.getYoCompositeSearchManager();
      sizeEditorController.initialize(toolkit, yoCompositeSearchManager.getYoTuple3DCollection(), false);
      sizeEditorController.setCompositeName("Size");

      styleEditorController.initialize(toolkit);
      nameEditorController.initialize(toolkit, yoGraphicToEdit);

      inputsValidityProperty = Bindings.and(positionEditorController.inputsValidityProperty(), orientationEditorController.inputsValidityProperty())
                                       .and(sizeEditorController.inputsValidityProperty()).and(nameEditorController.inputsValidityProperty());

      positionEditorController.bindYoCompositeDoubleProperty(yoGraphicToEdit.getPosition());
      orientationEditorController.bindYoCompositeDoubleProperty(yoGraphicToEdit.getOrientation());
      sizeEditorController.bindYoCompositeDoubleProperty(yoGraphicToEdit.getSize());
      styleEditorController.bindYoGraphicFX3D(yoGraphicToEdit);
      nameEditorController.bindYoGraphicFXItem(yoGraphicToEdit);

      positionEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
      orientationEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
      sizeEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
      styleEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
      nameEditorController.addAnyChangeListener(this::updateHasChangesPendingProperty);

      resetFields();
   }

   private <T> void updateHasChangesPendingProperty(ObservableValue<? extends T> observable, T oldValue, T newValue)
   {
      hasChangesPendingProperty.set(!definitionBeforeEdits.equals(YoGraphicTools.toYoGraphicBox3DDefinition(yoGraphicToEdit)));
   }

   @Override
   public void resetFields()
   {
      positionEditorController.setInput(definitionBeforeEdits.getPosition());
      orientationEditorController.setInput(definitionBeforeEdits.getOrientation());
      sizeEditorController.setInput(definitionBeforeEdits.getSize());
      styleEditorController.setInput(definitionBeforeEdits);
      nameEditorController.setInput(definitionBeforeEdits.getName(), yoGraphicToEdit.getNamespace());
   }

   @Override
   public void saveChanges()
   {
      definitionBeforeEdits = YoGraphicTools.toYoGraphicBox3DDefinition(yoGraphicToEdit);
      hasChangesPendingProperty.set(false);
   }

   @Override
   public ReadOnlyBooleanProperty hasChangesPendingProperty()
   {
      return hasChangesPendingProperty;
   }

   @Override
   public ObservableBooleanValue inputsValidityProperty()
   {
      return inputsValidityProperty;
   }

   @Override
   public YoBoxFX3D getYoGraphicFX()
   {
      return yoGraphicToEdit;
   }

   @Override
   public VBox getMainPane()
   {
      return mainPane;
   }
}
