package us.ihmc.scs2.simulation.robot.multiBodySystem.interfaces;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.mecano.multiBodySystem.interfaces.JointMatrixIndexProvider;
import us.ihmc.mecano.multiBodySystem.interfaces.JointReadOnly;
import us.ihmc.mecano.multiBodySystem.interfaces.MultiBodySystemReadOnly;
import us.ihmc.mecano.multiBodySystem.iterators.SubtreeStreams;
import us.ihmc.mecano.tools.MultiBodySystemFactories;
import us.ihmc.scs2.simulation.screwTools.SimMultiBodySystemFactories.SimJointBuilder;
import us.ihmc.scs2.simulation.screwTools.SimMultiBodySystemFactories.SimRigidBodyBuilder;
import us.ihmc.yoVariables.registry.YoRegistry;

public interface SimMultiBodySystemReadOnly extends MultiBodySystemReadOnly
{
   YoRegistry getRegistry();

   /** {@inheritDoc} */
   @Override
   SimRigidBodyReadOnly getRootBody();

   /** {@inheritDoc} */
   @Override
   default List<? extends SimJointReadOnly> getAllJoints()
   {
      return SubtreeStreams.fromChildren(SimJointReadOnly.class, getRootBody()).collect(Collectors.toList());
   }

   /** {@inheritDoc} */
   @Override
   default List<? extends SimJointReadOnly> getJointsToConsider()
   {
      return getAllJoints();
   }

   /** {@inheritDoc} */
   @Override
   default List<? extends SimJointReadOnly> getJointsToIgnore()
   {
      return getAllJoints().stream().filter(joint -> !getJointsToConsider().contains(joint)).collect(Collectors.toList());
   }

   /**
    * Creates a new input from the given {@code rootBody}. The resulting input will consider all the
    * joints composing the subtree starting off the given body.
    *
    * @param rootBody the support body to the subtree to consider. Not modified.
    * @return the new input.
    */
   public static SimMultiBodySystemReadOnly toMultiBodySystemInput(SimRigidBodyReadOnly rootBody)
   {
      return toMultiBodySystemInput(rootBody, Collections.emptyList());
   }

   /**
    * Creates a new input from the given {@code rootBody}. The resulting input will consider all the
    * joints composing the subtree starting off the given body less the given joints to ignore and
    * their respective descendants.
    *
    * @param rootBody       the support body to the subtree to consider. Not modified.
    * @param jointsToIgnore the array of joints to ignore. Not modified.
    * @return the new input.
    */
   public static SimMultiBodySystemReadOnly toMultiBodySystemInput(SimRigidBodyReadOnly rootBody, SimJointReadOnly[] jointsToIgnore)
   {
      return toMultiBodySystemInput(rootBody, Arrays.asList(jointsToIgnore));
   }

   /**
    * Creates a new input from the given {@code rootBody}. The resulting input will consider all the
    * joints composing the subtree starting off the given body less the given joints to ignore and
    * their respective descendants.
    *
    * @param rootBody       the support body to the subtree to consider. Not modified.
    * @param jointsToIgnore the array of joints to ignore. Not modified.
    * @return the new input.
    */
   public static SimMultiBodySystemReadOnly toMultiBodySystemInput(SimRigidBodyReadOnly rootBody, List<? extends SimJointReadOnly> jointsToIgnore)
   {
      List<? extends SimJointReadOnly> allJoints = SubtreeStreams.fromChildren(SimJointReadOnly.class, rootBody).collect(Collectors.toList());
      List<? extends SimJointReadOnly> jointsToConsider = extractJointsToConsider(rootBody, jointsToIgnore);
      JointMatrixIndexProvider jointMatrixIndexProvider = JointMatrixIndexProvider.toIndexProvider(jointsToConsider);

      return new SimMultiBodySystemReadOnly()
      {
         @Override
         public YoRegistry getRegistry()
         {
            return rootBody.getRegistry();
         }

         @Override
         public SimRigidBodyReadOnly getRootBody()
         {
            return rootBody;
         }

         @Override
         public List<? extends SimJointReadOnly> getAllJoints()
         {
            return allJoints;
         }

         @Override
         public List<? extends SimJointReadOnly> getJointsToConsider()
         {
            return jointsToConsider;
         }

         @Override
         public List<? extends SimJointReadOnly> getJointsToIgnore()
         {
            return jointsToIgnore;
         }

         @Override
         public JointMatrixIndexProvider getJointMatrixIndexProvider()
         {
            return jointMatrixIndexProvider;
         }
      };
   }

