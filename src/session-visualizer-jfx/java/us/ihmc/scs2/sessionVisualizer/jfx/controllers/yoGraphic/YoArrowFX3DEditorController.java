package us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic;

import com.jfoenix.controls.JFXCheckBox;

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
import us.ihmc.scs2.definition.yoGraphic.YoGraphicArrow3DDefinition;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.yoTextFields.YoDoubleTextField;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.YoCompositeSearchManager;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoArrowFX3D;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGraphicTools;

public class YoArrowFX3DEditorController implements YoGraphicFXCreatorController<YoArrowFX3D>
{
   @FXML
   private VBox mainPane;
   @FXML
   private YoCompositeEditorPaneController originEditorController, directionEditorController;
   @FXML
   private TextField bodyLengthTextField, headLengthTextField;
   @FXML
   private TextField bodyRadiusTextField, headRadiusTextField;
   @FXML
   private JFXCheckBox scaleLengthCheckBox, scaleRadiusCheckBox;
   @FXML
   private YoGraphic3DStyleEditorPaneController styleEditorController;
   @FXML
   private YoGraphicNameEditorPaneController nameEditorController;

   @FXML
   private ImageView bodyLengthValidImageView, headLengthValidImageView;
   @FXML
   private ImageView bodyRadiusValidImageView, headRadiusValidImageView;

   private YoDoubleTextField yoBodyLengthTextField;
   private YoDoubleTextField yoBodyRadiusTextField;
   private YoDoubleTextField yoHeadLengthTextField;
   private YoDoubleTextField yoHeadRadiusTextField;
   private ObservableBooleanValue inputsValidityProperty;

   private YoArrowFX3D yoGraphicToEdit;
   private YoGraphicArrow3DDefinition definitionBeforeEdits;
   private YoCompositeSearchManager yoCompositeSearchManager;

   private BooleanProperty hasChangesPendingProperty = new SimpleBooleanProperty(this, "hasChangesPending", false);

