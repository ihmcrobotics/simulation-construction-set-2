package us.ihmc.scs2.sessionVisualizer.jfx.multiBodySystem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javafx.collections.ObservableMap;
import javafx.scene.Node;
import us.ihmc.euclid.matrix.interfaces.Matrix3DReadOnly;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.referenceFrame.interfaces.FrameVector3DReadOnly;
import us.ihmc.euclid.transform.RigidBodyTransform;
import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;
import us.ihmc.mecano.multiBodySystem.CrossFourBarJoint;
import us.ihmc.mecano.multiBodySystem.PlanarJoint;
import us.ihmc.mecano.multiBodySystem.PrismaticJoint;
import us.ihmc.mecano.multiBodySystem.RevoluteJoint;
import us.ihmc.mecano.multiBodySystem.SixDoFJoint;
import us.ihmc.mecano.multiBodySystem.SphericalJoint;
import us.ihmc.mecano.multiBodySystem.interfaces.CrossFourBarJointBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.CrossFourBarJointReadOnly;
import us.ihmc.mecano.multiBodySystem.interfaces.JointBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.PlanarJointBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.PrismaticJointBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.RevoluteJointBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.RevoluteJointReadOnly;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyReadOnly;
import us.ihmc.mecano.multiBodySystem.interfaces.SixDoFJointBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.SphericalJointBasics;
import us.ihmc.mecano.tools.MultiBodySystemFactories;
import us.ihmc.mecano.tools.MultiBodySystemFactories.JointBuilder;
import us.ihmc.mecano.tools.MultiBodySystemFactories.RigidBodyBuilder;
import us.ihmc.mecano.yoVariables.multiBodySystem.YoCrossFourBarJoint;
import us.ihmc.mecano.yoVariables.multiBodySystem.YoPlanarJoint;
import us.ihmc.mecano.yoVariables.multiBodySystem.YoPrismaticJoint;
import us.ihmc.mecano.yoVariables.multiBodySystem.YoRevoluteJoint;
import us.ihmc.mecano.yoVariables.multiBodySystem.YoSixDoFJoint;
import us.ihmc.mecano.yoVariables.multiBodySystem.YoSphericalJoint;
import us.ihmc.scs2.definition.robot.CrossFourBarJointDefinition;
import us.ihmc.scs2.definition.robot.RigidBodyDefinition;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.visual.VisualDefinition;
import us.ihmc.scs2.sessionVisualizer.jfx.definition.JavaFXVisualTools;
import us.ihmc.yoVariables.registry.YoRegistry;

public class JavaFXMultiBodySystemFactories
{
   public static JavaFXRigidBody toJavaFXMultiBodySystem(RigidBodyReadOnly originalRootBody,
                                                         ReferenceFrame cloneStationaryFrame,
                                                         RobotDefinition robotDefinition)
   {
      return toYoJavaFXMultiBodySystem(originalRootBody, cloneStationaryFrame, robotDefinition, null, null);
   }

   public static JavaFXRigidBody toYoJavaFXMultiBodySystem(RigidBodyReadOnly originalRootBody,
                                                           ReferenceFrame cloneStationaryFrame,
                                                           RobotDefinition robotDefinition,
                                                           YoRegistry registry,
                                                           Executor graphicLoader)
   {
      JavaFXRigidBodyBuilder rigidBodyBuilder = new JavaFXRigidBodyBuilder(robotDefinition, graphicLoader);
      JavaFXJointBuilder jointBuilder = new JavaFXJointBuilder(registry, rigidBodyBuilder);
      return (JavaFXRigidBody) MultiBodySystemFactories.cloneMultiBodySystem(originalRootBody, cloneStationaryFrame, "", rigidBodyBuilder, jointBuilder);
   }

