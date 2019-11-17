package us.ihmc.scs2.sharedMemory;

import us.ihmc.yoVariables.variable.YoVariable;

interface PushRequest<T extends YoVariable<T>>
{
   void push();

   boolean isPushNecessary();
}