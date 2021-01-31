package us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic;

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
import javafx.stage.Window;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicCoordinateSystem3DDefinition;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.yoTextFields.YoDoubleTextField;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.YoCompositeSearchManager;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoCoordinateSystemFX3D;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGraphicTools;

public class YoCoordinateSystemFX3DEditorController implements YoGraphicFXCreatorController<YoCoordinateSystemFX3D>
{
   @FXML
   private VBox mainPane;
   @FXML
   private YoCompositeEditorPaneController positionEditorController, orientationEditorController;
   @FXML
   private TextField bodyLengthTextField, headLengthTextField;
   @FXML
   private TextField bodyRadiusTextField, headRadiusTextField;
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

   private YoCoordinateSystemFX3D yoGraphicToEdit;
   private YoGraphicCoordinateSystem3DDefinition definitionBeforeEdits;
   private YoCompositeSearchManager yoCompositeSearchManager;

   private BooleanProperty hasChangesPendingProperty = new SimpleBooleanProperty(this, "hasChangesPending", false);

   @Override
   public void initialize(SessionVisualizerToolkit toolkit, YoCoordinateSystemFX3D yoGraphicToEdit, Window owner)
   {
      this.yoGraphicToEdit = yoGraphicToEdit;
      definitionBeforeEdits = YoGraphicTools.toYoGraphicCoordinateSystem3DDefinition(yoGraphicToEdit);
      yoGraphicToEdit.visibleProperty().addListener((observable, oldValue, newValue) -> definitionBeforeEdits.setVisible(newValue));
      yoCompositeSearchManager = toolkit.getYoCompositeSearchManager();

      positionEditorController.initialize(toolkit, toolkit.getYoCompositeSearchManager().getYoTuple3DCollection(), true);
      positionEditorController.setCompositeName("Position");
      orientationEditorController.initialize(toolkit, toolkit.getYoCompositeSearchManager().getYoQuaternionCollection(), true);
      orientationEditorController.setCompositeName("Orientation");
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

      inputsValidityProperty = Bindings.and(positionEditorController.inputsValidityProperty(), orientationEditorController.inputsValidityProperty())
                                       .and(yoBodyLengthTextField.getValidityProperty()).and(yoBodyRadiusTextField.getValidityProperty())
                                       .and(yoHeadLengthTextField.getValidityProperty()).and(yoHeadRadiusTextField.getValidityProperty())
                                       .and(styleEditorController.inputsValidityProperty()).and(nameEditorController.inputsValidityProperty());

      positionEditorController.bindYoCompositeDoubleProperty(yoGraphicToEdit.getPosition());
      orientationEditorController.bindYoCompositeDoubleProperty(yoGraphicToEdit.getOrientation());
      yoBodyLengthTextField.supplierProperty().addListener((o, oldValue, newValue) -> yoGraphicToEdit.setBodyLength(newValue));
      yoHeadLengthTextField.supplierProperty().addListener((o, oldValue, newValue) -> yoGraphicToEdit.setHeadLength(newValue));
      yoBodyRadiusTextField.supplierProperty().addListener((o, oldValue, newValue) -> yoGraphicToEdit.setBodyRadius(newValue));
      yoHeadRadiusTextField.supplierProperty().addListener((o, oldValue, newValue) -> yoGraphicToEdit.setHeadRadius(newValue));
      styleEditorController.bindYoGraphicFX3D(yoGraphicToEdit);
      nameEditorController.bindYoGraphicFXItem(yoGraphicToEdit);

      positionEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
      orientationEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
      bodyLengthTextField.textProperty().addListener(this::updateHasChangesPendingProperty);
      headLengthTextField.textProperty().addListener(this::updateHasChangesPendingProperty);
      bodyRadiusTextField.textProperty().addListener(this::updateHasChangesPendingProperty);
      headRadiusTextField.textProperty().addListener(this::updateHasChangesPendingProperty);
      styleEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
      nameEditorController.addAnyChangeListener(this::updateHasChangesPendingProperty);

      resetFields();
   }

   private <T> void updateHasChangesPendingProperty(ObservableValue<? extends T> observable, T oldValue, T newValue)
   {
      hasChangesPendingProperty.set(!definitionBeforeEdits.equals(YoGraphicTools.toYoGraphicCoordinateSystem3DDefinition(yoGraphicToEdit)));
   }

   @Override
   public void resetFields()
   {
      positionEditorController.setInput(definitionBeforeEdits.getPosition());
      orientationEditorController.setInput(definitionBeforeEdits.getOrientation());
      bodyLengthTextField.setText(definitionBeforeEdits.getBodyLength());
      headLengthTextField.setText(definitionBeforeEdits.getHeadLength());
      bodyRadiusTextField.setText(definitionBeforeEdits.getBodyRadius());
      headRadiusTextField.setText(definitionBeforeEdits.getHeadRadius());
      styleEditorController.setInput(definitionBeforeEdits);
      nameEditorController.setInput(definitionBeforeEdits.getName(), yoGraphicToEdit.getNamespace());
   }

   @Override
   public void saveChanges()
   {
      definitionBeforeEdits = YoGraphicTools.toYoGraphicCoordinateSystem3DDefinition(yoGraphicToEdit);
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
   public YoCoordinateSystemFX3D getYoGraphicFX()
   {
      return yoGraphicToEdit;
   }

   @Override
   public VBox getMainPane()
   {
      return mainPane;
   }
}
