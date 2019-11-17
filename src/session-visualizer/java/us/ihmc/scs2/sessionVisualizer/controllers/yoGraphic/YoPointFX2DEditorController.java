package us.ihmc.scs2.sessionVisualizer.controllers.yoGraphic;

import com.jfoenix.controls.JFXComboBox;

import javafx.beans.binding.Bindings;
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
import javafx.stage.Window;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicPoint2DDefinition;
import us.ihmc.scs2.sessionVisualizer.controllers.yoGraphic.yoTextFields.YoDoubleTextField;
import us.ihmc.scs2.sessionVisualizer.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.managers.YoCompositeSearchManager;
import us.ihmc.scs2.sessionVisualizer.yoGraphic.YoGraphicFX2D;
import us.ihmc.scs2.sessionVisualizer.yoGraphic.YoGraphicFXResourceManager;
import us.ihmc.scs2.sessionVisualizer.yoGraphic.YoGraphicTools;
import us.ihmc.scs2.sessionVisualizer.yoGraphic.YoPointFX2D;

public class YoPointFX2DEditorController implements YoGraphicFXCreatorController<YoPointFX2D>
{
   public static final double DEFAULT_STROKE_WIDTH = YoGraphicFX2D.DEFAULT_STROKE_WIDTH.get();

   @FXML
   private VBox mainPane;
   @FXML
   private YoCompositeEditorPaneController positionEditorController;
   @FXML
   private TextField sizeTextField;
   @FXML
   private YoGraphic2DStyleEditorPaneController styleEditorController;
   @FXML
   private JFXComboBox<String> graphicComboBox;
   @FXML
   private YoGraphicNameEditorPaneController nameEditorController;
   @FXML
   private ImageView sizeValidImageView;

   private ObservableBooleanValue inputsValidityProperty;

   private YoDoubleTextField yoSizeTextField;

   private YoPointFX2D yoGraphicToEdit;
   private YoGraphicPoint2DDefinition definitionBeforeEdits;
   private YoCompositeSearchManager yoCompositeSearchManager;

   private BooleanProperty hasChangesPendingProperty = new SimpleBooleanProperty(this, "hasChangesPending", false);

   @Override
   public void initialize(SessionVisualizerToolkit toolkit, YoPointFX2D yoGraphicToEdit, Window owner)
   {
      this.yoGraphicToEdit = yoGraphicToEdit;
      definitionBeforeEdits = YoGraphicTools.toYoGraphicPoint2DDefinition(yoGraphicToEdit);
      yoGraphicToEdit.visibleProperty().addListener((observable, oldValue, newValue) -> definitionBeforeEdits.setVisible(newValue));
      yoCompositeSearchManager = toolkit.getYoCompositeSearchManager();

      positionEditorController.initialize(toolkit, toolkit.getYoCompositeSearchManager().getYoTuple2DCollection(), true);
      positionEditorController.setCompositeName("Position");
      yoSizeTextField = new YoDoubleTextField(sizeTextField, yoCompositeSearchManager, sizeValidImageView);
      styleEditorController.initialize(toolkit);

      yoSizeTextField.setupAutoCompletion();

      YoGraphicFXResourceManager yoGraphicFXResourceManager = toolkit.getYoGraphicFXManager().getYoGraphicFXResourceManager();
      graphicComboBox.setItems(FXCollections.observableArrayList(yoGraphicFXResourceManager.getGraphic2DNameList()));

      nameEditorController.initialize(toolkit, yoGraphicToEdit);

      inputsValidityProperty = Bindings.and(positionEditorController.inputsValidityProperty(), yoSizeTextField.getValidityProperty())
                                       .and(styleEditorController.inputsValidityProperty()).and(nameEditorController.inputsValidityProperty());

      positionEditorController.bindYoCompositeDoubleProperty(yoGraphicToEdit.getPosition());
      yoSizeTextField.supplierProperty().addListener((o, oldValue, newValue) -> yoGraphicToEdit.setSize(newValue));
      styleEditorController.bindYoGraphicFX2D(yoGraphicToEdit);

      graphicComboBox.valueProperty()
                     .addListener((o, oldValue, newValue) -> yoGraphicToEdit.setGraphicResource(yoGraphicFXResourceManager.loadGraphic2DResource(newValue)));

      nameEditorController.bindYoGraphicFXItem(yoGraphicToEdit);

      positionEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
      sizeTextField.textProperty().addListener(this::updateHasChangesPendingProperty);
      styleEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
      graphicComboBox.valueProperty().addListener(this::updateHasChangesPendingProperty);
      nameEditorController.addAnyChangeListener(this::updateHasChangesPendingProperty);

      resetFields();
   }

   private <T> void updateHasChangesPendingProperty(ObservableValue<? extends T> observable, T oldValue, T newValue)
   {
      hasChangesPendingProperty.set(!definitionBeforeEdits.equals(YoGraphicTools.toYoGraphicPoint2DDefinition(yoGraphicToEdit)));
   }

   @Override
   public void resetFields()
   {
      positionEditorController.setInput(definitionBeforeEdits.getPosition());
      sizeTextField.setText(definitionBeforeEdits.getSize());
      styleEditorController.setInput(definitionBeforeEdits);
      if (definitionBeforeEdits.getGraphicName() == null)
         definitionBeforeEdits.setGraphicName(YoGraphicFXResourceManager.DEFAULT_POINT2D_GRAPHIC_RESOURCE.getResourceName());
      graphicComboBox.setValue(definitionBeforeEdits.getGraphicName());
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
      definitionBeforeEdits = YoGraphicTools.toYoGraphicPoint2DDefinition(yoGraphicToEdit);
      hasChangesPendingProperty.set(false);
   }

   @Override
   public ReadOnlyBooleanProperty hasChangesPendingProperty()
   {
      return hasChangesPendingProperty;
   }

   @Override
   public YoPointFX2D getYoGraphicFX()
   {
      return yoGraphicToEdit;
   }

   @Override
   public VBox getMainPane()
   {
      return mainPane;
   }
}
