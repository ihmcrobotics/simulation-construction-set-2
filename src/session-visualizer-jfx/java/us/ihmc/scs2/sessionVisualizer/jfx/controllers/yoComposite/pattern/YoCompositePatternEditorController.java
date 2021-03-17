package us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoComposite.pattern;

import static us.ihmc.scs2.sessionVisualizer.jfx.tools.ListViewTools.addAfterMenuItemFactory;
import static us.ihmc.scs2.sessionVisualizer.jfx.tools.ListViewTools.addBeforeMenuItemFactory;
import static us.ihmc.scs2.sessionVisualizer.jfx.tools.ListViewTools.removeMenuItemFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;

import javafx.animation.AnimationTimer;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.converter.DefaultStringConverter;
import us.ihmc.log.LogTools;
import us.ihmc.scs2.definition.yoChart.YoChartGroupModelDefinition;
import us.ihmc.scs2.definition.yoChart.YoChartIdentifierDefinition;
import us.ihmc.scs2.definition.yoComposite.YoCompositePatternDefinition;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerIOTools;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.ControllerListCell;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.SCSDefaultUIController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.chart.YoChartGroupModelEditorController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.YoGraphicFXControllerTools;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.ContextMenuTools;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.JavaFXMissingTools;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.ObservedAnimationTimer;

public class YoCompositePatternEditorController implements SCSDefaultUIController
{
   private static final String NEW_COMPONENT_IDENTIFIER = "c";

   @FXML
   private VBox mainPane;
   @FXML
   private JFXTextField compositeNameTextField;
   @FXML
   private JFXCheckBox crossRegistryCompositeCheckBox;
   @FXML
   private JFXButton addComponentButton;
   @FXML
   private ListView<String> componentIdentifiersListView;
   @FXML
   private ListView<YoChartGroupModelEditorController> chartGroupModelEditorListView;
   @FXML
   private ImageView patternNameValidImageView;

   private final BooleanProperty patternNameValidityProperty = new SimpleBooleanProperty(this, "patternNameValidity", false);
   private final BooleanProperty componentIdentifiersValidityProperty = new SimpleBooleanProperty(this, "componentIdentifiersValidity", false);
   private final BooleanProperty chartGroupModelsValidityProperty = new SimpleBooleanProperty(this, "chartGroupModelsValidity", true);
   private final BooleanBinding inputsValidityProperty = patternNameValidityProperty.and(componentIdentifiersValidityProperty)
                                                                                    .and(chartGroupModelsValidityProperty);

   private final ObjectProperty<YoCompositePatternDefinition> patternDefinitionProperty = new SimpleObjectProperty<>(this, "patternDefinition", null);
   private YoCompositePatternDefinition definitionBeforeEdits;

   private BooleanProperty hasChangesPendingProperty = new SimpleBooleanProperty(this, "hasChangesPending", false);

   private List<String> nameOfOtherPatterns = new ArrayList<>();

   private SessionVisualizerToolkit toolkit;
   private Window owner;

