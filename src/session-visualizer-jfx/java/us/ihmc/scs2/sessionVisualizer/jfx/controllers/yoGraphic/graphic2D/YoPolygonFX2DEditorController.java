package us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.graphic2D;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicPolygon2DDefinition;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.YoGraphicFXCreatorController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.editor.YoCompositeListEditorPaneController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.editor.YoGraphic2DStyleEditorPaneController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.editor.YoGraphicNameEditorPaneController;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.YoCompositeSearchManager;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.Tuple2DProperty;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGraphicTools;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoPolygonFX2D;

public class YoPolygonFX2DEditorController implements YoGraphicFXCreatorController<YoPolygonFX2D>
{
   @FXML
   private VBox mainPane;
   @FXML
   private YoCompositeListEditorPaneController vertexListEditorController;
   @FXML
   private YoGraphic2DStyleEditorPaneController styleEditorController;
   @FXML
   private YoGraphicNameEditorPaneController nameEditorController;

   private ObservableBooleanValue inputsValidityProperty;

   private YoPolygonFX2D yoGraphicToEdit;
   private YoGraphicPolygon2DDefinition definitionBeforeEdits;

   private BooleanProperty hasChangesPendingProperty = new SimpleBooleanProperty(this, "hasChangesPending", false);

   @Override
   public void initialize(SessionVisualizerToolkit toolkit, YoPolygonFX2D yoGraphicToEdit)
   {
      this.yoGraphicToEdit = yoGraphicToEdit;
      definitionBeforeEdits = YoGraphicTools.toYoGraphicPolygon2DDefinition(yoGraphicToEdit);
      yoGraphicToEdit.visibleProperty().addListener((observable, oldValue, newValue) -> definitionBeforeEdits.setVisible(newValue));

      YoCompositeSearchManager yoCompositeSearchManager = toolkit.getYoCompositeSearchManager();
      vertexListEditorController.initialize(toolkit, yoCompositeSearchManager.getYoTuple2DCollection(), true);
      vertexListEditorController.setCompositeName("Vertex", "Vertices");
      styleEditorController.initialize(toolkit);
      nameEditorController.initialize(toolkit, yoGraphicToEdit);

      inputsValidityProperty = Bindings.and(vertexListEditorController.inputsValidityProperty(), styleEditorController.inputsValidityProperty())
                                       .and(nameEditorController.inputsValidityProperty());

      styleEditorController.bindYoGraphicFX2D(yoGraphicToEdit);
      nameEditorController.bindYoGraphicFXItem(yoGraphicToEdit);
      vertexListEditorController.numberOfCompositesProperty().addListener((o, oldValue, newValue) -> yoGraphicToEdit.setNumberOfVertices(newValue));
      vertexListEditorController.addInputListener(yoGraphicToEdit::setVertices, Tuple2DProperty::new);

      vertexListEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
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
      hasChangesPendingProperty.set(!definitionBeforeEdits.equals(YoGraphicTools.toYoGraphicPolygon2DDefinition(yoGraphicToEdit)));
   }

   @Override
   public ObservableBooleanValue inputsValidityProperty()
   {
      return inputsValidityProperty;
   }

   @Override
   public void resetFields()
   {
      vertexListEditorController.setInputFromDefinition(definitionBeforeEdits.getVertices(), definitionBeforeEdits.getNumberOfVertices());
      styleEditorController.setInput(definitionBeforeEdits);
      nameEditorController.setInput(definitionBeforeEdits.getName(), yoGraphicToEdit.getNamespace());
   }

   @Override
   public void saveChanges()
   {
      definitionBeforeEdits = YoGraphicTools.toYoGraphicPolygon2DDefinition(yoGraphicToEdit);
      hasChangesPendingProperty.set(false);
   }

   @Override
   public ReadOnlyBooleanProperty hasChangesPendingProperty()
   {
      return hasChangesPendingProperty;
   }

   @Override
   public YoPolygonFX2D getYoGraphicFX()
   {
      return yoGraphicToEdit;
   }

   @Override
   public VBox getMainPane()
   {
      return mainPane;
   }
}