   @Override
   public void initialize(SessionVisualizerToolkit toolkit, YoArrowFX3D yoGraphicToEdit)
   {
      this.yoGraphicToEdit = yoGraphicToEdit;
      definitionBeforeEdits = YoGraphicTools.toYoGraphicArrow3DDefinition(yoGraphicToEdit);
      yoGraphicToEdit.visibleProperty().addListener((observable, oldValue, newValue) -> definitionBeforeEdits.setVisible(newValue));
      yoCompositeSearchManager = toolkit.getYoCompositeSearchManager();

      originEditorController.initialize(toolkit, toolkit.getYoCompositeSearchManager().getYoTuple3DCollection(), true);
      originEditorController.setCompositeName("Origin");
      directionEditorController.initialize(toolkit, toolkit.getYoCompositeSearchManager().getYoTuple3DCollection(), true);
      directionEditorController.setCompositeName("Direction");
      yoBodyLengthTextField = new YoDoubleTextField(bodyLengthTextField, yoCompositeSearchManager, bodyLengthValidImageView);
      yoHeadLengthTextField = new YoDoubleTextField(headLengthTextField, yoCompositeSearchManager, headLengthValidImageView);
      yoBodyRadiusTextField = new YoDoubleTextField(bodyRadiusTextField, yoCompositeSearchManager, bodyRadiusValidImageView);
      yoHeadRadiusTextField = new YoDoubleTextField(headRadiusTextField, yoCompositeSearchManager, headRadiusValidImageView);

      yoBodyLengthTextField.setupAutoCompletion();
      yoBodyRadiusTextField.setupAutoCompletion();
      yoHeadLengthTextField.setupAutoCompletion();
      yoHeadRadiusTextField.setupAutoCompletion();

      styleEditorController.initialize(toolkit);
      nameEditorController.initialize(toolkit, yoGraphicToEdit);

      inputsValidityProperty = Bindings.and(originEditorController.inputsValidityProperty(), directionEditorController.inputsValidityProperty())
                                       .and(yoBodyLengthTextField.getValidityProperty()).and(yoBodyRadiusTextField.getValidityProperty())
                                       .and(yoHeadLengthTextField.getValidityProperty()).and(yoHeadRadiusTextField.getValidityProperty())
                                       .and(styleEditorController.inputsValidityProperty()).and(nameEditorController.inputsValidityProperty());

      originEditorController.bindYoCompositeDoubleProperty(yoGraphicToEdit.getOrigin());
      directionEditorController.bindYoCompositeDoubleProperty(yoGraphicToEdit.getDirection());
      scaleLengthCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> yoGraphicToEdit.setScaleLength(newValue));
      scaleRadiusCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> yoGraphicToEdit.setScaleRadius(newValue));
      yoBodyLengthTextField.supplierProperty().addListener((o, oldValue, newValue) -> yoGraphicToEdit.setBodyLength(newValue));
      yoHeadLengthTextField.supplierProperty().addListener((o, oldValue, newValue) -> yoGraphicToEdit.setHeadLength(newValue));
      yoBodyRadiusTextField.supplierProperty().addListener((o, oldValue, newValue) -> yoGraphicToEdit.setBodyRadius(newValue));
      yoHeadRadiusTextField.supplierProperty().addListener((o, oldValue, newValue) -> yoGraphicToEdit.setHeadRadius(newValue));
      styleEditorController.bindYoGraphicFX3D(yoGraphicToEdit);
      nameEditorController.bindYoGraphicFXItem(yoGraphicToEdit);

      originEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
      directionEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
      bodyLengthTextField.textProperty().addListener(this::updateHasChangesPendingProperty);
      headLengthTextField.textProperty().addListener(this::updateHasChangesPendingProperty);
      scaleLengthCheckBox.selectedProperty().addListener(this::updateHasChangesPendingProperty);
      bodyRadiusTextField.textProperty().addListener(this::updateHasChangesPendingProperty);
      headRadiusTextField.textProperty().addListener(this::updateHasChangesPendingProperty);
      scaleRadiusCheckBox.selectedProperty().addListener(this::updateHasChangesPendingProperty);
      styleEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
      nameEditorController.addAnyChangeListener(this::updateHasChangesPendingProperty);

      resetFields();
   }

   private <T> void updateHasChangesPendingProperty(ObservableValue<? extends T> observable, T oldValue, T newValue)
   {
      hasChangesPendingProperty.set(!definitionBeforeEdits.equals(YoGraphicTools.toYoGraphicArrow3DDefinition(yoGraphicToEdit)));
   }

   @Override
   public void resetFields()
   {
      originEditorController.setInput(definitionBeforeEdits.getOrigin());
      directionEditorController.setInput(definitionBeforeEdits.getDirection());
      bodyLengthTextField.setText(definitionBeforeEdits.getBodyLength());
      headLengthTextField.setText(definitionBeforeEdits.getHeadLength());
      scaleLengthCheckBox.setSelected(definitionBeforeEdits.isScaleLength());
      bodyRadiusTextField.setText(definitionBeforeEdits.getBodyRadius());
      headRadiusTextField.setText(definitionBeforeEdits.getHeadRadius());
      scaleRadiusCheckBox.setSelected(definitionBeforeEdits.isScaleRadius());
      styleEditorController.setInput(definitionBeforeEdits);
      nameEditorController.setInput(definitionBeforeEdits.getName(), yoGraphicToEdit.getNamespace());
   }

   @Override
   public void saveChanges()
   {
      definitionBeforeEdits = YoGraphicTools.toYoGraphicArrow3DDefinition(yoGraphicToEdit);
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
   public YoArrowFX3D getYoGraphicFX()
   {
      return yoGraphicToEdit;
   }

   @Override
   public VBox getMainPane()
   {
      return mainPane;
   }
}
