package us.ihmc.scs2.examples.invertedPendulum;

import javafx.stage.Stage;
import javafx.util.Pair;
import us.ihmc.euclid.Axis;
import us.ihmc.euclid.transform.RigidBodyTransform;
import us.ihmc.mecano.multiBodySystem.interfaces.JointReadOnly;
import us.ihmc.mecano.multiBodySystem.interfaces.OneDoFJointReadOnly;
import us.ihmc.scs2.definition.controller.ControllerInput;
import us.ihmc.scs2.definition.controller.ControllerOutput;
import us.ihmc.scs2.definition.controller.interfaces.Controller;
import us.ihmc.scs2.definition.controller.interfaces.ControllerDefinition;
import us.ihmc.scs2.definition.geometry.BoxGeometryDefinition;
import us.ihmc.scs2.definition.geometry.CylinderGeometryDefinition;
import us.ihmc.scs2.definition.geometry.SphereGeometryDefinition;
import us.ihmc.scs2.definition.robot.PrismaticJointDefinition;
import us.ihmc.scs2.definition.robot.RevoluteJointDefinition;
import us.ihmc.scs2.definition.robot.RigidBodyDefinition;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.state.OneDoFJointState;
import us.ihmc.scs2.definition.state.interfaces.JointStateReadOnly;
import us.ihmc.scs2.definition.visual.ColorDefinition;
import us.ihmc.scs2.definition.visual.VisualDefinition;
import us.ihmc.scs2.definition.visual.VisualDefinition.MaterialDefinition;
import us.ihmc.scs2.sessionVisualizer.SessionVisualizer;
import us.ihmc.scs2.sessionVisualizer.SessionVisualizerTopics;
import us.ihmc.scs2.sessionVisualizer.tools.JavaFXMissingTools;
import us.ihmc.scs2.simulation.SimulationSession;
import us.ihmc.yoVariables.variable.YoDouble;

import java.io.File;
import java.nio.file.Paths;

public class InvertedPendulumDefinition extends RobotDefinition
{
   private ControllerInput controllerInput;
   private ControllerOutput controllerOutput;
   private YoDouble simulationTime;
   private OneDoFJointReadOnly sliderJoint;
   private OneDoFJointReadOnly pinJoint;

   public InvertedPendulumDefinition()
   {
      super("invertedPendulum");

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
      ControllerDefinition controller = this::controllerDefinition;
      simulationSession.addRobot(this, controller, this::initialJointState);

      SessionVisualizer sessionVisualizer = new SessionVisualizer();
      double isoCameraZoomOut = 4.0;
      sessionVisualizer.setUserInitialCameraSetup(camera -> camera.changeCameraPosition(isoCameraZoomOut, 0.0, 0.0));


      //      sessionVisualizer.getToolkit().getYoManager().

      JavaFXMissingTools.runApplication(sessionVisualizer, () ->
      {
         sessionVisualizer.startSession(simulationSession);
         //         YoChartGroupPanelController yoChartGroupPanelController = new YoChartGroupPanelController();
         //         sessionVisualizer.getToolkit().addYoChartGroupController(yoChartGroupPanelController);
         //         YoChartGroupConfigurationDefinition definition = new YoChartGroupConfigurationDefinition();
         //         definition.setNumberOfColumns(1);
         //         definition.setNumberOfRows(2);
         //         YoChartConfigurationDefinition qdChart = new YoChartConfigurationDefinition();
         //         qdChart.setYoVariables(Lists.newArrayList("q_pin"));
         //         definition.setChartConfigurations(Lists.newArrayList(qdChart));
         //         YoChartConfigurationDefinition qddPin = new YoChartConfigurationDefinition();
         //         qddPin.setYoVariables(Lists.newArrayList("qdd_pin"));
         //         definition.setChartConfigurations(Lists.newArrayList(qddPin));
         //         yoChartGroupPanelController.setChartGroupConfiguration(definition);
      });

      JavaFXMissingTools.runNFramesLater(5, () ->
      {
         File result = Paths.get(System.getProperty("user.home")).resolve(".ihmc/invertedPendulum.scs2.chart").toFile();
         if (result.exists())
         {
            SessionVisualizerTopics topics = sessionVisualizer.getToolkit().getTopics();
            Stage mainWindow = sessionVisualizer.getToolkit().getMainWindow();
            sessionVisualizer.getToolkit().getMessager().submitMessage(topics.getYoChartGroupLoadConfiguration(), new Pair<>(mainWindow, result));
         }
         simulationTime = (YoDouble) sessionVisualizer.getToolkit().getYoManager().getRootRegistry().getVariable("simulationTime");
      });

      //      SessionVisualizerTopics topics = sessionVisualizer.getToolkit().getTopics();
      //      sessionVisualizer.getToolkit().getMessager().submitMessage(topics.getSessionCurrentMode(), SessionMode.RUNNING);
   }

   private Controller controllerDefinition(ControllerInput controllerInput, ControllerOutput controllerOutput)
   {
      this.controllerInput = controllerInput;
      this.controllerOutput = controllerOutput;

      for (JointReadOnly allJoint : controllerInput.getInput().getAllJoints())
      {
         if (allJoint.getName().equals("pin"))
         {
            pinJoint = (OneDoFJointReadOnly) allJoint;
         }
         else if (allJoint.getName().equals("slider"))
         {
            sliderJoint = (OneDoFJointReadOnly) allJoint;
         }
      }

      return this::doControl;
   }

   private void doControl()
   {

      double t = simulationTime.getDoubleValue();
      long tfloor = (long) Math.floor(t);
      if (tfloor % 2 == 0)
      {
         controllerOutput.getOneDoFJointOutput(pinJoint).setEffort(1.0);
      }
      else
      {
         controllerOutput.getOneDoFJointOutput(pinJoint).setEffort(-1.0);
      }
   }

   private JointStateReadOnly initialJointState(String jointName)
   {
      OneDoFJointState pinJoint = new OneDoFJointState();
      pinJoint.setConfiguration(0.00001);
      return pinJoint;
   }

   public static void main(String[] args)
   {
      new InvertedPendulumDefinition();
   }
}
