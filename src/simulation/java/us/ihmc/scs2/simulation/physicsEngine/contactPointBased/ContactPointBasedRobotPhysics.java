package us.ihmc.scs2.simulation.physicsEngine.contactPointBased;

import java.util.List;
import java.util.stream.Collectors;

import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;
import us.ihmc.mecano.algorithms.ForwardDynamicsCalculator;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyReadOnly;
import us.ihmc.mecano.spatial.interfaces.WrenchReadOnly;
import us.ihmc.mecano.tools.JointStateType;
import us.ihmc.mecano.tools.MultiBodySystemTools;
import us.ihmc.scs2.simulation.collision.Collidable;
import us.ihmc.scs2.simulation.collision.FrameShapePosePredictor;
import us.ihmc.scs2.simulation.physicsEngine.impulseBased.RobotJointLimitImpulseBasedCalculator;
import us.ihmc.scs2.simulation.physicsEngine.impulseBased.SingleContactImpulseCalculator;
import us.ihmc.scs2.simulation.robot.Robot;
import us.ihmc.scs2.simulation.robot.RobotPhysicsOutput;
import us.ihmc.scs2.simulation.robot.multiBodySystem.SimRigidBody;
import us.ihmc.scs2.simulation.screwTools.RigidBodyWrenchRegistry;
import us.ihmc.scs2.simulation.screwTools.SingleRobotFirstOrderIntegrator;
import us.ihmc.yoVariables.registry.YoRegistry;

public class ContactPointBasedRobotPhysics
{
   private static final String ContactCalculatorNameSuffix = SingleContactImpulseCalculator.class.getSimpleName();

   private final Robot owner;
   private final ReferenceFrame inertialFrame;

   private final YoRegistry environmentContactCalculatorRegistry = new YoRegistry("Environment" + ContactCalculatorNameSuffix);
   private final YoRegistry interRobotContactCalculatorRegistry = new YoRegistry("InterRobot" + ContactCalculatorNameSuffix);
   private final YoRegistry selfContactCalculatorRegistry = new YoRegistry("Self" + ContactCalculatorNameSuffix);

   private final RigidBodyWrenchRegistry rigidBodyWrenchRegistry = new RigidBodyWrenchRegistry();

   private final List<Collidable> collidables;

   // TODO Following fields are specific to the type of engine used, they need interfacing.
   private final ForwardDynamicsCalculator forwardDynamicsCalculator;

   private final SingleRobotFirstOrderIntegrator integrator;

   private final RobotPhysicsOutput physicsOutput;

   public ContactPointBasedRobotPhysics(Robot owner)
   {
      this.owner = owner;
      inertialFrame = owner.getInertialFrame();

      SimRigidBody rootBody = owner.getRootBody();
      collidables = rootBody.subtreeStream().flatMap(body -> body.getCollidables().stream()).collect(Collectors.toList());

      forwardDynamicsCalculator = new ForwardDynamicsCalculator(owner);
      FrameShapePosePredictor frameShapePosePredictor = new FrameShapePosePredictor(forwardDynamicsCalculator);
      collidables.forEach(collidable -> collidable.setFrameShapePosePredictor(frameShapePosePredictor));

      YoRegistry jointLimitConstraintCalculatorRegistry = new YoRegistry(RobotJointLimitImpulseBasedCalculator.class.getSimpleName());

      integrator = new SingleRobotFirstOrderIntegrator();

      physicsOutput = new RobotPhysicsOutput(forwardDynamicsCalculator.getAccelerationProvider(), null, rigidBodyWrenchRegistry, null);

      owner.getRegistry().addChild(jointLimitConstraintCalculatorRegistry);
      owner.getRegistry().addChild(environmentContactCalculatorRegistry);
      owner.getRegistry().addChild(interRobotContactCalculatorRegistry);
      owner.getRegistry().addChild(selfContactCalculatorRegistry);
   }

   public void resetCalculators()
   {
      forwardDynamicsCalculator.setExternalWrenchesToZero();
      rigidBodyWrenchRegistry.reset();
   }

   public void addRigidBodyExternalWrench(RigidBodyReadOnly target, WrenchReadOnly wrenchToAdd)
   {
      rigidBodyWrenchRegistry.addWrench(target, wrenchToAdd);
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

   public void integrateState(double dt)
   {
      physicsOutput.setDT(dt);
      integrator.integrate(dt, owner);
   }

   public RobotPhysicsOutput getPhysicsOutput()
   {
      return physicsOutput;
   }
}
