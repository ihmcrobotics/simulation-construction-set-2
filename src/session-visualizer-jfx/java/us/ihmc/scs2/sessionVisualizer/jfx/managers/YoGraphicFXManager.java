package us.ihmc.scs2.sessionVisualizer.jfx.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.scene.Node;
import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.log.LogTools;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicListDefinition;
import us.ihmc.scs2.session.Session;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerTopics;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.JavaFXMissingTools;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.ObservedAnimationTimer;
import us.ihmc.scs2.sessionVisualizer.jfx.xml.XMLTools;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGraphicFXItem;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGraphicFXResourceManager;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGraphicTools;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGroupFX;

public class YoGraphicFXManager extends ObservedAnimationTimer implements Manager
{
   private final YoGroupFX root = YoGroupFX.createRoot();
   private final JavaFXMessager messager;
   private final SessionVisualizerTopics topics;
   private final YoManager yoManager;
   private final BackgroundExecutorManager backgroundExecutorManager;
   private final ReferenceFrameManager referenceFrameManager;

   // TODO Not sure if that belongs here.
   private final YoGraphicFXResourceManager yoGraphicFXResourceManager = new YoGraphicFXResourceManager();

   public YoGraphicFXManager(JavaFXMessager messager, SessionVisualizerTopics topics, YoManager yoManager, BackgroundExecutorManager backgroundExecutorManager,
                             ReferenceFrameManager referenceFrameManager)
   {
      this.messager = messager;
      this.topics = topics;
      this.yoManager = yoManager;
      this.backgroundExecutorManager = backgroundExecutorManager;
      this.referenceFrameManager = referenceFrameManager;

      messager.registerJavaFXSyncedTopicListener(topics.getYoGraphicRootGroupRequest(), this::processRootGroupRequest);
      messager.registerJavaFXSyncedTopicListener(topics.getYoGraphicLoadRequest(), this::loadYoGraphicFromFile);
      messager.registerJavaFXSyncedTopicListener(topics.getYoGraphicSaveRequest(), this::saveYoGraphicToFile);

      backgroundExecutorManager.scheduleTaskInBackground(this::computeBackground, 1000, 100, TimeUnit.MILLISECONDS);
   }

   private void computeBackground()
   {
      try
      {
         root.computeBackground();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   @Override
   public void handleImpl(long now)
   {
      root.render();
   }

   @Override
   public void startSession(Session session)
   {
      start();
   }

   @Override
   public void stopSession()
   {
      root.clear();
   }

   @Override
   public boolean isSessionLoaded()
   {
      return true;
   }

   private void processRootGroupRequest(Boolean request)
   {
      messager.submitMessage(topics.getYoGraphicRootGroupData(), root);
   }

   private void loadYoGraphicFromFile(File file)
   {
      if (XMLTools.isYoGraphicContextReady())
      {
         if (Platform.isFxApplicationThread())
            loadYoGraphicFromFileNow(file);
         else
            JavaFXMissingTools.runLater(getClass(), () -> loadYoGraphicFromFileNow(file));
      }
      else
      {
         LogTools.info("Loading file scheduled: " + file);
         backgroundExecutorManager.scheduleInBackgroundWithCondition(() -> XMLTools.isYoGraphicContextReady(),
                                                                     () -> JavaFXMissingTools.runLater(getClass(), () -> loadYoGraphicFromFileNow(file)));
      }
   }

   private void loadYoGraphicFromFileNow(File file)
   {
      if (!Platform.isFxApplicationThread())
         throw new IllegalStateException("Load must only be used from the FX Application Thread");

      LogTools.info("Loading file: " + file);
      try
      {
         YoGraphicListDefinition yoGraphicListDefinition = XMLTools.loadYoGraphicListDefinition(new FileInputStream(file));
         backgroundExecutorManager.queueTaskToExecuteInBackground(this, () ->
         {
            List<YoGraphicFXItem> items = YoGraphicTools.createYoGraphicFXs(yoManager.getRootRegistryDatabase(),
                                                                            root,
                                                                            yoGraphicFXResourceManager,
                                                                            referenceFrameManager,
                                                                            yoGraphicListDefinition);
            if (items != null && !items.isEmpty())
               JavaFXMissingTools.runLater(getClass(), () -> items.forEach(root::addYoGraphicFXItem));
         });
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   public void saveYoGraphicToFile(File file)
   {
      if (!Platform.isFxApplicationThread())
         throw new IllegalStateException("Save must only be used from the FX Application Thread");

      LogTools.info("Saving file: " + file);
      try
      {
         XMLTools.saveYoGraphicListDefinition(new FileOutputStream(file), YoGraphicTools.toYoGraphicListDefinition(root.getItemChildren()));
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   public YoGroupFX getRootGroup()
   {
      return root;
   }

   public Node getRootNode2D()
   {
      return root.getNode2D();
   }

   public Node getRootNode3D()
   {
      return root.getNode3D();
   }

   public YoGraphicFXResourceManager getYoGraphicFXResourceManager()
   {
      return yoGraphicFXResourceManager;
   }
}
