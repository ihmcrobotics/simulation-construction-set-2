package us.ihmc.scs2.simulation.screwTools;

import org.ejml.data.DMatrix;
import us.ihmc.euclid.tools.EuclidCoreTools;
import us.ihmc.mecano.multiBodySystem.interfaces.*;
import us.ihmc.mecano.multiBodySystem.iterators.SubtreeStreams;
import us.ihmc.mecano.tools.MultiBodySystemTools;
import us.ihmc.scs2.simulation.physicsEngine.YoMatrix;
import us.ihmc.scs2.simulation.robot.multiBodySystem.interfaces.*;
import us.ihmc.yoVariables.registry.YoRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static us.ihmc.mecano.tools.MultiBodySystemTools.filterJoints;

/**
 * This class provides a variety of tools to facilitate operations that need to navigate through a
 * multi-body system.
 */
public class SimMultiBodySystemTools
{
   /**
    * Retrieves and gets the root body of the multi-body system the given {@code body} belongs to.
    *
    * @param body an arbitrary body that belongs to the multi-body system that this method is to find
    *             the root.
    * @return the root body.
    */
   public static SimRigidBodyReadOnly getRootBody(SimRigidBodyReadOnly body)
   {
      SimRigidBodyReadOnly root = body;

      while (root.getParentJoint() != null)
      {
         root = root.getParentJoint().getPredecessor();
      }

      return root;
   }

   /**
    * Retrieves and gets the root body of the multi-body system the given {@code body} belongs to.
    *
    * @param body an arbitrary body that belongs to the multi-body system that this method is to find
    *             the root.
    * @return the root body.
    */
   public static SimRigidBodyBasics getRootBody(SimRigidBodyBasics body)
   {
      SimRigidBodyBasics root = body;

      while (root.getParentJoint() != null)
      {
         root = root.getParentJoint().getPredecessor();
      }

      return root;
   }

   /**
    * Travels the multi-body system from {@code start} to {@code end} and stores in order the joints
    * that implement {@code OneDoFJointBasics} and that are in between and return them as an array.
    * <p>
    * WARNING: This method generates garbage.
    * </p>
    *
    * @param start the rigid-body from where to begin the collection of joints.
    * @param end   the rigid-body where to stop the collection of joints.
    * @return the array of joints representing the path from {@code start} to {@code end}.
    */
   public static SimOneDoFJointBasics[] createOneDoFJointPath(SimRigidBodyBasics start, SimRigidBodyBasics end)
   {
      return filterJoints(createJointPath(start, end), SimOneDoFJointBasics.class);
   }

   /**
    * Travels the multi-body system from {@code start} to {@code end} and stores in order the joints
    * that are in between and return them as an array.
    * <p>
    * WARNING: This method generates garbage.
    * </p>
    *
    * @param start the rigid-body from where to begin the collection of joints.
    * @param end   the rigid-body where to stop the collection of joints.
    * @return the array of joints representing the path from {@code start} to {@code end}, or
    *         {@code null} if the given rigid-bodies are not part of the same multi-body system.
    */
   public static SimJointBasics[] createJointPath(SimRigidBodyBasics start, SimRigidBodyBasics end)
   {
      List<SimJointBasics> jointPath = new ArrayList<>();
      collectJointPath(start, end, jointPath);
      return jointPath.toArray(new SimJointBasics[jointPath.size()]);
   }

   /**
    * Travels the multi-body system from {@code start} to {@code end} and stores in order the joints
    * that are in between and return them as an array.
    * <p>
    * WARNING: This method generates garbage.
    * </p>
    *
    * @param start the rigid-body from where to begin the collection of joints.
    * @param end   the rigid-body where to stop the collection of joints.
    * @return the array of joints representing the path from {@code start} to {@code end}, or
    *         {@code null} if the given rigid-bodies are not part of the same multi-body system.
    */
   public static SimJointReadOnly[] createJointPath(SimRigidBodyReadOnly start, SimRigidBodyReadOnly end)
   {
      List<SimJointReadOnly> jointPath = new ArrayList<>();
      collectJointPath(start, end, jointPath);
      return jointPath.toArray(new SimJointReadOnly[jointPath.size()]);
   }

   /**
    * Travels the multi-body system from {@code start} to {@code end} and stores in order the joints
    * that are in between in the given {@code jointPathToPack}.
    * <p>
    * The resulting joint path represent the shortest path connecting {@code start} and {@code end}. No
    * assumption is made on the relative position of the two rigid-bodies in the multi-body system.
    * </p>
    *
    * @param start           the rigid-body where to begin collecting the joints.
    * @param end             the rigid-body where to stop collecting the joints.
    * @param jointPathToPack the list in which the joint path is stored. Note that the list is first
    *                        cleared before storing the joint path.
    * @return the nearest common ancestor of {@code start} and {@code end}.
    */
   public static SimRigidBodyReadOnly collectJointPath(SimRigidBodyReadOnly start, SimRigidBodyReadOnly end, List<SimJointReadOnly> jointPathToPack)
   {
      jointPathToPack.clear();

      SimRigidBodyReadOnly ancestor = computeNearestCommonAncestor(start, end);
      SimRigidBodyReadOnly currentBody;

      currentBody = start;

      while (currentBody != ancestor)
      {
         SimJointReadOnly parentJoint = currentBody.getParentJoint();
         jointPathToPack.add(parentJoint);
         currentBody = parentJoint.getPredecessor();
      }

      int distance = jointPathToPack.size();
      currentBody = end;

      while (currentBody != ancestor)
      {
         currentBody = currentBody.getParentJoint().getPredecessor();
         distance++;
      }

      while (jointPathToPack.size() < distance)
         jointPathToPack.add(null);

      currentBody = end;

      for (int i = distance - 1; currentBody != ancestor; i--)
      {
         SimJointReadOnly parentJoint = currentBody.getParentJoint();
         jointPathToPack.set(i, parentJoint);
         currentBody = parentJoint.getPredecessor();
      }
      return ancestor;
   }

