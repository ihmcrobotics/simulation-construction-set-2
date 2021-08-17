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
import us.ihmc.scs2.definition.yoGraphic.YoGraphicCapsule3DDefinition;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.YoGraphicFXCreatorController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.editor.YoCompositeEditorPaneController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.editor.YoGraphic3DStyleEditorPaneController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.editor.YoGraphicNameEditorPaneController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.editor.yoTextField.YoDoubleTextField;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.YoCompositeSearchManager;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoCapsuleFX3D;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGraphicTools;
import us.ihmc.scs2.sharedMemory.LinkedYoRegistry;

public class YoCapsuleFX3DEditorController implements YoGraphicFXCreatorController<YoCapsuleFX3D>
{
   @FXML
   private VBox mainPane;
   @FXML
   private YoCompositeEditorPaneController centerEditorController, axisEditorController;
   @FXML
   private TextField lengthTextField, radiusTextField;
   @FXML
   private YoGraphic3DStyleEditorPaneController styleEditorController;
   @FXML
   private YoGraphicNameEditorPaneController nameEditorController;

   @FXML
   private ImageView lengthValidImageView, radiusValidImageView;

   private YoDoubleTextField yoLengthTextField;
   private YoDoubleTextField yoRadiusTextField;
   private ObservableBooleanValue inputsValidityProperty;

   private YoCapsuleFX3D yoGraphicToEdit;
   private YoGraphicCapsule3DDefinition definitionBeforeEdits;
   private YoCompositeSearchManager yoCompositeSearchManager;

   private BooleanProperty hasChangesPendingProperty = new SimpleBooleanProperty(this, "hasChangesPending", false);

   @Override
   public void initialize(SessionVisualizerToolkit toolkit, YoCapsuleFX3D yoGraphicToEdit)
   {
      this.yoGraphicToEdit = yoGraphicToEdit;
      definitionBeforeEdits = YoGraphicTools.toYoGraphicCapsule3DDefinition(yoGraphicToEdit);
      yoGraphicToEdit.visibleProperty().addListener((observable, oldValue, newValue) -> definitionBeforeEdits.setVisible(newValue));
      yoCompositeSearchManager = toolkit.getYoCompositeSearchManager();
      LinkedYoRegistry linkedRootRegistry = toolkit.getYoManager().getLinkedRootRegistry();

      centerEditorController.initialize(toolkit, yoCompositeSearchManager.getYoTuple3DCollection(), true);
      centerEditorController.setCompositeName("Center");
      axisEditorController.initialize(toolkit, yoCompositeSearchManager.getYoTuple3DCollection(), true);
      axisEditorController.setCompositeName("Axis");
      yoLengthTextField = new YoDoubleTextField(lengthTextField, yoCompositeSearchManager, linkedRootRegistry, lengthValidImageView);
      yoRadiusTextField = new YoDoubleTextField(radiusTextField, yoCompositeSearchManager, linkedRootRegistry, radiusValidImageView);

      yoLengthTextField.setupAutoCompletion();
      yoRadiusTextField.setupAutoCompletion();

      styleEditorController.initialize(toolkit);
      nameEditorController.initialize(toolkit, yoGraphicToEdit);

      inputsValidityProperty = Bindings.and(centerEditorController.inputsValidityProperty(), axisEditorController.inputsValidityProperty())
                                       .and(yoLengthTextField.getValidityProperty()).and(yoRadiusTextField.getValidityProperty())
                                       .and(styleEditorController.inputsValidityProperty()).and(nameEditorController.inputsValidityProperty());

      centerEditorController.bindYoCompositeDoubleProperty(yoGraphicToEdit.getCenter());
      axisEditorController.bindYoCompositeDoubleProperty(yoGraphicToEdit.getAxis());
      yoLengthTextField.supplierProperty().addListener((o, oldValue, newValue) -> yoGraphicToEdit.setLength(newValue));
      yoRadiusTextField.supplierProperty().addListener((o, oldValue, newValue) -> yoGraphicToEdit.setRadius(newValue));
      styleEditorController.bindYoGraphicFX3D(yoGraphicToEdit);
      nameEditorController.bindYoGraphicFXItem(yoGraphicToEdit);

      centerEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
      axisEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
      lengthTextField.textProperty().addListener(this::updateHasChangesPendingProperty);
      radiusTextField.textProperty().addListener(this::updateHasChangesPendingProperty);
      styleEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
      nameEditorController.addAnyChangeListener(this::updateHasChangesPendingProperty);

      resetFields();
   }

   private <T> void updateHasChangesPendingProperty(ObservableValue<? extends T> observable, T oldValue, T newValue)
   {
      hasChangesPendingProperty.set(!definitionBeforeEdits.equals(YoGraphicTools.toYoGraphicCapsule3DDefinition(yoGraphicToEdit)));
   }

   @Override
   public void resetFields()
   {
      centerEditorController.setInput(definitionBeforeEdits.getCenter());
      axisEditorController.setInput(definitionBeforeEdits.getAxis());
      lengthTextField.setText(definitionBeforeEdits.getLength());
      radiusTextField.setText(definitionBeforeEdits.getRadius());
      styleEditorController.setInput(definitionBeforeEdits);
      nameEditorController.setInput(definitionBeforeEdits.getName(), yoGraphicToEdit.getNamespace());
   }

   @Override
   public void saveChanges()
   {
      definitionBeforeEdits = YoGraphicTools.toYoGraphicCapsule3DDefinition(yoGraphicToEdit);
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
   public YoCapsuleFX3D getYoGraphicFX()
   {
      return yoGraphicToEdit;
   }

   @Override
   public VBox getMainPane()
   {
      return mainPane;
   }
}
