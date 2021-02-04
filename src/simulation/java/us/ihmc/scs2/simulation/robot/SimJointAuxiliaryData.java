package us.ihmc.scs2.simulation.robot;

import java.util.ArrayList;
import java.util.List;

import us.ihmc.scs2.definition.robot.ExternalWrenchPointDefinition;
import us.ihmc.scs2.definition.robot.GroundContactPointDefinition;
import us.ihmc.scs2.definition.robot.IMUSensorDefinition;
import us.ihmc.scs2.definition.robot.KinematicPointDefinition;
import us.ihmc.scs2.definition.robot.WrenchSensorDefinition;
import us.ihmc.yoVariables.registry.YoRegistry;

public class SimJointAuxiliaryData
{
   private final SimJointBasics joint;
   private final YoRegistry registry;

   private final List<KinematicPoint> kinematicPoints = new ArrayList<>();
   private final List<ExternalWrenchPoint> externalWrenchPoints = new ArrayList<>();
   private final List<GroundContactPoint> groundContactPoints = new ArrayList<>();

   private final List<SimIMUSensor> imuSensors = new ArrayList<>();
   private final List<SimWrenchSensor> wrenchSensors = new ArrayList<>();

   public SimJointAuxiliaryData(SimJointBasics joint, YoRegistry registry)
   {
      this.joint = joint;
      this.registry = registry;
   }

   public void addKinematicPoint(KinematicPointDefinition definition)
   {
      kinematicPoints.add(new KinematicPoint(definition, joint, registry));
   }

   public void addExternalWrenchPoint(ExternalWrenchPointDefinition definition)
   {
      externalWrenchPoints.add(new ExternalWrenchPoint(definition, joint, registry));
   }

   public void addGroundContactPoint(GroundContactPointDefinition definition)
   {
      groundContactPoints.add(new GroundContactPoint(definition, joint, registry));
   }

   public void addIMUSensor(IMUSensorDefinition definition)
   {
      imuSensors.add(new SimIMUSensor(definition, joint, registry));
   }

   public void addWrenchSensor(WrenchSensorDefinition definition)
   {
      wrenchSensors.add(new SimWrenchSensor(definition, joint, registry));
   }
}