   @Override
   public void initialize(SessionVisualizerToolkit toolkit, Window owner)
   {
      this.toolkit = toolkit;
      this.owner = owner;

      compositeNameTextField.textProperty().addListener((o, oldValue, newValue) ->
      {
         YoCompositePatternDefinition newPattern = new YoCompositePatternDefinition(patternDefinitionProperty.get());
         newPattern.setName(newValue);
         patternDefinitionProperty.set(newPattern);
      });

      crossRegistryCompositeCheckBox.selectedProperty().addListener((o, oldValue, newValue) ->
      {
         YoCompositePatternDefinition newPattern = new YoCompositePatternDefinition(patternDefinitionProperty.get());
         newPattern.setCrossRegistry(newValue);
         patternDefinitionProperty.set(newPattern);
      });

      componentIdentifiersListView.setCellFactory(param -> new TextFieldListCell<>(new DefaultStringConverter()));
      componentIdentifiersListView.getItems().addListener((ListChangeListener<String>) change ->
      {
         YoCompositePatternDefinition newPattern = new YoCompositePatternDefinition(patternDefinitionProperty.get());
         newPattern.setIdentifiers(new ArrayList<>(change.getList()));
      });

      componentIdentifiersListView.getItems().addListener((ListChangeListener<String>) change ->
      {
         YoCompositePatternDefinition newPattern = new YoCompositePatternDefinition(patternDefinitionProperty.get());

         while (change.next())
         {
            if (change.wasPermutated())
            {
               for (int oldIndex = change.getFrom(); oldIndex < change.getTo(); oldIndex++)
               {
                  int newIndex = change.getPermutation(oldIndex);
                  Collections.swap(newPattern.getIdentifiers(), oldIndex, newIndex);

                  for (YoChartGroupModelDefinition model : newPattern.getPreferredConfigurations())
                     Collections.swap(model.getChartIdentifiers(), oldIndex, newIndex);
               }
            }
            else if (change.wasUpdated())
            {
               LogTools.error("wasUpdated is not handled!");
            }
            else if (change.wasReplaced())
            { // The component has been renamed
               for (int i = change.getFrom(); i < change.getTo(); i++)
               {
                  newPattern.getIdentifiers().set(i, change.getList().get(i));
               }
            }
            else
            {
               if (change.wasRemoved())
               {
                  for (int i = 0; i < change.getRemovedSize(); i++)
                  {
                     newPattern.getIdentifiers().remove(change.getFrom());
                     for (YoChartGroupModelDefinition model : newPattern.getPreferredConfigurations())
                        model.getChartIdentifiers().remove(change.getFrom());
                  }
               }

               if (change.wasAdded())
               {
                  for (int i = change.getFrom(); i < change.getTo(); i++)
                  {
                     newPattern.getIdentifiers().add(i, change.getList().get(i));
                     for (YoChartGroupModelDefinition model : newPattern.getPreferredConfigurations())
                        model.getChartIdentifiers().add(i, new YoChartIdentifierDefinition());
                  }
               }
            }

            patternDefinitionProperty.set(newPattern);
            ObservableList<YoChartGroupModelEditorController> chartGroupModelControllers = chartGroupModelEditorListView.getItems();

            for (int i = 0; i < chartGroupModelControllers.size(); i++)
            {
               YoChartGroupModelEditorController controller = chartGroupModelControllers.get(i);
               YoChartGroupModelDefinition chartGroupModel = newPattern.getPreferredConfigurations().get(i);
               controller.setInput(chartGroupModel, newPattern.getIdentifiers());
            }
         }
      });

      chartGroupModelEditorListView.setCellFactory(param -> new ControllerListCell<>());
      chartGroupModelEditorListView.getItems().addListener((ListChangeListener<YoChartGroupModelEditorController>) change ->
      {
         ObservableList<? extends YoChartGroupModelEditorController> newList = change.getList();

         { // Initialize with the new list values.
            YoCompositePatternDefinition newPattern = new YoCompositePatternDefinition(patternDefinitionProperty.get());
            List<YoChartGroupModelDefinition> newPreferredConfigurations = newList.stream().map(controller -> controller.chartGroupModelProperty().get())
                                                                                  .collect(Collectors.toList());
            newPattern.setPreferredConfigurations(newPreferredConfigurations);
            patternDefinitionProperty.set(newPattern);
         }

         for (int i = 0; i < newList.size(); i++)
         { // Setup listeners for each individual controller.
            int indexFinal = i;
            newList.get(i).chartGroupModelProperty().addListener((o, oldValue, newValue) ->
            {
               YoCompositePatternDefinition updatedPattern = new YoCompositePatternDefinition(patternDefinitionProperty.get());
               updatedPattern.getPreferredConfigurations().set(indexFinal, newValue);
               patternDefinitionProperty.set(updatedPattern);
            });
         }
      });

      setupValidityProperties();

      patternDefinitionProperty.addListener((o, oldValue, newValue) -> hasChangesPendingProperty.set(!definitionBeforeEdits.equals(newValue)));

      IntFunction<String> addAction = index ->
      {
         startEditingComponentIdentifier(index);
         return NEW_COMPONENT_IDENTIFIER + index;
      };
      ContextMenuTools.setupContextMenu(componentIdentifiersListView,
                                        addBeforeMenuItemFactory(addAction),
                                        addAfterMenuItemFactory(addAction),
                                        removeMenuItemFactory(false));
      ContextMenuTools.setupContextMenu(chartGroupModelEditorListView,
                                        addBeforeMenuItemFactory(() -> newYoChartGroupModelEditor()),
                                        addAfterMenuItemFactory(() -> newYoChartGroupModelEditor()),
                                        removeMenuItemFactory(false));

      setPrefHeight();
   }