   /**
    * Creates a new input from the given joints to consider.
    * <p>
    * The resulting root body and joints to ignore are automatically evaluated.
    * </p>
    *
    * @param jointsToConsider the joints to consider. Not modified.
    * @return the new input.
    */
   public static SimMultiBodySystemReadOnly toMultiBodySystemReadOnly(SimJointReadOnly[] jointsToConsider)
   {
      return toMultiBodySystemInput(Arrays.asList(jointsToConsider));
   }

   /**
    * Creates a new input from the given joints to consider.
    * <p>
    * The resulting root body and joints to ignore are automatically evaluated.
    * </p>
    *
    * @param jointsToConsider the joints to consider. Not modified.
    * @return the new input.
    */
   public static SimMultiBodySystemReadOnly toMultiBodySystemInput(List<? extends SimJointReadOnly> jointsToConsider)
   {
      SimRigidBodyReadOnly rootBody = (SimRigidBodyReadOnly) MultiBodySystemReadOnly.getClosestJointToRoot(jointsToConsider).getPredecessor();
      List<? extends SimJointReadOnly> allJoints = SubtreeStreams.fromChildren(SimJointReadOnly.class, rootBody).collect(Collectors.toList());
      List<? extends SimJointReadOnly> jointsToIgnore = SubtreeStreams.fromChildren(SimJointReadOnly.class, rootBody)
                                                                    .filter(joint -> !jointsToConsider.contains(joint)).collect(Collectors.toList());
      JointMatrixIndexProvider jointMatrixIndexProvider = JointMatrixIndexProvider.toIndexProvider(jointsToConsider);

      return new SimMultiBodySystemReadOnly()
      {
         @Override
         public YoRegistry getRegistry()
         {
            return rootBody.getRegistry();
         }

         @Override
         public SimRigidBodyReadOnly getRootBody()
         {
            return rootBody;
         }

         @Override
         public List<? extends SimJointReadOnly> getAllJoints()
         {
            return allJoints;
         }

         @Override
         public List<? extends SimJointReadOnly> getJointsToConsider()
         {
            return jointsToConsider;
         }

         @Override
         public List<? extends SimJointReadOnly> getJointsToIgnore()
         {
            return jointsToIgnore;
         }

         @Override
         public JointMatrixIndexProvider getJointMatrixIndexProvider()
         {
            return jointMatrixIndexProvider;
         }
      };
   }

   /**
    * Navigates through the subtree starting off of {@code rootBody} and collects all the joints that
    * are to be considered.
    * <p>
    * A joint is ignored if it is in the given list {@code jointsToIgnore} or it is a descendant of
    * another joint to ignore.
    * </p>
    *
    * @param rootBody       the supporting body of the subtree to collect joints from. Not modified.
    * @param jointsToIgnore the list of joints to ignore. Not modified.
    * @return the list of joints to consider.
    */
   public static List<? extends SimJointReadOnly> extractJointsToConsider(SimRigidBodyReadOnly rootBody, List<? extends SimJointReadOnly> jointsToIgnore)
   {
      return SubtreeStreams.fromChildren(SimJointReadOnly.class, rootBody)
                           .filter(candidate -> !MultiBodySystemReadOnly.isJointToBeIgnored(candidate, jointsToIgnore)).collect(Collectors.toList());
   }

   /**
    * Performs a deep copy of {@code original}, preserving naming, root body, and the joints to ignore.
    * The clone is attached to the given {@code clonerootFrame}.
    *
    * @param original       the multi-body system to clone. Not modified.
    * @param cloneRootFrame the root frame to which the clone system is attached.
    * @return the clone.
    */
   public static SimMultiBodySystemReadOnly clone(MultiBodySystemReadOnly original, ReferenceFrame cloneRootFrame, YoRegistry cloneRegistry)
   {
      SimRigidBodyReadOnly cloneRootBody = (SimRigidBodyReadOnly) MultiBodySystemFactories.cloneMultiBodySystem(original.getRootBody(),
                                                                                                            cloneRootFrame,
                                                                                                            "",
                                                                                                            new SimRigidBodyBuilder(cloneRegistry),
                                                                                                            new SimJointBuilder());
      Set<String> namesOfJointsToConsider = SubtreeStreams.fromChildren(SimJointReadOnly.class, original.getRootBody()).map(JointReadOnly::getName)
                                                          .collect(Collectors.toSet());
      List<? extends SimJointReadOnly> jointsToConsider = SubtreeStreams.fromChildren(SimJointReadOnly.class, cloneRootBody)
                                                                      .filter(joint -> namesOfJointsToConsider.contains(joint.getName()))
                                                                      .collect(Collectors.toList());
      return toMultiBodySystemInput(jointsToConsider);
   }
}