   /**
    * Travels the multi-body system from {@code start} to {@code end} and stores in order the joints
    * that are in between in the given {@code jointPathToPack}.
    * <p>
    * The resulting joint path represent the shortest path connecting {@code start} and {@code end}. No
    * assumption is made on the relative position of the two rigid-bodies in the multi-body system.
    * </p>
    *
    * @param start           the rigid-body where to begin collecting the joints.
    * @param end             the rigid-body where to stop collecting the joints.
    * @param jointPathToPack the list in which the joint path is stored. Note that the list is first
    *                        cleared before storing the joint path.
    * @return the nearest common ancestor of {@code start} and {@code end}.
    */
   public static SimRigidBodyBasics collectJointPath(SimRigidBodyBasics start, SimRigidBodyBasics end, List<SimJointBasics> jointPathToPack)
   {
      jointPathToPack.clear();

      SimRigidBodyBasics ancestor = computeNearestCommonAncestor(start, end);
      SimRigidBodyBasics currentBody;

      currentBody = start;

      while (currentBody != ancestor)
      {
         SimJointBasics parentJoint = currentBody.getParentJoint();
         jointPathToPack.add(parentJoint);
         currentBody = parentJoint.getPredecessor();
      }

      int distance = jointPathToPack.size();
      currentBody = end;

      while (currentBody != ancestor)
      {
         currentBody = currentBody.getParentJoint().getPredecessor();
         distance++;
      }

      while (jointPathToPack.size() < distance)
         jointPathToPack.add(null);

      currentBody = end;

      for (int i = distance - 1; currentBody != ancestor; i--)
      {
         SimJointBasics parentJoint = currentBody.getParentJoint();
         jointPathToPack.set(i, parentJoint);
         currentBody = parentJoint.getPredecessor();
      }
      return ancestor;
   }

   /**
    * Travels the multi-body system from {@code start} to {@code end} and stores in order the
    * rigid-bodies that connect {@code start} to {@code end} in the given {@code rigidBodyPathToPack}.
    * <p>
    * The resulting resulting path includes both {@code start} and {@code end} and represent the
    * shortest path connecting the two rigid-bodies. No assumption is made on the relative position of
    * the two rigid-bodies in the multi-body system.
    * </p>
    *
    * @param start               the rigid-body where to begin collecting the rigid-bodies.
    * @param end                 the rigid-body where to stop collecting the rigid-bodies.
    * @param rigidBodyPathToPack the list in which the rigid-body path is stored. Note that the list is
    *                            first cleared before storing the rigid-body path.
    * @return the nearest common ancestor of {@code start} and {@code end}.
    */
   public static SimRigidBodyReadOnly collectRigidBodyPath(SimRigidBodyReadOnly start, SimRigidBodyReadOnly end, List<SimRigidBodyReadOnly> rigidBodyPathToPack)
   {
      rigidBodyPathToPack.clear();

      if (start == end)
      {
         rigidBodyPathToPack.add(end);
         return end;
      }

      SimRigidBodyReadOnly ancestor = computeNearestCommonAncestor(start, end);
      SimRigidBodyReadOnly currentBody;

      currentBody = start;

      if (start == ancestor)
         rigidBodyPathToPack.add(start);

      while (currentBody != ancestor)
      {
         rigidBodyPathToPack.add(currentBody);
         currentBody = currentBody.getParentJoint().getPredecessor();
      }

      int distance = rigidBodyPathToPack.size();
      currentBody = end;

      while (currentBody != ancestor)
      {
         currentBody = currentBody.getParentJoint().getPredecessor();
         distance++;
      }

      while (rigidBodyPathToPack.size() < distance)
         rigidBodyPathToPack.add(null);

      currentBody = end;

      if (end == ancestor)
         rigidBodyPathToPack.add(end);

      for (int i = distance - 1; currentBody != ancestor; i--)
      {
         rigidBodyPathToPack.set(i, currentBody);
         currentBody = currentBody.getParentJoint().getPredecessor();
      }
      return ancestor;
   }

   /**
    * Travels the multi-body system from {@code start} to {@code end} and stores in order the
    * rigid-bodies that connect {@code start} to {@code end} in the given {@code rigidBodyPathToPack}.
    * <p>
    * The resulting resulting path includes both {@code start} and {@code end} and represent the
    * shortest path connecting the two rigid-bodies. No assumption is made on the relative position of
    * the two rigid-bodies in the multi-body system.
    * </p>
    *
    * @param start               the rigid-body where to begin collecting the rigid-bodies.
    * @param end                 the rigid-body where to stop collecting the rigid-bodies.
    * @param rigidBodyPathToPack the list in which the rigid-body path is stored. Note that the list is
    *                            first cleared before storing the rigid-body path.
    * @return the nearest common ancestor of {@code start} and {@code end}.
    */
   public static SimRigidBodyBasics collectRigidBodyPath(SimRigidBodyBasics start, SimRigidBodyBasics end, List<SimRigidBodyBasics> rigidBodyPathToPack)
   {
      rigidBodyPathToPack.clear();

      if (start == end)
      {
         rigidBodyPathToPack.add(end);
         return end;
      }

      SimRigidBodyBasics ancestor = computeNearestCommonAncestor(start, end);
      SimRigidBodyBasics currentBody;

      currentBody = start;

      if (start == ancestor)
         rigidBodyPathToPack.add(start);

      while (currentBody != ancestor)
      {
         rigidBodyPathToPack.add(currentBody);
         currentBody = currentBody.getParentJoint().getPredecessor();
      }

      int distance = rigidBodyPathToPack.size();
      currentBody = end;

      while (currentBody != ancestor)
      {
         currentBody = currentBody.getParentJoint().getPredecessor();
         distance++;
      }

      while (rigidBodyPathToPack.size() < distance)
         rigidBodyPathToPack.add(null);

      currentBody = end;

      if (end == ancestor)
         rigidBodyPathToPack.add(end);

      for (int i = distance - 1; currentBody != ancestor; i--)
      {
         rigidBodyPathToPack.set(i, currentBody);
         currentBody = currentBody.getParentJoint().getPredecessor();
      }
      return ancestor;
   }

   /**
    * Finds the common ancestor of {@code firstBody} and {@code secondBody} that minimizes the distance
    * {@code d}:
    *
    * <pre>
    * d = <i>computeDistanceToAncestor</i>(firstBody, ancestor) + <i>computeDistanceToAncestor</i>(secondBody, ancestor)
    * </pre>
    *
    * @param firstBody  the first rigid-body of the query.
    * @param secondBody the second rigid-body of the query.
    * @return the nearest common ancestor.
    * @throws IllegalArgumentException if the two rigid-bodies do not belong to the same multi-body
    *                                  system.
    */
   public static SimRigidBodyBasics computeNearestCommonAncestor(SimRigidBodyBasics firstBody, SimRigidBodyBasics secondBody)
   {
      return (SimRigidBodyBasics) computeNearestCommonAncestor((SimRigidBodyReadOnly) firstBody, (SimRigidBodyReadOnly) secondBody);
   }

