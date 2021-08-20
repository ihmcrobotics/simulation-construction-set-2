package us.ihmc.scs2.sessionVisualizer.jfx.managers;

import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.log.LogTools;
import us.ihmc.messager.MessagerAPIFactory;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.terrain.TerrainObjectDefinition;
import us.ihmc.scs2.session.Session;
import us.ihmc.scs2.session.SessionMessagerAPI;
import us.ihmc.scs2.session.SessionState;
import us.ihmc.scs2.session.YoSharedBufferMessagerAPI;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizer;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerMessagerAPI;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerTopics;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.BufferedJavaFXMessager;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.ObservedAnimationTimer;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGroupFX;

public class SessionVisualizerToolkit extends ObservedAnimationTimer
{
   private final BufferedJavaFXMessager messager;
   private final SessionVisualizerTopics topics = new SessionVisualizerTopics();

   private final YoManager yoManager = new YoManager();
   private final ChartDataManager chartDataManager;
   private final ChartRenderManager chartRenderManager = new ChartRenderManager();
   private final YoGraphicFXManager yoGraphicFXManager;
   private final YoCompositeSearchManager yoCompositeSearchManager;
   private final SnapshotManager snapshotManager;
   private final KeyFrameManager keyFrameManager;

   private final BackgroundExecutorManager backgroundExecutorManager = new BackgroundExecutorManager(4);
   private final EnvironmentManager environmentManager = new EnvironmentManager(backgroundExecutorManager);
   private final ReferenceFrameManager referenceFrameManager = new ReferenceFrameManager(backgroundExecutorManager);
   private final YoRobotFXManager yoRobotFXManager;
   private final SecondaryWindowManager secondaryWindowManager;

   private Stage mainWindow;

   private final ObjectProperty<Session> activeSessionProperty = new SimpleObjectProperty<>(this, "activeSession", null);
   private final ObservableList<RobotDefinition> sessionRobotDefinitions = FXCollections.observableArrayList();
   private final ObservableList<TerrainObjectDefinition> sessionTerrainObjectDefinitions = FXCollections.observableArrayList();

   public SessionVisualizerToolkit(Stage mainWindow) throws Exception
   {
      this.mainWindow = mainWindow;

      MessagerAPIFactory apiFactory = new MessagerAPIFactory();
      apiFactory.createRootCategory("SCS2");
      apiFactory.includeMessagerAPIs(SessionMessagerAPI.API, YoSharedBufferMessagerAPI.API, SessionVisualizerMessagerAPI.API);
      messager = new BufferedJavaFXMessager(apiFactory.getAPIAndCloseFactory());

      topics.setupTopics();
      messager.startMessager();

      snapshotManager = new SnapshotManager(mainWindow, messager, topics);
      chartDataManager = new ChartDataManager(messager, topics, yoManager, backgroundExecutorManager);
      yoGraphicFXManager = new YoGraphicFXManager(messager, topics, yoManager, backgroundExecutorManager, referenceFrameManager);
      yoCompositeSearchManager = new YoCompositeSearchManager(messager, topics, yoManager, backgroundExecutorManager);
      keyFrameManager = new KeyFrameManager(messager, topics);
      yoRobotFXManager = new YoRobotFXManager(messager, topics, yoManager, referenceFrameManager, backgroundExecutorManager);
      secondaryWindowManager = new SecondaryWindowManager(this);

      activeSessionProperty.addListener((o, oldValue, newValue) ->
      {
         sessionRobotDefinitions.clear();
         sessionTerrainObjectDefinitions.clear();

         if (newValue == null)
            return;

         List<RobotDefinition> newRobotDefinitions = newValue.getRobotDefinitions();
         if (newRobotDefinitions != null && !newRobotDefinitions.isEmpty())
            sessionRobotDefinitions.setAll(newRobotDefinitions);

         List<TerrainObjectDefinition> newTerrainObjectDefinitions = newValue.getTerrainObjectDefinitions();
         if (newTerrainObjectDefinitions != null && !newTerrainObjectDefinitions.isEmpty())
            sessionTerrainObjectDefinitions.setAll(newTerrainObjectDefinitions);
      });
   }

