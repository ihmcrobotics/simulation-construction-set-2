package us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.DrawMode;
import javafx.util.Duration;
import us.ihmc.euclid.geometry.interfaces.Pose3DBasics;
import us.ihmc.euclid.referenceFrame.tools.ReferenceFrameTools;
import us.ihmc.euclid.tuple4D.interfaces.QuaternionBasics;
import us.ihmc.log.LogTools;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.yoComposite.YoOrientation3DDefinition;
import us.ihmc.scs2.definition.yoComposite.YoQuaternionDefinition;
import us.ihmc.scs2.definition.yoComposite.YoTuple3DDefinition;
import us.ihmc.scs2.definition.yoComposite.YoYawPitchRollDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicRobotDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicRobotDefinition.YoOneDoFJointStateDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicRobotDefinition.YoRobotStateDefinition;
import us.ihmc.scs2.sessionVisualizer.jfx.multiBodySystem.FrameNode;
import us.ihmc.scs2.sessionVisualizer.jfx.multiBodySystem.RigidBodyFrameNodeFactories;
import us.ihmc.scs2.sessionVisualizer.jfx.properties.YoDoubleProperty;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.CompositePropertyTools;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.JavaFXMissingTools;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.YoVariableDatabase;
import us.ihmc.scs2.sharedMemory.LinkedYoDouble;
import us.ihmc.scs2.sharedMemory.LinkedYoVariable;
import us.ihmc.scs2.simulation.robot.Robot;
import us.ihmc.scs2.simulation.robot.multiBodySystem.interfaces.SimOneDoFJointBasics;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class YoGhostRobotFX extends YoGraphicFX3D
{ // FIXME Need to handle the color property
   private final Group rootNode = new Group();
   private final YoVariableDatabase yoVariableDatabase;

   private YoGraphicRobotDefinition graphicRobotDefinition;
   private Robot robot;
   private final ObservableMap<String, FrameNode> rigidBodyFrameNodeMap = FXCollections.observableMap(new ConcurrentHashMap<>(64));

   private final List<LinkedYoVariable<?>> linkedYoVariables = new ArrayList<>();

   private boolean initialize = true;

   private final List<Runnable> clearStateBindingTasks = new ArrayList<>();

   private boolean robotDefinitionChanged = true;
   private boolean robotStateDefinitionChanged = true;

   public YoGhostRobotFX(YoVariableDatabase yoVariableDatabase)
   {
      this.yoVariableDatabase = yoVariableDatabase;

      rigidBodyFrameNodeMap.addListener((MapChangeListener<String, FrameNode>) change ->
      {
         if (change.wasRemoved())
         {
            FrameNode removedNode = change.getValueRemoved();
            JavaFXMissingTools.runLaterIfNeeded(getClass(), () -> rootNode.getChildren().remove(removedNode.getNode()));
         }
         else if (change.wasAdded())
         {
            FrameNode addedNode = change.getValueAdded();
            Node node = addedNode.getNode();
            node.setScaleX(0.0);
            node.setScaleY(0.0);
            node.setScaleZ(0.0);
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.25),
                                                          new KeyValue(node.scaleXProperty(), 1.0),
                                                          new KeyValue(node.scaleYProperty(), 1.0),
                                                          new KeyValue(node.scaleZProperty(), 1.0)));
            timeline.setCycleCount(1);

            JavaFXMissingTools.runLaterIfNeeded(getClass(), () ->
            {
               addedNode.updatePose();
               timeline.playFromStart();
               rootNode.getChildren().add(addedNode.getNode());
            });
         }
      });
   }

   public void setInput(YoGraphicRobotDefinition input)
   {
      if (graphicRobotDefinition == input)
         return;
      if (graphicRobotDefinition == null)
      {
         robotDefinitionChanged = true;
         robotStateDefinitionChanged = true;
      }
      else
      {
         if (!Objects.equals(graphicRobotDefinition.getRobotDefinition(), input.getRobotDefinition()))
         {
            robotDefinitionChanged = true;
         }
         if (!Objects.equals(graphicRobotDefinition.getRobotStateDefinition(), input.getRobotStateDefinition()))
         {
            robotStateDefinitionChanged = true;
         }
      }

      graphicRobotDefinition = input;
      setName(graphicRobotDefinition.getName());
   }

   private void updateRobotDefinition()
   {
      if (!robotDefinitionChanged)
         return;

      robotDefinitionChanged = false;
      rigidBodyFrameNodeMap.clear();
      RobotDefinition robotDefinition = graphicRobotDefinition.getRobotDefinition();
      robot = new Robot(robotDefinition, ReferenceFrameTools.constructARootFrame("dummy"), false);
      RigidBodyFrameNodeFactories.createRobotFrameNodeMap(robot.getRootBody(), robotDefinition, null, rigidBodyFrameNodeMap);
      // Need to update the robot state definition as the robot has changed.
      robotStateDefinitionChanged = true;
   }

   private void updateRobotStateDefinition()
   {
      if (!robotStateDefinitionChanged)
         return;

      robotStateDefinitionChanged = false;
      initialize = true;

      // Clear previous bindings
      clearRobotStateBindings();

      if (graphicRobotDefinition == null)
         return;

      YoRobotStateDefinition robotStateDefinition = graphicRobotDefinition.getRobotStateDefinition();

      if (robotStateDefinition == null)
         return;

      // We link the robot state to the yoVariables from YoRobotStateDefinition.
      YoTuple3DDefinition rootJointPositionDefinition = robotStateDefinition.getRootJointPosition();

      if (rootJointPositionDefinition != null)
      {
         Pose3DBasics pose = robot.getFloatingRootJoint().getJointPose();

         DoubleProperty x = toDoubleProperty(rootJointPositionDefinition.getX());
         DoubleProperty y = toDoubleProperty(rootJointPositionDefinition.getY());
         DoubleProperty z = toDoubleProperty(rootJointPositionDefinition.getZ());
         x.addListener((o, oldValue, newValue) -> pose.setX(newValue.doubleValue()));
         y.addListener((o, oldValue, newValue) -> pose.setY(newValue.doubleValue()));
         z.addListener((o, oldValue, newValue) -> pose.setZ(newValue.doubleValue()));
      }

      YoOrientation3DDefinition rootJointOrientationDefinition = robotStateDefinition.getRootJointOrientation();

      if (rootJointOrientationDefinition != null)
      {
         QuaternionBasics orientation = robot.getFloatingRootJoint().getJointPose().getOrientation();

         if (rootJointOrientationDefinition instanceof YoYawPitchRollDefinition yawPitchRollDefinition)
         {
            DoubleProperty yaw = toDoubleProperty(yawPitchRollDefinition.getYaw());
            DoubleProperty pitch = toDoubleProperty(yawPitchRollDefinition.getPitch());
            DoubleProperty roll = toDoubleProperty(yawPitchRollDefinition.getRoll());
            yaw.addListener((o, oldValue, newValue) -> orientation.setYawPitchRoll(newValue.doubleValue(), pitch.getValue(), roll.getValue()));
            pitch.addListener((o, oldValue, newValue) -> orientation.setYawPitchRoll(yaw.getValue(), newValue.doubleValue(), roll.getValue()));
            roll.addListener((o, oldValue, newValue) -> orientation.setYawPitchRoll(yaw.getValue(), pitch.getValue(), newValue.doubleValue()));
         }
         else if (rootJointOrientationDefinition instanceof YoQuaternionDefinition quaternionDefinition)
         {
            DoubleProperty x = toDoubleProperty(quaternionDefinition.getX());
            DoubleProperty y = toDoubleProperty(quaternionDefinition.getY());
            DoubleProperty z = toDoubleProperty(quaternionDefinition.getZ());
            DoubleProperty s = toDoubleProperty(quaternionDefinition.getS());
            x.addListener((o, oldValue, newValue) -> orientation.setUnsafe(newValue.doubleValue(), y.getValue(), z.getValue(), s.getValue()));
            y.addListener((o, oldValue, newValue) -> orientation.setUnsafe(x.getValue(), newValue.doubleValue(), z.getValue(), s.getValue()));
            z.addListener((o, oldValue, newValue) -> orientation.setUnsafe(x.getValue(), y.getValue(), newValue.doubleValue(), s.getValue()));
            s.addListener((o, oldValue, newValue) -> orientation.setUnsafe(x.getValue(), y.getValue(), z.getValue(), newValue.doubleValue()));
         }
         else
         {
            LogTools.error("Unsupported root joint orientation: " + rootJointOrientationDefinition);
         }
      }

      if (robotStateDefinition.getJointPositions() != null)
      {
         for (YoOneDoFJointStateDefinition jointPositionDefinition : robotStateDefinition.getJointPositions())
         {
            String jointName = jointPositionDefinition.getJointName();
            DoubleProperty jointPosition = toDoubleProperty(jointPositionDefinition.getJointPosition());
            SimOneDoFJointBasics joint = robot.getOneDoFJoint(jointName);
            if (joint == null)
               LogTools.error("Could not find joint: " + jointName);
            else
               jointPosition.addListener((o, oldValue, newValue) -> joint.setQ(newValue.doubleValue()));
         }
      }
   }

   private DoubleProperty toDoubleProperty(String variableName)
   {
      if (variableName == null)
         return new SimpleDoubleProperty(this, "dummy", 0.0);
      DoubleProperty doubleProperty = CompositePropertyTools.toDoubleProperty(yoVariableDatabase, variableName);
      if (doubleProperty instanceof YoDoubleProperty yoDoubleProperty)
      {
         LinkedYoDouble linkedYoDouble = yoDoubleProperty.getLinkedBuffer();
         linkedYoVariables.add(linkedYoDouble);
         clearStateBindingTasks.add(() ->
                                    {
                                       yoDoubleProperty.dispose();
                                       linkedYoDouble.dispose();
                                    });
      }
      return doubleProperty;
   }

   @Override
   public void computeBackground()
   {
      updateRobotDefinition();
      updateRobotStateDefinition();
   }

   @Override
   public void render()
   {
      if (robot == null)
         return;

      boolean updateRobot = false;
      for (LinkedYoVariable<?> linkedYoVariable : linkedYoVariables)
      {
         updateRobot |= linkedYoVariable.pull();
      }

      if (updateRobot || initialize)
      {
         robot.getRootBody().updateFramesRecursively();
         rigidBodyFrameNodeMap.values().forEach(FrameNode::updatePose);
         initialize = false;
      }
   }

   public void setDrawMode(DrawMode drawMode)
   {
      JavaFXMissingTools.runLaterIfNeeded(getClass(), () -> JavaFXMissingTools.setDrawModeRecursive(rootNode, drawMode));
   }

   public YoGraphicRobotDefinition getGraphicRobotDefinition()
   {
      return graphicRobotDefinition;
   }

   public RobotDefinition getRobotDefinition()
   {
      return graphicRobotDefinition.getRobotDefinition();
   }

   public boolean isRobotLoaded()
   {
      return !initialize;
   }

   @Override
   public void clear()
   {
      robot = null;
      rigidBodyFrameNodeMap.clear();
      rootNode.getChildren().clear();
      clearRobotStateBindings();
   }

   private void clearRobotStateBindings()
   {
      clearStateBindingTasks.forEach(Runnable::run);
      clearStateBindingTasks.clear();
      linkedYoVariables.clear();
   }

   @Override
   public YoGraphicFX clone()
   {
      return new YoGhostRobotFX(yoVariableDatabase);
   }

   @Override
   public Node getNode()
   {
      return rootNode;
   }
}