   /**
    * Finds the common ancestor of {@code firstBody} and {@code secondBody} that minimizes the distance
    * {@code d}:
    *
    * <pre>
    * d = <i>computeDistanceToAncestor</i>(firstBody, ancestor) + <i>computeDistanceToAncestor</i>(secondBody, ancestor)
    * </pre>
    *
    * @param firstBody  the first rigid-body of the query.
    * @param secondBody the second rigid-body of the query.
    * @return the nearest common ancestor.
    * @throws IllegalArgumentException if the two rigid-bodies do not belong to the same multi-body
    *                                  system.
    */
   public static SimRigidBodyReadOnly computeNearestCommonAncestor(SimRigidBodyReadOnly firstBody, SimRigidBodyReadOnly secondBody)
   {
      return (SimRigidBodyReadOnly) MultiBodySystemTools.computeNearestCommonAncestor(firstBody, secondBody);
   }

   /**
    * Collects in order the successor of each joint, i.e. {@link SimJointReadOnly#getSuccessor()}.
    * <p>
    * Note on kinematic loops: if {@code joints} contains the two joints terminating a kinematic loop,
    * then the common successor will occur twice in the result.
    * </p>
    * <p>
    * WARNING: This method generates garbage.
    * </p>
    *
    * @param joints the joints to collect the successors of.
    * @return the array containing in order the successor of each joint.
    */
   // TODO Consider explicitly handling successor of kinematic loops
   public static SimRigidBodyReadOnly[] collectSuccessors(SimJointReadOnly... joints)
   {
      return Stream.of(joints).map(SimJointReadOnly::getSuccessor).toArray(SimRigidBodyReadOnly[]::new);
   }

   /**
    * Collects in order the successor of each joint, i.e. {@link SimJointReadOnly#getSuccessor()}.
    * <p>
    * Note on kinematic loops: if {@code joints} contains the two joints terminating a kinematic loop,
    * then the common successor will occur twice in the result.
    * </p>
    * <p>
    * WARNING: This method generates garbage.
    * </p>
    *
    * @param joints the joints to collect the successors of.
    * @return the array containing in order the successor of each joint.
    */
   // TODO Consider explicitly handling successor of kinematic loops
   public static SimRigidBodyBasics[] collectSuccessors(SimJointBasics... joints)
   {
      return Stream.of(joints).map(SimJointBasics::getSuccessor).toArray(SimRigidBodyBasics[]::new);
   }

   /**
    * Collects any rigid-body that composes any of the subtrees originating at the given
    * {@code joints}.
    * <p>
    * Note on kinematic loops: if {@code joints} contains the two joints terminating a kinematic loop,
    * then the subtree of the common successor will occur twice in the result.
    * </p>
    * <p>
    * WARNING: This method generates garbage.
    * </p>
    *
    * @param joints the joints indicating the start of each subtree to collect.
    * @return the array containing all the rigid-bodies composing the subtrees.
    */
   // TODO Consider explicitly handling successor of kinematic loops
   public static SimRigidBodyReadOnly[] collectSubtreeSuccessors(SimJointReadOnly... joints)
   {
      return Stream.of(joints).map(SimJointReadOnly::getSuccessor).flatMap(SimRigidBodyReadOnly::subtreeStream).distinct().toArray(SimRigidBodyReadOnly[]::new);
   }

   /**
    * Collects any rigid-body that composes any of the subtrees originating at the given
    * {@code joints}.
    * <p>
    * Note on kinematic loops: if {@code joints} contains the two joints terminating a kinematic loop,
    * then the subtree of the common successor will occur twice in the result.
    * </p>
    * <p>
    * WARNING: This method generates garbage.
    * </p>
    *
    * @param joints the joints indicating the start of each subtree to collect.
    * @return the array containing all the rigid-bodies composing the subtrees.
    */
   // TODO Consider explicitly handling successor of kinematic loops
   public static SimRigidBodyBasics[] collectSubtreeSuccessors(SimJointBasics... joints)
   {
      return Stream.of(joints).map(SimJointBasics::getSuccessor).flatMap(SimRigidBodyBasics::subtreeStream).distinct().toArray(SimRigidBodyBasics[]::new);
   }

   /**
    * Collects and returns all the joints located between the given {@code rigidBody} and the root
    * body.
    * <p>
    * Note on kinematic loops: this method does not collect the joints on the secondary branch of a
    * kinematic loop, i.e. the branch that starts off the primary branch and ends with the loop closure
    * joint.
    * </p>
    * <p>
    * WARNING: This method generates garbage.
    * </p>
    *
    * @param rigidBody the rigid-body to collect the support joints of.
    * @return the array containing the support joints of the given rigid-body.
    */
   public static SimJointReadOnly[] collectSupportJoints(SimRigidBodyReadOnly rigidBody)
   {
      return createJointPath(getRootBody(rigidBody), rigidBody);
   }

   /**
    * Collects and returns all the joints located between the given {@code rigidBody} and the root
    * body.
    * <p>
    * Note on kinematic loops: this method does not collect the joints on the secondary branch of a
    * kinematic loop, i.e. the branch that starts off the primary branch and ends with the loop closure
    * joint.
    * </p>
    * <p>
    * WARNING: This method generates garbage.
    * </p>
    *
    * @param rigidBody the rigid-body to collect the support joints of.
    * @return the array containing the support joints of the given rigid-body.
    */
   public static SimJointBasics[] collectSupportJoints(SimRigidBodyBasics rigidBody)
   {
      return createJointPath(getRootBody(rigidBody), rigidBody);
   }

   /**
    * Collects for each rigid-body all their support joints, i.e. the joints that are between the
    * rigid-body and the root body, and returns an array containing no duplicate elements.
    * <p>
    * Note on kinematic loops: this method does not collect the joints on the secondary branch of a
    * kinematic loop, i.e. the branch that starts off the primary branch and ends with the loop closure
    * joint.
    * </p>
    * <p>
    * WARNING: This method generates garbage.
    * </p>
    *
    * @param rigidBodies the rigid-bodies to collect the support joints of.
    * @return the array containing the support joints of all the given rigid-bodies.
    */
   public static SimJointReadOnly[] collectSupportJoints(SimRigidBodyReadOnly... rigidBodies)
   {
      return Stream.of(rigidBodies).map(SimMultiBodySystemTools::collectSupportJoints).flatMap(Stream::of).distinct().toArray(SimJointReadOnly[]::new);
   }