   public void startSession(Session session, Runnable sessionLoadedCallback)
   {
      if (activeSessionProperty.get() != null)
      {
         LogTools.warn("Session already in progress. Stop the current session before starting a new one.");
         return;
      }

      activeSessionProperty.set(session);

      session.setupWithMessager(messager);

      backgroundExecutorManager.executeInBackground(() ->
      {
         yoManager.startSession(session);
         referenceFrameManager.startSession(session);
         yoRobotFXManager.startSession(session);
         environmentManager.startSession(session);
         chartDataManager.startSession(session);
         chartRenderManager.startSession(session);
         yoGraphicFXManager.startSession(session);
         yoCompositeSearchManager.startSession(session);
         keyFrameManager.startSession(session);
         secondaryWindowManager.startSession(session);

         while (!yoRobotFXManager.isSessionLoaded())
         {
            try
            {
               Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
               e.printStackTrace();
               return;
            }
         }

         referenceFrameManager.refreshReferenceFramesNow();
         messager.submitMessage(topics.getSessionCurrentState(), SessionState.ACTIVE);
         sessionLoadedCallback.run();
      });

      mainWindow.setTitle(session.getSessionName());
   }

   public void stopSession()
   {
      if (activeSessionProperty.get() == null)
         return;

      activeSessionProperty.set(null);

      yoManager.stopSession();
      yoRobotFXManager.stopSession();
      chartDataManager.stopSession();
      chartRenderManager.stopSession();
      yoGraphicFXManager.stopSession();
      referenceFrameManager.stopSession();
      yoCompositeSearchManager.stopSession();
      environmentManager.stopSession();
      keyFrameManager.stopSession();
      backgroundExecutorManager.stopSession();
      secondaryWindowManager.stopSession();

      mainWindow.setTitle(SessionVisualizer.NO_ACTIVE_SESSION_TITLE);

      messager.submitMessage(topics.getSessionCurrentState(), SessionState.INACTIVE);
   }

   public boolean hasActiveSession()
   {
      return activeSessionProperty.get() != null;
   }

   public Session getSession()
   {
      return activeSessionProperty.get();
   }

   @Override
   public void handleImpl(long now)
   {
   }

   @Override
   public void stop()
   {
      super.stop();
      yoRobotFXManager.stop();
      yoGraphicFXManager.stop();
      backgroundExecutorManager.shutdown();
      try
      {
         messager.closeMessager();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   public BufferedJavaFXMessager getMessager()
   {
      return messager;
   }

   public SessionVisualizerTopics getTopics()
   {
      return topics;
   }

   public Stage getMainWindow()
   {
      return mainWindow;
   }

   public YoManager getYoManager()
   {
      return yoManager;
   }

   public YoGraphicFXManager getYoGraphicFXManager()
   {
      return yoGraphicFXManager;
   }

   public YoGroupFX getYoGraphicFXRootGroup()
   {
      return yoGraphicFXManager.getRootGroup();
   }

   public YoGroupFX getYoGraphicFXSessionRootGroup()
   {
      return yoGraphicFXManager.getSessionRootGroup();
   }

   public YoRobotFXManager getYoRobotFXManager()
   {
      return yoRobotFXManager;
   }

   public ChartDataManager getChartDataManager()
   {
      return chartDataManager;
   }

   public ChartRenderManager getChartRenderManager()
   {
      return chartRenderManager;
   }

   public YoCompositeSearchManager getYoCompositeSearchManager()
   {
      return yoCompositeSearchManager;
   }

   public ReferenceFrame getWorldFrame()
   {
      return referenceFrameManager.getWorldFrame();
   }

   public ReferenceFrameManager getReferenceFrameManager()
   {
      return referenceFrameManager;
   }

   public EnvironmentManager getEnvironmentManager()
   {
      return environmentManager;
   }

   public KeyFrameManager getKeyFrameManager()
   {
      return keyFrameManager;
   }

   public SnapshotManager getSnapshotManager()
   {
      return snapshotManager;
   }

   public BackgroundExecutorManager getBackgroundExecutorManager()
   {
      return backgroundExecutorManager;
   }

   public SecondaryWindowManager getWindowManager()
   {
      return secondaryWindowManager;
   }

   public ObservableList<RobotDefinition> getSessionRobotDefinitions()
   {
      return sessionRobotDefinitions;
   }

   public ObservableList<TerrainObjectDefinition> getSessionTerrainObjectDefinitions()
   {
      return sessionTerrainObjectDefinitions;
   }
}
