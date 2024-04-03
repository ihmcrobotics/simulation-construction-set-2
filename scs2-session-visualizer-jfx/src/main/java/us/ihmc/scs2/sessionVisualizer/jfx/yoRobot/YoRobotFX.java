package us.ihmc.scs2.sessionVisualizer.jfx.yoRobot;

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
import us.ihmc.euclid.tuple4D.interfaces.QuaternionBasics;
import us.ihmc.log.LogTools;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyReadOnly;
import us.ihmc.mecano.tools.MultiBodySystemTools;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.visual.VisualDefinition;
import us.ihmc.scs2.definition.yoComposite.YoOrientation3DDefinition;
import us.ihmc.scs2.definition.yoComposite.YoQuaternionDefinition;
import us.ihmc.scs2.definition.yoComposite.YoTuple3DDefinition;
import us.ihmc.scs2.definition.yoComposite.YoYawPitchRollDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicRobotDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicRobotDefinition.YoOneDoFJointStateDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicRobotDefinition.YoRobotStateDefinition;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.ReferenceFrameManager;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.YoManager;
import us.ihmc.scs2.sessionVisualizer.jfx.multiBodySystem.FrameNode;
import us.ihmc.scs2.sessionVisualizer.jfx.multiBodySystem.RigidBodyFrameNodeFactories;
import us.ihmc.scs2.sessionVisualizer.jfx.properties.YoDoubleProperty;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.CompositePropertyTools;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.JavaFXMissingTools;
import us.ihmc.scs2.sharedMemory.LinkedYoDouble;
import us.ihmc.scs2.sharedMemory.LinkedYoVariable;
import us.ihmc.scs2.simulation.SimulationSession;
import us.ihmc.scs2.simulation.robot.Robot;
import us.ihmc.scs2.simulation.robot.SimJointAuxiliaryData;
import us.ihmc.scs2.simulation.robot.multiBodySystem.interfaces.SimOneDoFJointBasics;
import us.ihmc.scs2.simulation.robot.multiBodySystem.interfaces.SimRigidBodyBasics;
import us.ihmc.scs2.simulation.robot.sensors.SimSensor;
import us.ihmc.scs2.simulation.robot.trackers.KinematicPoint;
import us.ihmc.yoVariables.euclid.referenceFrame.YoFramePoseUsingYawPitchRoll;
import us.ihmc.yoVariables.registry.YoRegistry;
import us.ihmc.yoVariables.variable.YoDouble;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public class YoRobotFX
{
   private final Group rootNode = new Group();
   private final YoGraphicRobotDefinition graphicRobotDefinition;
   private final YoManager yoManager;
   private final ReferenceFrameManager referenceFrameManager;

   private SimRigidBodyBasics rootBody;
   private final ObservableMap<String, FrameNode> rigidBodyFrameNodeMap = FXCollections.observableMap(new ConcurrentHashMap<>(64));

   private final List<LinkedYoVariable<?>> linkedYoVariables = new ArrayList<>();

   private boolean initialize = true;

   private final List<Runnable> cleanupTasks = new ArrayList<>();

   public YoRobotFX(YoManager yoManager, ReferenceFrameManager referenceFrameManager, YoGraphicRobotDefinition graphicRobotDefinition)
   {
      this.yoManager = yoManager;
      this.referenceFrameManager = referenceFrameManager;
      this.graphicRobotDefinition = graphicRobotDefinition;
   }

   public void loadRobot(Executor graphicLoader)
   {
      RobotDefinition robotDefinition = graphicRobotDefinition.getRobotDefinition();
      String robotName = graphicRobotDefinition.getName() != null ? graphicRobotDefinition.getName() : graphicRobotDefinition.getRobotDefinition().getName();
      LogTools.info("Loading robot: " + robotName);

      // Check if we need to do a deep copy to avoid modifying the original robot definition.
      if (!robotName.equals(robotDefinition.getName()) || graphicRobotDefinition.getMaterialDefinition() != null)
      {
         robotDefinition = new RobotDefinition(robotDefinition);
         robotDefinition.setName(robotName);
         graphicRobotDefinition.setRobotDefinition(robotDefinition);

         if (graphicRobotDefinition.getMaterialDefinition() != null)
         {
            robotDefinition.forEachRigidBodyDefinition(rigidBodyDefinition ->
                                                       {
                                                          if (rigidBodyDefinition.getVisualDefinitions() != null)
                                                          {
                                                             for (VisualDefinition visualDefinition : rigidBodyDefinition.getVisualDefinitions())
                                                             {
                                                                visualDefinition.setMaterialDefinition(graphicRobotDefinition.getMaterialDefinition());
                                                             }
                                                          }
                                                       });
         }
      }

      graphicRobotDefinition.setName(robotName); // Ensure consistency between the graphic definition and the robot definition.

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

      createRobot();

      RigidBodyFrameNodeFactories.createRobotFrameNodeMap(rootBody, robotDefinition, graphicLoader, rigidBodyFrameNodeMap);
      LogTools.info("Loaded robot: " + robotName);
   }

   private void createRobot()
   {
      Robot robot = new Robot(graphicRobotDefinition.getRobotDefinition(), referenceFrameManager.getWorldFrame().getReferenceFrame(), false);
      rootBody = robot.getRootBody();

      YoRobotStateDefinition robotStateDefinition = graphicRobotDefinition.getRobotStateDefinition();

      if (robotStateDefinition == null)
      {
         YoRegistry rootRegistry = new YoRegistry(SimulationSession.ROOT_REGISTRY_NAME);
         YoRegistry robotRegistry = robot.getRegistry();
         rootRegistry.addChild(robotRegistry);
         robotRegistry.getVariables().forEach(var ->
                                              {
                                                 if (var.getName().startsWith("q_"))
                                                 { // Only link the YoVariables for the joint angles and root joint configuration.
                                                    linkedYoVariables.add(yoManager.newLinkedYoVariable(var, this));
                                                 }
                                              });

         for (SimRigidBodyBasics rigidBody : rootBody.subtreeIterable())
         {
            if (rigidBody.isRootBody())
               continue;

            SimJointAuxiliaryData auxiliaryData = rigidBody.getParentJoint().getAuxiliaryData();

            auxiliaryData.getKinematicPoints().forEach(this::linkKinematicPointFrame);
            auxiliaryData.getExternalWrenchPoints().forEach(this::linkKinematicPointFrame);
            auxiliaryData.getGroundContactPoints().forEach(this::linkKinematicPointFrame);

            auxiliaryData.getIMUSensors().forEach(this::linkSensorFrame);
            auxiliaryData.getWrenchSensors().forEach(this::linkSensorFrame);
            auxiliaryData.getCameraSensors().forEach(this::linkSensorFrame);
         }

         cleanupTasks.add(() ->
                          {
                             linkedYoVariables.forEach(LinkedYoVariable::dispose);
                             linkedYoVariables.clear();
                          });
      }
      else
      { // We link the robot state to the yoVariables from YoRobotStateDefinition.
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
               DoubleProperty s = toDoubleProperty(quaternionDefinition.getS());
               DoubleProperty x = toDoubleProperty(quaternionDefinition.getX());
               DoubleProperty y = toDoubleProperty(quaternionDefinition.getY());
               DoubleProperty z = toDoubleProperty(quaternionDefinition.getZ());
               s.addListener((o, oldValue, newValue) -> orientation.setUnsafe(newValue.doubleValue(), x.getValue(), y.getValue(), z.getValue()));
               x.addListener((o, oldValue, newValue) -> orientation.setUnsafe(s.getValue(), newValue.doubleValue(), y.getValue(), z.getValue()));
               y.addListener((o, oldValue, newValue) -> orientation.setUnsafe(s.getValue(), x.getValue(), newValue.doubleValue(), z.getValue()));
               z.addListener((o, oldValue, newValue) -> orientation.setUnsafe(s.getValue(), x.getValue(), y.getValue(), newValue.doubleValue()));
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
   }

   private DoubleProperty toDoubleProperty(String variableName)
   {
      if (variableName == null)
         return new SimpleDoubleProperty(this, "dummy", 0.0);
      DoubleProperty doubleProperty = CompositePropertyTools.toDoubleProperty(yoManager.getRootRegistryDatabase(), variableName);
      if (doubleProperty instanceof YoDoubleProperty yoDoubleProperty)
      {
         LinkedYoDouble linkedYoDouble = yoDoubleProperty.getLinkedBuffer();
         linkedYoVariables.add(linkedYoDouble);
         cleanupTasks.add(() ->
                          {
                             yoDoubleProperty.dispose();
                             linkedYoDouble.dispose();
                          });
      }
      return doubleProperty;
   }

   private void linkKinematicPointFrame(KinematicPoint kp)
   {
      linkYoFrameOffset(kp.getOffset());
   }

   private void linkSensorFrame(SimSensor sensor)
   {
      linkYoFrameOffset(sensor.getOffset());
   }

   private void linkYoFrameOffset(YoFramePoseUsingYawPitchRoll offset)
   {
      YoDouble[] offsetVariables = {offset.getYoX(), offset.getYoY(), offset.getYoZ(), offset.getYoYaw(), offset.getYoPitch(), offset.getYoRoll()};

      for (YoDouble variable : offsetVariables)
      {
         linkedYoVariables.add(yoManager.newLinkedYoVariable(variable, this));
      }
   }

   public void render()
   {
      if (rootBody != null)
      {
         boolean updateRobot = false;
         for (LinkedYoVariable<?> linkedYoVariable : linkedYoVariables)
         {
            updateRobot |= linkedYoVariable.pull();
         }

         if (updateRobot || initialize)
         {
            rootBody.updateFramesRecursively();
            rigidBodyFrameNodeMap.values().forEach(FrameNode::updatePose);
            initialize = false;
         }
      }
   }

   public void setDrawMode(DrawMode drawMode)
   {
      JavaFXMissingTools.runLaterIfNeeded(getClass(), () -> JavaFXMissingTools.setDrawModeRecursive(rootNode, drawMode));
   }

   public RigidBodyReadOnly getRootBody()
   {
      return rootBody;
   }

   public RigidBodyBasics findRigidBody(String rigidBodyName)
   {
      return MultiBodySystemTools.findRigidBody(rootBody, rigidBodyName);
   }

   public FrameNode findRigidBodyFrameNode(String rigidBodyName)
   {
      return rigidBodyFrameNodeMap.get(rigidBodyName);
   }

   public YoGraphicRobotDefinition getGraphicRobotDefinition()
   {
      return graphicRobotDefinition;
   }

   public RobotDefinition getRobotDefinition()
   {
      return graphicRobotDefinition.getRobotDefinition();
   }

   public Node getRootNode()
   {
      return rootNode;
   }

   public boolean isRobotLoaded()
   {
      return !initialize;
   }

   public void dispose()
   {
      cleanupTasks.forEach(Runnable::run);
      cleanupTasks.clear();
   }
}