   /**
    * Collects for each rigid-body all their support joints, i.e. the joints that are between the
    * rigid-body and the root body, and returns an array containing no duplicate elements.
    * <p>
    * Note on kinematic loops: this method does not collect the joints on the secondary branch of a
    * kinematic loop, i.e. the branch that starts off the primary branch and ends with the loop closure
    * joint.
    * </p>
    * <p>
    * WARNING: This method generates garbage.
    * </p>
    *
    * @param rigidBodies the rigid-bodies to collect the support joints of.
    * @return the array containing the support joints of all the given rigid-bodies.
    */
   public static SimJointBasics[] collectSupportJoints(SimRigidBodyBasics... rigidBodies)
   {
      return Stream.of(rigidBodies).map(SimMultiBodySystemTools::collectSupportJoints).flatMap(Stream::of).distinct().toArray(SimJointBasics[]::new);
   }

   /**
    * Collects all the joints that are part of any of the subtrees originating from the given
    * {@code rootBodies}, and returns an array containing no duplicate elements.
    * <p>
    * WARNING: This method generates garbage.
    * </p>
    *
    * @param rootBodies the rigid-bodies from which the subtree to collect start off.
    * @return the array containing all the joint composing the subtrees.
    */
   public static SimJointReadOnly[] collectSubtreeJoints(SimRigidBodyReadOnly... rootBodies)
   {
      return Stream.of(rootBodies).flatMap(SubtreeStreams::fromChildren).distinct().toArray(SimJointReadOnly[]::new);
   }

   /**
    * Collects all the joints that are part of any of the subtrees originating from the given
    * {@code rootBodies}, and returns an array containing no duplicate elements.
    * <p>
    * WARNING: This method generates garbage.
    * </p>
    *
    * @param rootBodies the rigid-bodies from which the subtree to collect start off.
    * @return the array containing all the joint composing the subtrees.
    */
   public static SimJointBasics[] collectSubtreeJoints(SimRigidBodyBasics... rootBodies)
   {
      return Stream.of(rootBodies).flatMap(SubtreeStreams::fromChildren).distinct().toArray(SimJointBasics[]::new);
   }

   /**
    * Collects all the joints that are part of any of the subtrees originating from the given
    * {@code rootBodies}, and returns an array containing no duplicate elements.
    * <p>
    * WARNING: This method generates garbage.
    * </p>
    *
    * @param rootBodies the rigid-bodies from which the subtree to collect start off.
    * @return the array containing all the joint composing the subtrees.
    */
   public static SimJointReadOnly[] collectSubtreeJoints(List<? extends SimRigidBodyReadOnly> rootBodies)
   {
      return rootBodies.stream().flatMap(SubtreeStreams::fromChildren).distinct().toArray(SimJointReadOnly[]::new);
   }

   /**
    * Combines {@link #collectSupportJoints(SimRigidBodyReadOnly)} with
    * {@link #collectSubtreeJoints(SimRigidBodyReadOnly...)}.
    * <p>
    * WARNING: This method generates garbage.
    * </p>
    *
    * @param rigidBody the rigid-body to collect the support and subtree joints of.
    * @return the array containing the support and subtree joints.
    * @see #collectSupportJoints(SimRigidBodyReadOnly)
    * @see #collectSubtreeJoints(SimRigidBodyReadOnly...)
    */
   public static SimJointReadOnly[] collectSupportAndSubtreeJoints(SimRigidBodyReadOnly rigidBody)
   {
      List<SimJointReadOnly> supportAndSubtreeJoints = SubtreeStreams.fromChildren(SimJointReadOnly.class, rigidBody).collect(Collectors.toList());
      supportAndSubtreeJoints.addAll(Arrays.asList(collectSupportJoints(rigidBody)));
      return supportAndSubtreeJoints.toArray(new SimJointReadOnly[supportAndSubtreeJoints.size()]);
   }

   /**
    * Combines {@link #collectSupportJoints(SimRigidBodyBasics)} with
    * {@link #collectSubtreeJoints(SimRigidBodyBasics...)}.
    * <p>
    * WARNING: This method generates garbage.
    * </p>
    *
    * @param rigidBody the rigid-body to collect the support and subtree joints of.
    * @return the array containing the support and subtree joints.
    * @see #collectSupportJoints(SimRigidBodyBasics)
    * @see #collectSubtreeJoints(SimRigidBodyBasics...)
    */
   public static SimJointBasics[] collectSupportAndSubtreeJoints(SimRigidBodyBasics rigidBody)
   {
      List<SimJointBasics> supportAndSubtreeJoints = new ArrayList<>();
      Stream.of(collectSupportJoints(rigidBody)).forEach(supportAndSubtreeJoints::add);
      rigidBody.childrenSubtreeIterable().forEach(supportAndSubtreeJoints::add);
      return supportAndSubtreeJoints.toArray(new SimJointBasics[supportAndSubtreeJoints.size()]);
   }

   /**
    * Combines {@link #collectSupportJoints(SimRigidBodyReadOnly...)} with
    * {@link #collectSubtreeJoints(SimRigidBodyReadOnly...)}, and returns an array containing no
    * duplicate elements.
    * <p>
    * WARNING: This method generates garbage.
    * </p>
    *
    * @param rigidBodies the rigid-bodies to collect the support and subtree joints of.
    * @return the array containing the support and subtree joints.
    * @see #collectSupportJoints(SimRigidBodyReadOnly)
    * @see #collectSubtreeJoints(SimRigidBodyReadOnly...)
    */
   public static SimJointReadOnly[] collectSupportAndSubtreeJoints(SimRigidBodyReadOnly... rigidBodies)
   {
      return Stream.of(rigidBodies)
                   .map(SimMultiBodySystemTools::collectSupportAndSubtreeJoints)
                   .flatMap(Stream::of)
                   .distinct()
                   .toArray(SimJointReadOnly[]::new);
   }