   public static void createRobotFrameNodeMap(RigidBodyReadOnly rootBody,
                                              RobotDefinition robotDefinition,
                                              Executor graphicLoader,
                                              ObservableMap<String, FrameNode> frameNodesToPack)
   {
      BiConsumer<String, FrameNode> frameNodeAddition = (bodyName, frameNode) ->
      {
         if (frameNode == null)
            return;
         if (graphicLoader != null)
            graphicLoader.execute(() -> frameNodesToPack.put(bodyName, frameNode));
         else
            frameNodesToPack.put(bodyName, frameNode);
      };

      for (RigidBodyReadOnly body : rootBody.subtreeIterable())
      {
         frameNodeAddition.accept(body.getName(), loadRigidBodyGraphic(robotDefinition, body));

         if (body.getParentJoint() != null)
         {
            if (body.getParentJoint() instanceof CrossFourBarJointReadOnly)
            {
               CrossFourBarJointReadOnly parentJoint = (CrossFourBarJointReadOnly) body.getParentJoint();
               CrossFourBarJointDefinition parentJointDefinition = (CrossFourBarJointDefinition) robotDefinition.getJointDefinition(parentJoint.getName());
               frameNodeAddition.accept(parentJoint.getBodyDA().getName(),
                                        loadRigidBodyGraphic(parentJointDefinition.getBodyDA(),
                                                             parentJoint.getBodyDA(),
                                                             robotDefinition.getResourceClassLoader()));
               frameNodeAddition.accept(parentJoint.getBodyBC().getName(),
                                        loadRigidBodyGraphic(parentJointDefinition.getBodyBC(),
                                                             parentJoint.getBodyBC(),
                                                             robotDefinition.getResourceClassLoader()));
            }
         }
      }
   }

   public static class JavaFXJointBuilder implements JointBuilder
   {
      private final YoRegistry registry;
      private final JavaFXRigidBodyBuilder rigidBodyBuilder;

      public JavaFXJointBuilder(JavaFXRigidBodyBuilder rigidBodyBuilder)
      {
         this(null, rigidBodyBuilder);
      }

      public JavaFXJointBuilder(YoRegistry registry, JavaFXRigidBodyBuilder rigidBodyBuilder)
      {
         this.registry = registry;
         this.rigidBodyBuilder = rigidBodyBuilder;
      }

      @Override
      public SixDoFJointBasics buildSixDoFJoint(String name, RigidBodyBasics predecessor, RigidBodyTransformReadOnly transformToParent)
      {
         if (registry != null)
            return new YoSixDoFJoint(name, predecessor, transformToParent, registry);
         else
            return new SixDoFJoint(name, predecessor, transformToParent);
      }

      @Override
      public PlanarJointBasics buildPlanarJoint(String name, RigidBodyBasics predecessor, RigidBodyTransformReadOnly transformToParent)
      {
         if (registry != null)
            return new YoPlanarJoint(name, predecessor, transformToParent, registry);
         else
            return new PlanarJoint(name, predecessor, transformToParent);
      }

      @Override
      public SphericalJointBasics buildSphericalJoint(String name, RigidBodyBasics predecessor, RigidBodyTransformReadOnly transformToParent)
      {
         if (registry != null)
            return new YoSphericalJoint(name, predecessor, transformToParent, registry);
         else
            return new SphericalJoint(name, predecessor, transformToParent);
      }

      @Override
      public RevoluteJointBasics buildRevoluteJoint(String name,
                                                    RigidBodyBasics predecessor,
                                                    RigidBodyTransformReadOnly transformToParent,
                                                    Vector3DReadOnly jointAxis)
      {
         if (registry != null)
            return new YoRevoluteJoint(name, predecessor, transformToParent, jointAxis, registry);
         else
            return new RevoluteJoint(name, predecessor, transformToParent, jointAxis);
      }

      @Override
      public PrismaticJointBasics buildPrismaticJoint(String name,
                                                      RigidBodyBasics predecessor,
                                                      RigidBodyTransformReadOnly transformToParent,
                                                      Vector3DReadOnly jointAxis)
      {
         if (registry != null)
            return new YoPrismaticJoint(name, predecessor, transformToParent, jointAxis, registry);
         else
            return new PrismaticJoint(name, predecessor, transformToParent, jointAxis);
      }

