package us.ihmc.scs2.simulation.robot.sensors;

import java.util.function.Function;
import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.log.LogTools;
import us.ihmc.mecano.algorithms.interfaces.RigidBodyAccelerationProvider;
import us.ihmc.mecano.algorithms.interfaces.RigidBodyTwistProvider;
import us.ihmc.mecano.frames.MovingReferenceFrame;
import us.ihmc.mecano.multiBodySystem.interfaces.JointReadOnly;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyReadOnly;
import us.ihmc.mecano.spatial.SpatialAcceleration;
import us.ihmc.mecano.spatial.Wrench;
import us.ihmc.mecano.spatial.interfaces.FixedFrameWrenchBasics;
import us.ihmc.mecano.spatial.interfaces.SpatialAccelerationBasics;
import us.ihmc.mecano.spatial.interfaces.SpatialAccelerationReadOnly;
import us.ihmc.mecano.spatial.interfaces.SpatialImpulseReadOnly;
import us.ihmc.mecano.spatial.interfaces.SpatialVectorReadOnly;
import us.ihmc.mecano.spatial.interfaces.TwistReadOnly;
import us.ihmc.mecano.spatial.interfaces.WrenchBasics;
import us.ihmc.mecano.spatial.interfaces.WrenchReadOnly;
import us.ihmc.mecano.yoVariables.spatial.YoFixedFrameWrench;
import us.ihmc.scs2.definition.robot.WrenchSensorDefinition;
import us.ihmc.scs2.definition.robot.WrenchSensorDefinition.SensorLocation;
import us.ihmc.scs2.simulation.robot.RobotPhysicsOutput;
import us.ihmc.scs2.simulation.robot.multiBodySystem.interfaces.SimJointBasics;
import us.ihmc.scs2.simulation.robot.multiBodySystem.interfaces.SimRigidBodyBasics;
import us.ihmc.yoVariables.euclid.referenceFrame.YoFrameVector3D;
import us.ihmc.yoVariables.registry.YoRegistry;
import us.ihmc.yoVariables.variable.YoBoolean;
import us.ihmc.yoVariables.variable.YoDouble;
import us.ihmc.yoVariables.variable.YoEnum;

public class SimWrenchSensor extends SimSensor
{
   private static final SensorLocation DEFAULT_LOCATION = SensorLocation.AFTER_BODY;

   private final YoFixedFrameWrench wrench;
   private final YoDouble filterBreakFrequency;
   private final YoBoolean filterInitialized;
   private final YoFixedFrameWrench wrenchFiltered;
   private final YoEnum<SensorLocation> sensorLocation;

   public SimWrenchSensor(WrenchSensorDefinition definition, SimJointBasics parentJoint)
   {
      this(definition.getName(), parentJoint, definition.getTransformToJoint());
      setSamplingRate(toSamplingRate(definition.getUpdatePeriod()));
      setSensorLocation(definition.getLocation());
   }

   public SimWrenchSensor(String name, SimJointBasics parentJoint, RigidBodyTransformReadOnly transformToParent)
   {
      super(name, parentJoint, transformToParent);

      YoRegistry registry = parentJoint.getRegistry();
      wrench = new YoFixedFrameWrench(parentJoint.getSuccessor().getBodyFixedFrame(),
                                      new YoFrameVector3D(name + "Moment", getFrame(), registry),
                                      new YoFrameVector3D(name + "Force", getFrame(), registry));

      filterBreakFrequency = new YoDouble(name + "FilterBreakFrequency", registry);
      filterBreakFrequency.set(Double.POSITIVE_INFINITY);
      getSamplingRate().addListener(v -> filterBreakFrequency.set(getSamplingRate().getValue() * ANTIALIASING_FILTER_RATIO));
      filterInitialized = new YoBoolean(name + "FilterInitialized", registry);
      wrenchFiltered = new YoFixedFrameWrench(parentJoint.getSuccessor().getBodyFixedFrame(),
                                              new YoFrameVector3D(name + "MomentFiltered", getFrame(), registry),
                                              new YoFrameVector3D(name + "ForceFiltered", getFrame(), registry));
      sensorLocation = new YoEnum<>(name + "SensorLocation", registry, SensorLocation.class);
      sensorLocation.set(DEFAULT_LOCATION);
   }

