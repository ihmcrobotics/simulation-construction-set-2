package us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.graphic3D;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Pair;
import org.jetbrains.annotations.NotNull;
import us.ihmc.log.LogTools;
import us.ihmc.scs2.definition.robot.OneDoFJointDefinition;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.robot.sdf.SDFTools;
import us.ihmc.scs2.definition.robot.sdf.items.SDFRoot;
import us.ihmc.scs2.definition.robot.urdf.URDFTools;
import us.ihmc.scs2.definition.robot.urdf.items.URDFModel;
import us.ihmc.scs2.definition.yoComposite.YoCompositePatternDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicRobotDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicRobotDefinition.YoOneDoFJointStateDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicRobotDefinition.YoRobotStateDefinition;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerIOTools;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.editor.YoCompositeEditorPaneController;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.CompositePropertyTools;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.JavaFXMissingTools;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.YoCompositeCollection;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.YoCompositePattern;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.YoCompositeTools;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGhostRobotFX;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGraphicTools;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.color.BaseColorFX;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class YoGhostRobotFXEditorController extends YoGraphicFX3DEditorController<YoGhostRobotFX>
{
   private static final String ROBOT_MODEL_PATH_KEY = "ROBOT_MODEL_PATH";

   @FXML
   private RadioButton robotModelFromSessionRadioButton, robotModelFromFileRadioButton;
   @FXML
   private ComboBox<RobotDefinition> sessionRobotModelsComboBox;
   @FXML
   private TextField loadedRobotModelFileTextField;
   @FXML
   private Button browseRobotModelsButton;
   @FXML
   private YoCompositeEditorPaneController rootJointPositionEditorController;
   @FXML
   private YoCompositeEditorPaneController rootJointOrientationEditorController;
   @FXML
   private Label oneDoFJointsPlaceholderLabel; // We use this label to keep track of where the actual controller goes.
   private YoCompositeEditorPaneController oneDoFJointPositionsEditorController;
   @FXML
   private CheckBox enableColorCheckBox;

   private YoGraphicRobotDefinition definitionBeforeEdits;

   private final Property<RobotDefinition> lastRobotModelFromFile = new SimpleObjectProperty<>(this, "lastRobotModelFromFile", null);
   private final Property<RobotDefinition> selectedRobotModel = new SimpleObjectProperty<>(this, "selectedRobotModel", null);

   private final Property<YoCompositePattern> oneDoFJointPattern = new SimpleObjectProperty<>(this, "previousOneDoFJointPattern", null);

   private final BooleanProperty oneDoFJointPositionValidityProperty = new SimpleBooleanProperty(this, "oneDoFJointPositionValidity", false);

   @Override
   public void initialize(SessionVisualizerToolkit toolkit, YoGhostRobotFX yoGraphicToEdit)
   {
      super.initialize(toolkit, yoGraphicToEdit);
      definitionBeforeEdits = YoGraphicTools.toYoGraphicRobotDefinition(yoGraphicToEdit);
      yoGraphicToEdit.visibleProperty().addListener((observable, oldValue, newValue) -> definitionBeforeEdits.setVisible(newValue));

      robotModelFromSessionRadioButton.setSelected(true);
      new ToggleGroup().getToggles().addAll(robotModelFromSessionRadioButton, robotModelFromFileRadioButton);
      sessionRobotModelsComboBox.disableProperty().bind(robotModelFromFileRadioButton.selectedProperty());
      loadedRobotModelFileTextField.disableProperty().bind(robotModelFromSessionRadioButton.selectedProperty());

      browseRobotModelsButton.disableProperty().bind(robotModelFromSessionRadioButton.selectedProperty());

      sessionRobotModelsComboBox.getItems().setAll(toolkit.getSessionRobotDefinitions());
      if (definitionBeforeEdits.getRobotDefinition() != null)
         sessionRobotModelsComboBox.getSelectionModel().select(definitionBeforeEdits.getRobotDefinition());
      sessionRobotModelsComboBox.setCellFactory(param -> new ListCell<>()
      {
         @Override
         protected void updateItem(RobotDefinition item, boolean empty)
         {
            super.updateItem(item, empty);
            if (empty || item == null)
               setText(null);
            else
               setText(item.getName());
         }
      });

      sessionRobotModelsComboBox.getSelectionModel().selectedItemProperty().addListener((o, oldValue, newValue) -> selectedRobotModel.setValue(newValue));
      robotModelFromSessionRadioButton.selectedProperty().addListener((o, oldValue, newValue) ->
                                                                      {
                                                                         if (newValue)
                                                                         {
                                                                            selectedRobotModel.setValue(sessionRobotModelsComboBox.getSelectionModel()
                                                                                                                                  .getSelectedItem());
                                                                         }
                                                                      });
      robotModelFromFileRadioButton.selectedProperty().addListener((o, oldValue, newValue) ->
                                                                   {
                                                                      if (newValue)
                                                                         selectedRobotModel.setValue(lastRobotModelFromFile.getValue());
                                                                   });
      browseRobotModelsButton.setOnAction(e ->
                                          {
                                             Pair<File, RobotDefinition> fileRobotDefinitionPair = loadRobotModelFromFile();
                                             if (fileRobotDefinitionPair != null)
                                             {
                                                lastRobotModelFromFile.setValue(fileRobotDefinitionPair.getValue());
                                                loadedRobotModelFileTextField.setText(fileRobotDefinitionPair.getKey().getAbsolutePath());
                                                selectedRobotModel.setValue(fileRobotDefinitionPair.getValue());
                                             }
                                          });

      selectedRobotModel.addListener((o, oldValue, newValue) ->
                                     {
                                        if (newValue != null)
                                        {
                                           YoGraphicRobotDefinition newDefinition = shallowCopy();
                                           RobotDefinition robotDefinition = new RobotDefinition(newValue);
                                           newDefinition.setRobotDefinition(robotDefinition);
                                           yoGraphicToEdit.setInput(newDefinition);

                                           if (newValue != null)
                                           {
                                              YoCompositePatternDefinition oneDoFJointPatternDefinition = createOneDoFJointPatternDefinition(newValue);

                                              String newPatternType = oneDoFJointPatternDefinition.getName();
                                              if (oldValue != null)
                                              {
                                                 YoCompositePatternDefinition oldPatternDefinition = createOneDoFJointPatternDefinition(oldValue);
                                                 if (!Objects.equals(newPatternType, oldPatternDefinition.getName()))
                                                    yoCompositeSearchManager.discardYoComposite(oldPatternDefinition.getName());
                                              }

                                              if (yoCompositeSearchManager.getCollectionFromType(newPatternType) == null)
                                              {
                                                 yoCompositeSearchManager.searchYoCompositeInBackground(YoCompositeTools.toYoCompositePattern(
                                                       oneDoFJointPatternDefinition), this::setupOneDoFJointPositionEditor);
                                              }
                                              else
                                              {
                                                 setupOneDoFJointPositionEditor(yoCompositeSearchManager.getCollectionFromType(newPatternType));
                                              }
                                           }
                                        }
                                     });

      setupCompositePropertyEditor(rootJointPositionEditorController, "Root Joint Position", false, YoCompositeTools.YO_TUPLE3D, null);
      setupCompositePropertyEditor(rootJointOrientationEditorController, "Root Joint Orientation", false, YoCompositeTools.YO_QUATERNION, null);

      rootJointPositionEditorController.addInputListener(coordinates ->
                                                         {
                                                            YoGraphicRobotDefinition newDefinition = shallowCopy();
                                                            YoRobotStateDefinition newState = new YoRobotStateDefinition(newDefinition.getRobotStateDefinition());
                                                            newState.setRootJointPosition(CompositePropertyTools.toYoTuple3DDefinition(coordinates));
                                                            newDefinition.setRobotStateDefinition(newState);
                                                            yoGraphicToEdit.setInput(newDefinition);
                                                         });
      rootJointOrientationEditorController.addInputListener(orientation ->
                                                            {
                                                               YoGraphicRobotDefinition newDefinition = shallowCopy();
                                                               YoRobotStateDefinition newState = new YoRobotStateDefinition(newDefinition.getRobotStateDefinition());
                                                               newState.setRootJointOrientation(CompositePropertyTools.toYoQuaternionDefinition(orientation));
                                                               newDefinition.setRobotStateDefinition(newState);
                                                               yoGraphicToEdit.setInput(newDefinition);
                                                            });

      inputsValidityProperty = Bindings.and(inputsValidityProperty, oneDoFJointPositionValidityProperty);

      enableColorCheckBox.setSelected(definitionBeforeEdits.getColor() != null);
      styleEditorController.getMainPane().setDisable(!enableColorCheckBox.isSelected());
      styleEditorController.bindYoGraphicFX3D(yoGraphicToEdit);
      enableColorCheckBox.selectedProperty().addListener((o, oldValue, newValue) ->
                                                         {
                                                            styleEditorController.getMainPane().setDisable(!newValue);
                                                            if (!newValue)
                                                               yoGraphicToEdit.setColor((BaseColorFX) null);
                                                            else
                                                               yoGraphicToEdit.setColor(styleEditorController.colorProperty().get());
                                                         });

      resetFields();
   }

   private void setupOneDoFJointPositionEditor(YoCompositeCollection collection)
   {
      JavaFXMissingTools.runLaterIfNeeded(getClass(), () -> setupOneDoFJointPositionEditorNow(collection));
   }

   private void setupOneDoFJointPositionEditorNow(YoCompositeCollection collection)
   {
      int index;
      if (oneDoFJointPositionsEditorController != null)
         index = mainPane.getChildren().indexOf(oneDoFJointPositionsEditorController.getMainPane());
      else
         index = mainPane.getChildren().indexOf(oneDoFJointsPlaceholderLabel);

      FXMLLoader fxmlLoader = new FXMLLoader(SessionVisualizerIOTools.YO_COMPOSITE_EDITOR_URL);
      try
      {
         fxmlLoader.load();
         oneDoFJointPositionsEditorController = fxmlLoader.getController();
         setupCompositePropertyEditor(oneDoFJointPositionsEditorController, "One DoF Joint Positions", false, collection.getPattern().getType(), null);

         oneDoFJointPositionsEditorController.addInputListener(positions ->
                                                               {
                                                                  YoGraphicRobotDefinition newDefinition = shallowCopy();
                                                                  YoRobotStateDefinition newState = new YoRobotStateDefinition(newDefinition.getRobotStateDefinition());
                                                                  List<OneDoFJointDefinition> oneDoFJoints = newDefinition.getRobotDefinition()
                                                                                                                          .getAllOneDoFJoints();
                                                                  if (oneDoFJoints.size() != positions.length)
                                                                  {
                                                                     LogTools.warn("Mismatch in the number of 1-DoF joints: expected={}, actual={}",
                                                                                   oneDoFJoints.size(),
                                                                                   positions.length);
                                                                     return;
                                                                  }

                                                                  List<YoOneDoFJointStateDefinition> newOneDoFJointStates = new ArrayList<>(oneDoFJoints.size());
                                                                  for (int i = 0; i < oneDoFJoints.size(); i++)
                                                                  {
                                                                     YoOneDoFJointStateDefinition newOneDoFJointState = new YoOneDoFJointStateDefinition();
                                                                     newOneDoFJointState.setJointName(oneDoFJoints.get(i).getName());
                                                                     newOneDoFJointState.setJointPosition(CompositePropertyTools.toDoublePropertyName(positions[i]));
                                                                     newOneDoFJointStates.add(newOneDoFJointState);
                                                                  }
                                                                  newState.setJointPositions(newOneDoFJointStates);
                                                                  newDefinition.setRobotStateDefinition(newState);
                                                                  yoGraphicToEdit.setInput(newDefinition);
                                                               });
         oneDoFJointPositionsEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));
         oneDoFJointPositionValidityProperty.bind(oneDoFJointPositionsEditorController.inputsValidityProperty());

         mainPane.getChildren().set(index, oneDoFJointPositionsEditorController.getMainPane());
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   @NotNull
   private static YoCompositePatternDefinition createOneDoFJointPatternDefinition(RobotDefinition newValue)
   {
      YoCompositePatternDefinition oneDoFJointPatternDefinition = new YoCompositePatternDefinition();
      List<OneDoFJointDefinition> oneDoFJoints = newValue.getAllOneDoFJoints();
      oneDoFJointPatternDefinition.setName(newValue.getName() + "OneDoFJoints[%d]".formatted(oneDoFJoints.size()));
      oneDoFJointPatternDefinition.setIdentifiers(oneDoFJoints.stream().map(OneDoFJointDefinition::getName).toArray(String[]::new));
      oneDoFJointPatternDefinition.setCrossRegistry(true);
      return oneDoFJointPatternDefinition;
   }

   private YoGraphicRobotDefinition shallowCopy()
   {
      if (yoGraphicToEdit.getGraphicRobotDefinition() != null)
      {
         YoGraphicRobotDefinition newDefinition = new YoGraphicRobotDefinition();
         YoGraphicRobotDefinition original = yoGraphicToEdit.getGraphicRobotDefinition();
         newDefinition.setRobotDefinition(original.getRobotDefinition());
         newDefinition.setRobotStateDefinition(original.getRobotStateDefinition());
         newDefinition.setName(original.getName());
         newDefinition.setVisible(original.isVisible());
         newDefinition.setColor(original.getColor());
         return newDefinition;
      }
      else
      {
         return YoGraphicTools.toYoGraphicRobotDefinition(yoGraphicToEdit);
      }
   }

   @Override
   public void resetFields()
   {
      styleEditorController.setInput(definitionBeforeEdits);
      nameEditorController.setInput(definitionBeforeEdits.getName(), yoGraphicToEdit.getNamespace());
   }

   @Override
   public void saveChanges()
   {
      definitionBeforeEdits = YoGraphicTools.toYoGraphicRobotDefinition(yoGraphicToEdit);
      hasChangesPendingProperty.set(false);
   }

   @Override
   public boolean hasChangesPending()
   {
      return super.hasChangesPending();
   }

   @Override
   protected <T> void updateHasChangesPendingProperty(ObservableValue<? extends T> observable, T oldValue, T newValue)
   {
      hasChangesPendingProperty.set(!definitionBeforeEdits.equals(yoGraphicToEdit.getGraphicRobotDefinition()));
   }

   private Pair<File, RobotDefinition> loadRobotModelFromFile()
   {
      File result = SessionVisualizerIOTools.showOpenDialog(toolkit.getMainWindow(),
                                                            "Load Robot Model",
                                                            List.of(new ExtensionFilter("All Model Files", "*.urdf", "*.sdf"),
                                                                    new ExtensionFilter("URDF Files", "*.urdf"),
                                                                    new ExtensionFilter("SDF Files", "*.sdf")),
                                                            ROBOT_MODEL_PATH_KEY);
      if (result == null)
         return null;
      if (result.getName().toLowerCase().endsWith("urdf"))
      {
         URDFModel urdfModel = null;
         try
         {
            urdfModel = URDFTools.loadURDFModel(result, createResourceDirectories(result));
         }
         catch (JAXBException e)
         {
            throw new RuntimeException(e);
         }
         return new Pair<>(result, URDFTools.toRobotDefinition(urdfModel));
      }
      else if (result.getName().toLowerCase().endsWith("sdf"))
      {
         SDFRoot sdfRoot = null;
         try
         {
            sdfRoot = SDFTools.loadSDFRoot(result);
         }
         catch (JAXBException e)
         {
            throw new RuntimeException(e);
         }
         return new Pair<>(result, SDFTools.toFloatingRobotDefinition(sdfRoot.getModels().get(0)));
      }
      else
      {
         return null;
      }
   }

   @NotNull
   private static List<String> createResourceDirectories(File modelFile)
   {
      List<String> resourceDirectories = new ArrayList<>();
      resourceDirectories.add(modelFile.getParent());

      File candidate = modelFile.getParentFile();

      for (int i = 0; i < 3; i++)
      {
         candidate = candidate.getParentFile();

         if (candidate == null)
            break;
         if (candidate.getName().equals("models"))
         {
            resourceDirectories.add(candidate.getAbsolutePath());
            break;
         }
      }
      return resourceDirectories;
   }
}
