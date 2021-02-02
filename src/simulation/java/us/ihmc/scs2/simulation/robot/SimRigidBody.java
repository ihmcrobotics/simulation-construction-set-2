package us.ihmc.scs2.simulation.robot;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import us.ihmc.euclid.matrix.interfaces.Matrix3DReadOnly;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Tuple3DReadOnly;
import us.ihmc.mecano.multiBodySystem.RigidBody;
import us.ihmc.mecano.multiBodySystem.interfaces.JointBasics;
import us.ihmc.mecano.multiBodySystem.iterators.JointIterable;
import us.ihmc.mecano.multiBodySystem.iterators.RigidBodyIterable;
import us.ihmc.mecano.multiBodySystem.iterators.SubtreeStreams;
import us.ihmc.scs2.definition.robot.RigidBodyDefinition;

public class SimRigidBody extends RigidBody
{
   public SimRigidBody(String bodyName, ReferenceFrame parentStationaryFrame)
   {
      super(bodyName, parentStationaryFrame);
   }

   public SimRigidBody(String bodyName, RigidBodyTransformReadOnly transformToParent, ReferenceFrame parentStationaryFrame)
   {
      super(bodyName, transformToParent, parentStationaryFrame);
   }

   public SimRigidBody(String bodyName, SimJointBasics parentJoint, double Ixx, double Iyy, double Izz, double mass, Tuple3DReadOnly centerOfMassOffset)
   {
      super(bodyName, parentJoint, Ixx, Iyy, Izz, mass, centerOfMassOffset);
   }

   public SimRigidBody(String bodyName, SimJointBasics parentJoint, Matrix3DReadOnly momentOfInertia, double mass, Tuple3DReadOnly centerOfMassOffset)
   {
      super(bodyName, parentJoint, momentOfInertia, mass, centerOfMassOffset);
   }

   public SimRigidBody(String bodyName, SimJointBasics parentJoint, Matrix3DReadOnly momentOfInertia, double mass, RigidBodyTransformReadOnly inertiaPose)
   {
      super(bodyName, parentJoint, momentOfInertia, mass, inertiaPose);
   }

   public SimRigidBody(RigidBodyDefinition definition, ReferenceFrame parentStationaryFrame)
   {
      super(definition.getName(), definition.getInertiaPose(), parentStationaryFrame);
   }

   public SimRigidBody(RigidBodyDefinition definition, SimJointBasics parentJoint)
   {
      super(definition.getName(), parentJoint, definition.getMomentOfInertia(), definition.getMass(), definition.getInertiaPose());
   }

   @Override
   public SimJointBasics getParentJoint()
   {
      return (SimJointBasics) super.getParentJoint();
   }

   @Override
   public void addChildJoint(JointBasics joint)
   {
      if (joint instanceof SimJointBasics)
         super.addChildJoint(joint);
      else
         throw new IllegalArgumentException("Can only add a " + SimJointBasics.class.getSimpleName() + " as child of a " + getClass().getSimpleName());
   }

   @Override
   public void addParentLoopClosureJoint(JointBasics parentLoopClosureJoint)
   {
      if (parentLoopClosureJoint instanceof SimJointBasics)
         super.addParentLoopClosureJoint(parentLoopClosureJoint);
      else
         throw new IllegalArgumentException("Can only add a " + SimJointBasics.class.getSimpleName() + " as parent of a " + getClass().getSimpleName());
   }

   public Iterable<? extends SimRigidBody> subtreeIterable()
   {
      return new RigidBodyIterable<>(SimRigidBody.class, null, this);
   }

   public Iterable<? extends SimJointBasics> childrenSubtreeIterable()
   {
      return new JointIterable<>(SimJointBasics.class, null, getChildrenJoints());
   }

   public Stream<? extends SimRigidBody> subtreeStream()
   {
      return SubtreeStreams.from(SimRigidBody.class, this);
   }

   public List<? extends SimRigidBody> subtreeList()
   {
      return subtreeStream().collect(Collectors.toList());
   }

   public SimRigidBody[] subtreeArray()
   {
      return subtreeStream().toArray(SimRigidBody[]::new);
   }
}
