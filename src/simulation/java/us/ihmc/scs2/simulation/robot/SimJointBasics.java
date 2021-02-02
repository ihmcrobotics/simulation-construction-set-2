package us.ihmc.scs2.simulation.robot;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import us.ihmc.mecano.multiBodySystem.interfaces.JointBasics;
import us.ihmc.mecano.multiBodySystem.iterators.JointIterable;
import us.ihmc.mecano.multiBodySystem.iterators.SubtreeStreams;

public interface SimJointBasics extends JointBasics
{
   @Override
   SimRigidBody getPredecessor();

   @Override
   SimRigidBody getSuccessor();

   @Override
   default Iterable<? extends SimJointBasics> subtreeIterable()
   {
      return new JointIterable<>(SimJointBasics.class, null, this);
   }

   @Override
   default Stream<? extends SimJointBasics> subtreeStream()
   {
      return SubtreeStreams.from(SimJointBasics.class, this);
   }

   @Override
   default List<? extends SimJointBasics> subtreeList()
   {
      return subtreeStream().collect(Collectors.toList());
   }

   @Override
   default JointBasics[] subtreeArray()
   {
      return subtreeStream().toArray(SimJointBasics[]::new);
   }
}
