package us.ihmc.scs2.simulation.physicsEngine;

import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;
import us.ihmc.mecano.algorithms.interfaces.RigidBodyAccelerationProvider;
import us.ihmc.mecano.algorithms.interfaces.RigidBodyTwistProvider;
import us.ihmc.mecano.multiBodySystem.interfaces.MultiBodySystemReadOnly;

public interface InertialMeasurementReader
{
   void initialize(MultiBodySystemReadOnly multiBodySystem, RigidBodyAccelerationProvider accelerationProvider, RigidBodyTwistProvider twistChangeProvider);

   void read(double dt, Vector3DReadOnly gravity);
}