      @Override
      public CrossFourBarJointBasics cloneCrossFourBarJoint(CrossFourBarJointReadOnly original, String cloneSuffix, RigidBodyBasics clonePredecessor)
      {
         RevoluteJointReadOnly originalJointA = original.getJointA();
         RevoluteJointReadOnly originalJointB = original.getJointB();
         RevoluteJointReadOnly originalJointC = original.getJointC();
         RevoluteJointReadOnly originalJointD = original.getJointD();
         RigidBodyReadOnly originalBodyDA = originalJointA.getSuccessor();
         RigidBodyReadOnly originalBodyBC = originalJointB.getSuccessor();
         int loopClosureIndex;
         if (originalJointA.isLoopClosure())
            loopClosureIndex = 0;
         else if (originalJointB.isLoopClosure())
            loopClosureIndex = 1;
         else if (originalJointC.isLoopClosure())
            loopClosureIndex = 2;
         else
            loopClosureIndex = 3;

         String cloneName = original.getName() + cloneSuffix;
         String cloneJointNameA = originalJointA.getName() + cloneSuffix;
         String cloneJointNameB = originalJointB.getName() + cloneSuffix;
         String cloneJointNameC = originalJointC.getName() + cloneSuffix;
         String cloneJointNameD = originalJointD.getName() + cloneSuffix;
         String cloneBodyNameDA = originalBodyDA.getName() + cloneSuffix;
         String cloneBodyNameBC = originalBodyBC.getName() + cloneSuffix;
         RigidBodyTransform transformAToPredecessor = originalJointA.getFrameBeforeJoint().getTransformToParent();
         RigidBodyTransform transformBToPredecessor = originalJointB.getFrameBeforeJoint().getTransformToParent();
         RigidBodyTransform transformCToB = originalJointC.getFrameBeforeJoint().getTransformToParent();
         RigidBodyTransform transformDToA = originalJointD.getFrameBeforeJoint().getTransformToParent();
         Matrix3DReadOnly bodyInertiaDA = originalBodyDA.getInertia().getMomentOfInertia();
         Matrix3DReadOnly bodyInertiaBC = originalBodyBC.getInertia().getMomentOfInertia();
         double bodyMassDA = originalBodyDA.getInertia().getMass();
         double bodyMassBC = originalBodyBC.getInertia().getMass();
         RigidBodyTransform bodyInertiaPoseDA = originalBodyDA.getBodyFixedFrame().getTransformToParent();
         RigidBodyTransform bodyInertiaPoseBC = originalBodyBC.getBodyFixedFrame().getTransformToParent();
         int actuatedJointIndex = original.getActuatedJointIndex();
         FrameVector3DReadOnly jointAxis = original.getJointAxis();

         if (registry != null)
         {
            return new YoCrossFourBarJoint(cloneName,
                                           clonePredecessor,
                                           cloneJointNameA,
                                           cloneJointNameB,
                                           cloneJointNameC,
                                           cloneJointNameD,
                                           cloneBodyNameDA,
                                           cloneBodyNameBC,
                                           transformAToPredecessor,
                                           transformBToPredecessor,
                                           transformCToB,
                                           transformDToA,
                                           bodyInertiaDA,
                                           bodyInertiaBC,
                                           bodyMassDA,
                                           bodyMassBC,
                                           bodyInertiaPoseDA,
                                           bodyInertiaPoseBC,
                                           rigidBodyBuilder,
                                           actuatedJointIndex,
                                           loopClosureIndex,
                                           jointAxis,
                                           registry);
         }
         else
         {
            return new CrossFourBarJoint(cloneName,
                                         clonePredecessor,
                                         cloneJointNameA,
                                         cloneJointNameB,
                                         cloneJointNameC,
                                         cloneJointNameD,
                                         cloneBodyNameDA,
                                         cloneBodyNameBC,
                                         transformAToPredecessor,
                                         transformBToPredecessor,
                                         transformCToB,
                                         transformDToA,
                                         bodyInertiaDA,
                                         bodyInertiaBC,
                                         bodyMassDA,
                                         bodyMassBC,
                                         bodyInertiaPoseDA,
                                         bodyInertiaPoseBC,
                                         rigidBodyBuilder,
                                         actuatedJointIndex,
                                         loopClosureIndex,
                                         jointAxis);
         }
      }
   }

   public static class JavaFXRigidBodyBuilder implements RigidBodyBuilder
   {
      private final RigidBodyBuilder rigidBodyBuilder;
      private final Function<String, RigidBodyDefinition> rigidBodyDefinitionProvider;
      private final Executor graphicLoader;
      private final ClassLoader resourceClassLoader;

      public JavaFXRigidBodyBuilder(RobotDefinition robotDefinition, Executor graphicLoader)
      {
         this(MultiBodySystemFactories.DEFAULT_RIGID_BODY_BUILDER,
              toRigidBodyDefinitionProvider(robotDefinition),
              graphicLoader,
              robotDefinition.getResourceClassLoader());
      }

      public JavaFXRigidBodyBuilder(RigidBodyBuilder rigidBodyBuilder,
                                    Function<String, RigidBodyDefinition> rigidBodyDefinitionProvider,
                                    Executor graphicLoader,
                                    ClassLoader resourceClassLoader)
      {
         this.rigidBodyBuilder = rigidBodyBuilder;
         this.rigidBodyDefinitionProvider = rigidBodyDefinitionProvider;
         this.graphicLoader = graphicLoader;
         this.resourceClassLoader = resourceClassLoader;
      }

