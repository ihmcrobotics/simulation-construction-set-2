package us.ihmc.scs2.simulation.robot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;

import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;
import us.ihmc.mecano.algorithms.ForwardDynamicsCalculator;
import us.ihmc.mecano.algorithms.interfaces.RigidBodyTwistProvider;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyReadOnly;
import us.ihmc.mecano.spatial.interfaces.SpatialImpulseReadOnly;
import us.ihmc.mecano.spatial.interfaces.WrenchReadOnly;
import us.ihmc.mecano.tools.JointStateType;
import us.ihmc.mecano.tools.MultiBodySystemTools;
import us.ihmc.scs2.simulation.collision.Collidable;
import us.ihmc.scs2.simulation.collision.FrameShapePosePredictor;
import us.ihmc.scs2.simulation.physicsEngine.RobotJointLimitImpulseBasedCalculator;
import us.ihmc.scs2.simulation.physicsEngine.SingleContactImpulseCalculator;
import us.ihmc.scs2.simulation.physicsEngine.YoRobotJointLimitImpulseBasedCalculator;
import us.ihmc.scs2.simulation.physicsEngine.YoSingleContactImpulseCalculatorPool;
import us.ihmc.scs2.simulation.robot.multiBodySystem.SimRigidBody;
import us.ihmc.scs2.simulation.screwTools.RigidBodyDeltaTwistCalculator;
import us.ihmc.scs2.simulation.screwTools.RigidBodyImpulseRegistry;
import us.ihmc.scs2.simulation.screwTools.RigidBodyWrenchRegistry;
import us.ihmc.scs2.simulation.screwTools.SimJointStateType;
import us.ihmc.scs2.simulation.screwTools.SimMultiBodySystemTools;
import us.ihmc.scs2.simulation.screwTools.SingleRobotFirstOrderIntegrator;
import us.ihmc.yoVariables.registry.YoRegistry;

public class RobotPhysics
{
   private static final String ContactCalculatorNameSuffix = SingleContactImpulseCalculator.class.getSimpleName();

   private final Robot owner;
   private final ReferenceFrame inertialFrame;

   private final YoRegistry environmentContactCalculatorRegistry = new YoRegistry("Environment" + ContactCalculatorNameSuffix);
   private final YoRegistry interRobotContactCalculatorRegistry = new YoRegistry("InterRobot" + ContactCalculatorNameSuffix);
   private final YoRegistry selfContactCalculatorRegistry = new YoRegistry("Self" + ContactCalculatorNameSuffix);

   private final DMatrixRMaj jointDeltaVelocityMatrix;
   private final RigidBodyDeltaTwistCalculator rigidBodyDeltaTwistCalculator;
   private final RigidBodyTwistProvider rigidBodyDeltaTwistProvider;
   private final RigidBodyWrenchRegistry rigidBodyWrenchRegistry = new RigidBodyWrenchRegistry();
   private final RigidBodyImpulseRegistry rigidBodyImpulseRegistry = new RigidBodyImpulseRegistry();

   private final List<Collidable> collidables;

   // TODO Following fields are specific to the type of engine used, they need interfacing.
   private final ForwardDynamicsCalculator forwardDynamicsCalculator;
   private final RobotJointLimitImpulseBasedCalculator jointLimitConstraintCalculator;
   private final YoSingleContactImpulseCalculatorPool environmentContactConstraintCalculatorPool;
   private final YoSingleContactImpulseCalculatorPool selfContactConstraintCalculatorPool;
   private final Map<RigidBodyBasics, YoSingleContactImpulseCalculatorPool> interRobotContactConstraintCalculatorPools = new HashMap<>();

   private final SingleRobotFirstOrderIntegrator integrator;

   private final RobotPhysicsOutput physicsOutput;