   private final Wrench intermediateWrench = new Wrench();
   private final SpatialAcceleration intermediateAcceleration = new SpatialAcceleration();

   @Override
   public void update(RobotPhysicsOutput robotPhysicsOutput)
   {
      super.update(robotPhysicsOutput);

      SimRigidBodyBasics body = getParentJoint().getSuccessor();

      double dt = robotPhysicsOutput.getDT();

      wrench.setToZero();

      sumSubtreeExternalWrenches(body, robotPhysicsOutput, wrench, intermediateWrench);

      switch (sensorLocation.getValue())
      {
         case AFTER_BODY:
         {
            // We omit the dynamic wrench generated by this body and only consider the subtree.
            for (JointReadOnly childJoint : body.getChildrenJoints())
               sumSubtreeDynamicWrenches(childJoint.getSuccessor(), robotPhysicsOutput, wrench, intermediateWrench, intermediateAcceleration);
            break;
         }
         case BEFORE_BODY:
         {
            sumSubtreeDynamicWrenches(body, robotPhysicsOutput, wrench, intermediateWrench, intermediateAcceleration);
            break;
         }
         default:
         {
            throw new IllegalArgumentException("Unexpected value: " + sensorLocation.getValue());
         }
      }

      if (dt <= 0.0)
      { // Initialize
         filterInitialized.set(false);
      }

      if (!filterInitialized.getValue())
      {
         wrenchFiltered.set(wrench);
         filterInitialized.set(true);
      }
      else
      {
         if (Double.isInfinite(filterBreakFrequency.getValue()))
            filterBreakFrequency.set(ANTIALIASING_FILTER_RATIO / dt);
         double alpha = 1.0 - computeLowPassFilterAlpha(filterBreakFrequency.getValue(), dt);
         wrenchFiltered.interpolate(wrench, alpha);
      }
   }

   private static void sumSubtreeExternalWrenches(RigidBodyReadOnly start,
                                                  RobotPhysicsOutput robotPhysicsOutput,
                                                  FixedFrameWrenchBasics wrenchSumToPack,
                                                  WrenchBasics intermediateWrench)
   {
      Function<RigidBodyReadOnly, WrenchReadOnly> externalWrenchProvider = robotPhysicsOutput.getExternalWrenchProvider();
      Function<RigidBodyReadOnly, SpatialImpulseReadOnly> externalImpulseProvider = robotPhysicsOutput.getExternalImpulseProvider();

      double dt = robotPhysicsOutput.getDT();
      WrenchReadOnly externalWrench = externalWrenchProvider == null ? null : externalWrenchProvider.apply(start);
      SpatialImpulseReadOnly externalImpulse = externalImpulseProvider == null ? null : externalImpulseProvider.apply(start);

      if (dt != 0.0 && externalImpulse != null)
      {
         intermediateWrench.setIncludingFrame(externalImpulse);
         intermediateWrench.scale(1.0 / dt);

         if (externalWrench != null)
         {
            intermediateWrench.add((SpatialVectorReadOnly) externalWrench);
         }

         intermediateWrench.changeFrame(wrenchSumToPack.getReferenceFrame());
         wrenchSumToPack.add((SpatialVectorReadOnly) intermediateWrench);
      }
      else if (externalWrench != null)
      {
         intermediateWrench.setIncludingFrame(externalWrench);
         intermediateWrench.changeFrame(wrenchSumToPack.getReferenceFrame());
         wrenchSumToPack.add((SpatialVectorReadOnly) intermediateWrench);
      }
      else
      {
         // Wrench is zero, do nothing
      }

      for (JointReadOnly childJoint : start.getChildrenJoints())
      {
         sumSubtreeExternalWrenches(childJoint.getSuccessor(), robotPhysicsOutput, wrenchSumToPack, intermediateWrench);
      }
   }

