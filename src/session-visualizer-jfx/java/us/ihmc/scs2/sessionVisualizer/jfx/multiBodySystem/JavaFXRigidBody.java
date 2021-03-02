package us.ihmc.scs2.sessionVisualizer.jfx.multiBodySystem;

import java.util.List;
import java.util.stream.Stream;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import us.ihmc.mecano.frames.MovingReferenceFrame;
import us.ihmc.mecano.multiBodySystem.interfaces.JointBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.mecano.multiBodySystem.iterators.RigidBodyIterable;
import us.ihmc.mecano.multiBodySystem.iterators.SubtreeStreams;
import us.ihmc.mecano.spatial.interfaces.SpatialInertiaBasics;
import us.ihmc.scs2.sessionVisualizer.jfx.definition.JavaFXVisualTools;

public class JavaFXRigidBody implements RigidBodyBasics
{
   private final RigidBodyBasics rigidBody;
   private final ObjectProperty<FrameNode> graphicsProperty = new SimpleObjectProperty<>(this, "graphics", null);

   public JavaFXRigidBody(RigidBodyBasics rigidBody)
   {
      this.rigidBody = rigidBody;
      if (!rigidBody.isRootBody())
         rigidBody.getParentJoint().setSuccessor(this);

      // Listener to set node ids to reflect the name of this rigid-body:
      graphicsProperty.addListener((o, oldValue, newValue) ->
      {
         if (newValue == null || newValue.getNode() == null)
            return;

         JavaFXVisualTools.setMissingNodeIDs(newValue.getNode(), getName());
      });
   }

   public void updateSubtreeGraphics()
   {
      updateGraphics();
      subtreeStream().forEach(JavaFXRigidBody::updateGraphics);
   }

   public void updateGraphics()
   {
      if (graphicsProperty.get() != null)
         graphicsProperty.get().updatePose();
   }

   public void setGraphics(FrameNode graphics)
   {
      graphicsProperty.set(graphics);
   }

   public FrameNode getGraphics()
   {
      return graphicsProperty.get();
   }

   public ObjectProperty<FrameNode> graphicsProperty()
   {
      return graphicsProperty;
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
   public List<JointBasics> getChildrenJoints()
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
   public Iterable<? extends JavaFXRigidBody> subtreeIterable()
   {
      return new RigidBodyIterable<>(JavaFXRigidBody.class, null, this);
   }

   @Override
   public Stream<? extends JavaFXRigidBody> subtreeStream()
   {
      return SubtreeStreams.from(JavaFXRigidBody.class, this);
   }
}
