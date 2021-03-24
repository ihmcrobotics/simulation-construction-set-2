package us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicPolygonExtruded3DDefinition;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.yoTextFields.YoDoubleTextField;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.YoCompositeSearchManager;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.Tuple2DProperty;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGraphicTools;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoPolygonExtrudedFX3D;

public class YoPolygonExtrudedFX3DEditorController implements YoGraphicFXCreatorController<YoPolygonExtrudedFX3D>
{
   @FXML
   private VBox mainPane;
   @FXML
   private YoCompositeEditorPaneController positionEditorController, orientationEditorController;
   @FXML
   private YoCompositeListEditorPaneController vertexListEditorController;
   @FXML
   private TextField thicknessTextField;
   @FXML
   private YoGraphic3DStyleEditorPaneController styleEditorController;
   @FXML
   private YoGraphicNameEditorPaneController nameEditorController;

   @FXML
   private ImageView thicknessValidImageView;

   private YoDoubleTextField yoThicknessTextField;
   private ObservableBooleanValue inputsValidityProperty;

   private YoPolygonExtrudedFX3D yoGraphicToEdit;
   private YoGraphicPolygonExtruded3DDefinition definitionBeforeEdits;

   private BooleanProperty hasChangesPendingProperty = new SimpleBooleanProperty(this, "hasChangesPending", false);

   @Override
   public void initialize(SessionVisualizerToolkit toolkit, YoPolygonExtrudedFX3D yoGraphicToEdit)
   {
      this.yoGraphicToEdit = yoGraphicToEdit;
      definitionBeforeEdits = YoGraphicTools.toYoGraphicPolygonExtruded3DDefinition(yoGraphicToEdit);
      yoGraphicToEdit.visibleProperty().addListener((observable, oldValue, newValue) -> definitionBeforeEdits.setVisible(newValue));

      positionEditorController.initialize(toolkit, toolkit.getYoCompositeSearchManager().getYoTuple3DCollection(), true);
      positionEditorController.setCompositeName("Position");
      orientationEditorController.initialize(toolkit, toolkit.getYoCompositeSearchManager().getYoQuaternionCollection(), true);
      orientationEditorController.setCompositeName("Orientation");

      YoCompositeSearchManager yoCompositeSearchManager = toolkit.getYoCompositeSearchManager();
      vertexListEditorController.initialize(toolkit, yoCompositeSearchManager.getYoTuple2DCollection(), false);
      vertexListEditorController.setCompositeName("Vertex", "Vertices");
      yoThicknessTextField = new YoDoubleTextField(thicknessTextField, yoCompositeSearchManager, thicknessValidImageView);

      yoThicknessTextField.setupAutoCompletion();

      styleEditorController.initialize(toolkit);
      nameEditorController.initialize(toolkit, yoGraphicToEdit);

      inputsValidityProperty = Bindings.and(positionEditorController.inputsValidityProperty(), orientationEditorController.inputsValidityProperty())
                                       .and(vertexListEditorController.inputsValidityProperty()).and(yoThicknessTextField.getValidityProperty())
                                       .and(nameEditorController.inputsValidityProperty());

      positionEditorController.bindYoCompositeDoubleProperty(yoGraphicToEdit.getPosition());
      orientationEditorController.bindYoCompositeDoubleProperty(yoGraphicToEdit.getOrientation());
      vertexListEditorController.numberOfCompositesProperty().addListener((o, oldValue, newValue) -> yoGraphicToEdit.setNumberOfVertices(newValue));
      vertexListEditorController.addInputListener(yoGraphicToEdit::setVertices, Tuple2DProperty::new);
      yoThicknessTextField.supplierProperty().addListener((o, oldValue, newValue) -> yoGraphicToEdit.setThickness(newValue));
      styleEditorController.bindYoGraphicFX3D(yoGraphicToEdit);
      nameEditorController.bindYoGraphicFXItem(yoGraphicToEdit);

      positionEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
      orientationEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
      vertexListEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
      thicknessTextField.textProperty().addListener(this::updateHasChangesPendingProperty);
      styleEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
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

         vertexListEditorController.setupHeightAdjustmentForScrollPane((ScrollPane) parent);
      });
   }

   private <T> void updateHasChangesPendingProperty(ObservableValue<? extends T> observable, T oldValue, T newValue)
   {
      hasChangesPendingProperty.set(!definitionBeforeEdits.equals(YoGraphicTools.toYoGraphicPolygonExtruded3DDefinition(yoGraphicToEdit)));
   }

   @Override
   public void resetFields()
   {
      positionEditorController.setInput(definitionBeforeEdits.getPosition());
      orientationEditorController.setInput(definitionBeforeEdits.getOrientation());
      vertexListEditorController.setInputFromDefinition(definitionBeforeEdits.getVertices(), definitionBeforeEdits.getNumberOfVertices());
      thicknessTextField.setText(definitionBeforeEdits.getThickness());
      styleEditorController.setInput(definitionBeforeEdits);
      nameEditorController.setInput(definitionBeforeEdits.getName(), yoGraphicToEdit.getNamespace());
   }

   @Override
   public void saveChanges()
   {
      definitionBeforeEdits = YoGraphicTools.toYoGraphicPolygonExtruded3DDefinition(yoGraphicToEdit);
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
   public YoPolygonExtrudedFX3D getYoGraphicFX()
   {
      return yoGraphicToEdit;
   }

   @Override
   public VBox getMainPane()
   {
      return mainPane;
   }
}
