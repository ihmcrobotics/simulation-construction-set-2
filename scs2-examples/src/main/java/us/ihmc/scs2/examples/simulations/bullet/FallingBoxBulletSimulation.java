package us.ihmc.scs2.examples.simulations.bullet;

import us.ihmc.euclid.geometry.Pose3D;
import us.ihmc.euclid.tuple3D.Vector3D;
import us.ihmc.scs2.definition.state.SixDoFJointState;
import us.ihmc.scs2.examples.simulations.BoxRobotDefinition;
import us.ihmc.scs2.examples.simulations.SlopeGroundDefinition;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizer;
import us.ihmc.scs2.simulation.SimulationSession;
import us.ihmc.scs2.simulation.bullet.physicsEngine.BulletMultiBodyJointParameters;
import us.ihmc.scs2.simulation.bullet.physicsEngine.BulletMultiBodyParameters;
import us.ihmc.scs2.simulation.bullet.physicsEngine.BulletPhysicsEngine;

public class FallingBoxBulletSimulation
{
   public static void main(String[] args)
   {
      BoxRobotDefinition definition = new BoxRobotDefinition();
      SixDoFJointState initialJointState = new SixDoFJointState();
      initialJointState.setConfiguration(new Pose3D(0.0, 0.0, 3.0, 0.0, 0.0, 0.0));
      initialJointState.setVelocity(new Vector3D(1.0, 0.0, 0.0), new Vector3D(2.0, 0.0, 0.0));
      definition.getRootJointDefinitions().get(0).setInitialJointState(initialJointState);

      SimulationSession simulationSession = new SimulationSession((frame,
                                                                   rootRegistry) -> new BulletPhysicsEngine(frame,
                                                                                                            rootRegistry,
                                                                                                            BulletMultiBodyParameters.defaultBulletMultiBodyParameters(),
                                                                                                            BulletMultiBodyJointParameters.defaultBulletMultiBodyJointParameters()));
      simulationSession.addRobot(definition);
      simulationSession.addTerrainObject(new SlopeGroundDefinition(Math.toRadians(0.0)));

      //SessionVisualizer.startSessionVisualizer(simulationSession);
      SessionVisualizer sessionVisualizer = BulletExampleSimulationTools.startSessionVisualizerWithDebugDrawing(simulationSession);
      sessionVisualizer.getToolkit().getSession().runTick();
   }
}