   private void setupValidityProperties()
   {
      YoGraphicFXControllerTools.bindValidityImageView(patternNameValidityProperty, patternNameValidImageView);

      compositeNameTextField.textProperty().addListener((o, oldValue, newValue) ->
      {
         if (newValue == null || newValue.isEmpty())
            patternNameValidityProperty.set(false);
         else
            patternNameValidityProperty.set(!nameOfOtherPatterns.contains(newValue));
      });

      componentIdentifiersListView.setStyle(YoCompositePatternControllerTools.getValidityStyleBorder(componentIdentifiersValidityProperty.get()));
      componentIdentifiersValidityProperty.addListener((o, oldValue,
                                                        newValue) -> componentIdentifiersListView.setStyle(YoCompositePatternControllerTools.getValidityStyleBorder(newValue)));

      componentIdentifiersListView.getItems()
                                  .addListener((ListChangeListener<String>) change -> componentIdentifiersValidityProperty.set(YoCompositePatternControllerTools.areComponentIdentifierNamesValid(change.getList())));

      chartGroupModelEditorListView.setStyle(YoCompositePatternControllerTools.getValidityStyleBorder(chartGroupModelsValidityProperty.get()));
      chartGroupModelsValidityProperty.addListener((o, oldValue,
                                                    newValue) -> chartGroupModelEditorListView.setStyle(YoCompositePatternControllerTools.getValidityStyleBorder(newValue)));

      chartGroupModelEditorListView.getItems().addListener((ListChangeListener<YoChartGroupModelEditorController>) change ->
      {
         if (change.getList().isEmpty())
         {
            chartGroupModelsValidityProperty.set(true);
            return;
         }

         while (change.next() && change.wasAdded())
         {
            for (YoChartGroupModelEditorController controller : change.getAddedSubList())
            {
               controller.configurationNameProperty().addListener((o, oldValue, newValue) ->
               {
                  chartGroupModelsValidityProperty.set(YoCompositePatternControllerTools.areChartGroupModelNamesValid(chartGroupModelEditorListView.getItems()));
               });
            }
         }

         chartGroupModelsValidityProperty.set(YoCompositePatternControllerTools.areChartGroupModelNamesValid(change.getList()));
      });
   }

   public void setInput(YoCompositePatternDefinition definitionBeforeEdits)
   {
      this.definitionBeforeEdits = definitionBeforeEdits;
      resetFields();
   }

   public void saveChanges()
   {
      definitionBeforeEdits = new YoCompositePatternDefinition(patternDefinitionProperty.get());
      hasChangesPendingProperty.set(false);
   }

   public void resetFields()
   {
      compositeNameTextField.setText(definitionBeforeEdits.getName());
      crossRegistryCompositeCheckBox.setSelected(definitionBeforeEdits.isCrossRegistry());

      ObservableList<String> idsListItems = componentIdentifiersListView.getItems();
      while (idsListItems.size() < definitionBeforeEdits.getIdentifiers().size())
         addComponent(false);
      while (idsListItems.size() > definitionBeforeEdits.getIdentifiers().size())
         idsListItems.remove(idsListItems.size() - 1);
      for (int i = 0; i < idsListItems.size(); i++)
         idsListItems.set(i, definitionBeforeEdits.getIdentifiers().get(i));

      ObservableList<YoChartGroupModelEditorController> modelListItems = chartGroupModelEditorListView.getItems();
      while (modelListItems.size() < definitionBeforeEdits.getPreferredConfigurations().size())
         addChartGroupModel();
      while (modelListItems.size() > definitionBeforeEdits.getPreferredConfigurations().size())
         modelListItems.remove(modelListItems.size() - 1);
      for (int i = 0; i < modelListItems.size(); i++)
         modelListItems.get(i).setInput(definitionBeforeEdits.getPreferredConfigurations().get(i), definitionBeforeEdits.getIdentifiers());
   }

   private AnimationTimer prefHeightAdjustmentAnimation;

