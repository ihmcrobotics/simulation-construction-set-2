package us.ihmc.scs2.simulation.robot;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import us.ihmc.mecano.multiBodySystem.interfaces.JointBasics;
import us.ihmc.mecano.multiBodySystem.iterators.JointIterable;
import us.ihmc.mecano.multiBodySystem.iterators.SubtreeStreams;
import us.ihmc.scs2.definition.robot.ExternalWrenchPointDefinition;
import us.ihmc.scs2.definition.robot.GroundContactPointDefinition;
import us.ihmc.scs2.definition.robot.IMUSensorDefinition;
import us.ihmc.scs2.definition.robot.KinematicPointDefinition;
import us.ihmc.scs2.definition.robot.WrenchSensorDefinition;

public interface SimJointBasics extends JointBasics
{
   @Override
   SimRigidBody getPredecessor();

   @Override
   SimRigidBody getSuccessor();

   SimJointAuxiliaryData getAuxialiryData();

   default void addKinematicPoint(KinematicPointDefinition definition)
   {
      getAuxialiryData().addKinematicPoint(definition);
   }

   default void addExternalWrenchPoint(ExternalWrenchPointDefinition definition)
   {
      getAuxialiryData().addExternalWrenchPoint(definition);
   }

   default void addGroundContactPoint(GroundContactPointDefinition definition)
   {
      getAuxialiryData().addGroundContactPoint(definition);
   }

   default void addIMUSensor(IMUSensorDefinition definition)
   {
      getAuxialiryData().addIMUSensor(definition);
   }

   default void addWrenchSensor(WrenchSensorDefinition definition)
   {
      getAuxialiryData().addWrenchSensor(definition);
   }

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