      public static Function<String, RigidBodyDefinition> toRigidBodyDefinitionProvider(RobotDefinition robotDefinition)
      {
         Map<String, RigidBodyDefinition> nameToDefinitionMap = new HashMap<>();
         robotDefinition.forEachRigidBodyDefinition(definition -> nameToDefinitionMap.put(definition.getName(), definition));
         robotDefinition.forEachOneDoFJointDefinition(joint ->
         {
            if (joint instanceof CrossFourBarJointDefinition)
            {
               CrossFourBarJointDefinition fourBarDefinition = (CrossFourBarJointDefinition) joint;
               nameToDefinitionMap.putIfAbsent(fourBarDefinition.getBodyDA().getName(), fourBarDefinition.getBodyDA());
               nameToDefinitionMap.putIfAbsent(fourBarDefinition.getBodyBC().getName(), fourBarDefinition.getBodyBC());
            }
         });
         return nameToDefinitionMap::get;
      }

      @Override
      public JavaFXRigidBody buildRoot(String bodyName, RigidBodyTransformReadOnly transformToParent, ReferenceFrame parentStationaryFrame)
      {
         RigidBodyBasics rootBody = rigidBodyBuilder.buildRoot(bodyName, transformToParent, parentStationaryFrame);
         return toJavaFXRigidBody(rootBody, rigidBodyDefinitionProvider.apply(rootBody.getName()), graphicLoader, resourceClassLoader);
      }

      @Override
      public JavaFXRigidBody build(String bodyName,
                                   JointBasics parentJoint,
                                   Matrix3DReadOnly momentOfInertia,
                                   double mass,
                                   RigidBodyTransformReadOnly inertiaPose)
      {
         RigidBodyBasics rigidBody = rigidBodyBuilder.build(bodyName, parentJoint, momentOfInertia, mass, inertiaPose);
         return toJavaFXRigidBody(rigidBody, rigidBodyDefinitionProvider.apply(rigidBody.getName()), graphicLoader, resourceClassLoader);
      }
   }

   public static JavaFXRigidBody toJavaFXRigidBody(RigidBodyBasics rigidBody,
                                                   RigidBodyDefinition rigidBodyDefinition,
                                                   Executor graphicLoader,
                                                   ClassLoader resourceClassLoader)
   {
      JavaFXRigidBody javaFXRigidBody = new JavaFXRigidBody(rigidBody);
      List<VisualDefinition> visualDefinitions = rigidBodyDefinition.getVisualDefinitions();

      if (graphicLoader != null)
      {
         graphicLoader.execute(() -> loadRigidBodyGraphic(visualDefinitions, javaFXRigidBody, resourceClassLoader));
      }
      else
      {
         loadRigidBodyGraphic(visualDefinitions, javaFXRigidBody, resourceClassLoader);
      }

      return javaFXRigidBody;
   }

   private static void loadRigidBodyGraphic(List<VisualDefinition> visualDefinitions, JavaFXRigidBody javaFXRigidBody, ClassLoader resourceClassLoader)
   {
      FrameNode rigidBodyGraphic = loadRigidBodyGraphic(visualDefinitions, (RigidBodyReadOnly) javaFXRigidBody, resourceClassLoader);

      if (rigidBodyGraphic != null)
         javaFXRigidBody.setGraphics(rigidBodyGraphic);
   }

   private static FrameNode loadRigidBodyGraphic(RobotDefinition robotDefinition, RigidBodyReadOnly rigidBody)
   {
      return loadRigidBodyGraphic(robotDefinition.getRigidBodyDefinition(rigidBody.getName()), rigidBody, robotDefinition.getResourceClassLoader());
   }

   private static FrameNode loadRigidBodyGraphic(RigidBodyDefinition rigidBodyDefinition, RigidBodyReadOnly rigidBody, ClassLoader resourceClassLoader)
   {
      if (rigidBodyDefinition == null)
         return null;
      else
         return loadRigidBodyGraphic(rigidBodyDefinition.getVisualDefinitions(), rigidBody, resourceClassLoader);
   }

   private static FrameNode loadRigidBodyGraphic(List<VisualDefinition> visualDefinitions, RigidBodyReadOnly rigidBody, ClassLoader resourceClassLoader)
   {
      if (visualDefinitions == null || visualDefinitions.isEmpty())
         return null;

      Node graphicNode = JavaFXVisualTools.collectNodes(visualDefinitions, resourceClassLoader);

      if (graphicNode == null)
         return null;

      ReferenceFrame graphicFrame = rigidBody.isRootBody() ? rigidBody.getBodyFixedFrame() : rigidBody.getParentJoint().getFrameAfterJoint();
      return new FrameNode(graphicFrame, graphicNode);
   }
}