   /**
    * Combines {@link #collectSupportJoints(SimRigidBodyBasics...)} with
    * {@link #collectSubtreeJoints(SimRigidBodyBasics...)}, and returns an array containing no
    * duplicate elements.
    * <p>
    * WARNING: This method generates garbage.
    * </p>
    *
    * @param rigidBodies the rigid-bodies to collect the support and subtree joints of.
    * @return the array containing the support and subtree joints.
    * @see #collectSupportJoints(SimRigidBodyBasics)
    * @see #collectSubtreeJoints(SimRigidBodyBasics...)
    */
   public static SimJointBasics[] collectSupportAndSubtreeJoints(SimRigidBodyBasics... rigidBodies)
   {
      return Stream.of(rigidBodies).map(SimMultiBodySystemTools::collectSupportAndSubtreeJoints).flatMap(Stream::of).distinct().toArray(SimJointBasics[]::new);
   }

   /**
    * Collects starting from the given {@code rigidBody} all descendant that has no children, i.e. all
    * end-effector.
    * <p>
    * WARNING: This method generates garbage.
    * </p>
    *
    * @param rigidBody the rigid-body to collect of descendant end-effectors of.
    * @return the array containing the end-effectors.
    */
   public static SimRigidBodyBasics[] collectSubtreeEndEffectors(SimRigidBodyBasics rigidBody)
   {
      return rigidBody.subtreeStream().filter(body -> body.getChildrenJoints().isEmpty()).toArray(SimRigidBodyBasics[]::new);
   }

   /**
    * Collects starting from the given {@code rigidBody} all descendant that has no children, i.e. all
    * end-effector.
    * <p>
    * WARNING: This method generates garbage.
    * </p>
    *
    * @param rigidBody the rigid-body to collect of descendant end-effectors of.
    * @return the array containing the end-effectors.
    */
   public static SimRigidBodyReadOnly[] collectSubtreeEndEffectors(SimRigidBodyReadOnly rigidBody)
   {
      return rigidBody.subtreeStream().filter(body -> body.getChildrenJoints().isEmpty()).toArray(SimRigidBodyReadOnly[]::new);
   }

   /**
    * Copies the requested state from the {@code source} joints to the {@code destination} joints.
    * <p>
    * The two lists should be of same length and each pair (source, destination) joint for any given
    * index should be of the same type.
    * </p>
    *
    * @param source         the joints holding the state to copy over. Not modified.
    * @param destination    the joints which state is to be be updated. Modified.
    * @param stateSelection the state that is to be copied over.
    */
   public static void copyJointsState(List<? extends SimJointReadOnly> source, List<? extends SimJointBasics> destination, SimJointStateType stateSelection)
   {
      if (source.size() != destination.size())
         throw new IllegalArgumentException("Inconsistent argument size: source = " + source.size() + ", destination = " + destination.size() + ".");

      switch (stateSelection)
      {
         case VELOCITY_CHANGE:
            copyJointsDeltaVelocity(source, destination);
            return;
         default:
            MultiBodySystemTools.copyJointsState(source, destination, stateSelection.toJointStateType());
            return;
      }
   }

   private static void copyJointsDeltaVelocity(List<? extends SimJointReadOnly> source, List<? extends SimJointBasics> destination)
   {
      for (int jointIndex = 0; jointIndex < source.size(); jointIndex++)
      {
         SimJointReadOnly sourceJoint = source.get(jointIndex);
         SimJointBasics destinationJoint = destination.get(jointIndex);
         destinationJoint.setJointDeltaTwist(sourceJoint);
      }
   }

   /**
    * Iterates through the given {@code joints}, extract the requested state {@code stateSelection} for
    * each joint, and finally stores the states in order in the given matrix {@code matrixToPack}.
    *
    * @param joints         the joints to extract the state of. Not modified.
    * @param stateSelection indicates what state is to be extract, i.e. it can be either configuration,
    *                       velocity, acceleration, or tau (or effort).
    * @param matrixToPack   the matrix in which the state of the joints is to be stored. Modified.
    * @return the number of rows used to store the information in the matrix.
    */
   public static int extractJointsState(List<? extends SimJointReadOnly> joints, SimJointStateType stateSelection, DMatrix matrixToPack)
   {
      switch (stateSelection)
      {
         case VELOCITY_CHANGE:
            return extractJointsDeltaVelocity(joints, 0, matrixToPack);
         default:
            return MultiBodySystemTools.extractJointsState(joints, stateSelection.toJointStateType(), matrixToPack);
      }
   }

   private static int extractJointsDeltaVelocity(List<? extends SimJointReadOnly> joints, int startIndex, DMatrix matrixToPack)
   {
      for (int jointIndex = 0; jointIndex < joints.size(); jointIndex++)
      {
         SimJointReadOnly joint = joints.get(jointIndex);
         startIndex = joint.getJointDeltaVelocity(startIndex, matrixToPack);
      }

      return startIndex;
   }

   /**
    * Iterates through the given {@code joints}, extract the requested state {@code stateSelection} for
    * each joint, and finally stores the states in order in the given matrix {@code matrixToPack}.
    *
    * @param joints         the joints to extract the state of. Not modified.
    * @param stateSelection indicates what state is to be extract, i.e. it can be either configuration,
    *                       velocity, acceleration, or tau (or effort).
    * @param matrixToPack   the matrix in which the state of the joints is to be stored. Modified.
    * @return the number of rows used to store the information in the matrix.
    */
   public static int extractJointsState(SimJointReadOnly[] joints, SimJointStateType stateSelection, DMatrix matrixToPack)
   {
      switch (stateSelection)
      {
         case VELOCITY_CHANGE:
            return extractJointsDeltaVelocity(joints, 0, matrixToPack);
         default:
            return MultiBodySystemTools.extractJointsState(joints, stateSelection.toJointStateType(), matrixToPack);
      }
   }

   private static int extractJointsDeltaVelocity(SimJointReadOnly[] joints, int startIndex, DMatrix matrixToPack)
   {
      for (int jointIndex = 0; jointIndex < joints.length; jointIndex++)
      {
         SimJointReadOnly joint = joints[jointIndex];
         startIndex = joint.getJointDeltaVelocity(startIndex, matrixToPack);
      }

      return startIndex;
   }

   private interface JointStateInsertor
   {
      int insertState(SimJointBasics joint, int startIndex, DMatrix state);
   }

