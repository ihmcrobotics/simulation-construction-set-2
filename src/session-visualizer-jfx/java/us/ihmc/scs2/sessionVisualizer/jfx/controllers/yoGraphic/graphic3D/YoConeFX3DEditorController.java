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
import us.ihmc.scs2.definition.yoGraphic.YoGraphicCone3DDefinition;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.YoGraphicFXCreatorController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.editor.YoCompositeEditorPaneController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.editor.YoGraphic3DStyleEditorPaneController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.editor.YoGraphicNameEditorPaneController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.editor.yoTextField.YoDoubleTextField;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.YoCompositeSearchManager;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoConeFX3D;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGraphicTools;
import us.ihmc.scs2.sharedMemory.LinkedYoRegistry;

public class YoConeFX3DEditorController implements YoGraphicFXCreatorController<YoConeFX3D>
{
   @FXML
   private VBox mainPane;
   @FXML
   private YoCompositeEditorPaneController positionEditorController, axisEditorController;
   @FXML
   private TextField heightTextField, radiusTextField;
   @FXML
   private YoGraphic3DStyleEditorPaneController styleEditorController;
   @FXML
   private YoGraphicNameEditorPaneController nameEditorController;

   @FXML
   private ImageView heightValidImageView, radiusValidImageView;

   private YoDoubleTextField yoHeightTextField;
   private YoDoubleTextField yoRadiusTextField;
   private ObservableBooleanValue inputsValidityProperty;

   private YoConeFX3D yoGraphicToEdit;
   private YoGraphicCone3DDefinition definitionBeforeEdits;
   private YoCompositeSearchManager yoCompositeSearchManager;

   private BooleanProperty hasChangesPendingProperty = new SimpleBooleanProperty(this, "hasChangesPending", false);

   @Override
   public void initialize(SessionVisualizerToolkit toolkit, YoConeFX3D yoGraphicToEdit)
   {
      this.yoGraphicToEdit = yoGraphicToEdit;
      definitionBeforeEdits = YoGraphicTools.toYoGraphicCone3DDefinition(yoGraphicToEdit);
      yoGraphicToEdit.visibleProperty().addListener((observable, oldValue, newValue) -> definitionBeforeEdits.setVisible(newValue));
      yoCompositeSearchManager = toolkit.getYoCompositeSearchManager();
      LinkedYoRegistry linkedRootRegistry = toolkit.getYoManager().getLinkedRootRegistry();

      positionEditorController.initialize(toolkit, yoCompositeSearchManager.getYoTuple3DCollection(), true);
      positionEditorController.setCompositeName("Position");
      axisEditorController.initialize(toolkit, yoCompositeSearchManager.getYoTuple3DCollection(), true);
      axisEditorController.setCompositeName("Axis");
      yoHeightTextField = new YoDoubleTextField(heightTextField, yoCompositeSearchManager, linkedRootRegistry, heightValidImageView);
      yoRadiusTextField = new YoDoubleTextField(radiusTextField, yoCompositeSearchManager, linkedRootRegistry, radiusValidImageView);

      yoHeightTextField.setupAutoCompletion();
      yoRadiusTextField.setupAutoCompletion();

      styleEditorController.initialize(toolkit);
      nameEditorController.initialize(toolkit, yoGraphicToEdit);

      inputsValidityProperty = Bindings.and(positionEditorController.inputsValidityProperty(), axisEditorController.inputsValidityProperty())
                                       .and(yoHeightTextField.getValidityProperty()).and(yoRadiusTextField.getValidityProperty())
                                       .and(styleEditorController.inputsValidityProperty()).and(nameEditorController.inputsValidityProperty());

      positionEditorController.bindYoCompositeDoubleProperty(yoGraphicToEdit.getPosition());
      axisEditorController.bindYoCompositeDoubleProperty(yoGraphicToEdit.getAxis());
      yoHeightTextField.supplierProperty().addListener((o, oldValue, newValue) -> yoGraphicToEdit.setHeight(newValue));
      yoRadiusTextField.supplierProperty().addListener((o, oldValue, newValue) -> yoGraphicToEdit.setRadius(newValue));
      styleEditorController.bindYoGraphicFX3D(yoGraphicToEdit);
      nameEditorController.bindYoGraphicFXItem(yoGraphicToEdit);

      positionEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
      axisEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
      heightTextField.textProperty().addListener(this::updateHasChangesPendingProperty);
      radiusTextField.textProperty().addListener(this::updateHasChangesPendingProperty);
      styleEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
      nameEditorController.addAnyChangeListener(this::updateHasChangesPendingProperty);

      resetFields();
   }

   private <T> void updateHasChangesPendingProperty(ObservableValue<? extends T> observable, T oldValue, T newValue)
   {
      hasChangesPendingProperty.set(!definitionBeforeEdits.equals(YoGraphicTools.toYoGraphicCone3DDefinition(yoGraphicToEdit)));
   }

   @Override
   public void resetFields()
   {
      positionEditorController.setInput(definitionBeforeEdits.getPosition());
      axisEditorController.setInput(definitionBeforeEdits.getAxis());
      heightTextField.setText(definitionBeforeEdits.getHeight());
      radiusTextField.setText(definitionBeforeEdits.getRadius());
      styleEditorController.setInput(definitionBeforeEdits);
      nameEditorController.setInput(definitionBeforeEdits.getName(), yoGraphicToEdit.getNamespace());
   }

   @Override
   public void saveChanges()
   {
      definitionBeforeEdits = YoGraphicTools.toYoGraphicCone3DDefinition(yoGraphicToEdit);
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
   public YoConeFX3D getYoGraphicFX()
   {
      return yoGraphicToEdit;
   }

   @Override
   public VBox getMainPane()
   {
      return mainPane;
   }
}
