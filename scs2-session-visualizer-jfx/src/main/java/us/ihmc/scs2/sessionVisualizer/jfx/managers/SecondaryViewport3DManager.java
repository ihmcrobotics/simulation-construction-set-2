package us.ihmc.scs2.sessionVisualizer.jfx.managers;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.event.Event;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Sphere;
import us.ihmc.euclid.Axis3D;
import us.ihmc.javaFXExtensions.raycast.CustomPickRayTools;
import us.ihmc.scs2.session.Session;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.camera.PerspectiveCameraController;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.ObservedAnimationTimer;

public class SecondaryViewport3DManager implements SingleViewport3DManager
{
   private final Pane container;
   private final Group rootNode3D;
   private WritableImage image;
   private final ImageView cameraView;
   private final PerspectiveCamera camera;
   private final PerspectiveCameraController cameraController;

   private final SnapshotParameters snapshotParameters = new SnapshotParameters();

   private final ObservedAnimationTimer animationTimer = new ObservedAnimationTimer(getClass().getSimpleName() + " - Updater")
   {
      @Override
      public void handleImpl(long now)
      {
         update(now);
      }
   };

   public SecondaryViewport3DManager(Group mainView3DRoot,
                                     YoManager yoManager,
                                     YoCompositeSearchManager yoCompositeSearchManager,
                                     ReferenceFrameManager referenceFrameManager)
   {
      this.rootNode3D = mainView3DRoot;

      cameraView = new ImageView();
      cameraView.setPreserveRatio(true);
      cameraView.setOnMousePressed(event -> cameraView.requestFocus());

      // Embedding the view in the pane
      container = new Pane(cameraView);

      // Creating camera
      camera = new PerspectiveCamera(true);
      camera.setNearClip(0.05);
      camera.setFarClip(2.0e5);

      // Setting up the camera controller.
      cameraController = new PerspectiveCameraController(widthProperty(), heightProperty(), camera, Axis3D.Z, Axis3D.X);
      cameraController.enableShiftClickFocusTranslation(e -> CustomPickRayTools.pick(e.getX(),
                                                                                     e.getY(),
                                                                                     image.getWidth(),
                                                                                     image.getHeight(),
                                                                                     camera,
                                                                                     rootNode3D));
      cameraController.start();
      cameraView.addEventHandler(Event.ANY, cameraController);

      Sphere focusPointViz = cameraController.getFocalPointViz();
      if (focusPointViz != null)
      {
         rootNode3D.getChildren().add(focusPointViz);
         focusPointViz.visibleProperty().bind(cameraView.focusedProperty());
      }
      MainViewport3DManager.setupContextMenu(cameraController, yoCompositeSearchManager, yoManager, referenceFrameManager, cameraView);

      snapshotParameters.setCamera(camera);
      snapshotParameters.setDepthBuffer(true);
      snapshotParameters.setFill(Color.web(MainViewport3DManager.VIEWPORT_BACKGROUND_COLOR));

      animationTimer.start();
   }

   private void update(long now)
   {
      int height = (int) heightProperty().get();
      int width = (int) widthProperty().get();

      if (image == null || height != image.getHeight() || width != image.getWidth())
      {
         snapshotParameters.setViewport(new Rectangle2D(0, 0, width, height));
         image = new WritableImage(width, height);
         cameraView.setImage(image);
      }

      rootNode3D.snapshot(snapshotParameters, image);
   }

   @Override
   public void startSession(Session session)
   {
   }

   @Override
   public void stopSession()
   {
      cameraController.cameraPositionCoordinatesToTrackProperty().setValue(null);
      cameraController.cameraOrbitalCoordinatesToTrackProperty().setValue(null);
      cameraController.cameraLevelOrbitalCoordinatesToTrackProperty().setValue(null);
      cameraController.getFocalPointHandler().coordinatesToTrackProperty().set(null);
      cameraController.getFocalPointHandler().nodeToTrackProperty().set(null);
   }

   @Override
   public boolean isSessionLoaded()
   {
      return true;
   }

   @Override
   public Pane getPane()
   {
      return container;
   }

   @Override
   public PerspectiveCamera getCamera()
   {
      return camera;
   }

   @Override
   public PerspectiveCameraController getCameraController()
   {
      return cameraController;
   }

   @Override
   public void dispose()
   {
      animationTimer.stop();
      cameraController.stop();
   }

   public ReadOnlyDoubleProperty widthProperty()
   {
      return container.widthProperty();
   }

   public ReadOnlyDoubleProperty heightProperty()
   {
      return container.heightProperty();
   }
}
