package us.ihmc.scs2.sessionVisualizer.jfx.managers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.javafx.application.PlatformImpl;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Pair;
import us.ihmc.euclid.tools.EuclidCoreIOTools;
import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.session.Session;
import us.ihmc.scs2.sessionVisualizer.jfx.MainWindowController;
import us.ihmc.scs2.sessionVisualizer.jfx.SCSGuiConfiguration;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerIOTools;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerTopics;
import us.ihmc.scs2.sessionVisualizer.jfx.SidePaneController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.chart.YoChartGroupPanelController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.menu.YoChartMenuController;
import us.ihmc.scs2.sessionVisualizer.jfx.session.SessionControlsController;
import us.ihmc.scs2.sessionVisualizer.jfx.session.SessionInfoController;
import us.ihmc.scs2.sessionVisualizer.jfx.session.log.LogSessionManagerController;
import us.ihmc.scs2.sessionVisualizer.jfx.session.remote.RemoteSessionManagerController;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.JavaFXMissingTools;
import us.ihmc.scs2.sessionVisualizer.jfx.xml.XMLTools;

public class MultiSessionManager
{
   private final SessionVisualizerToolkit toolkit;
   private final MainWindowController mainWindowController;
   private final SidePaneController sidePaneController;

   private final Map<Class<? extends SessionControlsController>, SessionControlsController> inactiveControllerMap = new HashMap<>();
   private final ObjectProperty<SessionControlsController> activeController = new SimpleObjectProperty<>(this, "activeSessionControls", null);
   private final ObjectProperty<Session> activeSession = new SimpleObjectProperty<>(this, "activeSession", null);

   public MultiSessionManager(SessionVisualizerToolkit toolkit, MainWindowController mainWindowController, SidePaneController sidePaneController)
   {
      this.toolkit = toolkit;
      this.mainWindowController = mainWindowController;
      this.sidePaneController = sidePaneController;

      activeSession.addListener((o, oldValue, newValue) ->
      {
         PlatformImpl.runAndWait(() -> stopSession());

         if (newValue != null)
            startSession(newValue, () -> activeController.get().notifySessionLoaded());
      });

      SessionVisualizerTopics topics = toolkit.getTopics();
      JavaFXMessager messager = toolkit.getMessager();
      messager.registerJavaFXSyncedTopicListener(topics.getRemoteSessionControlsRequest(), m -> openRemoteSessionControls());
      messager.registerJavaFXSyncedTopicListener(topics.getLogSessionControlsRequest(), m -> openLogSessionControls());
   }

   public void startSession(Session session, Runnable sessionLoadedCallback)
   {
      Runnable callback = () ->
      {
         loadSessionConfiguration(session);
         if (sessionLoadedCallback != null)
            sessionLoadedCallback.run();
      };
      JavaFXMissingTools.runLaterIfNeeded(() -> toolkit.startSession(session, callback));
   }

   public void stopSession()
   {
      if (!toolkit.hasActiveSession())
         return;

      saveSessionConfiguration();
      toolkit.stopSession();
      inactiveControllerMap.values().forEach(SessionControlsController::unloadSession);
   }

   public void openRemoteSessionControls()
   {
      openSessionControls(RemoteSessionManagerController.class, SessionVisualizerIOTools.REMOTE_SESSION_MANAGER_PANE_FXML_URL);
   }

   public void openLogSessionControls()
   {
      openSessionControls(LogSessionManagerController.class, SessionVisualizerIOTools.LOG_SESSION_MANAGER_PANE_FXML_URL);
   }

