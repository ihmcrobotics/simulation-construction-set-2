package us.ihmc.scs2.sessionVisualizer.jfx.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.scene.Node;
import us.ihmc.log.LogTools;
import us.ihmc.messager.SynchronizeHint;
import us.ihmc.messager.javafx.JavaFXMessager;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicDefinition;
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
   private static final String SESSION_GRAPHICS = "SessionGraphics";

   private final YoGroupFX root = YoGroupFX.createGUIRoot();
   private final YoGroupFX sessionRoot = new YoGroupFX(SESSION_GRAPHICS);

   private final YoManager yoManager;
   private final BackgroundExecutorManager backgroundExecutorManager;
   private final ReferenceFrameManager referenceFrameManager;
   private Future<?> backgroundTask = null;

   // TODO Not sure if that belongs here.
   private final YoGraphicFXResourceManager yoGraphicFXResourceManager = new YoGraphicFXResourceManager();

   public YoGraphicFXManager(JavaFXMessager messager,
                             SessionVisualizerTopics topics,
                             YoManager yoManager,
                             BackgroundExecutorManager backgroundExecutorManager,
                             ReferenceFrameManager referenceFrameManager)
   {
      this.yoManager = yoManager;
      this.backgroundExecutorManager = backgroundExecutorManager;
      this.referenceFrameManager = referenceFrameManager;

      messager.addFXTopicListener(topics.getYoGraphicLoadRequest(), this::loadYoGraphicFromFile);
      messager.addFXTopicListener(topics.getYoGraphicSaveRequest(), this::saveYoGraphicToFile);
      messager.addTopicListener(topics.getRemoveYoGraphicRequest(), this::removeYoGraphic);
      messager.addTopicListener(topics.getSetYoGraphicVisibleRequest(), pair -> setYoGraphicVisible(pair.getKey(), pair.getValue()));
      messager.addTopicListener(topics.getAddYoGraphicRequest(), this::setupYoGraphicDefinition);
      messager.addTopicListener(topics.getAddYoGraphicRequest(), this::setupYoGraphicDefinition);
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
      try
      {
         root.render();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   @Override
   public void startSession(Session session)
   {
      if (!session.getYoGraphicDefinitions().isEmpty())
      {
         setupYoGraphics(new YoGraphicListDefinition(session.getYoGraphicDefinitions()),
                         sessionRoot,
                         SynchronizeHint.SYNCHRONOUS,
                         () -> root.addChild(sessionRoot));
      }

      start();
      backgroundTask = backgroundExecutorManager.scheduleTaskInBackground(this::computeBackground, 1000, 100, TimeUnit.MILLISECONDS);
   }

   @Override
   public void stopSession()
   {
      root.clear();
      sessionRoot.clear();
      stop();
      if (backgroundTask != null)
      {
         backgroundTask.cancel(false);
         backgroundTask = null;
      }
   }

   @Override
   public boolean isSessionLoaded()
   {
      return true;
   }

   public boolean removeYoGraphic(String name)
   {
      YoGraphicFXItem graphic = root.findYoGraphicFX(name);
      if (graphic == null)
         return false;
      return JavaFXMissingTools.runAndWait(getClass(), () ->
      {
         YoGroupFX parent = graphic.getParentGroup();
         if (parent == null)
            return false;
         return parent.removeYoGraphicFXItem(graphic);
      });
   }

   public boolean setYoGraphicVisible(String name, boolean visible)
   {
      YoGraphicFXItem graphic = root.findYoGraphicFX(name);
      if (graphic == null)
         return false;
      JavaFXMissingTools.runAndWait(getClass(), () -> graphic.setVisible(visible));
      return true;
   }

   private void loadYoGraphicFromFile(File file, SynchronizeHint hint)
   {
      LogTools.info("Loading file: " + file);
      try
      {
         YoGraphicListDefinition yoGraphicListDefinition = XMLTools.loadYoGraphicListDefinition(new FileInputStream(file));
         setupYoGraphics(yoGraphicListDefinition, root, hint);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   private void setupYoGraphics(YoGraphicListDefinition definition, YoGroupFX parentGroup, SynchronizeHint hint)
   {
      setupYoGraphics(definition, parentGroup, hint, null);
   }

   private void setupYoGraphics(YoGraphicListDefinition definition, YoGroupFX parentGroup, SynchronizeHint hint, Runnable postLoadingCallback)
   {
      if (hint == SynchronizeHint.SYNCHRONOUS)
      {
         List<YoGraphicFXItem> items = YoGraphicTools.createYoGraphicFXs(yoManager.getRootRegistryDatabase(),
                                                                         parentGroup,
                                                                         yoGraphicFXResourceManager,
                                                                         referenceFrameManager,
                                                                         definition);
         if (items != null && !items.isEmpty())
         {
            JavaFXMissingTools.runAndWait(getClass(), () ->
            {
               items.forEach(parentGroup::addYoGraphicFXItem);

               if (postLoadingCallback != null)
                  postLoadingCallback.run();
            });
         }
      }
      else
      {
         backgroundExecutorManager.queueTaskToExecuteInBackground(this, () ->
         {
            List<YoGraphicFXItem> items = YoGraphicTools.createYoGraphicFXs(yoManager.getRootRegistryDatabase(),
                                                                            parentGroup,
                                                                            yoGraphicFXResourceManager,
                                                                            referenceFrameManager,
                                                                            definition);
            if (items != null && !items.isEmpty())
            {
               JavaFXMissingTools.runLater(getClass(), () ->
               {
                  items.forEach(parentGroup::addYoGraphicFXItem);

                  if (postLoadingCallback != null)
                     postLoadingCallback.run();
               });
            }
         });
      }
   }

   private void setupYoGraphicDefinition(YoGraphicDefinition definition)
   {
      backgroundExecutorManager.queueTaskToExecuteInBackground(this, () ->
      {
         YoGraphicFXItem item = YoGraphicTools.createYoGraphicFX(yoManager.getRootRegistryDatabase(),
                                                                 root,
                                                                 yoGraphicFXResourceManager,
                                                                 referenceFrameManager,
                                                                 definition);
         if (item != null)
            JavaFXMissingTools.runLater(getClass(), () -> root.addYoGraphicFXItem(item));
      });
   }

   public void saveYoGraphicToFile(File file)
   {
      if (!Platform.isFxApplicationThread())
         throw new IllegalStateException("Save must only be used from the FX Application Thread");

      LogTools.info("Saving file: " + file);
      try
      {
         YoGraphicListDefinition yoGraphicListDefinition = YoGraphicTools.toYoGraphicListDefinition(root.getItemChildren()
                                                                                                        .stream()
                                                                                                        .filter(item -> item != sessionRoot)
                                                                                                        .collect(Collectors.toList()));
         XMLTools.saveYoGraphicListDefinition(new FileOutputStream(file), yoGraphicListDefinition);
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

   public YoGroupFX getSessionRootGroup()
   {
      return sessionRoot;
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
