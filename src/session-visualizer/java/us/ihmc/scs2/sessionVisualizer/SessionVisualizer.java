package us.ihmc.scs2.sessionVisualizer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import us.ihmc.javaFXToolkit.cameraControllers.FocusBasedCameraMouseEventHandler;
import us.ihmc.javaFXToolkit.scenes.View3DFactory;
import us.ihmc.log.LogTools;
import us.ihmc.scs2.session.Session;
import us.ihmc.scs2.sessionVisualizer.controllers.yoGraphic.YoGraphicFXControllerTools;
import us.ihmc.scs2.sessionVisualizer.managers.MultiSessionManager;
import us.ihmc.scs2.sessionVisualizer.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.plotter.Plotter2D;
import us.ihmc.scs2.sessionVisualizer.tools.CameraTools;
import us.ihmc.scs2.sessionVisualizer.tools.JavaFXMissingTools;
import us.ihmc.scs2.sessionVisualizer.xml.XMLTools;

public class SessionVisualizer extends Application
{
   public static final String NO_ACTIVE_SESSION_TITLE = "No Active Session";

   static
   {
      XMLTools.loadResources();
      YoGraphicFXControllerTools.loadResources();
   }

   private SessionVisualizerToolkit toolkit;
   private MultiSessionManager multiSessionManager;

   private SidePaneController sidePaneController;

   private final Plotter2D plotter2D = new Plotter2D();
   private MainWindowController mainWindowController;

   private double initialZoomOut = Double.NaN;

   public SessionVisualizer()
   {
   }

   @Override
   public void start(Stage primaryStage) throws Exception
   {
      toolkit = new SessionVisualizerToolkit(primaryStage);

      FXMLLoader loader = new FXMLLoader(SessionVisualizerIOTools.MAIN_WINDOW_URL);
      Parent mainPane = loader.load();
      mainWindowController = loader.getController();
      mainWindowController.initialize(toolkit);

      loader = new FXMLLoader(SessionVisualizerIOTools.SIDE_PANE_URL);
      mainWindowController.setupDrawer((Pane) loader.load());
      sidePaneController = loader.getController();

      View3DFactory view3dFactory = View3DFactory.createSubscene();
      view3dFactory.addDefaultLighting();
      view3dFactory.addNodeToView(toolkit.getYoRobotFXManager().getRootNode());
      view3dFactory.addNodeToView(toolkit.getEnvironmentManager().getRootNode());
      FocusBasedCameraMouseEventHandler cameraController = view3dFactory.addCameraController(0.05, 2.0e5, true);
      if (initialZoomOut != Double.NaN)
      {
         cameraController.changeCameraPosition(-initialZoomOut, initialZoomOut, initialZoomOut);
      }
      CameraTools.setupNodeTrackingContextMenu(cameraController, view3dFactory.getSubScene());

      toolkit.getEnvironmentManager().addWorldCoordinateSystem(0.3);
      toolkit.getEnvironmentManager().addSkybox(view3dFactory.getSubScene());

      sidePaneController.initialize(toolkit);

      mainWindowController.setupPlotter2D(plotter2D);

      view3dFactory.addNodeToView(toolkit.getYoGraphicFXManager().getRootNode3D());
      mainWindowController.setupViewport3D(view3dFactory.getSubSceneWrappedInsidePane());

      primaryStage.setOnCloseRequest(e -> stop());
      primaryStage.getIcons().add(SessionVisualizerIOTools.SCS_ICON_IMAGE);
      primaryStage.setTitle(NO_ACTIVE_SESSION_TITLE);

      Scene mainScene = new Scene(mainPane, 1024, 768);
      toolkit.getSnapshotManager().registerRecordable(mainScene);
      primaryStage.setScene(mainScene);
      multiSessionManager = new MultiSessionManager(toolkit, mainWindowController, sidePaneController);

      mainWindowController.start();
      sidePaneController.start();
      toolkit.start();
      primaryStage.show();
   }

   public void startSession(Session session)
   {
      startSession(session, null);
   }

   public void startSession(Session session, Runnable sessionLoadedCallback)
   {
      multiSessionManager.startSession(session, sessionLoadedCallback);
   }

   public void stopSession()
   {
      multiSessionManager.stopSession();
   }

   @Override
   public void stop()
   {
      LogTools.info("Simulation GUI is going down.");
      try
      {
         stopSession();
         multiSessionManager.shutdown();
         mainWindowController.stop();
         sidePaneController.stop();
         toolkit.stop();
         Platform.exit();
         System.exit(0);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   public AnchorPane getSceneAnchorPane()
   {
      return mainWindowController.getSceneAnchorPane();
   }

   public SessionVisualizerToolkit getToolkit()
   {
      return toolkit;
   }

   public void setInitialZoomOut(double initialZoomOut)
   {
      this.initialZoomOut = initialZoomOut;
   }

   public static void main(String[] args)
   {
      launch(args);
   }

   public static void startSessionVisualizer(Session session)
   {
      SessionVisualizer sessionVisualizer = new SessionVisualizer();
      JavaFXMissingTools.runApplication(sessionVisualizer, () -> sessionVisualizer.startSession(session));
   }
}