   private void openSessionControls(Class<? extends SessionControlsController> controllerType, URL fxml)
   {
      SessionControlsController activeSessionControls = activeController.get();

      if (activeSessionControls != null)
      {
         if (!controllerType.isInstance(activeSessionControls))
         {
            closeSessionControls(activeSessionControls);
         }
         else
         {
            activeSessionControls.bringUp();
            return;
         }
      }

      SessionControlsController controller = inactiveControllerMap.remove(controllerType);

      if (controller == null)
      {
         try
         {
            FXMLLoader loader = new FXMLLoader(fxml);
            loader.load();
            controller = loader.getController();
            controller.initialize(toolkit);
            controller.activeSessionProperty().addListener((o, oldValue, newValue) -> activeSession.set(newValue));
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      }

      SessionInfoController sessionInfoController = controller.getSessionInfoController();

      if (sessionInfoController != null)
      {
         Pane infoPane = sessionInfoController.getMainPane();
         mainWindowController.getSceneAnchorPane().getChildren().add(infoPane);
         AnchorPane.setLeftAnchor(infoPane, 5.0);
         AnchorPane.setBottomAnchor(infoPane, 5.0);
      }

      activeController.set(controller);
      controller.bringUp();
   }

   private void closeSessionControls(SessionControlsController controller)
   {
      SessionInfoController sessionInfo = controller.getSessionInfoController();
      if (sessionInfo != null)
         mainWindowController.getSceneAnchorPane().getChildren().remove(sessionInfo.getMainPane());

      controller.getStage().close();

      if (activeController.get() != null)
         inactiveControllerMap.put(activeController.get().getClass(), activeController.get());

      if (activeController.get() == controller)
         activeController.set(null);
   }

   private String robotName;
   private String sessionName;

   public void loadSessionConfiguration(Session session)
   {
      JavaFXMessager messager = toolkit.getMessager();

      if (session.getRobotDefinitions().isEmpty())
      {
         robotName = "UnknownRobot";
      }
      else
      {
         robotName = EuclidCoreIOTools.getCollectionString("-", session.getRobotDefinitions(), RobotDefinition::getName);
      }

      sessionName = session.getSessionName();

      SCSGuiConfiguration configuration = SCSGuiConfiguration.defaultLoader(robotName, sessionName);

      if (!configuration.exists())
         return;

      SessionVisualizerTopics topics = toolkit.getTopics();
      if (configuration.hasYoGraphicsConfiguration())
         messager.submitMessage(topics.getYoGraphicLoadRequest(), configuration.getYoGraphicsConfigurationFile());

      if (configuration.hasYoCompositeConfiguration())
         messager.submitMessage(topics.getYoCompositePatternLoadRequest(), configuration.getYoCompositeConfigurationFile());

      PlatformImpl.runAndWait(() ->
      {
         if (configuration.hasYoEntryConfiguration())
            sidePaneController.getYoEntryTabPaneController().load(configuration.getYoEntryConfigurationFile());
      });

      if (configuration.hasMainYoChartGroupConfiguration())
         messager.submitMessage(topics.getYoChartGroupLoadConfiguration(),
                                new Pair<>(toolkit.getMainWindow(), configuration.getMainYoChartGroupConfigurationFile()));

      PlatformImpl.runAndWait(() ->
      {
         // TODO When the main window is already up, changing its configuration is quite unpleasant.
         //         if (configuration.hasMainWindowConfiguration())
         //            configuration.getMainWindowConfiguration(toolkit.getMainWindow());

         for (int i = 0; i < configuration.getNumberOfSecondaryYoChartGroupConfigurations(); i++)
         {
            Window secondaryWindow = YoChartMenuController.newSecondaryChartWindow(toolkit);
            File secondaryFile = configuration.getSecondaryYoChartGroupConfigurationFile(i);
            messager.submitMessage(topics.getYoChartGroupLoadConfiguration(), new Pair<>(secondaryWindow, secondaryFile));
         }
         if (configuration.hasSecondaryWindowConfigurations())
            configuration.getSecondaryWindowConfigurations(toolkit.getSecondaryWindows());

         for (Stage secondaryWindow : toolkit.getSecondaryWindows())
            secondaryWindow.show();
      });

      if (configuration.hasBufferSize())
         messager.submitMessage(topics.getYoBufferCurrentSizeRequest(), configuration.getBufferSize());
      messager.submitMessage(topics.getShowOverheadPlotter(), configuration.getShowOverheadPlotter());
      messager.submitMessage(topics.getShowAdvancedControls(), configuration.getShowAdvancedControls());
   }

   public void saveSessionConfiguration()
   {
      SCSGuiConfiguration configuration = SCSGuiConfiguration.defaultSaver(robotName, sessionName, toolkit.getSecondaryWindows().size());

      // Can't use the messager as the JavaFX is going down which prevents to save properly.
      if (XMLTools.isYoGraphicContextReady())
         toolkit.getYoGraphicFXManager().saveYoGraphicToFile(configuration.getYoGraphicsConfigurationFile());
      toolkit.getYoCompositeSearchManager().saveYoCompositePatternToFile(configuration.getYoCompositeConfigurationFile());
      sidePaneController.getYoEntryTabPaneController().exportAllTabs(configuration.getYoEntryConfigurationFile());
      mainWindowController.getYoChartGroupPanelController().saveChartGroupConfiguration(toolkit.getMainWindow(),
                                                                                        configuration.getMainYoChartGroupConfigurationFile());
      configuration.setMainStage(toolkit.getMainWindow());

      List<Stage> secondaryWindows = toolkit.getSecondaryWindows();
      List<YoChartGroupPanelController> yoChartGroupPanelControllers = toolkit.getYoChartGroupPanelControllers();

      for (int i = 0; i < secondaryWindows.size(); i++)
      {
         Window secondaryWindow = secondaryWindows.get(i);
         YoChartGroupPanelController yoChartGroupPanelController = yoChartGroupPanelControllers.get(i);
         File secondaryYoChartGroupFile = configuration.getSecondaryYoChartGroupConfigurationFile(i);

         yoChartGroupPanelController.saveChartGroupConfiguration(secondaryWindow, secondaryYoChartGroupFile);
      }

      configuration.setSecondaryWindows(secondaryWindows);

      int currentBufferSize = toolkit.getYoManager().getBufferSize();
      configuration.setBufferSize(currentBufferSize);
      configuration.setShowOverheadPlotter(mainWindowController.showOverheadPlotterProperty().getValue());
      configuration.setShowAdvancedControls(mainWindowController.showAdvancedControlsProperty().get());

      configuration.writeConfiguration();
   }

   public void shutdown()
   {
      inactiveControllerMap.values().forEach(controller -> controller.shutdown());
   }
}