   private static void sumSubtreeDynamicWrenches(RigidBodyReadOnly start,
                                                 RobotPhysicsOutput robotPhysicsOutput,
                                                 FixedFrameWrenchBasics wrenchSumToPack,
                                                 WrenchBasics intermediateWrench,
                                                 SpatialAccelerationBasics intermediateAcceleration)
   {
      double dt = robotPhysicsOutput.getDT();
      RigidBodyTwistProvider deltaTwistProvider = robotPhysicsOutput.getDeltaTwistProvider();
      RigidBodyAccelerationProvider accelerationProvider = robotPhysicsOutput.getAccelerationProvider();

      MovingReferenceFrame bodyFixedFrame = start.getBodyFixedFrame();

      boolean isAccelerationSet = false;

      if (dt != 0.0 && deltaTwistProvider != null)
      {
         TwistReadOnly bodyDeltaTwist = deltaTwistProvider.getTwistOfBody(start);

         if (bodyDeltaTwist != null)
         {
            intermediateAcceleration.setIncludingFrame(bodyDeltaTwist);
            intermediateAcceleration.scale(1.0 / dt);
            isAccelerationSet = true;
         }
      }

      SpatialAccelerationReadOnly bodyAcceleration = accelerationProvider.getAccelerationOfBody(start);

      if (bodyAcceleration != null)
      {
         if (!isAccelerationSet)
            intermediateAcceleration.setIncludingFrame(bodyAcceleration);
         else
            intermediateAcceleration.add((SpatialVectorReadOnly) bodyAcceleration);
         isAccelerationSet = true;
      }

      if (isAccelerationSet)
         start.getInertia().computeDynamicWrenchFast(intermediateAcceleration, bodyFixedFrame.getTwistOfFrame(), intermediateWrench);
      else
         start.getInertia().computeDynamicWrenchFast(null, bodyFixedFrame.getTwistOfFrame(), intermediateWrench);

      intermediateWrench.changeFrame(wrenchSumToPack.getReferenceFrame());
      wrenchSumToPack.sub((SpatialVectorReadOnly) intermediateWrench);

      for (JointReadOnly childJoint : start.getChildrenJoints())
      {
         sumSubtreeDynamicWrenches(childJoint.getSuccessor(), robotPhysicsOutput, wrenchSumToPack, intermediateWrench, intermediateAcceleration);
      }
   }

   /**
    * Sets hints as to whether this simulated sensor should measured the dynamic wrench of the
    * rigid-body it is attached to.
    * 
    * @param sensorLocation the location hint.
    * @see SensorLocation
    */
   public void setSensorLocation(SensorLocation sensorLocation)
   {
      if (sensorLocation == null)
      {
         LogTools.error("Given sensor location is null, setting to {}", DEFAULT_LOCATION);
         this.sensorLocation.set(DEFAULT_LOCATION);
      }
      else
      {
         this.sensorLocation.set(sensorLocation);
      }
   }

   public YoEnum<SensorLocation> getSensorLocation()
   {
      return sensorLocation;
   }

   /**
    * Overrides the default break frequency used to compute the filtered measurements.
    * 
    * @param breakFrequency the new break frequency.
    */
   public void setFilterBreakFrequency(double breakFrequency)
   {
      filterBreakFrequency.set(breakFrequency);
   }

   /**
    * Gets the break frequency variable used to compute the filtered measurements.
    * 
    * @return the filter break frequency.
    */
   public YoDouble getFilterBreakFrequency()
   {
      return filterBreakFrequency;
   }

   /**
    * Gets the variable indicating whether the filtered measurements have been initialized or not.
    * 
    * @return the filter initialized variable.
    */
   public YoBoolean getFilterInitialized()
   {
      return filterInitialized;
   }

   /**
    * Gets the raw measured wrench.
    * <p>
    * It is recommended to use the filtered measurement which provides anti-aliasing.
    * </p>
    * 
    * @return the raw wrench measurement.
    * @see #getWrenchFiltered()
    */
   public YoFixedFrameWrench getWrench()
   {
      return wrench;
   }

   /**
    * Gets the filtered wrench measurement.
    * 
    * @return the filtered wrench measurement.
    */
   public YoFixedFrameWrench getWrenchFiltered()
   {
      return wrenchFiltered;
   }
}
