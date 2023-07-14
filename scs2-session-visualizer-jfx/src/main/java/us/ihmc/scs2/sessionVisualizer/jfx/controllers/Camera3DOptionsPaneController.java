package us.ihmc.scs2.sessionVisualizer.jfx.controllers;

import org.apache.commons.lang3.mutable.MutableBoolean;

import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.transform.Transform;
import javafx.scene.transform.TransformChangedEvent;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.camera.CameraTargetTracker;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.camera.CameraTargetTracker.TrackingTargetType;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.camera.PerspectiveCameraController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.editor.YoCompositeEditorPaneController;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.ReferenceFrameManager;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.YoCompositeSearchManager;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.Tuple3DProperty;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.YoCompositeCollection;
import us.ihmc.scs2.sharedMemory.LinkedYoRegistry;

public class Camera3DOptionsPaneController
{
   @FXML
   private RadioButton trackCoordinatesButton, trackNodeButton;
   @FXML
   private YoCompositeEditorPaneController yoCoordinateEditorController;
   @FXML
   private TextField trackingNodeTextField;
   @FXML
   private TextField xFocalPointCurrentTextField, yFocalPointCurrentTextField, zFocalPointCurrentTextField;

   private enum CameraPositionType
   {
      Position, Orbital, LevelOrbital
   };

   @FXML
   private ComboBox<CameraPositionType> cameraPositionComboxBox;

   // The following controls are for either:
   //  - x, y, z
   //  - distance, yaw, pitch
   //  - distance, yaw, z
   /** Label using for either "x" or "distance". */
   @FXML
   private Label cameraLabel1;
   /** Label using for either "y" or "yaw". */
   @FXML
   private Label cameraLabel2;
   /** Label using for either "z" or "pitch". */
   @FXML
   private Label cameraLabel3;
   /** Entry for setting either "x" or "distance". */
   @FXML
   private TextField cameraTextField1;
   /** Entry for setting either "y" or "yaw". */
   @FXML
   private TextField cameraTextField2;
   /** Entry for setting either "z" or "pitch". */
   @FXML
   private TextField cameraTextField3;
   /** Validation for either "x" or "distance". */
   @FXML
   private ImageView cameraValidImageView1;
   /** Validation for either "y" or "yaw". */
   @FXML
   private ImageView cameraValidImageView2;
   /** Validation for either "z" or "pitch". */
   @FXML
   private ImageView cameraValidImageView3;

   @FXML
   private TextField xCameraCurrentTextField, yCameraCurrentTextField, zCameraCurrentTextField;

   public void initialize(PerspectiveCameraController cameraController,
                          YoCompositeSearchManager searchManager,
                          LinkedYoRegistry linkedRootRegistry,
                          ReferenceFrameManager referenceFrameManager)
   {
      ToggleGroup toggleGroup = new ToggleGroup();
      toggleGroup.getToggles().addAll(trackCoordinatesButton, trackNodeButton); // TODO initialize which one is selected

      CameraTargetTracker targetTracker = cameraController.getTargetTracker();
      ObjectProperty<TrackingTargetType> targetTypeProperty = targetTracker.targetTypeProperty();

      trackNodeButton.setSelected(targetTypeProperty.get() == TrackingTargetType.Node);
      trackCoordinatesButton.setSelected(targetTypeProperty.get() == TrackingTargetType.YoCoordinates);

      MutableBoolean updatingTarget = new MutableBoolean(false);

      toggleGroup.selectedToggleProperty().addListener((o, oldValue, newValue) ->
      {
         if (updatingTarget.booleanValue())
            return;
         updatingTarget.setTrue();

         if (newValue == trackNodeButton)
            targetTypeProperty.set(TrackingTargetType.Node);
         else if (newValue == trackCoordinatesButton)
            targetTypeProperty.set(TrackingTargetType.YoCoordinates);
         else
            targetTypeProperty.set(TrackingTargetType.Disabled);

         updatingTarget.setFalse();
      });

      targetTypeProperty.addListener((o, oldValue, newValue) ->
      {
         if (updatingTarget.booleanValue())
            return;
         updatingTarget.setTrue();

         trackNodeButton.setSelected(targetTypeProperty.get() == TrackingTargetType.Node);
         trackCoordinatesButton.setSelected(targetTypeProperty.get() == TrackingTargetType.YoCoordinates);

         updatingTarget.setFalse();
      });

      cameraController.getFocusPointViz().localToSceneTransformProperty().addListener((o, oldValue, newValue) ->
      {
         xFocalPointCurrentTextField.setText(Double.toString(newValue.getTx()));
         yFocalPointCurrentTextField.setText(Double.toString(newValue.getTy()));
         zFocalPointCurrentTextField.setText(Double.toString(newValue.getTz()));
      });

      cameraController.getCamera().localToSceneTransformProperty().addListener((o, oldValue, newValue) ->
      {
         xCameraCurrentTextField.setText(Double.toString(newValue.getTx()));
         yCameraCurrentTextField.setText(Double.toString(newValue.getTy()));
         zCameraCurrentTextField.setText(Double.toString(newValue.getTz()));
      });

      YoCompositeCollection yoTuple3DCollection = searchManager.getYoTuple3DCollection();
      if (yoTuple3DCollection == null)
      {
         yoCoordinateEditorController.getMainPane().setDisable(true);
         // Happens when no session is loaded
      }
      else
      {
         ObjectProperty<Tuple3DProperty> coordinatesToTrack = targetTracker.coordinatesToTrackProperty();
         yoCoordinateEditorController.initialize(searchManager, referenceFrameManager, linkedRootRegistry, yoTuple3DCollection, true);
         yoCoordinateEditorController.setCompositeName("Tracking Coordinates");
         yoCoordinateEditorController.getMainPane().disableProperty().bind(trackCoordinatesButton.selectedProperty().not());
         if (coordinatesToTrack.get() != null)
            yoCoordinateEditorController.setInput(coordinatesToTrack.get());

         yoCoordinateEditorController.addInputListener((coords, frame) -> coordinatesToTrack.set(new Tuple3DProperty(frame, coords)));
      }

      trackingNodeTextField.setEditable(false); // We use this field to provide info about the currently selected Node
      trackingNodeTextField.setTooltip(new Tooltip("To select a node to track, right click on it in the 3D view and select in the context menu."));
      trackingNodeTextField.disableProperty().bind(trackNodeButton.selectedProperty().not());

      ObjectProperty<Node> nodeToTrack = targetTracker.nodeToTrackProperty();
      trackingNodeTextField.setText(nodeToTrack.get() == null ? "null" : nodeToTrack.get().getId());

      nodeToTrack.addListener((o, oldValue, newValue) ->
      {
         trackingNodeTextField.setText(newValue == null ? "null" : newValue.getId());
      });

      cameraPositionComboxBox.setItems(FXCollections.observableArrayList(CameraPositionType.values()));
      // TODO setup the position controls
   }
}
