package us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicPointcloud3DDefinition;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.yoTextFields.YoDoubleTextField;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.YoCompositeSearchManager;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.Tuple3DProperty;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGraphicFXResourceManager;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGraphicTools;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoPointcloudFX3D;

public class YoPointcloudFX3DEditorController implements YoGraphicFXCreatorController<YoPointcloudFX3D>
{
   @FXML
   private VBox mainPane;
   @FXML
   private YoCompositeListEditorPaneController pointListEditorController;
   @FXML
   private JFXTextField sizeTextField;
   @FXML
   private ImageView sizeValidImageView;
   @FXML
   private YoGraphic3DStyleEditorPaneController styleEditorController;
   @FXML
   private JFXComboBox<String> graphicComboBox;
   @FXML
   private YoGraphicNameEditorPaneController nameEditorController;

   private ObservableBooleanValue inputsValidityProperty;

   private YoDoubleTextField yoSizeTextField;

   private YoPointcloudFX3D yoGraphicToEdit;
   private YoGraphicPointcloud3DDefinition definitionBeforeEdits;

   private BooleanProperty hasChangesPendingProperty = new SimpleBooleanProperty(this, "hasChangesPending", false);

   @Override
   public void initialize(SessionVisualizerToolkit toolkit, YoPointcloudFX3D yoGraphicToEdit, Window owner)
   {
      this.yoGraphicToEdit = yoGraphicToEdit;
      definitionBeforeEdits = YoGraphicTools.toYoGraphicPointcloud3DDefinition(yoGraphicToEdit);
      yoGraphicToEdit.visibleProperty().addListener((observable, oldValue, newValue) -> definitionBeforeEdits.setVisible(newValue));

      YoCompositeSearchManager yoCompositeSearchManager = toolkit.getYoCompositeSearchManager();
      pointListEditorController.initialize(toolkit, yoCompositeSearchManager.getYoTuple3DCollection(), true);
      pointListEditorController.setCompositeName("Point");
      yoSizeTextField = new YoDoubleTextField(sizeTextField, yoCompositeSearchManager, sizeValidImageView);

      YoGraphicFXResourceManager yoGraphicFXResourceManager = toolkit.getYoGraphicFXManager().getYoGraphicFXResourceManager();
      graphicComboBox.setItems(FXCollections.observableArrayList(yoGraphicFXResourceManager.getGraphic3DNameList()));

      styleEditorController.initialize(toolkit);
      nameEditorController.initialize(toolkit, yoGraphicToEdit);
      yoSizeTextField.setupAutoCompletion();

      inputsValidityProperty = Bindings.and(pointListEditorController.inputsValidityProperty(), yoSizeTextField.getValidityProperty())
                                       .and(nameEditorController.inputsValidityProperty());

      pointListEditorController.numberOfCompositesProperty().addListener((o, oldValue, newValue) -> yoGraphicToEdit.setNumberOfPoints(newValue));
      pointListEditorController.addInputListener(yoGraphicToEdit::setPoints, Tuple3DProperty::new);
      yoSizeTextField.supplierProperty().addListener((o, oldValue, newValue) -> yoGraphicToEdit.setSize(newValue));
      styleEditorController.bindYoGraphicFX3D(yoGraphicToEdit);

      graphicComboBox.valueProperty()
                     .addListener((o, oldValue, newValue) -> yoGraphicToEdit.setGraphicResource(yoGraphicFXResourceManager.loadGraphic3DResource(newValue)));

      nameEditorController.bindYoGraphicFXItem(yoGraphicToEdit);

      pointListEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
      sizeTextField.textProperty().addListener(this::updateHasChangesPendingProperty);
      styleEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
      graphicComboBox.valueProperty().addListener(this::updateHasChangesPendingProperty);
      nameEditorController.addAnyChangeListener(this::updateHasChangesPendingProperty);

      setupHeightAdjustment();
      resetFields();
   }

   private void setupHeightAdjustment()
   {
      mainPane.parentProperty().addListener((o, oldValue, newValue) ->
      {
         Region parent = (Region) newValue;

         while (parent != null && !(parent instanceof ScrollPane))
            parent = (Region) parent.getParent();

         if (parent == null)
            return;

         pointListEditorController.setupHeightAdjustmentForScrollPane((ScrollPane) parent);
      });
   }

   private <T> void updateHasChangesPendingProperty(ObservableValue<? extends T> observable, T oldValue, T newValue)
   {
      hasChangesPendingProperty.set(!definitionBeforeEdits.equals(YoGraphicTools.toYoGraphicPointcloud3DDefinition(yoGraphicToEdit)));
   }

   @Override
   public ObservableBooleanValue inputsValidityProperty()
   {
      return inputsValidityProperty;
   }

   @Override
   public void resetFields()
   {
      pointListEditorController.setInputFromDefinition(definitionBeforeEdits.getPoints(), definitionBeforeEdits.getNumberOfPoints());
      if (definitionBeforeEdits.getGraphicName() == null)
         definitionBeforeEdits.setGraphicName(YoGraphicFXResourceManager.DEFAULT_POINT3D_GRAPHIC_RESOURCE.getResourceName());
      graphicComboBox.setValue(definitionBeforeEdits.getGraphicName());
      sizeTextField.setText(definitionBeforeEdits.getSize());
      styleEditorController.setInput(definitionBeforeEdits);
      nameEditorController.setInput(definitionBeforeEdits.getName(), yoGraphicToEdit.getNamespace());
   }

   @Override
   public void saveChanges()
   {
      definitionBeforeEdits = YoGraphicTools.toYoGraphicPointcloud3DDefinition(yoGraphicToEdit);
      hasChangesPendingProperty.set(false);
   }

   @Override
   public ReadOnlyBooleanProperty hasChangesPendingProperty()
   {
      return hasChangesPendingProperty;
   }

   @Override
   public YoPointcloudFX3D getYoGraphicFX()
   {
      return yoGraphicToEdit;
   }

   @Override
   public Pane getMainPane()
   {
      return mainPane;
   }
}