   private static final JointStateInsertor jointConfigurationInsertor = (joint, startIndex, state) -> joint.setJointConfiguration(startIndex, state);
   private static final JointStateInsertor jointVelocityInsertor = (joint, startIndex, state) -> joint.setJointVelocity(startIndex, state);
   private static final JointStateInsertor jointDeltaVelocityInsertor = (joint, startIndex, state) -> joint.setJointDeltaVelocity(startIndex, state);
   private static final JointStateInsertor jointAccelerationInsertor = (joint, startIndex, state) -> joint.setJointAcceleration(startIndex, state);
   private static final JointStateInsertor jointEffortInsertor = (joint, startIndex, state) -> joint.setJointTau(startIndex, state);

   /**
    * Iterates through the given {@code joints}, and update their requested state
    * {@code stateSelection} using the given {@code matrix} assuming the state has been previously
    * stored in the proper order.
    *
    * @param joints           the joints to update the state of. Modified.
    * @param stateSelection   indicates what state is to be updated, i.e. it can be either
    *                         configuration, velocity, acceleration, or tau (or effort).
    * @param matrix           the matrix in which the new state of the joints is stored. The data is
    *                         expected to be stored as a column vector starting at the first row. Not
    *                         modified.
    * @param maxMagnitude     asserts for each joint that the norm of the state being inserted is below
    *                         this value, throws an {@link IllegalArgumentException} is a state's norm
    *                         exceeds the value. This argument is ignored for
    *                         {@link SimJointStateType#CONFIGURATION}.
    * @param testFiniteValues when {@code true}, tests that the values are all finite, i.e. not
    *                         infinite or NaN, throws an {@link IllegalArgumentException} if at least
    *                         one value is not finite.
    * @return the number of rows that were used from the matrix.
    */
   public static int insertJointsState(List<? extends SimJointBasics> joints,
                                       SimJointStateType stateSelection,
                                       DMatrix matrix,
                                       double maxMagnitude,
                                       boolean testFiniteValues)
   {
      if (testFiniteValues)
      {
         checkFiniteValues(stateSelection, matrix);
      }

      JointStateInsertor stateInsertor = toJointStateInsertor(stateSelection);

      int startIndex = 0;

      if (stateSelection == SimJointStateType.CONFIGURATION || maxMagnitude == Double.POSITIVE_INFINITY)
      {
         for (int jointIndex = 0; jointIndex < joints.size(); jointIndex++)
         {
            SimJointBasics joint = joints.get(jointIndex);
            startIndex = stateInsertor.insertState(joint, startIndex, matrix);
         }
      }
      else
      {
         for (int jointIndex = 0; jointIndex < joints.size(); jointIndex++)
         {
            SimJointBasics joint = joints.get(jointIndex);
            checkStateNorm(joint, startIndex, matrix, maxMagnitude, stateSelection);
            startIndex = stateInsertor.insertState(joint, startIndex, matrix);
         }
      }
      return startIndex;
   }

   public static void checkFiniteValues(SimJointStateType stateSelection, DMatrix matrix)
   {
      checkFiniteValues(stateSelection, matrix, 0, matrix.getNumRows());
   }

   public static void checkFiniteValues(SimJointStateType stateSelection, DMatrix matrix, int startIndex, int numberOfElements)
   {
      for (int row = startIndex; row < startIndex + numberOfElements; row++)
      {
         if (!Double.isFinite(matrix.get(row, 0)))
            throw new IllegalArgumentException("The given state (" + stateSelection + ") matrix contains non-finite values: " + matrix);
      }
   }

   public static JointStateInsertor toJointStateInsertor(SimJointStateType stateSelection)
   {
      JointStateInsertor stateInsertor;

      switch (stateSelection)
      {
         case CONFIGURATION:
            stateInsertor = jointConfigurationInsertor;
            break;
         case VELOCITY:
            stateInsertor = jointVelocityInsertor;
            break;
         case VELOCITY_CHANGE:
            stateInsertor = jointDeltaVelocityInsertor;
            break;
         case ACCELERATION:
            stateInsertor = jointAccelerationInsertor;
            break;
         case EFFORT:
            stateInsertor = jointEffortInsertor;
            break;
         default:
            throw new RuntimeException("Unexpected value for stateSelection: " + stateSelection);
      }
      return stateInsertor;
   }

   private static void checkStateNorm(JointReadOnly joint, int startIndex, DMatrix stateMatrix, double maxMagnitude, SimJointStateType state)
   {

      double normSquared = 0.0;
      for (int dof = 0; dof < joint.getDegreesOfFreedom(); dof++)
      {
         normSquared += EuclidCoreTools.square(stateMatrix.get(startIndex + dof, 0));
      }

      if (normSquared > maxMagnitude * maxMagnitude)
      {
         throw new IllegalArgumentException("Joint (" + joint.getName() + ") state (" + state + ") exceeds max magnitude (" + maxMagnitude + "): "
               + Math.sqrt(normSquared));
      }
   }

   /**
    * Similar to {@link #insertJointsState(List, SimJointStateType, DMatrix, double, boolean)} but
    * allows to insert either a state {@code A} or {@code B} on a per-joint basis depending on the
    * result of the given predicate.
    * 
    * @param joints            the joints to update the state of. Modified.
    * @param predicateStateA   the predicate used to switch between states {@code A} and {@code B},
    *                          when the predicate returns {@code true}, the state {@code A} is used.
    * @param stateSelectionA   the state to be updated when inserting state {@code A}.
    * @param matrixA           the values for the state {@code A}. Not modified.
    * @param maxMagnitudeA
    * @param testFiniteValuesA
    * @param stateSelectionB   the state to be updated when inserting state {@code B}.
    * @param matrixB           the values for the state {@code B}. Not modified.
    * @param maxMagnitudeB
    * @param testFiniteValuesB
    * @return
    */
   public static int insertJointsStateWithBackup(List<? extends SimJointBasics> joints,
                                                 Predicate<SimJointBasics> predicateStateA,
                                                 SimJointStateType stateSelectionA,
                                                 DMatrix matrixA,
                                                 double maxMagnitudeA,
                                                 boolean testFiniteValuesA,
                                                 SimJointStateType stateSelectionB,
                                                 DMatrix matrixB,
                                                 double maxMagnitudeB,
                                                 boolean testFiniteValuesB)
   {
      JointStateInsertor stateInsertorA = toJointStateInsertor(stateSelectionA);
      JointStateInsertor stateInsertorB = toJointStateInsertor(stateSelectionB);

      int startIndex = 0;

      if (stateSelectionA == SimJointStateType.CONFIGURATION)
         maxMagnitudeA = Double.POSITIVE_INFINITY;
      if (stateSelectionB == SimJointStateType.CONFIGURATION)
         maxMagnitudeB = Double.POSITIVE_INFINITY;

      for (int jointIndex = 0; jointIndex < joints.size(); jointIndex++)
      {
         SimJointBasics joint = joints.get(jointIndex);
         if (predicateStateA.test(joint))
         {
            if (testFiniteValuesA)
            {
               if (stateSelectionA == SimJointStateType.CONFIGURATION)
                  checkFiniteValues(stateSelectionA, matrixA, startIndex, joint.getConfigurationMatrixSize());
               else
                  checkFiniteValues(stateSelectionA, matrixA, startIndex, joint.getDegreesOfFreedom());
            }

            if (maxMagnitudeA != Double.POSITIVE_INFINITY)
               checkStateNorm(joint, startIndex, matrixA, maxMagnitudeA, stateSelectionA);

            startIndex = stateInsertorA.insertState(joint, startIndex, matrixA);
         }
         else
         {
            if (testFiniteValuesB)
            {
               if (stateSelectionB == SimJointStateType.CONFIGURATION)
                  checkFiniteValues(stateSelectionB, matrixB, startIndex, joint.getConfigurationMatrixSize());
               else
                  checkFiniteValues(stateSelectionB, matrixB, startIndex, joint.getDegreesOfFreedom());
            }

            if (maxMagnitudeB != Double.POSITIVE_INFINITY)
               checkStateNorm(joint, startIndex, matrixB, maxMagnitudeB, stateSelectionB);

            startIndex = stateInsertorB.insertState(joint, startIndex, matrixB);
         }
      }
      return startIndex;
   }

