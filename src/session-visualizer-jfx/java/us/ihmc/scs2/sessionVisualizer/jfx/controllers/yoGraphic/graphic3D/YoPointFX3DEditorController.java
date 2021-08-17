package us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.graphic3D;

import com.jfoenix.controls.JFXComboBox;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicPoint3DDefinition;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.YoGraphicFXCreatorController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.editor.YoCompositeEditorPaneController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.editor.YoGraphic3DStyleEditorPaneController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.editor.YoGraphicNameEditorPaneController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.editor.yoTextField.YoDoubleTextField;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.YoCompositeSearchManager;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGraphicFXResourceManager;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGraphicTools;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoPointFX3D;
import us.ihmc.scs2.sharedMemory.LinkedYoRegistry;

public class YoPointFX3DEditorController implements YoGraphicFXCreatorController<YoPointFX3D>
{
   @FXML
   private VBox mainPane;
   @FXML
   private YoCompositeEditorPaneController positionEditorController;
   @FXML
   private TextField sizeTextField;
   @FXML
   private JFXComboBox<String> graphicComboBox;
   @FXML
   private YoGraphicNameEditorPaneController nameEditorController;
   @FXML
   private ImageView sizeValidImageView;
   @FXML
   private YoGraphic3DStyleEditorPaneController styleEditorController;

   private ObservableBooleanValue inputsValidityProperty;

   private YoDoubleTextField yoSizeTextField;

   private YoPointFX3D yoGraphicToEdit;
   private YoGraphicPoint3DDefinition definitionBeforeEdits;

   private BooleanProperty hasChangesPendingProperty = new SimpleBooleanProperty(this, "hasChangesPending", false);

   @Override
   public void initialize(SessionVisualizerToolkit toolkit, YoPointFX3D yoGraphicToEdit)
   {
      this.yoGraphicToEdit = yoGraphicToEdit;
      definitionBeforeEdits = YoGraphicTools.toYoGraphicPoint3DDefinition(yoGraphicToEdit);
      yoGraphicToEdit.visibleProperty().addListener((observable, oldValue, newValue) -> definitionBeforeEdits.setVisible(newValue));
      YoCompositeSearchManager yoCompositeSearchManager = toolkit.getYoCompositeSearchManager();
      LinkedYoRegistry linkedRootRegistry = toolkit.getYoManager().getLinkedRootRegistry();

      positionEditorController.initialize(toolkit, yoCompositeSearchManager.getYoTuple3DCollection(), true);
      positionEditorController.setCompositeName("Position");
      yoSizeTextField = new YoDoubleTextField(sizeTextField, yoCompositeSearchManager, linkedRootRegistry, sizeValidImageView);

      yoSizeTextField.setupAutoCompletion();

      YoGraphicFXResourceManager yoGraphicFXResourceManager = toolkit.getYoGraphicFXManager().getYoGraphicFXResourceManager();
      graphicComboBox.setItems(FXCollections.observableArrayList(yoGraphicFXResourceManager.getGraphic3DNameList()));

      styleEditorController.initialize(toolkit);
      nameEditorController.initialize(toolkit, yoGraphicToEdit);

      BooleanProperty radiusValidityProperty = yoSizeTextField.getValidityProperty();

      inputsValidityProperty = radiusValidityProperty.and(positionEditorController.inputsValidityProperty()).and(nameEditorController.inputsValidityProperty());

      graphicComboBox.valueProperty()
                     .addListener((o, oldValue, newValue) -> yoGraphicToEdit.setGraphicResource(yoGraphicFXResourceManager.loadGraphic3DResource(newValue)));
      positionEditorController.bindYoCompositeDoubleProperty(yoGraphicToEdit.getPosition());
      yoSizeTextField.supplierProperty().addListener((o, oldValue, newValue) -> yoGraphicToEdit.setSize(newValue));
      styleEditorController.bindYoGraphicFX3D(yoGraphicToEdit);
      nameEditorController.bindYoGraphicFXItem(yoGraphicToEdit);

      positionEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
      sizeTextField.textProperty().addListener(this::updateHasChangesPendingProperty);
      nameEditorController.addAnyChangeListener(this::updateHasChangesPendingProperty);
      graphicComboBox.valueProperty().addListener(this::updateHasChangesPendingProperty);
      styleEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));

      resetFields();

   }

   private <T> void updateHasChangesPendingProperty(ObservableValue<? extends T> observable, T oldValue, T newValue)
   {
      hasChangesPendingProperty.set(!definitionBeforeEdits.equals(YoGraphicTools.toYoGraphicPoint3DDefinition(yoGraphicToEdit)));
   }

   @Override
   public void resetFields()
   {
      positionEditorController.setInput(definitionBeforeEdits.getPosition());
      sizeTextField.setText(definitionBeforeEdits.getSize());
      if (definitionBeforeEdits.getGraphicName() == null)
         definitionBeforeEdits.setGraphicName(YoGraphicFXResourceManager.DEFAULT_POINT3D_GRAPHIC_RESOURCE.getResourceName());
      graphicComboBox.setValue(definitionBeforeEdits.getGraphicName());
      styleEditorController.setInput(definitionBeforeEdits);
      nameEditorController.setInput(definitionBeforeEdits.getName(), yoGraphicToEdit.getNamespace());
   }

   @Override
   public void saveChanges()
   {
      definitionBeforeEdits = YoGraphicTools.toYoGraphicPoint3DDefinition(yoGraphicToEdit);
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
   public YoPointFX3D getYoGraphicFX()
   {
      return yoGraphicToEdit;
   }

   @Override
   public VBox getMainPane()
   {
      return mainPane;
   }
}
