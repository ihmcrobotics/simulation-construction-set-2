package us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic;

import static us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.YoGraphicFXControllerTools.createAvailableYoGraphicFXItemName;

import java.util.LinkedHashMap;
import java.util.Map;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerIOTools;
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
   private ToggleButton yoGroupFXToggleButton, robotCollisionsToggleButton;
   @FXML
   private JFXTextField itemNameTextField;
   @FXML
   private JFXTextField itemNamespaceTextField;

   @FXML
   private ImageView itemNameValidImageView;

   @FXML
   private JFXButton createItemButton;

   private final ToggleGroup toggleGroup = new ToggleGroup();
   private final Stage stage = new Stage(StageStyle.UTILITY);
   private final BooleanProperty itemNameValidityProperty = new SimpleBooleanProperty(this, "itemNameValidity", false);
   private final BooleanProperty userValidatedProperty = new SimpleBooleanProperty(this, "userValidated", false);
   private final Map<Toggle, Class<? extends YoGraphicFXItem>> buttonToTypeMap = new LinkedHashMap<>();
   private final Map<Class<? extends YoGraphicFXItem>, String> typeToDefaultNameMap = new LinkedHashMap<>();

   private ReferenceFrame worldFrame;

   private YoGroupFX parent;

   public void initialize(SessionVisualizerToolkit toolkit)
   {
      worldFrame = toolkit.getReferenceFrameManager().getWorldFrame();

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

      toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) ->
      {
         Class<? extends YoGraphicFXItem> newItemType = toItemType(newValue);
         itemNameValidityProperty.set(isYoGraphicFXItemNameValid(itemNameTextField.getText(), newItemType));
         if (newItemType == null)
            itemNameTextField.setText("");
         else
            itemNameTextField.setText(createAvailableYoGraphicFXItemName(parent, typeToDefaultNameMap.get(newItemType), newItemType));
      });

      itemNameTextField.textProperty()
                       .addListener((observable,
                                     oldValue,
                                     newValue) -> itemNameValidityProperty.set(isYoGraphicFXItemNameValid(newValue,
                                                                                                          toItemType(toggleGroup.getSelectedToggle()))));
      YoGraphicFXControllerTools.bindValidityImageView(itemNameValidityProperty, itemNameValidImageView);

      createItemButton.disableProperty().bind(itemNameValidityProperty.not());

      stage.initOwner(toolkit.getMainWindow());
      Scene scene = new Scene(mainPane);

      stage.setTitle("YoGraphicFXItem creation");
      stage.getIcons().add(SessionVisualizerIOTools.SCS_ICON_IMAGE);
      stage.setScene(scene);
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

      return YoGraphicFXControllerTools.createYoGraphicFXItemAndRegister(worldFrame,
                                                                         parent,
                                                                         itemNameTextField.getText(),
                                                                         toItemType(toggleGroup.getSelectedToggle()));
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
}