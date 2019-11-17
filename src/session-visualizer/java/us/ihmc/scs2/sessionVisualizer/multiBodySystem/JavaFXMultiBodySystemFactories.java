package us.ihmc.scs2.sessionVisualizer.multiBodySystem;

import java.util.List;
import java.util.concurrent.Executor;

import javafx.scene.Node;
import us.ihmc.euclid.matrix.interfaces.Matrix3DReadOnly;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.transform.RigidBodyTransform;
import us.ihmc.mecano.multiBodySystem.interfaces.JointBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyReadOnly;
import us.ihmc.mecano.tools.MultiBodySystemFactories;
import us.ihmc.mecano.tools.MultiBodySystemFactories.JointBuilder;
import us.ihmc.mecano.tools.MultiBodySystemFactories.RigidBodyBuilder;
import us.ihmc.mecano.yoVariables.tools.YoMultiBodySystemFactories;
import us.ihmc.scs2.definition.robot.RigidBodyDefinition;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.visual.VisualDefinition;
import us.ihmc.scs2.sessionVisualizer.definition.JavaFXVisualTools;
import us.ihmc.yoVariables.registry.YoVariableRegistry;

public class JavaFXMultiBodySystemFactories
{
   public static JavaFXRigidBody toJavaFXMultiBodySystem(RigidBodyReadOnly originalRootBody, ReferenceFrame cloneStationaryFrame,
                                                         RobotDefinition robotDefinition)
   {
      return toJavaFXMultiBodySystem(originalRootBody, cloneStationaryFrame, robotDefinition, MultiBodySystemFactories.DEFAULT_JOINT_BUILDER);
   }

   public static JavaFXRigidBody toJavaFXMultiBodySystem(RigidBodyReadOnly originalRootBody, ReferenceFrame cloneStationaryFrame,
                                                         RobotDefinition robotDefinition, Executor graphicLoader)
   {
      return toJavaFXMultiBodySystem(originalRootBody, cloneStationaryFrame, robotDefinition, MultiBodySystemFactories.DEFAULT_JOINT_BUILDER, graphicLoader);
   }

   public static JavaFXRigidBody toYoJavaFXMultiBodySystem(RigidBodyReadOnly originalRootBody, ReferenceFrame cloneStationaryFrame,
                                                           RobotDefinition robotDefinition, YoVariableRegistry registry)
   {
      return toJavaFXMultiBodySystem(originalRootBody, cloneStationaryFrame, robotDefinition, YoMultiBodySystemFactories.newYoJointBuilder(registry), null);
   }

   public static JavaFXRigidBody toYoJavaFXMultiBodySystem(RigidBodyReadOnly originalRootBody, ReferenceFrame cloneStationaryFrame,
                                                           RobotDefinition robotDefinition, YoVariableRegistry registry, Executor graphicLoader)
   {
      return toJavaFXMultiBodySystem(originalRootBody,
                                     cloneStationaryFrame,
                                     robotDefinition,
                                     YoMultiBodySystemFactories.newYoJointBuilder(registry),
                                     graphicLoader);
   }

   public static JavaFXRigidBody toJavaFXMultiBodySystem(RigidBodyReadOnly originalRootBody, ReferenceFrame cloneStationaryFrame,
                                                         RobotDefinition robotDefinition, JointBuilder jointBuilder)
   {
      return toJavaFXMultiBodySystem(originalRootBody, cloneStationaryFrame, robotDefinition, jointBuilder, null);
   }

   public static JavaFXRigidBody toJavaFXMultiBodySystem(RigidBodyReadOnly originalRootBody, ReferenceFrame cloneStationaryFrame,
                                                         RobotDefinition robotDefinition, JointBuilder jointBuilder, Executor graphicLoader)
   {
      return (JavaFXRigidBody) MultiBodySystemFactories.cloneMultiBodySystem(originalRootBody,
                                                                             cloneStationaryFrame,
                                                                             "",
                                                                             newJavaFXRigidBodyBuilder(robotDefinition, graphicLoader),
                                                                             jointBuilder);
   }

   public static RigidBodyBuilder newJavaFXRigidBodyBuilder(RobotDefinition robotDefinition)
   {
      return newJavaFXRigidBodyBuilder(robotDefinition, null);
   }

   public static RigidBodyBuilder newJavaFXRigidBodyBuilder(RobotDefinition robotDefinition, Executor graphicLoader)
   {
      return newJavaFXRigidBodyBuilder(MultiBodySystemFactories.DEFAULT_RIGID_BODY_BUILDER, robotDefinition, graphicLoader);
   }

   public static RigidBodyBuilder newJavaFXRigidBodyBuilder(RigidBodyBuilder rigidBodyBuilder, RobotDefinition robotDefinition)
   {
      return newJavaFXRigidBodyBuilder(rigidBodyBuilder, robotDefinition, null);
   }

   public static RigidBodyBuilder newJavaFXRigidBodyBuilder(RigidBodyBuilder rigidBodyBuilder, RobotDefinition robotDefinition, Executor graphicLoader)
   {
      return new RigidBodyBuilder()
      {
         @Override
         public JavaFXRigidBody buildRoot(String bodyName, RigidBodyTransform transformToParent, ReferenceFrame parentStationaryFrame)
         {
            RigidBodyBasics rootBody = rigidBodyBuilder.buildRoot(bodyName, transformToParent, parentStationaryFrame);
            return toJavaFXRigidBody(rootBody, robotDefinition.getRigidBodyDefinition(rootBody.getName()), graphicLoader);
         }

         @Override
         public JavaFXRigidBody build(String bodyName, JointBasics parentJoint, Matrix3DReadOnly momentOfInertia, double mass, RigidBodyTransform inertiaPose)
         {
            RigidBodyBasics rigidBody = rigidBodyBuilder.build(bodyName, parentJoint, momentOfInertia, mass, inertiaPose);
            return toJavaFXRigidBody(rigidBody, robotDefinition.getRigidBodyDefinition(rigidBody.getName()), graphicLoader);
         }
      };
   }

   public static JavaFXRigidBody toJavaFXRigidBody(RigidBodyBasics rigidBody, RigidBodyDefinition rigidBodyDefinition)
   {
      return toJavaFXRigidBody(rigidBody, rigidBodyDefinition, null);
   }

   public static JavaFXRigidBody toJavaFXRigidBody(RigidBodyBasics rigidBody, RigidBodyDefinition rigidBodyDefinition, Executor graphicLoader)
   {
      JavaFXRigidBody javaFXRigidBody = new JavaFXRigidBody(rigidBody);
      List<VisualDefinition> visualDefinitions = rigidBodyDefinition.getVisualDefinitions();

      if (graphicLoader != null)
      {
         graphicLoader.execute(() -> loadRigidBodyGraphic(visualDefinitions, javaFXRigidBody));
      }
      else
      {
         loadRigidBodyGraphic(visualDefinitions, javaFXRigidBody);
      }

      return javaFXRigidBody;
   }

   private static void loadRigidBodyGraphic(List<VisualDefinition> visualDefinitions, JavaFXRigidBody javaFXRigidBody)
   {
      Node graphicNode = JavaFXVisualTools.collectNodes(visualDefinitions);
      ReferenceFrame graphicFrame = javaFXRigidBody.isRootBody() ? javaFXRigidBody.getBodyFixedFrame() : javaFXRigidBody.getParentJoint().getFrameAfterJoint();

      if (graphicNode != null)
         javaFXRigidBody.setGraphics(new FrameNode(graphicFrame, graphicNode));
   }
}
