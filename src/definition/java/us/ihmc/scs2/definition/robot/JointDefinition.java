package us.ihmc.scs2.definition.robot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import us.ihmc.euclid.interfaces.Transformable;
import us.ihmc.euclid.transform.RigidBodyTransform;
import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.euclid.transform.interfaces.Transform;
import us.ihmc.mecano.multiBodySystem.interfaces.JointBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.scs2.definition.state.interfaces.JointStateBasics;

// TODO Add option for loop closure
public abstract class JointDefinition implements Transformable
{
   private String name;
   private final RigidBodyTransform transformToParent = new RigidBodyTransform();

   private RigidBodyDefinition predecessor;
   private RigidBodyDefinition successor;
   private JointStateBasics initialJointState = null;

   private final List<SensorDefinition> sensorDefinitions = new ArrayList<>();
   private final List<KinematicPointDefinition> kinematicPointDefinitions = new ArrayList<>();
   private final List<ExternalWrenchPointDefinition> externalWrenchPointDefinitions = new ArrayList<>();
   private final List<GroundContactPointDefinition> groundContactPointDefinitions = new ArrayList<>();

   public JointDefinition()
   {
   }

   public JointDefinition(String name)
   {
      setName(name);
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public void setTransformToParent(RigidBodyTransformReadOnly transformToParent)
   {
      this.transformToParent.set(transformToParent);
   }

   public String getName()
   {
      return name;
   }

   public RigidBodyTransform getTransformToParent()
   {
      return transformToParent;
   }

   public void setPredecessor(RigidBodyDefinition predecessor)
   {
      this.predecessor = predecessor;
   }

   public RigidBodyDefinition getPredecessor()
   {
      return predecessor;
   }

   public void setSuccessor(RigidBodyDefinition successor)
   {
      if (this.successor != null)
         this.successor.setParentJoint(null);

      this.successor = successor;

      if (this.successor != null)
         this.successor.setParentJoint(this);
   }

   public RigidBodyDefinition getSuccessor()
   {
      return successor;
   }

   public JointDefinition getParentJoint()
   {
      if (predecessor == null)
         return null;
      else
         return predecessor.getParentJoint();
   }

   public void setInitialJointState(JointStateBasics initialJointState)
   {
      this.initialJointState = initialJointState;
   }

   public JointStateBasics getInitialJointState()
   {
      return initialJointState;
   }

   public void addSensorDefinition(SensorDefinition sensorDefinition)
   {
      sensorDefinitions.add(sensorDefinition);
   }

   public List<SensorDefinition> getSensorDefinitions()
   {
      return sensorDefinitions;
   }

   public <T extends SensorDefinition> List<T> getSensorDefinitions(Class<T> sensorType)
   {
      return sensorDefinitions.stream().filter(sensorType::isInstance).map(sensorType::cast).collect(Collectors.toList());
   }

   public List<KinematicPointDefinition> getKinematicPointDefinitions()
   {
      return kinematicPointDefinitions;
   }

   public void addKinematicPointDefinition(KinematicPointDefinition kinematicPointDefinition)
   {
      kinematicPointDefinitions.add(kinematicPointDefinition);
   }

   public List<ExternalWrenchPointDefinition> getExternalWrenchPointDefinitions()
   {
      return externalWrenchPointDefinitions;
   }

   public void addExternalWrenchPointDefinition(ExternalWrenchPointDefinition externalWrenchPointDefinition)
   {
      externalWrenchPointDefinitions.add(externalWrenchPointDefinition);
   }

   public List<GroundContactPointDefinition> getGroundContactPointDefinitions()
   {
      return groundContactPointDefinitions;
   }

   public void addGroundContactPointDefinition(GroundContactPointDefinition groundContactPointDefinition)
   {
      groundContactPointDefinitions.add(groundContactPointDefinition);
   }

   public abstract JointBasics toJoint(RigidBodyBasics predecessor);

   @Override
   public void applyTransform(Transform transform)
   {
      transform.transform(transformToParent);
      kinematicPointDefinitions.forEach(kp -> kp.applyTransform(transform));
      externalWrenchPointDefinitions.forEach(efp -> efp.applyTransform(transform));
      groundContactPointDefinitions.forEach(gcp -> gcp.applyTransform(transform));
      sensorDefinitions.forEach(sensor -> sensor.applyTransform(transform));
   }

   @Override
   public void applyInverseTransform(Transform transform)
   {
      transform.inverseTransform(transformToParent);
      kinematicPointDefinitions.forEach(kp -> kp.applyInverseTransform(transform));
      externalWrenchPointDefinitions.forEach(efp -> efp.applyInverseTransform(transform));
      groundContactPointDefinitions.forEach(gcp -> gcp.applyInverseTransform(transform));
      sensorDefinitions.forEach(sensor -> sensor.applyInverseTransform(transform));
   }

   @Override
   public String toString()
   {
      return name + ": origin: (x,y,z) " + transformToParent.getTranslation() + "(y,p,r) " + transformToParent.getRotation().toStringAsYawPitchRoll()
            + ", successor: " + successor.getName();
   }
}
