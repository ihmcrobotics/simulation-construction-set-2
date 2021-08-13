package us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic;

import static us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.YoGraphicFXControllerTools.createAvailableYoGraphicFXItemName;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.terrain.TerrainObjectDefinition;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerIOTools;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.ReferenceFrameManager;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoArrowFX3D;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoBoxFX3D;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoCapsuleFX3D;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoConeFX3D;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoCoordinateSystemFX3D;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoCylinderFX3D;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGraphicFX2D;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGraphicFX3D;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGraphicFXItem;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGraphicTools;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGroupFX;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoLineFX2D;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoPointFX2D;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoPointFX3D;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoPointcloudFX2D;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoPointcloudFX3D;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoPolygonExtrudedFX3D;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoPolygonFX2D;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoPolynomialFX3D;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoSTPBoxFX3D;

public class YoGraphicItemCreatorDialogController
{
   @FXML
   private Pane mainPane;
   @FXML
   private ToggleButton yoLineFX2DToggleButton, yoPointcloudFX2DToggleButton, yoPointFX2DToggleButton, yoPolygonFX2DToggleButton;
   @FXML
   private ToggleButton yoArrowFX3DToggleButton, yoBoxFX3DToggleButton, yoCapsuleFX3DToggleButton, yoConeFX3DToggleButton, yoCoordinateSystemFX3DToggleButton,
         yoCylinderFX3DToggleButton, yoPointcloudFX3DToggleButton, yoPointFX3DToggleButton, yoPolygonExtrudedFX3DToggleButton, yoPolynomialFX3DToggleButton,
         yoSTPBoxFX3DToggleButton;
   @FXML
   private FlowPane miscFlowPane;
   @FXML
   private ToggleButton yoGroupFXToggleButton;
   @FXML
   private JFXTextField itemNameTextField;
   @FXML
   private JFXTextField itemNamespaceTextField;
   @FXML
   private ImageView itemNameValidImageView;
   @FXML
   private JFXButton createItemButton;

   // These ToggleButtons are created on the fly when start/stop session and added to the miscFlowPane
   private final ObservableList<ToggleButton> robotCollisionsToggleButtons = FXCollections.observableArrayList();
   private ToggleButton terrainCollisionsToggleButton = null;

   private final ToggleGroup toggleGroup = new ToggleGroup();
   private final Stage stage = new Stage(StageStyle.UTILITY);
   private final BooleanProperty itemNameValidityProperty = new SimpleBooleanProperty(this, "itemNameValidity", false);
   private final BooleanProperty userValidatedProperty = new SimpleBooleanProperty(this, "userValidated", false);
   private final Map<Toggle, Class<? extends YoGraphicFXItem>> buttonToTypeMap = new LinkedHashMap<>();
   private final Map<Class<? extends YoGraphicFXItem>, String> typeToDefaultNameMap = new LinkedHashMap<>();

   private ReferenceFrame worldFrame;
   private ReferenceFrameManager referenceFrameManager;
   private ObservableList<RobotDefinition> sessionRobotDefinitions;
   private ObservableList<TerrainObjectDefinition> sessionTerrainObjectDefinitions;

   private YoGroupFX parent;

