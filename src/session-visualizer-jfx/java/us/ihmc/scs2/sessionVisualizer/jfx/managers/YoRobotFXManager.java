package us.ihmc.scs2.sessionVisualizer.jfx.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.animation.AnimationTimer;
import javafx.scene.Group;
import javafx.scene.Node;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.session.Session;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.JavaFXMissingTools;
import us.ihmc.scs2.sessionVisualizer.jfx.yoRobot.YoRobotFX;

public class YoRobotFXManager extends AnimationTimer implements Manager
{
   private final Group rootNode = new Group();
   private final List<YoRobotFX> robots = new ArrayList<>();
   private final YoManager yoManager;
   private final ReferenceFrameManager referenceFrameManager;
   private final BackgroundExecutorManager backgroundExecutorManager;

   private int numberOfRobotDefinitions = -1;

   public YoRobotFXManager(YoManager yoManager, ReferenceFrameManager referenceFrameManager, BackgroundExecutorManager backgroundExecutorManager)
   {
      this.yoManager = yoManager;
      this.referenceFrameManager = referenceFrameManager;
      this.backgroundExecutorManager = backgroundExecutorManager;
   }

   public void addRobotDefinition(RobotDefinition robotDefinition)
   {
      YoRobotFX robot = new YoRobotFX(yoManager, referenceFrameManager, robotDefinition);
      robot.loadRobot(command -> backgroundExecutorManager.queueTaskToExecuteInBackground(this, command));
      robots.add(robot);
      JavaFXMissingTools.runLaterIfNeeded(() -> rootNode.getChildren().add(robot.getRootNode()));
   }

   public void addRobotDefinitions(Collection<? extends RobotDefinition> robotDefinitions)
   {
      robotDefinitions.forEach(robotDefinition -> addRobotDefinition(robotDefinition));
   }

   public void removeRobotDefinition(RobotDefinition robotDefinition)
   {
      Optional<YoRobotFX> result = robots.stream().filter(robotFX -> robotFX.getRobotDefinition() == robotDefinition).findFirst();

      if (result.isPresent())
      {
         YoRobotFX robotToRemove = result.get();
         JavaFXMissingTools.runLaterIfNeeded(() -> rootNode.getChildren().remove(robotToRemove.getRootNode()));
         robots.remove(robotToRemove);
      }
   }

   public void removeRobotDefinitions(Collection<? extends RobotDefinition> robotDefinitions)
   {
      robotDefinitions.forEach(this::removeRobotDefinition);
   }

   public void removeAllRobotDefinitions()
   {
      List<Node> nodesToDetach = robots.stream().map(YoRobotFX::getRootNode).collect(Collectors.toList());
      JavaFXMissingTools.runLaterIfNeeded(() -> rootNode.getChildren().removeAll(nodesToDetach));
      robots.clear();
   }

   @Override
   public void handle(long now)
   {
      robots.forEach(YoRobotFX::render);
   }

   @Override
   public void startSession(Session session)
   {
      start();
      List<RobotDefinition> robotDefinitions = session.getRobotDefinitions();
      numberOfRobotDefinitions = robotDefinitions.size();
      addRobotDefinitions(robotDefinitions);
   }

   @Override
   public void stopSession()
   {
      removeAllRobotDefinitions();
      numberOfRobotDefinitions = -1;
   }

   @Override
   public boolean isSessionLoaded()
   {
      if (robots.size() < numberOfRobotDefinitions)
         return false;
      return robots.stream().allMatch(YoRobotFX::isRobotLoaded);
   }

   public Group getRootNode()
   {
      return rootNode;
   }
}
