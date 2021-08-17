package us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.graphic3D;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicSTPBox3DDefinition;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.YoGraphicFXCreatorController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.editor.YoCompositeEditorPaneController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.editor.YoGraphic3DStyleEditorPaneController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.editor.YoGraphicNameEditorPaneController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.editor.yoTextField.YoDoubleTextField;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.YoCompositeSearchManager;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.YawPitchRollProperty;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGraphicTools;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoSTPBoxFX3D;
import us.ihmc.scs2.sharedMemory.LinkedYoRegistry;

public class YoSTPBoxFX3DEditorController implements YoGraphicFXCreatorController<YoSTPBoxFX3D>
{
   @FXML
   private VBox mainPane;
   @FXML
   private YoCompositeEditorPaneController positionEditorController, orientationEditorController;
   @FXML
   private YoCompositeEditorPaneController sizeEditorController;
   @FXML
   private TextField minimumMarginTextField, maximumMarginTextField;
   @FXML
   private YoGraphic3DStyleEditorPaneController styleEditorController;
   @FXML
   private YoGraphicNameEditorPaneController nameEditorController;
   @FXML
   private ImageView minimumMarginValidImageView, maximumMarginValidImageView;

   private YoDoubleTextField yoMinimumMarginTextField;
   private YoDoubleTextField yoMaximumMarginTextField;
   private ObservableBooleanValue inputsValidityProperty;

   private YoSTPBoxFX3D yoGraphicToEdit;
   private YoGraphicSTPBox3DDefinition definitionBeforeEdits;

   private BooleanProperty hasChangesPendingProperty = new SimpleBooleanProperty(this, "hasChangesPending", false);

   @Override
   public void initialize(SessionVisualizerToolkit toolkit, YoSTPBoxFX3D yoGraphicToEdit)
   {
      this.yoGraphicToEdit = yoGraphicToEdit;
      definitionBeforeEdits = YoGraphicTools.toYoGraphicSTPBox3DDefinition(yoGraphicToEdit);
      yoGraphicToEdit.visibleProperty().addListener((observable, oldValue, newValue) -> definitionBeforeEdits.setVisible(newValue));
      YoCompositeSearchManager yoCompositeSearchManager = toolkit.getYoCompositeSearchManager();

      positionEditorController.initialize(toolkit, yoCompositeSearchManager.getYoTuple3DCollection(), true);
      positionEditorController.setCompositeName("Position");
      if (yoGraphicToEdit.getOrientation() != null && yoGraphicToEdit.getOrientation() instanceof YawPitchRollProperty)
         orientationEditorController.initialize(toolkit, yoCompositeSearchManager.getYoYawPitchRollCollection(), true);
      else
         orientationEditorController.initialize(toolkit, yoCompositeSearchManager.getYoQuaternionCollection(), true);
      orientationEditorController.setCompositeName("Orientation");

      sizeEditorController.initialize(toolkit, yoCompositeSearchManager.getYoTuple3DCollection(), false);
      sizeEditorController.setCompositeName("Size");

      LinkedYoRegistry linkedRootRegistry = toolkit.getYoManager().getLinkedRootRegistry();
      yoMinimumMarginTextField = new YoDoubleTextField(minimumMarginTextField, yoCompositeSearchManager, linkedRootRegistry, minimumMarginValidImageView);
      yoMaximumMarginTextField = new YoDoubleTextField(maximumMarginTextField, yoCompositeSearchManager, linkedRootRegistry, maximumMarginValidImageView);

      yoMinimumMarginTextField.setupAutoCompletion();
      yoMaximumMarginTextField.setupAutoCompletion();

      styleEditorController.initialize(toolkit);
      nameEditorController.initialize(toolkit, yoGraphicToEdit);

      inputsValidityProperty = Bindings.and(positionEditorController.inputsValidityProperty(), orientationEditorController.inputsValidityProperty())
                                       .and(sizeEditorController.inputsValidityProperty()).and(yoMinimumMarginTextField.getValidityProperty())
                                       .and(yoMaximumMarginTextField.getValidityProperty()).and(nameEditorController.inputsValidityProperty());

      positionEditorController.bindYoCompositeDoubleProperty(yoGraphicToEdit.getPosition());
      orientationEditorController.bindYoCompositeDoubleProperty(yoGraphicToEdit.getOrientation());
      sizeEditorController.bindYoCompositeDoubleProperty(yoGraphicToEdit.getSize());
      yoMinimumMarginTextField.supplierProperty().addListener((o, oldValue, newValue) -> yoGraphicToEdit.setMinimumMargin(newValue));
      yoMaximumMarginTextField.supplierProperty().addListener((o, oldValue, newValue) -> yoGraphicToEdit.setMaximumMargin(newValue));
      styleEditorController.bindYoGraphicFX3D(yoGraphicToEdit);
      nameEditorController.bindYoGraphicFXItem(yoGraphicToEdit);

      positionEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
      orientationEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
      sizeEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
      minimumMarginTextField.textProperty().addListener(this::updateHasChangesPendingProperty);
      maximumMarginTextField.textProperty().addListener(this::updateHasChangesPendingProperty);
      styleEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
      nameEditorController.addAnyChangeListener(this::updateHasChangesPendingProperty);

      resetFields();
   }

   private <T> void updateHasChangesPendingProperty(ObservableValue<? extends T> observable, T oldValue, T newValue)
   {
      hasChangesPendingProperty.set(!definitionBeforeEdits.equals(YoGraphicTools.toYoGraphicSTPBox3DDefinition(yoGraphicToEdit)));
   }

   @Override
   public void resetFields()
   {
      positionEditorController.setInput(definitionBeforeEdits.getPosition());
      orientationEditorController.setInput(definitionBeforeEdits.getOrientation());
      sizeEditorController.setInput(definitionBeforeEdits.getSize());
      minimumMarginTextField.setText(definitionBeforeEdits.getMinimumMargin());
      maximumMarginTextField.setText(definitionBeforeEdits.getMaximumMargin());
      styleEditorController.setInput(definitionBeforeEdits);
      nameEditorController.setInput(definitionBeforeEdits.getName(), yoGraphicToEdit.getNamespace());
   }

   @Override
   public void saveChanges()
   {
      definitionBeforeEdits = YoGraphicTools.toYoGraphicSTPBox3DDefinition(yoGraphicToEdit);
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
   public YoSTPBoxFX3D getYoGraphicFX()
   {
      return yoGraphicToEdit;
   }

   @Override
   public VBox getMainPane()
   {
      return mainPane;
   }
}