   public void initialize(SessionVisualizerToolkit toolkit)
   {
      referenceFrameManager = toolkit.getReferenceFrameManager();
      worldFrame = referenceFrameManager.getWorldFrame();
      sessionRobotDefinitions = toolkit.getSessionRobotDefinitions();
      sessionTerrainObjectDefinitions = toolkit.getSessionTerrainObjectDefinitions();

      // Buttons to types:
      // Graphic 2D:
      buttonToTypeMap.put(yoLineFX2DToggleButton, YoLineFX2D.class);
      buttonToTypeMap.put(yoPointcloudFX2DToggleButton, YoPointcloudFX2D.class);
      buttonToTypeMap.put(yoPointFX2DToggleButton, YoPointFX2D.class);
      buttonToTypeMap.put(yoPolygonFX2DToggleButton, YoPolygonFX2D.class);
      // Graphic 3D:
      buttonToTypeMap.put(yoArrowFX3DToggleButton, YoArrowFX3D.class);
      buttonToTypeMap.put(yoBoxFX3DToggleButton, YoBoxFX3D.class);
      buttonToTypeMap.put(yoCapsuleFX3DToggleButton, YoCapsuleFX3D.class);
      buttonToTypeMap.put(yoConeFX3DToggleButton, YoConeFX3D.class);
      buttonToTypeMap.put(yoCoordinateSystemFX3DToggleButton, YoCoordinateSystemFX3D.class);
      buttonToTypeMap.put(yoCylinderFX3DToggleButton, YoCylinderFX3D.class);
      buttonToTypeMap.put(yoPointcloudFX3DToggleButton, YoPointcloudFX3D.class);
      buttonToTypeMap.put(yoPointFX3DToggleButton, YoPointFX3D.class);
      buttonToTypeMap.put(yoPolygonExtrudedFX3DToggleButton, YoPolygonExtrudedFX3D.class);
      buttonToTypeMap.put(yoPolynomialFX3DToggleButton, YoPolynomialFX3D.class);
      buttonToTypeMap.put(yoSTPBoxFX3DToggleButton, YoSTPBoxFX3D.class);
      // Misc.:
      buttonToTypeMap.put(yoGroupFXToggleButton, YoGroupFX.class);

      // Default Names:
      // Graphic 2D:
      typeToDefaultNameMap.put(YoLineFX2D.class, "Line 2D");
      typeToDefaultNameMap.put(YoPointcloudFX2D.class, "Pointcloud 2D");
      typeToDefaultNameMap.put(YoPointFX2D.class, "Point 2D");
      typeToDefaultNameMap.put(YoPolygonFX2D.class, "Polygon 2D");
      // Graphic 3D:
      typeToDefaultNameMap.put(YoArrowFX3D.class, "Arrow 3D");
      typeToDefaultNameMap.put(YoBoxFX3D.class, "Box 3D");
      typeToDefaultNameMap.put(YoCapsuleFX3D.class, "Capsule 3D");
      typeToDefaultNameMap.put(YoConeFX3D.class, "Cone 3D");
      typeToDefaultNameMap.put(YoCoordinateSystemFX3D.class, "Coordinate System 3D");
      typeToDefaultNameMap.put(YoCylinderFX3D.class, "Cylinder 3D");
      typeToDefaultNameMap.put(YoPointcloudFX3D.class, "Pointcloud 3D");
      typeToDefaultNameMap.put(YoPointFX3D.class, "Point 3D");
      typeToDefaultNameMap.put(YoPolygonExtrudedFX3D.class, "Polygon Extruded 3D");
      typeToDefaultNameMap.put(YoPolynomialFX3D.class, "Polynomial 3D");
      typeToDefaultNameMap.put(YoSTPBoxFX3D.class, "STP Box 3D");
      // Misc.:
      typeToDefaultNameMap.put(YoGroupFX.class, "Group");

      buttonToTypeMap.keySet().forEach(button -> button.setToggleGroup(toggleGroup));

      sessionRobotDefinitions.addListener((ListChangeListener<RobotDefinition>) change -> refreshRobotCollisionsToggleButtons());
      refreshRobotCollisionsToggleButtons();
      sessionTerrainObjectDefinitions.addListener((ListChangeListener<TerrainObjectDefinition>) change -> refreshTerrainCollisionsToggleButton());
      refreshTerrainCollisionsToggleButton();

      toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) ->
      {
         Class<? extends YoGraphicFXItem> newItemType;
         String name;

         if (robotCollisionsToggleButtons.contains(newValue))
         {
            newItemType = YoGroupFX.class;
            name = sessionRobotDefinitions.get(robotCollisionsToggleButtons.indexOf(newValue)).getName() + " collisions";
         }
         else if (terrainCollisionsToggleButton != null && terrainCollisionsToggleButton == newValue)
         {
            newItemType = YoGroupFX.class;
            name = "Terrain collisions";
         }
         else
         {
            newItemType = toItemType(newValue);
            name = typeToDefaultNameMap.get(newItemType);
         }

         name = newItemType == null ? "" : createAvailableYoGraphicFXItemName(parent, name, newItemType);

         itemNameTextField.setText(name);
         itemNameValidityProperty.set(isYoGraphicFXItemNameValid(name, newItemType));
      });

      itemNameTextField.textProperty().addListener((observable, oldValue, newValue) ->
      {
         if (robotCollisionsToggleButtons.contains(toggleGroup.getSelectedToggle()))
            itemNameValidityProperty.set(isYoGraphicFXItemNameValid(newValue, YoGroupFX.class));
         else if (terrainCollisionsToggleButton != null && terrainCollisionsToggleButton == toggleGroup.getSelectedToggle())
            itemNameValidityProperty.set(isYoGraphicFXItemNameValid(newValue, YoGroupFX.class));
         else
            itemNameValidityProperty.set(isYoGraphicFXItemNameValid(newValue, toItemType(toggleGroup.getSelectedToggle())));
      });
      YoGraphicFXControllerTools.bindValidityImageView(itemNameValidityProperty, itemNameValidImageView);

      createItemButton.disableProperty().bind(itemNameValidityProperty.not());

      stage.initOwner(toolkit.getMainWindow());
      Scene scene = new Scene(mainPane);

      stage.setTitle("YoGraphicFXItem creation");
      SessionVisualizerIOTools.addSCSIconToWindow(stage);
      stage.setScene(scene);
   }

   private void refreshRobotCollisionsToggleButtons()
   {
      miscFlowPane.getChildren().removeAll(robotCollisionsToggleButtons);
      robotCollisionsToggleButtons.forEach(button -> button.setToggleGroup(null));

      robotCollisionsToggleButtons.clear();
      robotCollisionsToggleButtons.addAll(sessionRobotDefinitions.stream().map(this::createRobotCollisionsToggleButton).collect(Collectors.toList()));

      miscFlowPane.getChildren().addAll(robotCollisionsToggleButtons);
      robotCollisionsToggleButtons.forEach(button -> button.setToggleGroup(toggleGroup));
   }

   private void refreshTerrainCollisionsToggleButton()
   {
      if (sessionTerrainObjectDefinitions.isEmpty())
      { // There's no terrain collision, let's remove the button.
         if (terrainCollisionsToggleButton != null)
         {
            terrainCollisionsToggleButton.setToggleGroup(null);
            miscFlowPane.getChildren().remove(terrainCollisionsToggleButton);
            terrainCollisionsToggleButton = null;
         }
      }
      else
      { // There's some terrain, let's add the button.
         if (terrainCollisionsToggleButton == null)
         {
            terrainCollisionsToggleButton = createTerrainCollisionsToggleButton();
            miscFlowPane.getChildren().add(terrainCollisionsToggleButton);
            terrainCollisionsToggleButton.setToggleGroup(toggleGroup);
         }
      }
   }

   public void setParent(YoGroupFX parent)
   {
      this.parent = parent;
      userValidatedProperty.set(false);
      itemNamespaceTextField.setText(parent.getFullname());
      Class<? extends YoGraphicFXItem> itemType = toItemType(toggleGroup.getSelectedToggle());
      if (itemType != null)
      {
         String name = itemNameTextField.getText();
         if (name.trim().isEmpty())
            name = typeToDefaultNameMap.get(itemType);
         itemNameTextField.setText(createAvailableYoGraphicFXItemName(parent, name, itemType));
      }
      itemNameValidityProperty.set(isYoGraphicFXItemNameValid(itemNameTextField.getText(), itemType));
   }

   public void showAndWait()
   {
      stage.showAndWait();
   }

   public YoGraphicFXItem createItem()
   {
      if (!userValidatedProperty.get())
         return null;
      if (!itemNameValidityProperty.get())
         return null;
      if (toggleGroup.getSelectedToggle() == null)
         return null;

      Class<? extends YoGraphicFXItem> itemType = toItemType(toggleGroup.getSelectedToggle());
      if (itemType != null)
      {
         return YoGraphicFXControllerTools.createYoGraphicFXItemAndRegister(worldFrame, parent, itemNameTextField.getText(), itemType);
      }
      else if (robotCollisionsToggleButtons.contains(toggleGroup.getSelectedToggle()))
      {
         RobotDefinition robotDefinition = sessionRobotDefinitions.get(robotCollisionsToggleButtons.indexOf(toggleGroup.getSelectedToggle()));
         YoGroupFX robotCollisionShapeDefinitions = YoGraphicTools.convertRobotCollisionShapeDefinitions(referenceFrameManager, robotDefinition);
         robotCollisionShapeDefinitions.setName(itemNameTextField.getText());
         boolean success = parent.addChild(robotCollisionShapeDefinitions);
         return success ? robotCollisionShapeDefinitions : null;
      }
      else if (terrainCollisionsToggleButton != null && terrainCollisionsToggleButton == toggleGroup.getSelectedToggle())
      {
         YoGroupFX terrainCollisionShapeDefinitions = YoGraphicTools.convertTerrainObjectsCollisionShapeDefinitions(worldFrame,
                                                                                                                    sessionTerrainObjectDefinitions);
         terrainCollisionShapeDefinitions.setName(itemNameTextField.getText());
         boolean success = parent.addChild(terrainCollisionShapeDefinitions);
         return success ? terrainCollisionShapeDefinitions : null;
      }
      else
      {
         return null;
      }
   }

   private Class<? extends YoGraphicFXItem> toItemType(Toggle toggle)
   {
      if (toggle == null)
         return null;
      return buttonToTypeMap.get(toggle);
   }

   private boolean isYoGraphicFXItemNameValid(String itemName, Class<? extends YoGraphicFXItem> itemClass)
   {
      if (itemName == null || itemName.isEmpty())
         return false;
      if (itemClass == null)
         return false;

      if (YoGroupFX.class.isAssignableFrom(itemClass))
         return isYoGraphicFXGroupNameValid(itemName);
      else if (YoGraphicFX2D.class.isAssignableFrom(itemClass))
         return isYoGraphicFX2DNameValid(itemName);
      else if (YoGraphicFX3D.class.isAssignableFrom(itemClass))
         return isYoGraphicFX3DNameValid(itemName);
      else
         throw new RuntimeException("Unexpected item type: " + itemClass.getSimpleName());
   }

   private boolean isYoGraphicFXGroupNameValid(String itemName)
   {
      return !parent.containsChild(itemName);
   }

   private boolean isYoGraphicFX2DNameValid(String itemName)
   {
      return !parent.containsYoGraphicFX2D(itemName);
   }

   private boolean isYoGraphicFX3DNameValid(String itemName)
   {
      return !parent.containsYoGraphicFX3D(itemName);
   }

   @FXML
   private void cancelAndDispose(ActionEvent event)
   {
      userValidatedProperty.set(false);
      stage.close();
   }

   @FXML
   private void validateItemAndDispose(ActionEvent event)
   {
      userValidatedProperty.set(true);
      stage.close();
   }

   private ToggleButton createRobotCollisionsToggleButton(RobotDefinition robotdefinition)
   {
      FXMLLoader loader = new FXMLLoader(SessionVisualizerIOTools.YO_GRAPHIC_ROBOT_COLLISIONS_BUTTON_URL);
      try
      {
         ToggleButton button = loader.load();
         button.setText(StringUtils.capitalize(robotdefinition.getName()) + " Collisions");
         return button;
      }
      catch (IOException e)
      {
         e.printStackTrace();
         return null;
      }
   }

   private ToggleButton createTerrainCollisionsToggleButton()
   {
      FXMLLoader loader = new FXMLLoader(SessionVisualizerIOTools.YO_GRAPHIC_TERRAIN_COLLISIONS_BUTTON_URL);
      try
      {
         return loader.load();
      }
      catch (IOException e)
      {
         e.printStackTrace();
         return null;
      }
   }
}