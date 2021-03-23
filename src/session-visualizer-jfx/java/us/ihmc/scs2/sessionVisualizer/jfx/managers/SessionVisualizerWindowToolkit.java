package us.ihmc.scs2.sessionVisualizer.jfx.managers;

import javafx.stage.Stage;
import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerTopics;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGroupFX;

public class SessionVisualizerWindowToolkit
{
   private Stage window;
   private SessionVisualizerToolkit globalToolkit;

   public SessionVisualizerWindowToolkit(Stage window, SessionVisualizerToolkit globalToolkit)
   {
      this.window = window;
      this.globalToolkit = globalToolkit;
   }

   public Stage getWindow()
   {
      return window;
   }

   public SessionVisualizerToolkit getGlobalToolkit()
   {
      return globalToolkit;
   }

   public JavaFXMessager getMessager()
   {
      return globalToolkit.getMessager();
   }

   public SessionVisualizerTopics getTopics()
   {
      return globalToolkit.getTopics();
   }

   public YoCompositeSearchManager getYoCompositeSearchManager()
   {
      return globalToolkit.getYoCompositeSearchManager();
   }

   public BackgroundExecutorManager getBackgroundExecutorManager()
   {
      return globalToolkit.getBackgroundExecutorManager();
   }

   public ChartDataManager getChartDataManager()
   {
      return globalToolkit.getChartDataManager();
   }

   public YoManager getYoManager()
   {
      return globalToolkit.getYoManager();
   }

   public ChartRenderManager getChartRenderManager()
   {
      return globalToolkit.getChartRenderManager();
   }

   public KeyFrameManager getKeyFrameManager()
   {
      return globalToolkit.getKeyFrameManager();
   }

   public YoGroupFX getYoGraphicFXRootGroup()
   {
      return globalToolkit.getYoGraphicFXRootGroup();
   }

   public ReferenceFrameManager getReferenceFrameManager()
   {
      return globalToolkit.getReferenceFrameManager();
   }

   public YoGraphicFXManager getYoGraphicFXManager()
   {
      return globalToolkit.getYoGraphicFXManager();
   }
}
