package us.ihmc.scs2.definition.robot;

public class WrenchSensorDefinition extends SensorDefinition
{
   public WrenchSensorDefinition()
   {
   }

   public WrenchSensorDefinition(WrenchSensorDefinition other)
   {
      super(other);
   }

   @Override
   public SensorDefinition copy()
   {
      return new WrenchSensorDefinition(this);
   }
}
