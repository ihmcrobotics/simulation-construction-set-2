package us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.graphic3D;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.YoGraphicFXControllerTools;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.YoGraphicFXCreatorController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.editor.YoCompositeEditorPaneController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.editor.YoCompositeListEditorPaneController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.editor.YoGraphic3DStyleEditorPaneController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.editor.YoGraphicNameEditorPaneController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.editor.yoTextField.YoDoubleTextField;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.YoCompositeSearchManager;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.CompositeProperty;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.Orientation3DProperty;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.QuaternionProperty;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.Tuple2DProperty;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.Tuple3DProperty;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.YawPitchRollProperty;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.YoCompositeTools;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGraphicFX3D;
import us.ihmc.scs2.sharedMemory.LinkedYoRegistry;

public abstract class YoGraphicFX3DEditorController<G extends YoGraphicFX3D> implements YoGraphicFXCreatorController<G>
{
   @FXML
   protected VBox mainPane;
   @FXML
   protected YoGraphic3DStyleEditorPaneController styleEditorController;
   @FXML
   protected YoGraphicNameEditorPaneController nameEditorController;

   protected ObservableBooleanValue inputsValidityProperty;

   protected G yoGraphicToEdit;
   protected YoCompositeSearchManager yoCompositeSearchManager;
   protected SessionVisualizerToolkit toolkit;

   @Override
   public void initialize(SessionVisualizerToolkit toolkit, G yoGraphicToEdit)
   {
      this.toolkit = toolkit;
      this.yoGraphicToEdit = yoGraphicToEdit;
      yoCompositeSearchManager = toolkit.getYoCompositeSearchManager();

      styleEditorController.initialize(toolkit);

      nameEditorController.initialize(toolkit, yoGraphicToEdit);

      inputsValidityProperty = Bindings.and(styleEditorController.inputsValidityProperty(), nameEditorController.inputsValidityProperty());

      styleEditorController.bindYoGraphicFX3D(yoGraphicToEdit);
      nameEditorController.bindYoGraphicFXItem(yoGraphicToEdit);

      styleEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
      nameEditorController.addAnyChangeListener(this::updateHasChangesPendingProperty);
   }

   protected abstract <T> void updateHasChangesPendingProperty(ObservableValue<? extends T> observable, T oldValue, T newValue);

   protected void setupDoublePropertyEditor(TextField textField, ImageView validImageView, BiConsumer<G, DoubleProperty> setter)
   {
      LinkedYoRegistry linkedRootRegistry = toolkit.getYoManager().getLinkedRootRegistry();
      YoDoubleTextField yoDoubleTextField = new YoDoubleTextField(textField, yoCompositeSearchManager, linkedRootRegistry, validImageView);
      yoDoubleTextField.setupAutoCompletion();
      yoDoubleTextField.supplierProperty().addListener((o, oldValue, newValue) -> setter.accept(yoGraphicToEdit, newValue));
      textField.textProperty().addListener(this::updateHasChangesPendingProperty);
      inputsValidityProperty = Bindings.and(inputsValidityProperty, yoDoubleTextField.getValidityProperty());
   }

   protected void setupTuple3DPropertyEditor(YoCompositeEditorPaneController editor,
                                             String entryName,
                                             boolean setupReferenceFrame,
                                             Tuple3DProperty propertyToBind)
   {
      setupCompositePropertyEditor(editor, entryName, setupReferenceFrame, YoCompositeTools.YO_TUPLE3D, propertyToBind);
   }

   protected void setupOrientation3DProperty(YoCompositeEditorPaneController editor,
                                             String entryName,
                                             boolean setupReferenceFrame,
                                             Orientation3DProperty propertyToBind)
   {
      if (propertyToBind != null && propertyToBind instanceof YawPitchRollProperty)
         setupYawPitchRollProperty(editor, entryName, setupReferenceFrame, (YawPitchRollProperty) propertyToBind);
      else
         setupQuaternionProperty(editor, entryName, setupReferenceFrame, (QuaternionProperty) propertyToBind);
   }

   protected void setupQuaternionProperty(YoCompositeEditorPaneController editor,
                                          String entryName,
                                          boolean setupReferenceFrame,
                                          QuaternionProperty propertyToBind)
   {
      setupCompositePropertyEditor(editor, entryName, setupReferenceFrame, YoCompositeTools.YO_QUATERNION, propertyToBind);
   }

   protected void setupYawPitchRollProperty(YoCompositeEditorPaneController editor,
                                            String entryName,
                                            boolean setupReferenceFrame,
                                            YawPitchRollProperty propertyToBind)
   {
      setupCompositePropertyEditor(editor, entryName, setupReferenceFrame, YoCompositeTools.YO_YAW_PITCH_ROLL, propertyToBind);
   }

   protected void setupCompositePropertyEditor(YoCompositeEditorPaneController editor,
                                               String entryName,
                                               boolean setupReferenceFrame,
                                               String compositeType,
                                               CompositeProperty propertyToBind)
   {
      editor.initialize(toolkit, yoCompositeSearchManager.getCollectionFromType(compositeType), setupReferenceFrame);
      editor.setCompositeName(entryName);
      inputsValidityProperty = Bindings.and(inputsValidityProperty, editor.inputsValidityProperty());
      editor.bindYoCompositeDoubleProperty(propertyToBind);
      editor.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
   }

