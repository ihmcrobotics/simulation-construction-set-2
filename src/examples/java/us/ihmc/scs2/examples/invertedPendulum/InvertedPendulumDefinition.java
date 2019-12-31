package us.ihmc.scs2.examples.invertedPendulum;

import us.ihmc.euclid.Axis;
import us.ihmc.euclid.transform.RigidBodyTransform;
import us.ihmc.scs2.definition.controller.interfaces.ControllerDefinition;
import us.ihmc.scs2.definition.geometry.CylinderGeometryDefinition;
import us.ihmc.scs2.definition.geometry.SphereGeometryDefinition;
import us.ihmc.scs2.definition.robot.RevoluteJointDefinition;
import us.ihmc.scs2.definition.robot.RigidBodyDefinition;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.state.OneDoFJointState;
import us.ihmc.scs2.definition.state.interfaces.JointStateReadOnly;
import us.ihmc.scs2.definition.visual.ColorDefinition;
import us.ihmc.scs2.definition.visual.VisualDefinition;
import us.ihmc.scs2.definition.visual.VisualDefinition.MaterialDefinition;
import us.ihmc.scs2.session.SessionMode;
import us.ihmc.scs2.sessionVisualizer.SessionVisualizer;
import us.ihmc.scs2.sessionVisualizer.SessionVisualizerTopics;
import us.ihmc.scs2.sessionVisualizer.tools.JavaFXMissingTools;
import us.ihmc.scs2.simulation.SimulationSession;

public class InvertedPendulumDefinition extends RobotDefinition
{
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

      RigidBodyDefinition elevator = new RigidBodyDefinition("elevator");
      elevator.getChildrenJoints().add(revoluteJointDefinition);

      setRootBodyDefinition(elevator);
   }

   private JointStateReadOnly initialJointState(String jointName)
   {
      OneDoFJointState pinJoint = new OneDoFJointState();
      pinJoint.setConfiguration(1.0);
      return pinJoint;
   }

   public static void main(String[] args)
   {
      InvertedPendulumDefinition invertedPendulumDefinition = new InvertedPendulumDefinition();

      SimulationSession simulationSession = new SimulationSession();
      simulationSession.addRobot(invertedPendulumDefinition,
                                 ControllerDefinition.emptyControllerDefinition(),
                                 invertedPendulumDefinition::initialJointState);

      SessionVisualizer sessionVisualizer = new SessionVisualizer();
      sessionVisualizer.setInitialZoomOut(6.0);
      JavaFXMissingTools.runApplication(sessionVisualizer, () -> sessionVisualizer.startSession(simulationSession));

      SessionVisualizerTopics topics = sessionVisualizer.getToolkit().getTopics();
//      sessionVisualizer.getToolkit().getMessager().submitMessage(topics.getSessionCurrentMode(), SessionMode.RUNNING);
   }
}