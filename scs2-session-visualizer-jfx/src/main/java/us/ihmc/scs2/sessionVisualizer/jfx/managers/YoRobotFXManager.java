package us.ihmc.scs2.sessionVisualizer.jfx.managers;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import us.ihmc.log.LogTools;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyReadOnly;
import us.ihmc.messager.javafx.JavaFXMessager;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.robot.SixDoFJointDefinition;
import us.ihmc.scs2.session.Session;
import us.ihmc.scs2.session.SessionRobotDefinitionListChange;
import us.ihmc.scs2.sessionVisualizer.jfx.Camera3DRequest;
import us.ihmc.scs2.sessionVisualizer.jfx.Camera3DRequest.FocalPointRequest;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerTopics;
import us.ihmc.scs2.sessionVisualizer.jfx.multiBodySystem.FrameNode;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.JavaFXMissingTools;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.ObservedAnimationTimer;
import us.ihmc.scs2.sessionVisualizer.jfx.yoRobot.NewRobotVisualRequest;
import us.ihmc.scs2.sessionVisualizer.jfx.yoRobot.YoRobotFX;
import us.ihmc.yoVariables.exceptions.IllegalOperationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class YoRobotFXManager extends ObservedAnimationTimer implements Manager
{
   private final Group rootNode = new Group();
   private final List<YoRobotFX> robots = new ArrayList<>();
   private final ObservableList<RobotDefinition> robotDefinitions = FXCollections.observableArrayList();

   private int numberOfRobotDefinitions = -1;

   public YoRobotFXManager(JavaFXMessager messager,
                           SessionVisualizerTopics topics,
                           YoManager yoManager,
                           ReferenceFrameManager referenceFrameManager,
                           BackgroundExecutorManager backgroundExecutorManager)
   {
      robotDefinitions.addListener(new ListChangeListener<>()
      {
         @Override
         public void onChanged(Change<? extends RobotDefinition> c)
         {
            while (c.next())
            {
               if (c.wasAdded())
               {
                  List<YoRobotFX> robotsToAttach = c.getAddedSubList().stream().map(this::newYoRobotFX).toList();

                  JavaFXMissingTools.runLaterIfNeeded(getClass(), () ->
                  {
                     robots.addAll(robotsToAttach);
                     rootNode.getChildren().addAll(robotsToAttach.stream().map(YoRobotFX::getRootNode).toList());
                     if (robots.size() == 1)
                     {
                        YoRobotFX robotToTrack = robots.get(0);
                        SixDoFJointDefinition rootJoint = robotDefinitions.get(0).getFloatingRootJointDefinition();
                        if (rootJoint != null)
                        {
                           String mainBody = rootJoint.getSuccessor().getName();
                           backgroundExecutorManager.scheduleInBackgroundWithCondition(() -> isSessionLoaded(), () ->
                           {
                              FrameNode rigidBodyFrameNode = robotToTrack.findRigidBodyFrameNode(mainBody);
                              if (rigidBodyFrameNode != null && rigidBodyFrameNode.getNode() != null)
                              {
                                 messager.submitMessage(topics.getCamera3DRequest(),
                                                        new Camera3DRequest(FocalPointRequest.trackNode(rigidBodyFrameNode.getNode())));
                              }
                           });
                        }
                     }
                  });
               }
               if (c.wasRemoved())
               {
                  List<YoRobotFX> robotsToRemove = c.getRemoved()
                                                    .stream()
                                                    .map(robotDefinition -> getRobotFX(robotDefinition))
                                                    .filter(Optional::isPresent)
                                                    .map(Optional::get)
                                                    .toList();
                  JavaFXMissingTools.runLaterIfNeeded(getClass(), () ->
                  {
                     rootNode.getChildren().removeAll(robotsToRemove.stream().map(YoRobotFX::getRootNode).toList());
                     robots.removeAll(robotsToRemove);
                  });
               }
            }
         }

         private YoRobotFX newYoRobotFX(RobotDefinition robotDefinition)
         {
            YoRobotFX robot = new YoRobotFX(yoManager, referenceFrameManager, robotDefinition);
            robot.loadRobot(command -> backgroundExecutorManager.queueTaskToExecuteInBackground(this, command));
            return robot;
         }
      });

      messager.addTopicListener(topics.getCamera3DRequest(), request ->
      {
         if (!isSessionLoaded())
            throw new IllegalOperationException("Session has not been loaded yet.");

         FocalPointRequest focalPointRequest = request.getFocalPointRequest();
         if (focalPointRequest == null)
            return;

         String rigidBodyName = focalPointRequest.getRigidBodyName();
         String robotName = focalPointRequest.getRobotName();

         if (rigidBodyName != null)
         {
            Optional<FrameNode> result;

            if (robotName != null)
            {
               result = getRobotFX(robotName).map(r -> r.findRigidBodyFrameNode(rigidBodyName));
            }
            else
            {
               result = robots.stream().map(r -> r.findRigidBodyFrameNode(rigidBodyName)).filter(Objects::nonNull).findFirst();
            }

            result.ifPresent(rigidBodyFrameNode ->
                             {
                                if (rigidBodyFrameNode.getNode() != null)
                                   messager.submitMessage(topics.getCamera3DRequest(),
                                                          new Camera3DRequest(FocalPointRequest.trackNode(rigidBodyFrameNode.getNode())));
                             });
         }
      });

      messager.addTopicListener(topics.getRobotVisualRequest(), this::handleRobotVisualRequest);
      messager.addFXTopicListener(topics.getSessionRobotDefinitionListChangeState(), this::handleSessionRobotDefinitionListChangeState);
   }

   private void handleSessionRobotDefinitionListChangeState(SessionRobotDefinitionListChange change)
   {
      if (change.getAddedRobotDefinition() == null)
      {
         LogTools.warn("Received request but robot definition is null, ignoring.");
         return;
      }

      switch (change.getChangeType())
      {
         case ADD:
         {
            robotDefinitions.add(change.getAddedRobotDefinition());
            break;
         }
         case REMOVE:
         {
            robotDefinitions.remove(change.getRemovedRobotDefinition());
            break;
         }
         case REPLACE:
         {
            robotDefinitions.remove(change.getRemovedRobotDefinition());
            robotDefinitions.add(change.getAddedRobotDefinition());
            break;
         }
      }
   }

   private void handleRobotVisualRequest(NewRobotVisualRequest request)
   {
      String robotName = request.getRobotName();
      if (robotName == null)
      {
         LogTools.warn("Received request but robot name is null, ignoring.");
         return;
      }

      if (robotName.equals(NewRobotVisualRequest.ALL_ROBOTS))
      {
         if (request.getRequestedVisible() != null)
            robots.forEach(robot -> robot.getRootNode().setVisible(request.getRequestedVisible()));
         if (request.getRequestedDrawMode() != null)
            robots.forEach(robot -> robot.setDrawMode(request.getRequestedDrawMode()));
      }
      else
      {
         YoRobotFX robot = robots.stream().filter(r -> r.getRobotDefinition().getName().equalsIgnoreCase(robotName)).findFirst().orElse(null);

         if (robot == null)
         {
            LogTools.warn("Could not find robot named: {}, ignoring request.", robotName);
            return;
         }

         if (request.getRequestedVisible() != null)
            robot.getRootNode().setVisible(request.getRequestedVisible());
         if (request.getRequestedDrawMode() != null)
            robot.setDrawMode(request.getRequestedDrawMode());
      }
   }

   /**
    * Returns the list of {@link RobotDefinition} that are currently being rendered.
    * <p>
    * Any modification to this list will be reflected in the rendering.
    * </p>
    *
    * @return the list of {@link RobotDefinition} that are currently being rendered.
    */
   public ObservableList<RobotDefinition> getRobotDefinitions()
   {
      return robotDefinitions;
   }

   @Override
   public void handleImpl(long now)
   {
      robots.forEach(YoRobotFX::render);
   }

   @Override
   public void startSession(Session session)
   {
      start();
      List<RobotDefinition> robotDefinitions = session.getRobotDefinitions();
      numberOfRobotDefinitions = robotDefinitions.size();
      this.robotDefinitions.addAll(robotDefinitions);
   }

   @Override
   public void stopSession()
   {
      robotDefinitions.clear();
      numberOfRobotDefinitions = -1;
   }

   @Override
   public boolean isSessionLoaded()
   {
      if (robots.size() < numberOfRobotDefinitions)
         return false;
      return robots.stream().allMatch(YoRobotFX::isRobotLoaded);
   }

   public RigidBodyReadOnly getRobotRootBody(String robotName)
   {
      return getRobotFX(robotName).map(YoRobotFX::getRootBody).orElse(null);
   }

   private Optional<YoRobotFX> getRobotFX(RobotDefinition robotDefinition)
   {
      return getRobotFX(robotDefinition.getName());
   }

   private Optional<YoRobotFX> getRobotFX(String robotName)
   {
      return robots.stream().filter(robot -> robot.getRobotDefinition().getName().equalsIgnoreCase(robotName)).findFirst();
   }

   public Group getRootNode()
   {
      return rootNode;
   }
}
