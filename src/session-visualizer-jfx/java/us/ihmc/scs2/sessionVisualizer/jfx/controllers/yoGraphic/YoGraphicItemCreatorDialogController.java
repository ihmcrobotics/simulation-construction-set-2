package us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic;

import static us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.YoGraphicFXControllerTools.createAvailableYoGraphicFXItemName;
import static us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.YoGraphicFXControllerTools.yoGraphicFX2DTypes;
import static us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.YoGraphicFXControllerTools.yoGraphicFX3DTypes;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerIOTools;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGraphicFX2D;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGraphicFX3D;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGraphicFXItem;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGroupFX;

public class YoGraphicItemCreatorDialogController
{
   @FXML
   private Pane mainPane;
   @FXML
   private JFXTextField itemNameTextField;

   @FXML
   private JFXComboBox<String> itemTypeComboBox;

   @FXML
   private JFXTextField itemNamespaceTextField;

   @FXML
   private ImageView itemNameValidImageView;

   @FXML
   private JFXButton createItemButton;

   private final Stage stage = new Stage(StageStyle.UTILITY);
   private final BooleanProperty itemNameValidityProperty = new SimpleBooleanProperty(this, "itemNameValidity", false);
   private final BooleanProperty userValidatedProperty = new SimpleBooleanProperty(this, "userValidated", false);

   private YoGroupFX parent;

   private final Map<String, Class<? extends YoGraphicFXItem>> nameToTypeMap = new LinkedHashMap<>();

   public void initialize(SessionVisualizerToolkit toolkit, YoGroupFX parent)
   {
      this.parent = parent;

      itemNamespaceTextField.setText(parent.getFullname());

      nameToTypeMap.put(YoGroupFX.class.getSimpleName(), YoGroupFX.class);
      yoGraphicFX2DTypes.forEach(type -> nameToTypeMap.put(type.getSimpleName(), type));
      yoGraphicFX3DTypes.forEach(type -> nameToTypeMap.put(type.getSimpleName(), type));

      itemTypeComboBox.setItems(FXCollections.observableArrayList(nameToTypeMap.keySet()));

      itemTypeComboBox.valueProperty()
                      .addListener((observable, oldValue, newValue) -> itemNameValidityProperty.set(isYoGraphicFXItemNameValid(getItemName(), newValue)));
      itemTypeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> setTextFieldToDefault(newValue));
      itemNameTextField.textProperty()
                       .addListener((observable, oldValue,
                                     newValue) -> itemNameValidityProperty.set(isYoGraphicFXItemNameValid(newValue, itemTypeComboBox.getValue())));
      YoGraphicFXControllerTools.bindValidityImageView(itemNameValidityProperty, itemNameValidImageView);

      itemTypeComboBox.getSelectionModel().selectFirst();
      createItemButton.disableProperty().bind(itemNameValidityProperty.not());

      stage.initOwner(toolkit.getMainWindow());
      Scene scene = new Scene(mainPane);

      stage.setTitle("YoGraphicFXItem creation");
      stage.getIcons().add(SessionVisualizerIOTools.SCS_ICON_IMAGE);
      stage.setScene(scene);
   }

   private void setTextFieldToDefault(String itemType)
   {
      itemNameTextField.setText(createAvailableYoGraphicFXItemName(parent, itemType, toItemType(itemType)));
   }

   private Class<? extends YoGraphicFXItem> toItemType(String newValue)
   {
      return nameToTypeMap.get(newValue);
   }

   public void showAndWait()
   {
      stage.showAndWait();
   }

   public Optional<String> getItemNameResult()
   {
      return Optional.ofNullable(getItemName());
   }

   public Optional<Class<? extends YoGraphicFXItem>> getItemTypeResult()
   {
      return Optional.ofNullable(getItemType());
   }

   private String getItemName()
   {
      if (!itemNameValidityProperty.get())
         return null;
      if (!userValidatedProperty.get())
         return null;
      return itemNameTextField.getText();
   }

   private Class<? extends YoGraphicFXItem> getItemType()
   {
      if (!userValidatedProperty.get())
         return null;
      if (itemTypeComboBox.getValue() == null)
         return null;
      return nameToTypeMap.get(itemTypeComboBox.getValue());
   }

   private boolean isYoGraphicFXItemNameValid(String itemName, String itemType)
   {
      if (itemName == null || itemName.isEmpty())
         return false;
      if (itemType == null || itemType.isEmpty())
         return false;

      Class<? extends YoGraphicFXItem> itemClass = nameToTypeMap.get(itemType);

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