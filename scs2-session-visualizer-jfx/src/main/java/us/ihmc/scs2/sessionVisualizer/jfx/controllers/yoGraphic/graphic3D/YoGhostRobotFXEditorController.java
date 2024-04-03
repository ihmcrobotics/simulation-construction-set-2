package us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.graphic3D;

import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Pair;
import us.ihmc.scs2.definition.robot.OneDoFJointDefinition;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.robot.sdf.SDFTools;
import us.ihmc.scs2.definition.robot.sdf.items.SDFRoot;
import us.ihmc.scs2.definition.robot.urdf.URDFTools;
import us.ihmc.scs2.definition.robot.urdf.items.URDFModel;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicRobotDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicRobotDefinition.YoOneDoFJointStateDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicRobotDefinition.YoRobotStateDefinition;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerIOTools;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.editor.YoCompositeEditorPaneController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.editor.YoCompositeListEditorPaneController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.YoGraphicFXControllerTools;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.CompositePropertyTools;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.YoCompositeTools;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGhostRobotFX;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
   private YoCompositeListEditorPaneController oneDoFJointPositionListEditorController;
   @FXML
   private CheckBox enableColorCheckBox;

   private YoGraphicRobotDefinition definitionBeforeEdits;

   private final Property<RobotDefinition> lastRobotModelFromFile = new SimpleObjectProperty<>(this, "lastRobotModelFromFile", null);
   private final Property<RobotDefinition> selectedRobotModel = new SimpleObjectProperty<>(this, "selectedRobotModel", null);

   @Override
   public void initialize(SessionVisualizerToolkit toolkit, YoGhostRobotFX yoGraphicToEdit)
   {
      super.initialize(toolkit, yoGraphicToEdit);
      definitionBeforeEdits = yoGraphicToEdit.getGraphicRobotDefinition();
      if (definitionBeforeEdits == null)
         definitionBeforeEdits = new YoGraphicRobotDefinition();
      yoGraphicToEdit.visibleProperty().addListener((observable, oldValue, newValue) -> definitionBeforeEdits.setVisible(newValue));

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
            {
               setText(null);
            }
            else
            {
               setText(item.getName());
            }
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

                                           List<OneDoFJointDefinition> oneDoFJoints = robotDefinition.getAllOneDoFJoints();
                                           oneDoFJointPositionListEditorController.setElementNameProvider(i -> oneDoFJoints.get(i).getName());
                                           oneDoFJointPositionListEditorController.setNumberOfElements(oneDoFJoints.size());
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

      oneDoFJointPositionListEditorController.initializeFixedLength(YoGhostRobotFXEditorController.this.toolkit,
                                                                    yoCompositeSearchManager.getYoDoubleCollection(),
                                                                    false,
                                                                    0,
                                                                    null);
      oneDoFJointPositionListEditorController.setCompositeName("1-Dof Joint Positions", "q");
      inputsValidityProperty = Bindings.and(inputsValidityProperty, oneDoFJointPositionListEditorController.inputsValidityProperty());
      YoGraphicFXControllerTools.toSingletonDoubleSupplierListProperty(oneDoFJointPositionListEditorController.compositeListProperty())
                                .addListener((o, oldValue, newValue) ->
                                             {
                                                YoGraphicRobotDefinition newDefinition = shallowCopy();
                                                YoRobotStateDefinition newState = new YoRobotStateDefinition(newDefinition.getRobotStateDefinition());
                                                List<OneDoFJointDefinition> oneDoFJoints = newDefinition.getRobotDefinition().getAllOneDoFJoints();
                                                List<YoOneDoFJointStateDefinition> newOneDoFJointStates = new ArrayList<>(oneDoFJoints.size());
                                                for (int i = 0; i < oneDoFJoints.size(); i++)
                                                {
                                                   YoOneDoFJointStateDefinition newOneDoFJointState = new YoOneDoFJointStateDefinition();
                                                   newOneDoFJointState.setJointName(oneDoFJoints.get(i).getName());
                                                   newOneDoFJointState.setJointPosition(CompositePropertyTools.toDoublePropertyName(newValue.get(i)));
                                                   newOneDoFJointStates.add(newOneDoFJointState);
                                                }
                                                newState.setJointPositions(newOneDoFJointStates);
                                                newDefinition.setRobotStateDefinition(newState);
                                                yoGraphicToEdit.setInput(newDefinition);
                                             });
      oneDoFJointPositionListEditorController.addInputNotification(() -> updateHasChangesPendingProperty(null, null, null));

      resetFields();
   }

   private YoGraphicRobotDefinition shallowCopy()
   {
      YoGraphicRobotDefinition newDefinition = new YoGraphicRobotDefinition();
      YoGraphicRobotDefinition original = yoGraphicToEdit.getGraphicRobotDefinition();
      if (original != null)
      {
         newDefinition.setRobotDefinition(original.getRobotDefinition());
         newDefinition.setRobotStateDefinition(original.getRobotStateDefinition());
         newDefinition.setName(original.getName());
         newDefinition.setVisible(original.isVisible());
         newDefinition.setColor(original.getColor());
      }
      return newDefinition;
   }

   @Override
   public void resetFields()
   {

   }

   @Override
   public void saveChanges()
   {

   }

   @Override
   public boolean hasChangesPending()
   {
      return super.hasChangesPending();
   }

   @Override
   protected <T> void updateHasChangesPendingProperty(ObservableValue<? extends T> observable, T oldValue, T newValue)
   {

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
            urdfModel = URDFTools.loadURDFModel(result, List.of(result.getParent()));
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
}