   /**
    * Iterates through the given {@code joints}, and update their requested state
    * {@code stateSelection} using the given {@code matrix} assuming the state has been previously
    * stored in the proper order.
    *
    * @param joints           the joints to update the state of. Modified.
    * @param stateSelection   indicates what state is to be updated, i.e. it can be either
    *                         configuration, velocity, acceleration, or tau (or effort).
    * @param matrix           the matrix in which the new state of the joints is stored. The data is
    *                         expected to be stored as a column vector starting at the first row.
    *                         Modified.
    * @param maxMagnitude     asserts for each joint that the norm of the state being inserted is below
    *                         this value, throws an {@link IllegalArgumentException} is a state's norm
    *                         exceeds the value. This argument is ignored for
    *                         {@link SimJointStateType#CONFIGURATION}.
    * @param testFiniteValues when {@code true}, tests that the values are all finite, i.e. not
    *                         infinite or NaN, throws an {@link IllegalArgumentException} if at least
    *                         one value is not finite.
    * @return the number of rows that were used from the matrix.
    */
   public static int insertJointsState(SimJointBasics[] joints, SimJointStateType stateSelection, DMatrix matrix, double maxMagnitude, boolean testFiniteValues)
   {
      if (testFiniteValues)
      {
         checkFiniteValues(stateSelection, matrix);
      }

      switch (stateSelection)
      {
         case CONFIGURATION:
            return insertJointsConfiguration(joints, 0, matrix);
         case VELOCITY:
            return insertJointsVelocity(joints, 0, matrix, maxMagnitude);
         case VELOCITY_CHANGE:
            return insertJointsDeltaVelocity(joints, 0, matrix, maxMagnitude);
         case ACCELERATION:
            return insertJointsAcceleration(joints, 0, matrix, maxMagnitude);
         case EFFORT:
            return insertJointsTau(joints, 0, matrix, maxMagnitude);
         default:
            throw new RuntimeException("Unexpected value for stateSelection: " + stateSelection);
      }
   }

   private static int insertJointsConfiguration(JointBasics[] joints, int startIndex, DMatrix matrix)
   {
      for (int jointIndex = 0; jointIndex < joints.length; jointIndex++)
      {
         JointBasics joint = joints[jointIndex];
         startIndex = joint.setJointConfiguration(startIndex, matrix);
      }

      return startIndex;
   }

   private static int insertJointsVelocity(JointBasics[] joints, int startIndex, DMatrix matrix, double maxMagnitude)
   {
      if (maxMagnitude == Double.POSITIVE_INFINITY)
      {
         for (int jointIndex = 0; jointIndex < joints.length; jointIndex++)
         {
            JointBasics joint = joints[jointIndex];
            startIndex = joint.setJointVelocity(startIndex, matrix);
         }
      }
      else
      {
         double maxMagSquared = maxMagnitude * maxMagnitude;

         for (int jointIndex = 0; jointIndex < joints.length; jointIndex++)
         {
            JointBasics joint = joints[jointIndex];

            double normSquared = 0.0;
            for (int dof = 0; dof < joint.getDegreesOfFreedom(); dof++)
            {
               normSquared += EuclidCoreTools.square(matrix.get(startIndex + dof, 0));
            }

            if (normSquared > maxMagSquared)
            {
               throw new IllegalArgumentException("Joint (" + joint.getName() + ") velocity exceeds max magnitude (" + maxMagnitude + "): "
                     + Math.sqrt(normSquared));
            }

            startIndex = joint.setJointVelocity(startIndex, matrix);
         }
      }

      return startIndex;
   }

   private static int insertJointsDeltaVelocity(SimJointBasics[] joints, int startIndex, DMatrix matrix, double maxMagnitude)
   {
      if (maxMagnitude == Double.POSITIVE_INFINITY)
      {
         for (int jointIndex = 0; jointIndex < joints.length; jointIndex++)
         {
            SimJointBasics joint = joints[jointIndex];
            startIndex = joint.setJointDeltaVelocity(startIndex, matrix);
         }
      }
      else
      {
         double maxMagSquared = maxMagnitude * maxMagnitude;

         for (int jointIndex = 0; jointIndex < joints.length; jointIndex++)
         {
            SimJointBasics joint = joints[jointIndex];

            double normSquared = 0.0;
            for (int dof = 0; dof < joint.getDegreesOfFreedom(); dof++)
            {
               normSquared += EuclidCoreTools.square(matrix.get(startIndex + dof, 0));
            }

            if (normSquared > maxMagSquared)
            {
               throw new IllegalArgumentException("Joint (" + joint.getName() + ") dela-velocity exceeds max magnitude (" + maxMagnitude + "): "
                     + Math.sqrt(normSquared));
            }

            startIndex = joint.setJointDeltaVelocity(startIndex, matrix);
         }
      }

      return startIndex;
   }