   private void setPrefHeight()
   {
      if (prefHeightAdjustmentAnimation == null)
      {
         prefHeightAdjustmentAnimation = new ObservedAnimationTimer(getClass().getSimpleName())
         {
            @Override
            public void handleImpl(long now)
            {
               if (componentIdentifiersListView.getItems().isEmpty())
               {
                  componentIdentifiersListView.setMinHeight(0.0);
                  componentIdentifiersListView.setMaxHeight(0.0);
                  componentIdentifiersListView.setPrefHeight(0.0);
               }
               else
               {
                  double minHeight = 24.0;
                  int size = componentIdentifiersListView.getItems().size();
                  componentIdentifiersListView.setMinHeight(0.5 * size * minHeight);
                  componentIdentifiersListView.setPrefHeight(size * minHeight);
                  componentIdentifiersListView.setMaxHeight(2.0 * size * minHeight);
               }

               if (chartGroupModelEditorListView.getItems().isEmpty())
               {
                  chartGroupModelEditorListView.setMinHeight(0.0);
                  chartGroupModelEditorListView.setMaxHeight(0.0);
                  chartGroupModelEditorListView.setPrefHeight(0.0);
               }
               else
               {
                  double minHeight = chartGroupModelEditorListView.getItems().get(0).getMainPane().getHeight() + 10.0;
                  chartGroupModelEditorListView.setMinHeight(minHeight);
                  chartGroupModelEditorListView.setPrefHeight(minHeight);
                  chartGroupModelEditorListView.setMaxHeight(2.0 * minHeight);
               }
            }
         };
      }

      prefHeightAdjustmentAnimation.start();
   }

   public YoCompositePatternDefinition getDefinitionBeforeEdits()
   {
      return definitionBeforeEdits;
   }

   public ObjectProperty<YoCompositePatternDefinition> patternDefinitionProperty()
   {
      return patternDefinitionProperty;
   }

   public void startEditingCompositePatternName()
   {
      compositeNameTextField.requestFocus();
   }

   @FXML
   private void addComponent()
   {
      addComponent(true);
   }

   private void addComponent(boolean startEditing)
   {
      int newItemIndex = componentIdentifiersListView.getItems().size();
      componentIdentifiersListView.getItems().add(newItemIndex, NEW_COMPONENT_IDENTIFIER + newItemIndex);
      if (startEditing)
         startEditingComponentIdentifier(newItemIndex);
   }

   public void startEditingComponentIdentifier(int editIndex)
   {
      JavaFXMissingTools.runNFramesLater(1, () ->
      {
         componentIdentifiersListView.requestFocus();
         componentIdentifiersListView.getSelectionModel().select(editIndex);
         componentIdentifiersListView.edit(editIndex);
      });
   }

   @FXML
   private void addChartGroupModel()
   {
      chartGroupModelEditorListView.getItems().add(newYoChartGroupModelEditor());
   }

   private YoChartGroupModelEditorController newYoChartGroupModelEditor()
   {
      YoChartGroupModelDefinition initialModel = new YoChartGroupModelDefinition("New Chart Group Model");
      while (initialModel.getChartIdentifiers().size() < patternDefinitionProperty.get().getIdentifiers().size())
         initialModel.getChartIdentifiers().add(new YoChartIdentifierDefinition());
      return newYoChartGroupModelEditor(initialModel);
   }

   private YoChartGroupModelEditorController newYoChartGroupModelEditor(YoChartGroupModelDefinition initialModel)
   {
      FXMLLoader loader = new FXMLLoader(SessionVisualizerIOTools.CHART_GROUP_MODEL_EDITOR_PANE_URL);
      try
      {
         loader.load();
         YoChartGroupModelEditorController editor = loader.getController();
         editor.initialize(toolkit, owner);
         if (initialModel != null)
            editor.setInput(initialModel, patternDefinitionProperty.get().getIdentifiers());
         JavaFXMissingTools.runNFramesLater(1, () -> editor.startEditingChartGroupModelName());
         return editor;
      }
      catch (IOException e)
      {
         e.printStackTrace();
         return null;
      }
   }

   public void setNameOfOtherPatterns(List<String> names)
   {
      nameOfOtherPatterns.clear();
      nameOfOtherPatterns.addAll(names);
   }

   public BooleanExpression inputsValidityProperty()
   {
      return inputsValidityProperty;
   }

   public boolean hasChangesPending()
   {
      return hasChangesPendingProperty.get();
   }

   public ReadOnlyBooleanProperty hasChangesPendingProperty()
   {
      return hasChangesPendingProperty;
   }

   @Override
   public VBox getMainPane()
   {
      return mainPane;
   }
}
