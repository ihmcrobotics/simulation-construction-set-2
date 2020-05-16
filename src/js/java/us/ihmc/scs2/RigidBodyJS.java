package us.ihmc.scs2;

import java.util.List;
import java.util.stream.Stream;

import us.ihmc.mecano.frames.MovingReferenceFrame;
import us.ihmc.mecano.multiBodySystem.interfaces.JointBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.mecano.multiBodySystem.iterators.RigidBodyIterable;
import us.ihmc.mecano.multiBodySystem.iterators.SubtreeStreams;
import us.ihmc.mecano.spatial.interfaces.SpatialInertiaBasics;

public class RigidBodyJS implements RigidBodyBasics
{
   private final RigidBodyBasics rigidBody;
   private FrameMeshGroupJS frameMeshGroupJS;

   public RigidBodyJS(RigidBodyBasics rigidBody)
   {
      this.rigidBody = rigidBody;
      if (!rigidBody.isRootBody())
         rigidBody.getParentJoint().setSuccessor(this);
   }

   public void updateSubtreeGraphics()
   {
      updateGraphics();
      subtreeStream().forEach(RigidBodyJS::updateGraphics);
   }

   public void updateGraphics()
   {
      if (frameMeshGroupJS != null)
         frameMeshGroupJS.updatePose();
   }

   public void setGraphics(FrameMeshGroupJS graphics)
   {
      frameMeshGroupJS = graphics;
   }

   public FrameMeshGroupJS getGraphics()
   {
      return frameMeshGroupJS;
   }

   @Override
   public SpatialInertiaBasics getInertia()
   {
      return rigidBody.getInertia();
   }

   @Override
   public MovingReferenceFrame getBodyFixedFrame()
   {
      return rigidBody.getBodyFixedFrame();
   }

   @Override
   public JointBasics getParentJoint()
   {
      return rigidBody.getParentJoint();
   }

   @Override
   public void addChildJoint(JointBasics joint)
   {
      rigidBody.addChildJoint(joint);
   }

   @Override
   public List<? extends JointBasics> getChildrenJoints()
   {
      return rigidBody.getChildrenJoints();
   }

   @Override
   public String toString()
   {
      return rigidBody.toString();
   }

   @Override
   public String getName()
   {
      return rigidBody.getName();
   }

   @Override
   public String getNameId()
   {
      return rigidBody.getNameId();
   }

   @Override
   public Iterable<? extends RigidBodyJS> subtreeIterable()
   {
      return new RigidBodyIterable<>(RigidBodyJS.class, null, this);
   }

   @Override
   public Stream<? extends RigidBodyJS> subtreeStream()
   {
      return SubtreeStreams.from(RigidBodyJS.class, this);
   }
}
