package us.ihmc.scs2.sessionVisualizer.jfx;

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
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.YoGraphicFXControllerTools;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.MultiSessionManager;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.jfx.plotter.Plotter2D;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.CameraTools;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.JavaFXApplicationCreator;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.JavaFXMissingTools;
import us.ihmc.scs2.sessionVisualizer.jfx.xml.XMLTools;

public class SessionVisualizer
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

   public SessionVisualizer(Stage primaryStage) throws Exception
   {
      toolkit = new SessionVisualizerToolkit(primaryStage);

      FXMLLoader loader = new FXMLLoader(SessionVisualizerIOTools.MAIN_WINDOW_URL);
      Parent mainPane = loader.load();
      mainWindowController = loader.getController();
      mainWindowController.initialize(primaryStage, toolkit);

      loader = new FXMLLoader(SessionVisualizerIOTools.SIDE_PANE_URL);
      mainWindowController.setupDrawer((Pane) loader.load());
      sidePaneController = loader.getController();

      View3DFactory view3dFactory = View3DFactory.createSubscene();
      view3dFactory.addDefaultLighting();
      view3dFactory.addNodeToView(toolkit.getYoRobotFXManager().getRootNode());
      view3dFactory.addNodeToView(toolkit.getEnvironmentManager().getRootNode());
      FocusBasedCameraMouseEventHandler cameraController = view3dFactory.addCameraController(0.05, 2.0e5, true);
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

   public static void main(String[] args)
   {
      startSessionVisualizer(null);
   }

   public static void startSessionVisualizer(Session session)
   {
      JavaFXApplicationCreator.spawnJavaFXMainApplication();

      JavaFXMissingTools.runLater(SessionVisualizer.class, () ->
      {
         try
         {
            SessionVisualizer sessionVisualizer = new SessionVisualizer(new Stage());
            if (session != null)
               sessionVisualizer.startSession(session);
            JavaFXApplicationCreator.attachStopListener(sessionVisualizer::stop);
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      });
   }
}
