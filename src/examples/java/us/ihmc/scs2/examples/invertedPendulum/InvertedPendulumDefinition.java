package us.ihmc.scs2.examples.invertedPendulum;

import javafx.stage.Stage;
import javafx.util.Pair;
import us.ihmc.euclid.Axis;
import us.ihmc.euclid.transform.RigidBodyTransform;
import us.ihmc.scs2.definition.geometry.BoxGeometryDefinition;
import us.ihmc.scs2.definition.geometry.CylinderGeometryDefinition;
import us.ihmc.scs2.definition.geometry.SphereGeometryDefinition;
import us.ihmc.scs2.definition.robot.PrismaticJointDefinition;
import us.ihmc.scs2.definition.robot.RevoluteJointDefinition;
import us.ihmc.scs2.definition.robot.RigidBodyDefinition;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.visual.ColorDefinition;
import us.ihmc.scs2.definition.visual.VisualDefinition;
import us.ihmc.scs2.definition.visual.VisualDefinition.MaterialDefinition;
import us.ihmc.scs2.sessionVisualizer.SessionVisualizer;
import us.ihmc.scs2.sessionVisualizer.SessionVisualizerTopics;
import us.ihmc.scs2.sessionVisualizer.controllers.menu.YoChartMenuController;
import us.ihmc.scs2.sessionVisualizer.tools.JavaFXMissingTools;
import us.ihmc.scs2.simulation.SimulationSession;

import java.io.File;
import java.nio.file.Paths;

public class InvertedPendulumDefinition extends RobotDefinition
{
   public InvertedPendulumDefinition()
   {
      super("InvertedPendulumRobot");

      double rodLength = 1.0;

      RigidBodyDefinition pea = new RigidBodyDefinition("pea");
      pea.setMass(0.5);
      pea.getInertiaPose().setTranslationZ(rodLength);

      SphereGeometryDefinition sphereGeometryDefinition = new SphereGeometryDefinition(0.1);
      ColorDefinition ballColor = new ColorDefinition(1.0, 0.0, 0.0);
      MaterialDefinition materialDefinition = new MaterialDefinition(ballColor);
      RigidBodyTransform visualPose = new RigidBodyTransform();
      visualPose.setTranslationZ(rodLength);
      pea.addVisualDefinition(new VisualDefinition(visualPose, sphereGeometryDefinition, materialDefinition));

      CylinderGeometryDefinition cylinderGeometryDefinition = new CylinderGeometryDefinition(rodLength, 0.025);
      ColorDefinition rodColor = new ColorDefinition(0.0, 0.0, 1.0);
      MaterialDefinition rodMaterial = new MaterialDefinition(rodColor);
      RigidBodyTransform rodGraphicPose = new RigidBodyTransform();
      rodGraphicPose.setTranslationZ(rodLength / 2.0);
      pea.addVisualDefinition(new VisualDefinition(rodGraphicPose, cylinderGeometryDefinition, rodMaterial));

      RevoluteJointDefinition revoluteJointDefinition = new RevoluteJointDefinition("pin");
      revoluteJointDefinition.setSuccessor(pea);
      revoluteJointDefinition.getAxis().set(Axis.X);

      RigidBodyDefinition cart = new RigidBodyDefinition("cart");
      cart.setMass(1.0);

      BoxGeometryDefinition boxGeometryDefinition = new BoxGeometryDefinition(0.1, 0.2, 0.1);
      ColorDefinition boxColor = new ColorDefinition(0.0, 1.0, 0.0);
      MaterialDefinition boxMaterial = new MaterialDefinition(boxColor);
      RigidBodyTransform boxPose = new RigidBodyTransform();
      cart.addVisualDefinition(new VisualDefinition(boxPose, boxGeometryDefinition, boxMaterial));

      PrismaticJointDefinition prismaticJointDefinition = new PrismaticJointDefinition("slider");
      prismaticJointDefinition.setSuccessor(cart);
      prismaticJointDefinition.getAxis().set(Axis.Y);

      cart.getChildrenJoints().add(revoluteJointDefinition);

      RigidBodyDefinition elevator = new RigidBodyDefinition("elevator");
      elevator.getChildrenJoints().add(prismaticJointDefinition);

      setRootBodyDefinition(elevator);

      SimulationSession simulationSession = new SimulationSession();
      InvertedPendulumController invertedPendulumController = new InvertedPendulumController();
      simulationSession.addRobot(this, invertedPendulumController, invertedPendulumController::initialJointState);

      invertedPendulumController.registerYoVariables(simulationSession.getRootRegistry());

      SessionVisualizer sessionVisualizer = new SessionVisualizer();
      double isoCameraZoomOut = 4.0;
      sessionVisualizer.setUserInitialCameraSetup(camera -> camera.changeCameraPosition(isoCameraZoomOut, 0.0, 0.0));

      JavaFXMissingTools.runApplication(sessionVisualizer, () ->
      {
         sessionVisualizer.startSession(simulationSession);
      });

      JavaFXMissingTools.runNFramesLater(5, () ->
      {
         File result = Paths.get(System.getProperty("user.home")).resolve(".ihmc/invertedPendulum.scs2.chart").toFile();
         Stage mainWindow = sessionVisualizer.getToolkit().getMainWindow();
         mainWindow.setX(5);
         mainWindow.setY(200);
         mainWindow.setWidth(1500);
         mainWindow.setHeight(1000);
         if (result.exists())
         {
            SessionVisualizerTopics topics = sessionVisualizer.getToolkit().getTopics();
            sessionVisualizer.getToolkit().getMessager().submitMessage(topics.getYoChartGroupLoadConfiguration(), new Pair<>(mainWindow, result));
         }

         Stage newWindow = YoChartMenuController.newSecondaryChartWindow(sessionVisualizer.getToolkit());
         newWindow.show();
         newWindow.setX(1600);
         newWindow.setY(200);
         newWindow.setWidth(1000);
         newWindow.setHeight(1000);
         result = Paths.get(System.getProperty("user.home")).resolve(".ihmc/invertedPendulum2ndWindow.scs2.chart").toFile();
         if (result.exists())
         {
            SessionVisualizerTopics topics = sessionVisualizer.getToolkit().getTopics();
            sessionVisualizer.getToolkit().getMessager().submitMessage(topics.getYoChartGroupLoadConfiguration(), new Pair<>(newWindow, result));
         }
      });

      //      SessionVisualizerTopics topics = sessionVisualizer.getToolkit().getTopics();
      //      sessionVisualizer.getToolkit().getMessager().submitMessage(topics.getSessionCurrentMode(), SessionMode.RUNNING);
   }

   public static void main(String[] args)
   {
      new InvertedPendulumDefinition();
   }
}
