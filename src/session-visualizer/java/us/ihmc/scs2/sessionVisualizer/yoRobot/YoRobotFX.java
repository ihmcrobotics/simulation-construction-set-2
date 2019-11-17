package us.ihmc.scs2.sessionVisualizer.yoRobot;

import static us.ihmc.scs2.sessionVisualizer.multiBodySystem.JavaFXMultiBodySystemFactories.toYoJavaFXMultiBodySystem;

import java.util.concurrent.Executor;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.util.Duration;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.referenceFrame.tools.ReferenceFrameTools;
import us.ihmc.log.LogTools;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.sessionVisualizer.managers.ReferenceFrameManager;
import us.ihmc.scs2.sessionVisualizer.managers.YoManager;
import us.ihmc.scs2.sessionVisualizer.multiBodySystem.FrameNode;
import us.ihmc.scs2.sessionVisualizer.multiBodySystem.JavaFXRigidBody;
import us.ihmc.scs2.sessionVisualizer.tools.JavaFXMissingTools;
import us.ihmc.scs2.sharedMemory.LinkedYoVariableRegistry;
import us.ihmc.scs2.simulation.SimulationCore;
import us.ihmc.yoVariables.registry.YoVariableRegistry;

public class YoRobotFX
{
   private final Group rootNode = new Group();
   private final RobotDefinition robotDefinition;
   private final YoManager yoManager;
   private final ReferenceFrameManager referenceFrameManager;

   private JavaFXRigidBody rootBody;
   private final YoVariableRegistry robotRegistry;

   private LinkedYoVariableRegistry robotLinkedYoVariableRegistry;

   private boolean initialize = true;

   public YoRobotFX(YoManager yoManager, ReferenceFrameManager referenceFrameManager, RobotDefinition robotDefinition)
   {
      this.yoManager = yoManager;
      this.referenceFrameManager = referenceFrameManager;
      this.robotDefinition = robotDefinition;

      robotRegistry = new YoVariableRegistry(robotDefinition.getName());
      new YoVariableRegistry(SimulationCore.ROOT_REGISTRY_NAME).addChild(robotRegistry);
   }

   public void loadRobot(Executor graphicLoader)
   {
      LogTools.info("Loading robot: " + robotDefinition.getName());
      ReferenceFrame worldFrame = referenceFrameManager.getWorldFrame();

      rootBody = toYoJavaFXMultiBodySystem(robotDefinition.newIntance(ReferenceFrameTools.constructARootFrame("dummy")),
                                           worldFrame,
                                           robotDefinition,
                                           robotRegistry,
                                           graphicLoader);
      LogTools.info("Loaded robot: " + robotDefinition.getName());

      attachRobotToScene();
   }

   private void attachRobotToScene()
   {
      for (JavaFXRigidBody rigidBody : rootBody.subtreeIterable())
      {
         ChangeListener<? super FrameNode> listener = (o, oldValue, newValue) ->
         {
            if (newValue != null)
            {
               if (oldValue == null)
               {
                  Node node = newValue.getNode();
                  node.setScaleX(0.0);
                  node.setScaleY(0.0);
                  node.setScaleZ(0.0);
                  Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.25),
                                                                new KeyValue(node.scaleXProperty(), 1.0),
                                                                new KeyValue(node.scaleYProperty(), 1.0),
                                                                new KeyValue(node.scaleZProperty(), 1.0)));
                  timeline.setCycleCount(1);

                  JavaFXMissingTools.runLaterIfNeeded(() ->
                  {
                     newValue.updatePose();
                     timeline.playFromStart();
                     rootNode.getChildren().add(newValue.getNode());
                  });
               }
               else
               {
                  JavaFXMissingTools.runLaterIfNeeded(() -> rootNode.getChildren().remove(oldValue.getNode()));
                  JavaFXMissingTools.runLaterIfNeeded(() -> rootNode.getChildren().add(newValue.getNode()));
               }
            }
            else if (oldValue != null)
            {
               JavaFXMissingTools.runLaterIfNeeded(() -> rootNode.getChildren().remove(oldValue.getNode()));
            }
         };
         rigidBody.graphicsProperty().addListener(listener);
         listener.changed(null, null, rigidBody.getGraphics());
      }

      robotLinkedYoVariableRegistry = yoManager.newLinkedYoVariableRegistry(robotRegistry);
      yoManager.linkNewYoVariables();
   }

   public void render()
   {
      if (rootBody != null && robotLinkedYoVariableRegistry != null)
      {
         if (robotLinkedYoVariableRegistry.pull() || initialize)
         {
            rootBody.updateFramesRecursively();
            rootBody.updateSubtreeGraphics();
            initialize = false;
         }
      }
   }

   public RobotDefinition getRobotDefinition()
   {
      return robotDefinition;
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