   protected void setupDoublePropertyListEditor(YoCompositeListEditorPaneController editor,
                                                String entryName,
                                                Consumer<IntegerProperty> listSizeConsumer,
                                                Consumer<List<DoubleProperty>> listUpdateConsumer)
   {
      setupDoublePropertyListEditor(editor, entryName, null, listSizeConsumer, listUpdateConsumer);
   }

   protected void setupDoublePropertyListEditor(YoCompositeListEditorPaneController editor,
                                                String entryName,
                                                String entryComponentName,
                                                Consumer<IntegerProperty> listSizeConsumer,
                                                Consumer<List<DoubleProperty>> listUpdateConsumer)
   {
      editor.initialize(toolkit, yoCompositeSearchManager.getYoDoubleCollection(), false);
      editor.setCompositeName(entryName, entryComponentName);
      inputsValidityProperty = Bindings.and(inputsValidityProperty, editor.inputsValidityProperty());
      editor.numberOfCompositesProperty().addListener((o, oldValue, newValue) -> listSizeConsumer.accept(newValue));
      YoGraphicFXControllerTools.toSingletonDoubleSupplierListProperty(editor.compositeListProperty())
                                .addListener((o, oldValue, newValue) -> listUpdateConsumer.accept(newValue));
      editor.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
   }

   protected void setupTuple2DPropertyListEditor(YoCompositeListEditorPaneController editor,
                                                 String entryName,
                                                 boolean setupReferenceFrame,
                                                 Consumer<IntegerProperty> listSizeConsumer,
                                                 Consumer<List<Tuple2DProperty>> listUpdateConsumer)
   {
      setupTuple2DPropertyListEditor(editor, entryName, null, setupReferenceFrame, listSizeConsumer, listUpdateConsumer);
   }

   protected void setupTuple2DPropertyListEditor(YoCompositeListEditorPaneController editor,
                                                 String entryName,
                                                 String entryComponentName,
                                                 boolean setupReferenceFrame,
                                                 Consumer<IntegerProperty> listSizeConsumer,
                                                 Consumer<List<Tuple2DProperty>> listUpdateConsumer)
   {
      setupCompositePropertyListEditor(editor,
                                       entryName,
                                       entryComponentName,
                                       setupReferenceFrame,
                                       YoCompositeTools.YO_TUPLE2D,
                                       listSizeConsumer,
                                       listUpdateConsumer,
                                       Tuple2DProperty::new);
   }

   protected void setupTuple3DPropertyListEditor(YoCompositeListEditorPaneController editor,
                                                 String entryName,
                                                 boolean setupReferenceFrame,
                                                 Consumer<IntegerProperty> listSizeConsumer,
                                                 Consumer<List<Tuple3DProperty>> listUpdateConsumer)
   {
      setupTuple3DPropertyListEditor(editor, entryName, null, setupReferenceFrame, listSizeConsumer, listUpdateConsumer);
   }

   protected void setupTuple3DPropertyListEditor(YoCompositeListEditorPaneController editor,
                                                 String entryName,
                                                 String entryComponentName,
                                                 boolean setupReferenceFrame,
                                                 Consumer<IntegerProperty> listSizeConsumer,
                                                 Consumer<List<Tuple3DProperty>> listUpdateConsumer)
   {
      setupCompositePropertyListEditor(editor,
                                       entryName,
                                       entryComponentName,
                                       setupReferenceFrame,
                                       YoCompositeTools.YO_TUPLE3D,
                                       listSizeConsumer,
                                       listUpdateConsumer,
                                       Tuple3DProperty::new);
   }

   protected <T extends CompositeProperty> void setupCompositePropertyListEditor(YoCompositeListEditorPaneController editor,
                                                                                 String entryName,
                                                                                 boolean setupReferenceFrame,
                                                                                 String compositeType,
                                                                                 Consumer<IntegerProperty> listSizeConsumer,
                                                                                 Consumer<List<T>> listUpdateConsumer,
                                                                                 Supplier<T> compositePropertyBuilder)
   {
      setupCompositePropertyListEditor(editor,
                                       entryName,
                                       null,
                                       setupReferenceFrame,
                                       compositeType,
                                       listSizeConsumer,
                                       listUpdateConsumer,
                                       compositePropertyBuilder);
   }

   protected <T extends CompositeProperty> void setupCompositePropertyListEditor(YoCompositeListEditorPaneController editor,
                                                                                 String entryName,
                                                                                 String entryComponentName,
                                                                                 boolean setupReferenceFrame,
                                                                                 String compositeType,
                                                                                 Consumer<IntegerProperty> listSizeConsumer,
                                                                                 Consumer<List<T>> listUpdateConsumer,
                                                                                 Supplier<T> compositePropertyBuilder)
   {
      editor.initialize(toolkit, yoCompositeSearchManager.getCollectionFromType(compositeType), setupReferenceFrame);
      editor.setCompositeName(entryName, entryComponentName);
      inputsValidityProperty = Bindings.and(inputsValidityProperty, editor.inputsValidityProperty());
      editor.numberOfCompositesProperty().addListener((o, oldValue, newValue) -> listSizeConsumer.accept(newValue));
      editor.addInputListener(listUpdateConsumer, compositePropertyBuilder);
      editor.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
   }

   @Override
   public ObservableBooleanValue inputsValidityProperty()
   {
      return inputsValidityProperty;
   }

   @Override
   public VBox getMainPane()
   {
      return mainPane;
   }

   @Override
   public G getYoGraphicFX()
   {
      return yoGraphicToEdit;
   }
}
