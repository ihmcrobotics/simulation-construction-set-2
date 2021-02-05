package us.ihmc.scs2.simulation.robot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;

import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.mecano.algorithms.interfaces.RigidBodyTwistProvider;
import us.ihmc.mecano.multiBodySystem.interfaces.JointMatrixIndexProvider;
import us.ihmc.mecano.multiBodySystem.interfaces.JointReadOnly;
import us.ihmc.mecano.multiBodySystem.interfaces.OneDoFJointReadOnly;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyReadOnly;
import us.ihmc.mecano.multiBodySystem.interfaces.SixDoFJointReadOnly;
import us.ihmc.mecano.spatial.Twist;
import us.ihmc.mecano.spatial.interfaces.TwistReadOnly;
import us.ihmc.mecano.tools.MultiBodySystemTools;
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

   private final DMatrixRMaj velocityChangeMatrix;
   private final RigidBodyTwistChangeCalculator rigidBodyTwistChangeCalculator;
   private final RigidBodyTwistProvider rigidBodyTwistChangeProvider;

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

      velocityChangeMatrix = new DMatrixRMaj(MultiBodySystemTools.computeDegreesOfFreedom(owner.getAllJoints()), 1);
      rigidBodyTwistChangeCalculator = new RigidBodyTwistChangeCalculator(inertialFrame, owner.getJointMatrixIndexProvider());
      rigidBodyTwistChangeProvider = RigidBodyTwistProvider.toRigidBodyTwistProvider(rigidBodyTwistChangeCalculator, owner.getInertialFrame());

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

   public void setJointVelocityChange(DMatrixRMaj velocityChange)
   {
      if (velocityChange == null)
         return;
      velocityChangeMatrix.set(velocityChange);
   }

   public void addJointVelocityChange(DMatrixRMaj velocityChange)
   {
      if (velocityChange == null)
         return;
      CommonOps_DDRM.addEquals(velocityChangeMatrix, velocityChange);
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

   public void integrateState(double dt)
   {
      integrator.integrate(dt, velocityChangeMatrix);
   }

   public SingleRobotFirstOrderIntegrator getIntegrator()
   {
      return integrator;
   }

   public void resetCalculators()
   {
      velocityChangeMatrix.zero();
      rigidBodyTwistChangeCalculator.reset();
      environmentContactConstraintCalculatorPool.clear();
      selfContactConstraintCalculatorPool.clear();
      interRobotContactConstraintCalculatorPools.forEach((rigidBodyBasics, calculators) -> calculators.clear());
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

   public RigidBodyTwistProvider getRigidBodyTwistChangeProvider()
   {
      return rigidBodyTwistChangeProvider;
   }

   private class RigidBodyTwistChangeCalculator implements Function<RigidBodyReadOnly, TwistReadOnly>
   {
      private final ReferenceFrame inertialFrame;
      private final JointMatrixIndexProvider jointMatrixIndexProvider;

      public RigidBodyTwistChangeCalculator(ReferenceFrame inertialFrame, JointMatrixIndexProvider jointMatrixIndexProvider)
      {
         this.inertialFrame = inertialFrame;
         this.jointMatrixIndexProvider = jointMatrixIndexProvider;
      }

      public void reset()
      {
         rigidBodyTwistMap.clear();
      }

      private final Map<RigidBodyReadOnly, Twist> rigidBodyTwistMap = new HashMap<>();
      private final Twist jointTwist = new Twist();

      @Override
      public TwistReadOnly apply(RigidBodyReadOnly body)
      {
         Twist twistOfBody = rigidBodyTwistMap.get(body);

         if (twistOfBody == null)
         {
            JointReadOnly parentJoint = body.getParentJoint();
            TwistReadOnly twistOfParentBody;
            RigidBodyReadOnly parentBody = parentJoint.getPredecessor();
            if (parentBody.isRootBody())
               twistOfParentBody = null;
            else
               twistOfParentBody = apply(parentBody);

            // TODO Implements other joints
            if (parentJoint instanceof OneDoFJointReadOnly)
            {
               jointTwist.setIncludingFrame(((OneDoFJointReadOnly) parentJoint).getUnitJointTwist());
               jointTwist.scale(velocityChangeMatrix.get(jointMatrixIndexProvider.getJointDoFIndices(parentJoint)[0]));
            }
            else if (parentJoint instanceof SixDoFJointReadOnly)
            {
               jointTwist.set(jointMatrixIndexProvider.getJointDoFIndices(parentJoint)[0], velocityChangeMatrix);
               jointTwist.setReferenceFrame(parentJoint.getFrameAfterJoint());
               jointTwist.setBaseFrame(parentJoint.getFrameBeforeJoint());
               jointTwist.setBodyFrame(parentJoint.getFrameAfterJoint());
            }
            else
            {
               throw new UnsupportedOperationException("Implement me for: " + parentJoint.getClass().getSimpleName());
            }

            jointTwist.changeFrame(body.getBodyFixedFrame());
            jointTwist.setBaseFrame(parentBody.getBodyFixedFrame());
            jointTwist.setBodyFrame(body.getBodyFixedFrame());

            twistOfBody = new Twist();
            if (twistOfParentBody == null)
               twistOfBody.setToZero(parentBody.getBodyFixedFrame(), inertialFrame, parentBody.getBodyFixedFrame());
            else
               twistOfBody.setIncludingFrame(twistOfParentBody);
            twistOfBody.changeFrame(body.getBodyFixedFrame());
            twistOfBody.add(jointTwist);
            rigidBodyTwistMap.put(body, twistOfBody);
         }

         return twistOfBody;
      }
   }
}