   public RobotPhysics(Robot owner)
   {
      this.owner = owner;
      inertialFrame = owner.getInertialFrame();

      jointDeltaVelocityMatrix = new DMatrixRMaj(MultiBodySystemTools.computeDegreesOfFreedom(owner.getAllJoints()), 1);
      rigidBodyDeltaTwistCalculator = new RigidBodyDeltaTwistCalculator(inertialFrame, owner.getJointMatrixIndexProvider(), jointDeltaVelocityMatrix);
      rigidBodyDeltaTwistProvider = rigidBodyDeltaTwistCalculator.getDeltaTwistProvider();

      SimRigidBody rootBody = owner.getRootBody();
      collidables = rootBody.subtreeStream().flatMap(body -> body.getCollidables().stream()).collect(Collectors.toList());

      forwardDynamicsCalculator = new ForwardDynamicsCalculator(owner);
      FrameShapePosePredictor frameShapePosePredictor = new FrameShapePosePredictor(forwardDynamicsCalculator);
      collidables.forEach(collidable -> collidable.setFrameShapePosePredictor(frameShapePosePredictor));

      YoRegistry jointLimitConstraintCalculatorRegistry = new YoRegistry(RobotJointLimitImpulseBasedCalculator.class.getSimpleName());

      jointLimitConstraintCalculator = new YoRobotJointLimitImpulseBasedCalculator(rootBody, forwardDynamicsCalculator, jointLimitConstraintCalculatorRegistry);

      environmentContactConstraintCalculatorPool = new YoSingleContactImpulseCalculatorPool(20,
                                                                                            owner.getName() + "Single",
                                                                                            inertialFrame,
                                                                                            rootBody,
                                                                                            forwardDynamicsCalculator,
                                                                                            null,
                                                                                            null,
                                                                                            environmentContactCalculatorRegistry);

      selfContactConstraintCalculatorPool = new YoSingleContactImpulseCalculatorPool(8,
                                                                                     owner.getName() + "Self",
                                                                                     inertialFrame,
                                                                                     rootBody,
                                                                                     forwardDynamicsCalculator,
                                                                                     rootBody,
                                                                                     forwardDynamicsCalculator,
                                                                                     selfContactCalculatorRegistry);

      integrator = new SingleRobotFirstOrderIntegrator();

      physicsOutput = new RobotPhysicsOutput(forwardDynamicsCalculator.getAccelerationProvider(), rigidBodyDeltaTwistProvider, rigidBodyWrenchRegistry, rigidBodyImpulseRegistry);

      owner.getRegistry().addChild(jointLimitConstraintCalculatorRegistry);
      owner.getRegistry().addChild(environmentContactCalculatorRegistry);
      owner.getRegistry().addChild(interRobotContactCalculatorRegistry);
      owner.getRegistry().addChild(selfContactCalculatorRegistry);
   }

   public void resetCalculators()
   {
      jointDeltaVelocityMatrix.zero();
      forwardDynamicsCalculator.setExternalWrenchesToZero();
      rigidBodyDeltaTwistCalculator.reset();
      rigidBodyWrenchRegistry.reset();
      rigidBodyImpulseRegistry.reset();
      environmentContactConstraintCalculatorPool.clear();
      selfContactConstraintCalculatorPool.clear();
      interRobotContactConstraintCalculatorPools.forEach((rigidBodyBasics, calculators) -> calculators.clear());
   }

   public void addJointVelocityChange(DMatrixRMaj velocityChange)
   {
      if (velocityChange == null)
         return;
      CommonOps_DDRM.addEquals(jointDeltaVelocityMatrix, velocityChange);
   }

   public void addRigidBodyExternalWrench(RigidBodyReadOnly target, WrenchReadOnly wrenchToAdd)
   {
      rigidBodyWrenchRegistry.addWrench(target, wrenchToAdd);
   }

   public void addRigidBodyExternalImpulse(RigidBodyReadOnly target, SpatialImpulseReadOnly wrenchToAdd)
   {
      rigidBodyImpulseRegistry.addImpulse(target, wrenchToAdd);
   }

   public void updateCollidableBoundingBoxes()
   {
      collidables.forEach(collidable -> collidable.updateBoundingBox(inertialFrame));
   }

   public List<Collidable> getCollidables()
   {
      return collidables;
   }

   public ForwardDynamicsCalculator getForwardDynamicsCalculator()
   {
      return forwardDynamicsCalculator;
   }

   public void doForwardDynamics(Vector3DReadOnly gravity)
   {
      forwardDynamicsCalculator.setGravitionalAcceleration(gravity);
      forwardDynamicsCalculator.compute();
   }

   public void writeJointAccelerations()
   {
      MultiBodySystemTools.insertJointsState(owner.getJointsToConsider(), JointStateType.ACCELERATION, forwardDynamicsCalculator.getJointAccelerationMatrix());
   }

   public void writeJointDeltaVelocities()
   {
      SimMultiBodySystemTools.insertJointsState(owner.getJointsToConsider(), SimJointStateType.VELOCITY_CHANGE, jointDeltaVelocityMatrix);
   }

   public void integrateState(double dt)
   {
      physicsOutput.setDT(dt);
      integrator.integrate(dt, owner);
   }

   public RobotPhysicsOutput getPhysicsOutput()
   {
      return physicsOutput;
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
                                                                forwardDynamicsCalculator,
                                                                otherRobot.getRootBody(),
                                                                otherRobot.getRobotPhysics().getForwardDynamicsCalculator(),
                                                                interRobotContactCalculatorRegistry);
         interRobotContactConstraintCalculatorPools.put(otherRobot.getRootBody(), calculators);
      }

      return calculators.nextAvailable();
   }

   public RigidBodyTwistProvider getRigidBodyTwistChangeProvider()
   {
      return rigidBodyDeltaTwistProvider;
   }
}
