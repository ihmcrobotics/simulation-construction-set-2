package us.ihmc.scs2.sessionVisualizer.jfx.managers;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.log.LogTools;
import us.ihmc.messager.MessagerAPIFactory;
import us.ihmc.scs2.session.Session;
import us.ihmc.scs2.session.SessionMessagerAPI;
import us.ihmc.scs2.session.SessionState;
import us.ihmc.scs2.session.YoSharedBufferMessagerAPI;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizer;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerMessagerAPI;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerTopics;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.chart.YoChartGroupPanelController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.sliderboard.bcf2000.YoBCF2000SliderboardWindowController;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.BufferedJavaFXMessager;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.ObservedAnimationTimer;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGroupFX;

public class SessionVisualizerToolkit extends ObservedAnimationTimer
{
   private final JavaFXMessager messager;
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
   private final YoRobotFXManager yoRobotFXManager = new YoRobotFXManager(yoManager, referenceFrameManager, backgroundExecutorManager);

   private Stage mainWindow;
   private List<Stage> secondaryWindows = new ArrayList<>(); // TODO Only here to trigger events when start/close a session, should be done via the messager.
   private List<YoChartGroupPanelController> yoChartGroupPanelControllers = new ArrayList<>(); // TODO Only here to trigger events when start/close a session, should be done via the messager.
   private YoBCF2000SliderboardWindowController yoSliderboardWindowController; // TODO Only here to trigger events when start/close a session, should be done via the messager.

   private final ObjectProperty<Session> activeSessionProperty = new SimpleObjectProperty<>(this, "activeSession", null);

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

      for (int i = secondaryWindows.size() - 1; i >= 0; i--)
      {
         Stage secondaryWindow = secondaryWindows.get(i);
         secondaryWindow.close();
         secondaryWindow.fireEvent(new WindowEvent(secondaryWindow, WindowEvent.WINDOW_CLOSE_REQUEST));
      }

      secondaryWindows.clear();
      yoChartGroupPanelControllers.clear();

      if (yoSliderboardWindowController != null)
      {
         yoSliderboardWindowController.close();
         yoSliderboardWindowController = null;
      }

      mainWindow.setTitle(SessionVisualizer.NO_ACTIVE_SESSION_TITLE);

      messager.submitMessage(topics.getSessionCurrentState(), SessionState.INACTIVE);
   }

   public boolean hasActiveSession()
   {
      return activeSessionProperty.get() != null;
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

   public void addSecondaryWindow(Stage secondaryWindow)
   {
      secondaryWindows.add(secondaryWindow);
   }

   public void removeSecondaryWindow(Stage secondaryWindow)
   {
      secondaryWindows.remove(secondaryWindow);
   }

   public void addYoChartGroupController(YoChartGroupPanelController controller)
   {
      yoChartGroupPanelControllers.add(controller);
   }

   public void removeYoChartGroupController(YoChartGroupPanelController controller)
   {
      yoChartGroupPanelControllers.remove(controller);
   }

   public void setYoSliderboardWindowController(YoBCF2000SliderboardWindowController yoSliderboardWindowController)
   {
      this.yoSliderboardWindowController = yoSliderboardWindowController;
   }

   public JavaFXMessager getMessager()
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

   public List<Stage> getSecondaryWindows()
   {
      return secondaryWindows;
   }

   public List<YoChartGroupPanelController> getYoChartGroupPanelControllers()
   {
      return yoChartGroupPanelControllers;
   }

   public YoBCF2000SliderboardWindowController getYoSliderboardWindowController()
   {
      return yoSliderboardWindowController;
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
}
