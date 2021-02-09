package us.ihmc.scs2.simulation.robot;

import java.util.ArrayList;
import java.util.List;

import us.ihmc.scs2.definition.robot.ExternalWrenchPointDefinition;
import us.ihmc.scs2.definition.robot.GroundContactPointDefinition;
import us.ihmc.scs2.definition.robot.IMUSensorDefinition;
import us.ihmc.scs2.definition.robot.KinematicPointDefinition;
import us.ihmc.scs2.definition.robot.WrenchSensorDefinition;
import us.ihmc.scs2.simulation.robot.multiBodySystem.SimJointBasics;
import us.ihmc.scs2.simulation.robot.sensors.SimIMUSensor;
import us.ihmc.scs2.simulation.robot.sensors.SimWrenchSensor;

public class SimJointAuxiliaryData
{
   private final SimJointBasics joint;

   private final List<KinematicPoint> kinematicPoints = new ArrayList<>();
   private final List<ExternalWrenchPoint> externalWrenchPoints = new ArrayList<>();
   private final List<GroundContactPoint> groundContactPoints = new ArrayList<>();

   private final List<SimIMUSensor> imuSensors = new ArrayList<>();
   private final List<SimWrenchSensor> wrenchSensors = new ArrayList<>();

   public SimJointAuxiliaryData(SimJointBasics joint)
   {
      this.joint = joint;
   }

   public void update(RobotPhysicsOutput physicsOutput)
   {
      kinematicPoints.forEach(kp -> kp.update());
      externalWrenchPoints.forEach(ewp -> ewp.update());
      groundContactPoints.forEach(gcp -> gcp.update());

      imuSensors.forEach(imu -> imu.update(physicsOutput));
      wrenchSensors.forEach(wrench -> wrench.update(physicsOutput));
   }

   public void addKinematicPoint(KinematicPointDefinition definition)
   {
      kinematicPoints.add(new KinematicPoint(definition, joint));
   }

   public void addExternalWrenchPoint(ExternalWrenchPointDefinition definition)
   {
      externalWrenchPoints.add(new ExternalWrenchPoint(definition, joint));
   }

   public void addGroundContactPoint(GroundContactPointDefinition definition)
   {
      groundContactPoints.add(new GroundContactPoint(definition, joint));
   }

   public void addIMUSensor(IMUSensorDefinition definition)
   {
      imuSensors.add(new SimIMUSensor(definition, joint));
   }

   public void addWrenchSensor(WrenchSensorDefinition definition)
   {
      wrenchSensors.add(new SimWrenchSensor(definition, joint));
   }

}
