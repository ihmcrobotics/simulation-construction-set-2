package us.ihmc.scs2.sessionVisualizer.jfx.yoRobot;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.DrawMode;
import javafx.util.Duration;
import us.ihmc.log.LogTools;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyReadOnly;
import us.ihmc.mecano.tools.MultiBodySystemTools;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicRobotDefinition;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.ReferenceFrameManager;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.YoManager;
import us.ihmc.scs2.sessionVisualizer.jfx.multiBodySystem.FrameNode;
import us.ihmc.scs2.sessionVisualizer.jfx.multiBodySystem.RigidBodyFrameNodeFactories;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.JavaFXMissingTools;
import us.ihmc.scs2.sharedMemory.LinkedYoVariable;
import us.ihmc.scs2.simulation.SimulationSession;
import us.ihmc.scs2.simulation.robot.Robot;
import us.ihmc.scs2.simulation.robot.SimJointAuxiliaryData;
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
   private ObservableMap<String, FrameNode> rigidBodyFrameNodeMap = FXCollections.observableMap(new ConcurrentHashMap<>(64));

   private final List<LinkedYoVariable<?>> linkedYoVariables = new ArrayList<>();

   private boolean initialize = true;

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

      if (!robotName.equals(robotDefinition.getName()))
      {
         robotDefinition = new RobotDefinition(robotDefinition);
         robotDefinition.setName(robotName);
         graphicRobotDefinition.setRobotDefinition(robotDefinition);
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

      if (graphicRobotDefinition.getRobotStateDefinition() == null)
      {
         YoRegistry rootRegistry = new YoRegistry(SimulationSession.ROOT_REGISTRY_NAME);
         YoRegistry robotRegistry = robot.getRegistry();
         rootRegistry.addChild(robotRegistry);
         rootBody = robot.getRootBody();
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
      }
      else
      { // We link the robot state to the yoVariables from YoRobotStateDefinition.

      }
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
            rigidBodyFrameNodeMap.values().forEach(node -> node.updatePose());
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
}
