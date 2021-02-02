package us.ihmc.scs2.examples.model.viewer;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.javaFXToolkit.scenes.View3DFactory;
import us.ihmc.log.LogTools;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.robot.sdf.SDFTools;
import us.ihmc.scs2.definition.robot.sdf.items.SDFModel;
import us.ihmc.scs2.definition.robot.urdf.URDFTools;
import us.ihmc.scs2.definition.robot.urdf.items.URDFModel;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerIOTools;
import us.ihmc.scs2.sessionVisualizer.jfx.multiBodySystem.FrameNode;
import us.ihmc.scs2.sessionVisualizer.jfx.multiBodySystem.JavaFXMultiBodySystemFactories;
import us.ihmc.scs2.sessionVisualizer.jfx.multiBodySystem.JavaFXRigidBody;

public class SimpleModelViewer extends Application
{
   private static final ReferenceFrame rootFrame = ReferenceFrame.getWorldFrame();
   private static final String MODEL_FILE_KEY = "ROBOT_MODEL_FILE_SIMPLE_VIEWER";
   private File modelFile = null;

   @Override
   public void start(Stage primaryStage) throws Exception
   {
      if (modelFile == null)
      {
         FileChooser fileChooser = new FileChooser();
         fileChooser.getExtensionFilters().addAll(new ExtensionFilter("All Model Files", "*.urdf", "*.sdf"),
                                                  new ExtensionFilter("URDF Files", "*.urdf"),
                                                  new ExtensionFilter("SDF Files", "*.sdf"));
         fileChooser.setInitialDirectory(SessionVisualizerIOTools.getDefaultFilePath(MODEL_FILE_KEY));
         modelFile = fileChooser.showOpenDialog(primaryStage);
      }

      if (modelFile == null)
      {
         Platform.exit();
         return;
      }

      SessionVisualizerIOTools.setDefaultFilePath(MODEL_FILE_KEY, modelFile);

      RobotDefinition robotDefinition;

      if (modelFile.getName().toLowerCase().endsWith("urdf"))
      {
         URDFModel urdfModel = URDFTools.loadURDFModel(modelFile);
         robotDefinition = URDFTools.toFloatingRobotDefinition(urdfModel);
      }
      else if (modelFile.getName().toLowerCase().endsWith("sdf"))
      {
         SDFModel sdfModel = SDFTools.loadSDFRoot(modelFile).getModels().get(0);
         robotDefinition = SDFTools.toFloatingRobotDefinition(sdfModel);
      }
      else
      {
         LogTools.error("Unhandled file type: " + modelFile.getName());
         return;
      }

      RigidBodyBasics rigidBody = robotDefinition.newIntance(rootFrame);
      JavaFXRigidBody javaFXRootBody = JavaFXMultiBodySystemFactories.toJavaFXMultiBodySystem(rigidBody, rootFrame, robotDefinition);
      javaFXRootBody.updateFramesRecursively();
      javaFXRootBody.updateSubtreeGraphics();

      List<Node> graphicsNodes = collectGraphicsNodes(javaFXRootBody);

      View3DFactory view3dFactory = new View3DFactory(1024, 768);
      view3dFactory.addWorldCoordinateSystem(0.2);
      view3dFactory.addNodesToView(graphicsNodes);
      view3dFactory.addCameraController();

      primaryStage.setTitle("Robot Model Viewer");
      primaryStage.setScene(view3dFactory.getScene());
      primaryStage.show();
   }

   public static List<Node> collectGraphicsNodes(JavaFXRigidBody rootBody)
   {
      return rootBody.subtreeStream().map(JavaFXRigidBody::getGraphics).filter(graphics -> graphics != null).map(FrameNode::getNode)
                     .collect(Collectors.toList());
   }

   @Override
   public void stop()
   {
      System.out.println(getClass().getSimpleName() + " is going down.");
   }

   public static void main(String[] args)
   {
      launch(args);
   }
}
