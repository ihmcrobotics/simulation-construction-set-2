package us.ihmc.scs2.definition.state.interfaces;

import us.ihmc.mecano.multiBodySystem.interfaces.JointReadOnly;

public interface JointStateBasics extends JointStateReadOnly
{
   void clear();

   void addVelocity(JointReadOnly joint);

   void addAcceleration(JointReadOnly joint);

   void addEffort(JointReadOnly joint);
   
   void setConfiguration(JointReadOnly joint);
   
   void setVelocity(JointReadOnly joint);
   
   void setAcceleration(JointReadOnly joint);
   
   void setEffort(JointReadOnly joint);
}