package us.ihmc.scs2.sharedMemory;

import us.ihmc.yoVariables.variable.YoVariable;

interface PullRequest<T extends YoVariable<T>>
{
   void pull();
}