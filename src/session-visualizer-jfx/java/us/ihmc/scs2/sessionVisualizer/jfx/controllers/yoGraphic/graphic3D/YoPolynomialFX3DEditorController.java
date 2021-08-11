package us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.graphic3D;

import com.jfoenix.controls.JFXTextField;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicPolynomial3DDefinition;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.YoGraphicFXControllerTools;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.YoGraphicFXCreatorController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.editor.YoCompositeListEditorPaneController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.editor.YoGraphic3DStyleEditorPaneController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.editor.YoGraphicNameEditorPaneController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.editor.yoTextField.YoDoubleTextField;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.YoCompositeSearchManager;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGraphicTools;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoPolynomialFX3D;

public class YoPolynomialFX3DEditorController implements YoGraphicFXCreatorController<YoPolynomialFX3D>
{
   @FXML
   private VBox mainPane;
   @FXML
   private YoCompositeListEditorPaneController coefficientsXListEditorController, coefficientsYListEditorController, coefficientsZListEditorController;
   @FXML
   private JFXTextField startTimeTextField, endTimeTextField;
   @FXML
   private JFXTextField sizeTextField;
   @FXML
   private ImageView startTimeValidImageView, endTimeValidImageView;
   @FXML
   private ImageView sizeValidImageView;
   @FXML
   private YoGraphic3DStyleEditorPaneController styleEditorController;
   @FXML
   private YoGraphicNameEditorPaneController nameEditorController;

   private ObservableBooleanValue inputsValidityProperty;

   private YoDoubleTextField yoStartTimeTextField, yoEndTimeTextField;
   private YoDoubleTextField yoSizeTextField;

   private YoPolynomialFX3D yoGraphicToEdit;
   private YoGraphicPolynomial3DDefinition definitionBeforeEdits;

   private BooleanProperty hasChangesPendingProperty = new SimpleBooleanProperty(this, "hasChangesPending", false);

   @Override
   public void initialize(SessionVisualizerToolkit toolkit, YoPolynomialFX3D yoGraphicToEdit)
   {
      this.yoGraphicToEdit = yoGraphicToEdit;
      definitionBeforeEdits = YoGraphicTools.toYoGraphicPolynomial3DDefinition(yoGraphicToEdit);
      yoGraphicToEdit.visibleProperty().addListener((observable, oldValue, newValue) -> definitionBeforeEdits.setVisible(newValue));

      YoCompositeSearchManager yoCompositeSearchManager = toolkit.getYoCompositeSearchManager();
      coefficientsXListEditorController.initialize(toolkit, yoCompositeSearchManager.getYoDoubleCollection(), false);
      coefficientsXListEditorController.setCompositeName("Coefficient X", "Coefficients X");
      coefficientsYListEditorController.initialize(toolkit, yoCompositeSearchManager.getYoDoubleCollection(), false);
      coefficientsYListEditorController.setCompositeName("Coefficient Y", "Coefficients Y ");
      coefficientsZListEditorController.initialize(toolkit, yoCompositeSearchManager.getYoDoubleCollection(), false);
      coefficientsZListEditorController.setCompositeName("Coefficient Z", "Coefficients Z");
      yoStartTimeTextField = new YoDoubleTextField(startTimeTextField, yoCompositeSearchManager, startTimeValidImageView);
      yoEndTimeTextField = new YoDoubleTextField(endTimeTextField, yoCompositeSearchManager, endTimeValidImageView);
      yoSizeTextField = new YoDoubleTextField(sizeTextField, yoCompositeSearchManager, sizeValidImageView);

      styleEditorController.initialize(toolkit);
      nameEditorController.initialize(toolkit, yoGraphicToEdit);
      yoStartTimeTextField.setupAutoCompletion();
      yoEndTimeTextField.setupAutoCompletion();
      yoSizeTextField.setupAutoCompletion();

      inputsValidityProperty = Bindings.and(coefficientsXListEditorController.inputsValidityProperty(),
                                            coefficientsYListEditorController.inputsValidityProperty())
                                       .and(coefficientsZListEditorController.inputsValidityProperty()).and(yoStartTimeTextField.getValidityProperty())
                                       .and(yoEndTimeTextField.getValidityProperty()).and(yoSizeTextField.getValidityProperty())
                                       .and(nameEditorController.inputsValidityProperty());

      coefficientsXListEditorController.numberOfCompositesProperty().addListener((o, oldValue, newValue) -> yoGraphicToEdit.setNumberOfCoefficientsX(newValue));
      YoGraphicFXControllerTools.toSingletonDoubleSupplierListProperty(coefficientsXListEditorController.compositeListProperty())
                                .addListener((o, oldValue, newValue) -> yoGraphicToEdit.setCoefficientsX(newValue));
      coefficientsYListEditorController.numberOfCompositesProperty().addListener((o, oldValue, newValue) -> yoGraphicToEdit.setNumberOfCoefficientsY(newValue));
      YoGraphicFXControllerTools.toSingletonDoubleSupplierListProperty(coefficientsYListEditorController.compositeListProperty())
                                .addListener((o, oldValue, newValue) -> yoGraphicToEdit.setCoefficientsY(newValue));
      coefficientsZListEditorController.numberOfCompositesProperty().addListener((o, oldValue, newValue) -> yoGraphicToEdit.setNumberOfCoefficientsZ(newValue));
      YoGraphicFXControllerTools.toSingletonDoubleSupplierListProperty(coefficientsZListEditorController.compositeListProperty())
                                .addListener((o, oldValue, newValue) -> yoGraphicToEdit.setCoefficientsZ(newValue));
      yoStartTimeTextField.supplierProperty().addListener((o, oldValue, newValue) -> yoGraphicToEdit.setStartTime(newValue));
      yoEndTimeTextField.supplierProperty().addListener((o, oldValue, newValue) -> yoGraphicToEdit.setEndTime(newValue));
      yoSizeTextField.supplierProperty().addListener((o, oldValue, newValue) -> yoGraphicToEdit.setSize(newValue));
      styleEditorController.bindYoGraphicFX3D(yoGraphicToEdit);

      nameEditorController.bindYoGraphicFXItem(yoGraphicToEdit);

      coefficientsXListEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
      coefficientsYListEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
      coefficientsZListEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
      yoStartTimeTextField.supplierProperty().addListener(this::updateHasChangesPendingProperty);
      yoEndTimeTextField.supplierProperty().addListener(this::updateHasChangesPendingProperty);
      sizeTextField.textProperty().addListener(this::updateHasChangesPendingProperty);
      styleEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
      nameEditorController.addAnyChangeListener(this::updateHasChangesPendingProperty);

      coefficientsXListEditorController.setPrefHeight(4);
      coefficientsYListEditorController.setPrefHeight(4);
      coefficientsZListEditorController.setPrefHeight(4);
      resetFields();
   }

