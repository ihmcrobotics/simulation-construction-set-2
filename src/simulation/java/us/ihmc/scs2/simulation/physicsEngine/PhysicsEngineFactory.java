package us.ihmc.scs2.simulation.physicsEngine;

import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.yoVariables.registry.YoRegistry;

public interface PhysicsEngineFactory
{
   PhysicsEngine build(ReferenceFrame inertialFrame, YoRegistry rootRegistry);
}
