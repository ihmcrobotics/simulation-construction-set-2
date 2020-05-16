package us.ihmc.scs2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.session.Session;
import us.ihmc.scs2.sessionVisualizer.managers.BackgroundExecutorManager;
import us.ihmc.scs2.sessionVisualizer.managers.ReferenceFrameManager;
import us.ihmc.scs2.sessionVisualizer.managers.YoManager;
import us.ihmc.scs2.websocket.JavalinManager;

public class YoRobotJSManager implements Manager
{
   private final List<YoRobotJS> robots = new ArrayList<>();
   private final YoManager yoManager;
   private final JavalinManager javalinManager;
   private final ReferenceFrameManager referenceFrameManager;
   private final BackgroundExecutorManager backgroundExecutorManager;
   private Future<?> activeTask;

   public YoRobotJSManager(YoManager yoManager, JavalinManager javalinManager, ReferenceFrameManager referenceFrameManager,
                           BackgroundExecutorManager backgroundExecutorManager)
   {
      this.yoManager = yoManager;
      this.javalinManager = javalinManager;
      this.referenceFrameManager = referenceFrameManager;
      this.backgroundExecutorManager = backgroundExecutorManager;
   }

   public void addRobotDefinition(RobotDefinition robotDefinition)
   {
      robots.add(new YoRobotJS(yoManager, javalinManager, referenceFrameManager, robotDefinition));
   }

   public void addRobotDefinitions(Collection<? extends RobotDefinition> robotDefinitions)
   {
      robotDefinitions.forEach(robotDefinition -> addRobotDefinition(robotDefinition));
   }

   public void removeRobotDefinition(RobotDefinition robotDefinition)
   {
      Optional<YoRobotJS> result = robots.stream().filter(robotJS -> robotJS.getRobotDefinition() == robotDefinition).findFirst();

      if (result.isPresent())
      {
         YoRobotJS robotToRemove = result.get();
         robotToRemove.detachFromScene();
         robots.remove(robotToRemove);
      }
   }

   public void removeRobotDefinitions(Collection<? extends RobotDefinition> robotDefinitions)
   {
      robotDefinitions.forEach(this::removeRobotDefinition);
   }

   public void removeAllRobotDefinitions()
   {
      robots.forEach(YoRobotJS::detachFromScene);
      robots.clear();
   }

   public void update()
   {
      robots.forEach(YoRobotJS::update);
   }

   @Override
   public void startSession(Session session)
   {
      addRobotDefinitions(session.getRobotDefinitions());
      if (activeTask == null)
         activeTask = backgroundExecutorManager.scheduleTaskInBackground(this::update, 0L, 20, TimeUnit.MILLISECONDS);
   }

   @Override
   public void stopSession()
   {
      removeAllRobotDefinitions();
      if (activeTask != null)
      {
         activeTask.cancel(false);
         activeTask = null;
      }
   }

   @Override
   public boolean isSessionLoaded()
   {
      return true;
   }
}