   private <T> void updateHasChangesPendingProperty(ObservableValue<? extends T> observable, T oldValue, T newValue)
   {
      hasChangesPendingProperty.set(!definitionBeforeEdits.equals(YoGraphicTools.toYoGraphicPolynomial3DDefinition(yoGraphicToEdit)));
   }

   @Override
   public ObservableBooleanValue inputsValidityProperty()
   {
      return inputsValidityProperty;
   }

   @Override
   public void resetFields()
   {
      coefficientsXListEditorController.setInputSingletonComposites(definitionBeforeEdits.getCoefficientsX(), definitionBeforeEdits.getNumberOfCoefficientsX());
      coefficientsYListEditorController.setInputSingletonComposites(definitionBeforeEdits.getCoefficientsY(), definitionBeforeEdits.getNumberOfCoefficientsY());
      coefficientsZListEditorController.setInputSingletonComposites(definitionBeforeEdits.getCoefficientsZ(), definitionBeforeEdits.getNumberOfCoefficientsZ());
      styleEditorController.setInput(definitionBeforeEdits);
      startTimeTextField.setText(definitionBeforeEdits.getStartTime());
      endTimeTextField.setText(definitionBeforeEdits.getEndTime());
      sizeTextField.setText(definitionBeforeEdits.getSize());
      nameEditorController.setInput(definitionBeforeEdits.getName(), yoGraphicToEdit.getNamespace());
   }

   @Override
   public void saveChanges()
   {
      definitionBeforeEdits = YoGraphicTools.toYoGraphicPolynomial3DDefinition(yoGraphicToEdit);
      hasChangesPendingProperty.set(false);
   }

   @Override
   public ReadOnlyBooleanProperty hasChangesPendingProperty()
   {
      return hasChangesPendingProperty;
   }

   @Override
   public YoPolynomialFX3D getYoGraphicFX()
   {
      return yoGraphicToEdit;
   }

   @Override
   public VBox getMainPane()
   {
      return mainPane;
   }
}
