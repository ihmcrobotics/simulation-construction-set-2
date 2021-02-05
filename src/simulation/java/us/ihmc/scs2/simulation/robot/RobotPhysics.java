package us.ihmc.scs2.simulation.robot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.scs2.simulation.collision.Collidable;
import us.ihmc.scs2.simulation.collision.FrameShapePosePredictor;
import us.ihmc.scs2.simulation.physicsEngine.RobotJointLimitImpulseBasedCalculator;
import us.ihmc.scs2.simulation.physicsEngine.SingleContactImpulseCalculator;
import us.ihmc.scs2.simulation.physicsEngine.SingleRobotFirstOrderIntegrator;
import us.ihmc.scs2.simulation.physicsEngine.SingleRobotForwardDynamicsPlugin;
import us.ihmc.scs2.simulation.physicsEngine.YoRobotJointLimitImpulseBasedCalculator;
import us.ihmc.scs2.simulation.physicsEngine.YoSingleContactImpulseCalculatorPool;
import us.ihmc.yoVariables.registry.YoRegistry;

public class RobotPhysics
{
   private static final String ContactCalculatorNameSuffix = SingleContactImpulseCalculator.class.getSimpleName();

   private final Robot owner;
   private final ReferenceFrame inertialFrame;

   private final YoRegistry environmentContactCalculatorRegistry = new YoRegistry("Environment" + ContactCalculatorNameSuffix);
   private final YoRegistry interRobotContactCalculatorRegistry = new YoRegistry("InterRobot" + ContactCalculatorNameSuffix);
   private final YoRegistry selfContactCalculatorRegistry = new YoRegistry("Self" + ContactCalculatorNameSuffix);
   private final List<Collidable> collidables;

   // TODO Following fields are specific to the type of engine used, they need interfacing.
   private final SingleRobotForwardDynamicsPlugin forwardDynamicsPlugin;
   private final RobotJointLimitImpulseBasedCalculator jointLimitConstraintCalculator;
   private final YoSingleContactImpulseCalculatorPool environmentContactConstraintCalculatorPool;
   private final YoSingleContactImpulseCalculatorPool selfContactConstraintCalculatorPool;
   private final Map<RigidBodyBasics, YoSingleContactImpulseCalculatorPool> interRobotContactConstraintCalculatorPools = new HashMap<>();

   private final SingleRobotFirstOrderIntegrator integrator;

   public RobotPhysics(Robot owner)
   {
      this.owner = owner;
      inertialFrame = owner.getInertialFrame();

      SimRigidBody rootBody = owner.getRootBody();
      collidables = rootBody.subtreeStream().flatMap(body -> body.getCollidables().stream()).collect(Collectors.toList());

      forwardDynamicsPlugin = new SingleRobotForwardDynamicsPlugin(owner);
      FrameShapePosePredictor frameShapePosePredictor = new FrameShapePosePredictor(forwardDynamicsPlugin.getForwardDynamicsCalculator());
      collidables.forEach(collidable -> collidable.setFrameShapePosePredictor(frameShapePosePredictor));

      YoRegistry jointLimitConstraintCalculatorRegistry = new YoRegistry(RobotJointLimitImpulseBasedCalculator.class.getSimpleName());

      jointLimitConstraintCalculator = new YoRobotJointLimitImpulseBasedCalculator(rootBody,
                                                                                   forwardDynamicsPlugin.getForwardDynamicsCalculator(),
                                                                                   jointLimitConstraintCalculatorRegistry);

      environmentContactConstraintCalculatorPool = new YoSingleContactImpulseCalculatorPool(20,
                                                                                            owner.getName() + "Single",
                                                                                            inertialFrame,
                                                                                            rootBody,
                                                                                            forwardDynamicsPlugin.getForwardDynamicsCalculator(),
                                                                                            null,
                                                                                            null,
                                                                                            environmentContactCalculatorRegistry);

      selfContactConstraintCalculatorPool = new YoSingleContactImpulseCalculatorPool(8,
                                                                                     owner.getName() + "Self",
                                                                                     inertialFrame,
                                                                                     rootBody,
                                                                                     forwardDynamicsPlugin.getForwardDynamicsCalculator(),
                                                                                     rootBody,
                                                                                     forwardDynamicsPlugin.getForwardDynamicsCalculator(),
                                                                                     selfContactCalculatorRegistry);

      integrator = new SingleRobotFirstOrderIntegrator(owner);

      owner.getRegistry().addChild(jointLimitConstraintCalculatorRegistry);
      owner.getRegistry().addChild(environmentContactCalculatorRegistry);
      owner.getRegistry().addChild(interRobotContactCalculatorRegistry);
      owner.getRegistry().addChild(selfContactCalculatorRegistry);
   }

   public void updateCollidableBoundingBoxes()
   {
      collidables.forEach(collidable -> collidable.updateBoundingBox(inertialFrame));
   }

   public List<Collidable> getCollidables()
   {
      return collidables;
   }

   public SingleRobotForwardDynamicsPlugin getForwardDynamicsPlugin()
   {
      return forwardDynamicsPlugin;
   }

   public SingleRobotFirstOrderIntegrator getIntegrator()
   {
      return integrator;
   }

   public void resetCalculators()
   {
      environmentContactConstraintCalculatorPool.clear();
      selfContactConstraintCalculatorPool.clear();
      interRobotContactConstraintCalculatorPools.forEach((rigidBodyBasics, calculators) -> calculators.clear());
      integrator.reset();
   }

   public RobotJointLimitImpulseBasedCalculator getJointLimitConstraintCalculator()
   {
      return jointLimitConstraintCalculator;
   }

   public SingleContactImpulseCalculator getOrCreateEnvironmentContactConstraintCalculator()
   {
      return environmentContactConstraintCalculatorPool.nextAvailable();
   }

   public SingleContactImpulseCalculator getOrCreateSelfContactConstraintCalculator()
   {
      return selfContactConstraintCalculatorPool.nextAvailable();
   }

   public SingleContactImpulseCalculator getOrCreateInterRobotContactConstraintCalculator(Robot otherRobot)
   {
      if (otherRobot == null)
         return getOrCreateEnvironmentContactConstraintCalculator();
      if (otherRobot == owner)
         return getOrCreateSelfContactConstraintCalculator();

      YoSingleContactImpulseCalculatorPool calculators = interRobotContactConstraintCalculatorPools.get(otherRobot.getRootBody());

      if (calculators == null)
      {
         calculators = new YoSingleContactImpulseCalculatorPool(8,
                                                                owner.getName() + otherRobot.getName() + "Dual",
                                                                inertialFrame,
                                                                owner.getRootBody(),
                                                                forwardDynamicsPlugin.getForwardDynamicsCalculator(),
                                                                otherRobot.getRootBody(),
                                                                otherRobot.getRobotPhysics().getForwardDynamicsPlugin().getForwardDynamicsCalculator(),
                                                                interRobotContactCalculatorRegistry);
         interRobotContactConstraintCalculatorPools.put(otherRobot.getRootBody(), calculators);
      }

      return calculators.nextAvailable();
   }
}