   private static int insertJointsAcceleration(JointBasics[] joints, int startIndex, DMatrix matrix, double maxMagnitude)
   {
      if (maxMagnitude == Double.POSITIVE_INFINITY)
      {
         for (int jointIndex = 0; jointIndex < joints.length; jointIndex++)
         {
            JointBasics joint = joints[jointIndex];
            startIndex = joint.setJointAcceleration(startIndex, matrix);
         }
      }
      else
      {
         double maxMagSquared = maxMagnitude * maxMagnitude;

         for (int jointIndex = 0; jointIndex < joints.length; jointIndex++)
         {
            JointBasics joint = joints[jointIndex];

            double normSquared = 0.0;
            for (int dof = 0; dof < joint.getDegreesOfFreedom(); dof++)
            {
               normSquared += EuclidCoreTools.square(matrix.get(startIndex + dof, 0));
            }

            if (normSquared > maxMagSquared)
            {
               throw new IllegalArgumentException("Joint (" + joint.getName() + ") acceleration exceeds max magnitude (" + maxMagnitude + "): "
                     + Math.sqrt(normSquared));
            }

            startIndex = joint.setJointAcceleration(startIndex, matrix);
         }
      }

      return startIndex;
   }

   private static int insertJointsTau(JointBasics[] joints, int startIndex, DMatrix matrix, double maxMagnitude)
   {
      if (maxMagnitude == Double.POSITIVE_INFINITY)
      {
         for (int jointIndex = 0; jointIndex < joints.length; jointIndex++)
         {
            JointBasics joint = joints[jointIndex];
            startIndex = joint.setJointTau(startIndex, matrix);
         }
      }
      else
      {
         double maxMagSquared = maxMagnitude * maxMagnitude;

         for (int jointIndex = 0; jointIndex < joints.length; jointIndex++)
         {
            JointBasics joint = joints[jointIndex];

            double normSquared = 0.0;
            for (int dof = 0; dof < joint.getDegreesOfFreedom(); dof++)
            {
               normSquared += EuclidCoreTools.square(matrix.get(startIndex + dof, 0));
            }

            if (normSquared > maxMagSquared)
            {
               throw new IllegalArgumentException("Joint (" + joint.getName() + ") acceleration exceeds max magnitude (" + maxMagnitude + "): "
                     + Math.sqrt(normSquared));
            }

            startIndex = joint.setJointTau(startIndex, matrix);
         }
      }

      return startIndex;
   }

   private static YoMatrix createYoMatrixForJointsConfiguration(String prefix, String description, List<? extends JointReadOnly> joints, YoRegistry registry)
   {
      List<String> rowNames = new ArrayList<>();
      for (JointReadOnly joint : joints)
      {
         if (joint instanceof OneDoFJointReadOnly)
         {
            rowNames.add(joint.getName());
         }
         else if (joint instanceof SphericalJointReadOnly)
         {
            rowNames.add(joint.getName() + "_qs");
            rowNames.add(joint.getName() + "_qx");
            rowNames.add(joint.getName() + "_qy");
            rowNames.add(joint.getName() + "_qz");
         }
         else if (joint instanceof PlanarJointReadOnly)
         {
            rowNames.add(joint.getName() + "_ay");
            rowNames.add(joint.getName() + "_x");
            rowNames.add(joint.getName() + "_z");
         }
         else if (joint instanceof SixDoFJointReadOnly)
         {
            rowNames.add(joint.getName() + "_qs");
            rowNames.add(joint.getName() + "_qx");
            rowNames.add(joint.getName() + "_qy");
            rowNames.add(joint.getName() + "_qz");
            rowNames.add(joint.getName() + "_x");
            rowNames.add(joint.getName() + "_y");
            rowNames.add(joint.getName() + "_z");
         }
         else  // any other type of joint
         {
            // Need to use getConfigurationMatrixSize instead of getDegreesOfFreedom because of possible quaternions
            if (joint.getConfigurationMatrixSize() > 1)
            {
               for (int i = 0; i < joint.getConfigurationMatrixSize(); i++)
                  rowNames.add(joint.getName() + "_" + i);
            }
            else
            {
               rowNames.add(joint.getName());
            }
         }
      }

      String[] rowNamesArray = rowNames.toArray(new String[0]);
      return new YoMatrix(prefix, description, rowNamesArray.length, 1, rowNamesArray, null, registry);
   }

   public static YoMatrix createYoMatrixForJointsState(String prefix, String description, List<? extends JointReadOnly> joints, SimJointStateType state, YoRegistry registry)
   {
      switch (state)
      {
         case CONFIGURATION:
            return createYoMatrixForJointsConfiguration(prefix, description, joints, registry);
         case VELOCITY, ACCELERATION, EFFORT:
            List<String> rowNames = new ArrayList<>();
            for (JointReadOnly joint : joints)
            {
               if (joint instanceof OneDoFJointReadOnly)
               {
                  rowNames.add(joint.getName());
               }
               else if (joint instanceof SphericalJointReadOnly)
               {
                  rowNames.add(joint.getName() + "_ax");
                  rowNames.add(joint.getName() + "_ay");
                  rowNames.add(joint.getName() + "_az");
               }
               else if (joint instanceof PlanarJointReadOnly)
               {
                  rowNames.add(joint.getName() + "_ay");
                  rowNames.add(joint.getName() + "_x");
                  rowNames.add(joint.getName() + "_z");
               }
               else if (joint instanceof SixDoFJointReadOnly)
               {
                  rowNames.add(joint.getName() + "_ax");
                  rowNames.add(joint.getName() + "_ay");
                  rowNames.add(joint.getName() + "_az");
                  rowNames.add(joint.getName() + "_x");
                  rowNames.add(joint.getName() + "_y");
                  rowNames.add(joint.getName() + "_z");
               }
               else  // any other type of joint
               {
                  if (joint.getDegreesOfFreedom() > 1)
                  {
                     for (int i = 0; i < joint.getDegreesOfFreedom(); i++)
                        rowNames.add(joint.getName() + "_" + i);
                  }
                  else if  (joint.getDegreesOfFreedom() > 0)
                  { // Skip fixed-joints
                     rowNames.add(joint.getName());
                  }
               }
            }

            String[] rowNamesArray = rowNames.toArray(new String[0]);
            return new YoMatrix(prefix, description, rowNamesArray.length, 1, rowNamesArray, null, registry);
         default:
            throw new RuntimeException("Unexpected value for state: " + state);
      }
   }
}